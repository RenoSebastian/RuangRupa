package com.example.ruangai.data.repository.local

import com.example.ruangai.data.local.db.dao.DesignGenerationDao
import com.example.ruangai.data.local.db.dao.RoomSceneDao
import com.example.ruangai.data.local.db.mapper.DesignGenerationMapper
import com.example.ruangai.data.local.db.mapper.RoomSceneMapper
import com.example.ruangai.data.repository.mock.DeterministicMakeoverGenerator
import com.example.ruangai.data.repository.mock.MockCreditWalletRepository
import com.example.ruangai.data.repository.mock.MockData
import com.example.ruangai.domain.model.*
import com.example.ruangai.domain.repository.AIInteriorDesignRepository
import com.example.ruangai.domain.repository.CreditWalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Room-backed persistent implementation of [AIInteriorDesignRepository] (Milestone 7 Part B, E, F).
 *
 * Guarantees:
 * - Preserves the original room image URI (`originalImagePath` is never overwritten).
 * - Enforces atomic, idempotent credit reservation, capture on completion, and auto-refund on failure/cancellation.
 * - Prevents duplicate generation requests using unique `idempotencyKey` lookup in SQLite.
 * - Persists design generation requests and multiple variations across app restarts.
 */
class PersistentAIInteriorDesignRepository(
    private val designGenerationDao: DesignGenerationDao,
    private val roomSceneDao: RoomSceneDao,
    private val creditWalletRepository: CreditWalletRepository = MockCreditWalletRepository(),
    private val generator: DeterministicMakeoverGenerator = DeterministicMakeoverGenerator()
) : AIInteriorDesignRepository {

    override fun validateConfiguration(request: DesignGenerationRequest): DesignConfigValidationResult {
        return request.validate()
    }

    override fun observeDesignHistoryForScene(sceneId: String): Flow<List<DesignGenerationResult>> {
        return combine(
            designGenerationDao.observeRequestsForScene(sceneId),
            designGenerationDao.observeVariationsForScene(sceneId)
        ) { requestEntities, variationEntities ->
            val variationsByResult = variationEntities.groupBy { it.resultId }
            requestEntities.map { reqEntity ->
                DesignGenerationMapper.toDomainResult(
                    entity = reqEntity,
                    variationEntities = variationsByResult[reqEntity.resultId].orEmpty()
                )
            }
        }
    }

    override suspend fun getDesignHistoryForSceneSync(sceneId: String): List<DesignGenerationResult> =
        withContext(Dispatchers.IO) {
            val requests = designGenerationDao.getRequestsForSceneSync(sceneId)
            val variationsByResult = designGenerationDao.getVariationsForSceneSync(sceneId).groupBy { it.resultId }
            requests.map { reqEntity ->
                DesignGenerationMapper.toDomainResult(
                    entity = reqEntity,
                    variationEntities = variationsByResult[reqEntity.resultId].orEmpty()
                )
            }
        }

    override fun observeFavoriteVariations(sceneId: String?): Flow<List<DesignVariation>> {
        val sourceFlow = if (sceneId != null) {
            designGenerationDao.observeFavoriteVariationsForScene(sceneId)
        } else {
            designGenerationDao.observeAllFavoriteVariations()
        }
        return sourceFlow.map { list ->
            list.map { DesignGenerationMapper.toDomainVariation(it) }
        }
    }

    override suspend fun getResultByIdSync(resultId: String): DesignGenerationResult? =
        withContext(Dispatchers.IO) {
            val req = designGenerationDao.getRequestByResultIdSync(resultId) ?: return@withContext null
            val vars = designGenerationDao.getVariationsForResultSync(resultId)
            DesignGenerationMapper.toDomainResult(req, vars)
        }

    override suspend fun generateRoomMakeover(
        request: DesignGenerationRequest
    ): Result<DesignGenerationResult> = withContext(Dispatchers.IO) {
        val validation = request.validate()
        if (!validation.isValid) {
            return@withContext Result.failure(IllegalArgumentException(validation.errors.joinToString(" ")))
        }

        // Duplicate request protection via idempotencyKey
        val existingEntity = designGenerationDao.getRequestByIdempotencyKeySync(request.idempotencyKey)
        if (existingEntity != null) {
            val existingVars = designGenerationDao.getVariationsForResultSync(existingEntity.resultId)
            val existingDomain = DesignGenerationMapper.toDomainResult(existingEntity, existingVars)
            if (existingDomain.status == DesignGenerationStatus.COMPLETED) {
                return@withContext Result.success(existingDomain)
            }
        }

        // Ensure parent RoomScene exists in SQLite for FK integrity
        ensureParentSceneExists(request)

        // 1. Reserve credit atomically via idempotencyKey
        val reserveRes = creditWalletRepository.reserveCredit(request.idempotencyKey)
        if (reserveRes.isFailure) {
            return@withContext Result.failure(
                reserveRes.exceptionOrNull() ?: IllegalStateException("Saldo kredit tidak mencukupi untuk makeover ruangan.")
            )
        }

        val now = System.currentTimeMillis()
        val resultId = existingEntity?.resultId ?: "res_${request.requestId}"

        // 2. Persist PROCESSING state before executing generation
        val processingDomain = DesignGenerationResult(
            resultId = resultId,
            requestId = request.requestId,
            idempotencyKey = request.idempotencyKey,
            sceneId = request.sceneId,
            originalRoomImageUri = request.originalRoomImageUri,
            roomType = request.roomType,
            selectedStyle = request.selectedStyle,
            colorPaletteNames = request.colorPaletteNames,
            desiredAtmosphere = request.desiredAtmosphere,
            furniturePreferences = request.furniturePreferences,
            targetBudgetIdr = request.targetBudgetIdr,
            additionalInstructions = request.additionalInstructions,
            status = DesignGenerationStatus.PROCESSING,
            variations = emptyList(),
            selectedVariationId = null,
            creditCost = request.creditCost,
            creditReserved = true,
            creditCaptured = false,
            creditRefunded = false,
            createdAt = request.createdAt,
            updatedAt = now
        )
        designGenerationDao.upsertRequest(DesignGenerationMapper.toRequestEntity(processingDomain))

        // 3. Handle simulated failure and automatic credit refund
        if (request.simulateFailure) {
            creditWalletRepository.refundCredit(request.idempotencyKey)
            val failedDomain = processingDomain.copy(
                status = DesignGenerationStatus.FAILED,
                creditCaptured = false,
                creditRefunded = true,
                errorMessage = "Simulasi kegagalan pemrosesan render AI. 1 Kredit telah dikembalikan otomatis ke dompet Anda.",
                updatedAt = now + 10
            )
            designGenerationDao.upsertRequest(DesignGenerationMapper.toRequestEntity(failedDomain))
            return@withContext Result.failure(IllegalStateException(failedDomain.errorMessage))
        }

        // 4. Deterministic generation & credit capture
        val completedDomain = generator.generateMakeoverResult(
            request = request,
            resultId = resultId
        ).copy(
            updatedAt = now + 20
        )

        creditWalletRepository.captureCredit(request.idempotencyKey)

        // Persist parent request first, then child variations
        designGenerationDao.upsertRequest(DesignGenerationMapper.toRequestEntity(completedDomain))
        designGenerationDao.upsertVariations(
            completedDomain.variations.map { DesignGenerationMapper.toVariationEntity(it) }
        )

        // Update preferredStyle and activeMakeoverId on RoomScene WITHOUT modifying originalImagePath
        roomSceneDao.updatePreferredStyle(request.sceneId, request.selectedStyle.name, now + 20)
        val previewUri = completedDomain.activeVariation?.previewImageUrl ?: request.originalRoomImageUri
        roomSceneDao.updateActiveMakeover(
            sceneId = request.sceneId,
            activeMakeoverId = completedDomain.resultId,
            previewImagePath = previewUri,
            updatedAt = now + 20
        )

        Result.success(completedDomain)
    }

    override suspend fun retryDesignGeneration(requestId: String): Result<DesignGenerationResult> =
        withContext(Dispatchers.IO) {
            val existingEntity = designGenerationDao.getRequestByRequestIdSync(requestId)
                ?: designGenerationDao.getRequestByResultIdSync(requestId)
                ?: return@withContext Result.failure(NoSuchElementException("Permintaan desain $requestId tidak ditemukan."))

            val existingVars = designGenerationDao.getVariationsForResultSync(existingEntity.resultId)
            val existingDomain = DesignGenerationMapper.toDomainResult(existingEntity, existingVars)

            val retryKey = "${existingDomain.idempotencyKey}_retry_${System.currentTimeMillis()}"
            val retryRequest = DesignGenerationRequest(
                requestId = existingDomain.requestId,
                idempotencyKey = retryKey,
                sceneId = existingDomain.sceneId,
                originalRoomImageUri = existingDomain.originalRoomImageUri,
                roomType = existingDomain.roomType,
                selectedStyle = existingDomain.selectedStyle,
                colorPaletteNames = existingDomain.colorPaletteNames,
                desiredAtmosphere = existingDomain.desiredAtmosphere,
                furniturePreferences = existingDomain.furniturePreferences,
                targetBudgetIdr = existingDomain.targetBudgetIdr,
                additionalInstructions = existingDomain.additionalInstructions,
                variationCount = 3,
                creditCost = existingDomain.creditCost,
                simulateFailure = false,
                createdAt = existingDomain.createdAt
            )

            val reserveRes = creditWalletRepository.reserveCredit(retryKey)
            if (reserveRes.isFailure) {
                return@withContext Result.failure(
                    reserveRes.exceptionOrNull() ?: IllegalStateException("Saldo kredit tidak mencukupi untuk mencoba ulang.")
                )
            }

            val regenerated = generator.generateMakeoverResult(
                request = retryRequest,
                resultId = existingDomain.resultId
            ).copy(
                requestId = existingDomain.requestId,
                idempotencyKey = retryKey,
                createdAt = existingDomain.createdAt,
                updatedAt = System.currentTimeMillis()
            )

            creditWalletRepository.captureCredit(retryKey)
            designGenerationDao.upsertRequest(DesignGenerationMapper.toRequestEntity(regenerated))
            designGenerationDao.upsertVariations(
                regenerated.variations.map { DesignGenerationMapper.toVariationEntity(it) }
            )
            Result.success(regenerated)
        }

    override suspend fun cancelDesignGeneration(requestId: String): Result<DesignGenerationResult> =
        withContext(Dispatchers.IO) {
            val existingEntity = designGenerationDao.getRequestByRequestIdSync(requestId)
                ?: designGenerationDao.getRequestByResultIdSync(requestId)
                ?: return@withContext Result.failure(NoSuchElementException("Permintaan desain $requestId tidak ditemukan."))

            val existingVars = designGenerationDao.getVariationsForResultSync(existingEntity.resultId)
            val existingDomain = DesignGenerationMapper.toDomainResult(existingEntity, existingVars)

            if (existingDomain.creditReserved && !existingDomain.creditCaptured && !existingDomain.creditRefunded) {
                creditWalletRepository.refundCredit(existingDomain.idempotencyKey)
            }

            val cancelled = existingDomain.copy(
                status = DesignGenerationStatus.CANCELLED,
                creditRefunded = existingDomain.creditReserved && !existingDomain.creditCaptured,
                errorMessage = "Generasi desain dibatalkan oleh pengguna.",
                updatedAt = System.currentTimeMillis()
            )
            designGenerationDao.upsertRequest(DesignGenerationMapper.toRequestEntity(cancelled))
            Result.success(cancelled)
        }

    override suspend fun selectActiveVariation(
        resultId: String,
        variationId: String
    ): Result<DesignGenerationResult> = withContext(Dispatchers.IO) {
        val existing = getResultByIdSync(resultId)
            ?: return@withContext Result.failure(NoSuchElementException("Hasil desain $resultId tidak ditemukan."))
        val targetVar = existing.variations.find { it.variationId == variationId }
            ?: return@withContext Result.failure(IllegalArgumentException("Variasi $variationId tidak ditemukan."))

        val now = System.currentTimeMillis()
        val updated = existing.copy(
            selectedVariationId = variationId,
            updatedAt = now
        )
        designGenerationDao.upsertRequest(DesignGenerationMapper.toRequestEntity(updated))
        roomSceneDao.updateActiveMakeover(
            sceneId = updated.sceneId,
            activeMakeoverId = updated.resultId,
            previewImagePath = targetVar.previewImageUrl,
            updatedAt = now
        )
        Result.success(updated)
    }

    override suspend fun toggleFavoriteVariation(
        variationId: String,
        isFavorite: Boolean
    ): Result<DesignVariation> = withContext(Dispatchers.IO) {
        val existingVar = designGenerationDao.getVariationByIdSync(variationId)
            ?: return@withContext Result.failure(NoSuchElementException("Variasi $variationId tidak ditemukan."))
        designGenerationDao.updateVariationFavorite(variationId, isFavorite)
        val updatedVar = existingVar.copy(isFavorite = isFavorite)
        Result.success(DesignGenerationMapper.toDomainVariation(updatedVar))
    }

    private suspend fun ensureParentSceneExists(request: DesignGenerationRequest) {
        val existingScene = roomSceneDao.getSceneByIdSync(request.sceneId)
        if (existingScene == null) {
            val preloaded = MockData.preloadedScenes.find { it.id == request.sceneId }
            val fallbackScene = preloaded ?: RoomScene(
                id = request.sceneId,
                title = "Proyek ${request.roomType.displayName}",
                roomType = request.roomType,
                originalImage = OriginalRoomImage(
                    assetId = "img_${request.sceneId}",
                    localUri = request.originalRoomImageUri
                ),
                targetBudget = request.targetBudgetIdr,
                preferredStyle = request.selectedStyle
            )
            roomSceneDao.insertScene(RoomSceneMapper.toEntity(fallbackScene))
        }
    }
}

