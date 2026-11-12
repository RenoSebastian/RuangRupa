package com.example.ruangai.data.repository.mock

import com.example.ruangai.domain.model.*
import com.example.ruangai.domain.repository.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MockAuthRepository(
    initialState: AuthState = AuthState.Unauthenticated
) : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(initialState)

    override fun getAuthState(): Flow<AuthState> = _authState.asStateFlow()

    override suspend fun loginWithEmail(email: String, pass: String): Result<UserSession> {
        delay(600)
        val name = email.substringBefore('@').replace('.', ' ').replaceFirstChar { it.uppercase() }
        val session = UserSession(
            userId = "usr_${UUID.randomUUID().toString().take(8)}",
            email = email,
            displayName = name.ifBlank { "Pengguna RuangAI" }
        )
        _authState.value = AuthState.Authenticated(session)
        return Result.success(session)
    }

    override suspend fun registerWithEmail(email: String, pass: String, name: String): Result<UserSession> {
        delay(600)
        val session = UserSession(
            userId = "usr_${UUID.randomUUID().toString().take(8)}",
            email = email,
            displayName = name.ifBlank { email.substringBefore('@') }
        )
        _authState.value = AuthState.Authenticated(session)
        return Result.success(session)
    }

    override suspend fun signInWithGoogle(): Result<UserSession> {
        delay(800)
        val session = UserSession(
            userId = "usr_google_99",
            email = "dimas.pratama@gmail.com",
            displayName = "Dimas Pratama"
        )
        _authState.value = AuthState.Authenticated(session)
        return Result.success(session)
    }

    override suspend fun logout(): Result<Unit> {
        _authState.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        delay(500)
        _authState.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }
}

class MockRoomSceneRepository : RoomSceneRepository {
    private val _scenes = MutableStateFlow<List<RoomScene>>(MockData.preloadedScenes)

    override fun getRoomScenes(): Flow<List<RoomScene>> = _scenes.asStateFlow()

    override fun getRoomSceneById(sceneId: String): Flow<RoomScene?> {
        return _scenes.map { list -> list.find { it.id == sceneId } }
    }

    override suspend fun createRoomScene(title: String, type: RoomType, imageUri: String): Result<RoomScene> {
        val newScene = RoomScene(
            id = "scene_${UUID.randomUUID().toString().take(8)}",
            title = title,
            roomType = type,
            originalImage = OriginalRoomImage(
                assetId = "orig_${UUID.randomUUID().toString().take(8)}",
                localUri = imageUri
            ),
            protectedRegions = listOf(
                ProtectedSceneRegion("pr_gen_1", "Dinding & Jendela", 0f, 0f, 1f, 0.65f),
                ProtectedSceneRegion("pr_gen_2", "Lantai Utama", 0f, 0.65f, 1f, 0.35f)
            ),
            placements = emptyList()
        )
        _scenes.value = listOf(newScene) + _scenes.value
        return Result.success(newScene)
    }

    override suspend fun updatePlacements(sceneId: String, placements: List<ProductPlacement>): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) scene.copy(placements = placements, updatedAt = System.currentTimeMillis())
            else scene
        }
        return Result.success(Unit)
    }

    override suspend fun addPlacement(sceneId: String, placement: ProductPlacement): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) {
                val updated = scene.placements + placement
                scene.copy(placements = updated, updatedAt = System.currentTimeMillis())
            } else scene
        }
        return Result.success(Unit)
    }

    override suspend fun removePlacement(sceneId: String, placementId: String): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) {
                scene.copy(placements = scene.placements.filterNot { it.placementId == placementId })
            } else scene
        }
        return Result.success(Unit)
    }

    override suspend fun updatePlacementTransform(
        sceneId: String,
        placementId: String,
        transform: PlacementTransform
    ): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) {
                val updated = scene.placements.map { p ->
                    if (p.placementId == placementId) p.copy(transform = transform) else p
                }
                scene.copy(placements = updated)
            } else scene
        }
        return Result.success(Unit)
    }

    override suspend fun updateRoomSceneMetadata(
        sceneId: String,
        title: String,
        type: RoomType,
        targetBudget: Long
    ): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) {
                scene.copy(
                    title = title,
                    roomType = type,
                    targetBudget = targetBudget,
                    updatedAt = System.currentTimeMillis()
                )
            } else scene
        }
        return Result.success(Unit)
    }

    override suspend fun updatePreferredStyle(sceneId: String, style: DesignStyle): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) {
                scene.copy(preferredStyle = style, updatedAt = System.currentTimeMillis())
            } else scene
        }
        return Result.success(Unit)
    }

    override suspend fun updateRoomDimensions(sceneId: String, dimensionsCm: DimensionsCm?): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) {
                scene.copy(userDimensionsCm = dimensionsCm, updatedAt = System.currentTimeMillis())
            } else scene
        }
        return Result.success(Unit)
    }

    override suspend fun deleteRoomScene(sceneId: String): Result<Unit> {
        _scenes.value = _scenes.value.filterNot { it.id == sceneId }
        return Result.success(Unit)
    }
}

class MockRoomAnalysisRepository : RoomAnalysisRepository {
    override suspend fun analyzeRoom(sceneId: String, imageUri: String): Result<RoomAnalysisResult> {
        delay(1200)
        val result = RoomAnalysisResult(
            sceneId = sceneId,
            detectedType = RoomType.KOST_STUDIO,
            lighting = LightingInfo(
                naturalSource = "Jendela Kanan (Pencahayaan Alami 65%)",
                timeOfDay = "Siang Hari",
                intensity = "Cukup Terang"
            ),
            surface = SurfaceMaterial(
                wallColorHex = 0xFFF3EFE0,
                wallMaterial = "Plester Halus Cat Krem",
                floorType = "Keramik Putih Glossy 40x40cm"
            ),
            detectedItems = listOf(
                DetectedFurnitureItem("Kasur", "Ranjang Single 100x200cm", "Sisi Kiri", "Pertahankan ranjang, gunakan sprei beige netral"),
                DetectedFurnitureItem("Meja", "Meja Belajar Kayu Tua", "Sudut Depan", "Ganti dengan meja kerja kaki ramping agar lebih lapang"),
                DetectedFurnitureItem("Lampu", "Lampu Gantung Neon Putih", "Atas Ruangan", "Tambahkan standing lamp warm 3000K untuk suasana malam rileks")
            ),
            makeoverSuggestions = listOf(
                "Gunakan palet warna kayu alami dan kain linen untuk menciptakan tema Japandi yang bersih",
                "Pasang cermin gantung bulat untuk memantulkan cahaya dan memperluas visual kamar kost",
                "Gunakan karpet berukuran 120x160cm untuk menyatukan area duduk dan santai"
            )
        )
        return Result.success(result)
    }

    override suspend fun getCachedAnalysis(sceneId: String): RoomAnalysisResult? = null
}

class MockImageGenerationRepository : ImageGenerationRepository {
    private val _jobs = MutableStateFlow<Map<String, VisualizationJob>>(emptyMap())

    override suspend fun requestFullMakeover(sceneId: String, style: DesignStyle): Result<VisualizationJob> {
        val jobId = "job_makeover_${UUID.randomUUID().toString().take(6)}"
        val job = VisualizationJob(
            jobId = jobId,
            sceneId = sceneId,
            mode = JobMode.FULL_MAKEOVER,
            status = JobStatus.PROCESSING,
            creditsReserved = 1,
            message = "Sedang menghasilkan visualisasi ${style.displayName}..."
        )
        _jobs.value = _jobs.value + (jobId to job)
        return Result.success(job)
    }

    override suspend fun requestRealisticPlacement(
        sceneId: String,
        placements: List<ProductPlacement>
    ): Result<VisualizationJob> {
        val jobId = "job_tryon_${UUID.randomUUID().toString().take(6)}"
        val job = VisualizationJob(
            jobId = jobId,
            sceneId = sceneId,
            mode = JobMode.PRODUCT_PLACEMENT,
            status = JobStatus.PROCESSING,
            creditsReserved = 1,
            message = "Mengintegrasikan pencahayaan dan bayangan realistis..."
        )
        _jobs.value = _jobs.value + (jobId to job)
        return Result.success(job)
    }

    override fun observeJob(jobId: String): Flow<VisualizationJob> {
        return _jobs.map { map ->
            map[jobId] ?: VisualizationJob(jobId, "", JobMode.FULL_MAKEOVER, JobStatus.COMPLETED)
        }
    }
}

class MockProductCatalogRepository : ProductCatalogRepository {
    private val _products = MutableStateFlow(MockData.products)

    override fun getProducts(category: ProductCategory?, style: DesignStyle?, maxPrice: Long?): Flow<List<Product>> {
        return _products.map { list ->
            list.filter { p ->
                (category == null || category == ProductCategory.ALL || p.category == category) &&
                (style == null || p.designStyle == style || p.compatibleStyles.contains(style)) &&
                (maxPrice == null || p.priceIdr <= maxPrice)
            }
        }
    }

    override fun filterAndSortProducts(filter: ProductCatalogFilter): Flow<List<Product>> {
        return _products.map { list ->
            val filtered = list.filter { p ->
                val matchesCategory = filter.category == ProductCategory.ALL || p.category == filter.category
                val matchesStyle = filter.style == null || p.designStyle == filter.style || p.compatibleStyles.contains(filter.style)
                val minP = filter.minPriceIdr ?: filter.priceRange.minIdr
                val maxP = filter.maxPriceIdr ?: filter.priceRange.maxIdr
                val matchesMinPrice = minP == null || p.priceIdr >= minP
                val matchesMaxPrice = maxP == null || p.priceIdr <= maxP
                val q = filter.searchQuery.trim()
                val matchesQuery = q.isBlank() ||
                    p.title.contains(q, ignoreCase = true) ||
                    p.brand.contains(q, ignoreCase = true) ||
                    p.material.contains(q, ignoreCase = true) ||
                    p.color.contains(q, ignoreCase = true) ||
                    p.category.displayName.contains(q, ignoreCase = true) ||
                    p.description.contains(q, ignoreCase = true)

                matchesCategory && matchesStyle && matchesMinPrice && matchesMaxPrice && matchesQuery
            }

            when (filter.sortBy) {
                ProductSortOption.RELEVANCE -> filtered.sortedByDescending { it.compatibilityScore }
                ProductSortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.priceIdr }
                ProductSortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.priceIdr }
                ProductSortOption.POPULARITY -> filtered.sortedByDescending { it.popularityScore }
            }
        }
    }

    override suspend fun getProductById(productId: String): Product? {
        return _products.value.find { it.id == productId }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return _products.map { list ->
            if (query.isBlank()) list
            else list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.brand.contains(query, ignoreCase = true) ||
                it.category.displayName.contains(query, ignoreCase = true) ||
                it.material.contains(query, ignoreCase = true)
            }
        }
    }

    override fun getRelatedProducts(product: Product, limit: Int): List<Product> {
        // Complementary categories in compatible interior styles
        return _products.value
            .filter { it.id != product.id && it.category != product.category }
            .sortedByDescending { candidate ->
                var score = 0
                if (candidate.designStyle == product.designStyle) score += 50
                if (candidate.compatibleStyles.any { it in product.compatibleStyles }) score += 25
                score + candidate.popularityScore / 10
            }
            .take(limit)
    }

    override fun getAlternativeProducts(product: Product, limit: Int): List<Product> {
        // Same or equivalent furniture category with alternative price/style
        return _products.value
            .filter { it.id != product.id && it.category == product.category }
            .sortedBy { kotlin.math.abs(it.priceIdr - product.priceIdr) }
            .take(limit)
            .ifEmpty {
                _products.value.filter { it.id != product.id }.take(limit)
            }
    }
}

class MockShoppingListRepository : ShoppingListRepository {
    private val _items = MutableStateFlow<List<ShoppingCartItem>>(
        listOf(
            ShoppingCartItem(
                itemId = "cart_prod_sofa_01_demo_scene_kost_01",
                product = MockData.products[0],
                quantity = 1,
                isSelected = true,
                associatedSceneId = "demo_scene_kost_01",
                associatedPlacementId = "place_kost_1"
            ),
            ShoppingCartItem(
                itemId = "cart_prod_table_01_demo_scene_kost_01",
                product = MockData.products[3], // prod_table_01
                quantity = 1,
                isSelected = true,
                associatedSceneId = "demo_scene_kost_01",
                associatedPlacementId = "place_kost_2"
            )
        )
    )

    override fun getShoppingListItems(): Flow<List<ShoppingCartItem>> = _items.asStateFlow()

    override suspend fun addOrIncrementProduct(
        product: Product,
        sceneId: String?,
        placementId: String?,
        quantityToAdd: Int
    ): Result<ShoppingCartItem> {
        val targetSceneKey = sceneId ?: "general"
        val existing = _items.value.find {
            it.product.id == product.id && (it.associatedSceneId ?: "general") == targetSceneKey
        }
        val resultItem = if (existing != null) {
            val updated = existing.copy(
                quantity = existing.quantity + quantityToAdd,
                isSelected = true,
                associatedPlacementId = placementId ?: existing.associatedPlacementId
            )
            _items.value = _items.value.map { if (it.itemId == existing.itemId) updated else it }
            updated
        } else {
            val newItem = ShoppingCartItem(
                itemId = "cart_${product.id}_$targetSceneKey",
                product = product,
                quantity = quantityToAdd.coerceAtLeast(1),
                isSelected = true,
                associatedSceneId = sceneId,
                associatedPlacementId = placementId
            )
            _items.value = listOf(newItem) + _items.value
            newItem
        }
        return Result.success(resultItem)
    }

    override suspend fun updateItemQuantity(itemId: String, newQuantity: Int): Result<Unit> {
        if (newQuantity <= 0) {
            _items.value = _items.value.filterNot { it.itemId == itemId || it.product.id == itemId }
        } else {
            _items.value = _items.value.map {
                if (it.itemId == itemId || it.product.id == itemId) it.copy(quantity = newQuantity) else it
            }
        }
        return Result.success(Unit)
    }

    override suspend fun toggleItemSelection(itemId: String): Result<Unit> {
        _items.value = _items.value.map {
            if (it.itemId == itemId || it.product.id == itemId) it.copy(isSelected = !it.isSelected) else it
        }
        return Result.success(Unit)
    }

    override suspend fun removeItem(itemId: String): Result<Unit> {
        _items.value = _items.value.filterNot { it.itemId == itemId || it.product.id == itemId }
        return Result.success(Unit)
    }

    override suspend fun syncScenePlacementsToShoppingList(
        scene: RoomScene,
        allProducts: List<Product>
    ): Result<Int> {
        var syncedCount = 0
        val groupedByProduct = scene.placements.groupBy { it.productId }
        for ((productId, placements) in groupedByProduct) {
            val prod = allProducts.find { it.id == productId } ?: continue
            val targetQty = placements.size
            val itemId = "cart_${prod.id}_${scene.id}"
            val existing = _items.value.find { it.itemId == itemId }
            if (existing != null) {
                val updated = existing.copy(quantity = maxOf(existing.quantity, targetQty), isSelected = true)
                _items.value = _items.value.map { if (it.itemId == itemId) updated else it }
            } else {
                val newItem = ShoppingCartItem(
                    itemId = itemId,
                    product = prod,
                    quantity = targetQty,
                    isSelected = true,
                    associatedSceneId = scene.id,
                    associatedPlacementId = placements.first().placementId
                )
                _items.value = listOf(newItem) + _items.value
            }
            syncedCount += targetQty
        }
        return Result.success(syncedCount)
    }
}

class MockAffiliateLinkRepository : AffiliateLinkRepository {
    private val _clickHistory = MutableStateFlow<List<AffiliateClickRecord>>(emptyList())

    override suspend fun getAffiliateLink(
        productId: String,
        sceneId: String,
        placementId: String?
    ): Result<AffiliateRedirectInfo> {
        val prod = MockData.products.find { it.id == productId } ?: MockData.products.first()
        val clickId = "attr_sim_${UUID.randomUUID().toString().take(8)}"
        val slotId = placementId ?: "catalog_direct"
        val userHash = "usr_anon_8f92a1"
        val tracking = TrackingMetadata(
            projectId = sceneId,
            subId1 = sceneId,
            subId2 = userHash,
            subId3 = slotId
        )
        val info = AffiliateRedirectInfo(
            clickAttributionId = clickId,
            productId = productId,
            sceneId = sceneId,
            placementId = placementId,
            marketplace = prod.marketplace,
            originalProductUrl = prod.originalProductUrl,
            generatedAffiliateUrl = "simulated://affiliate.ruangai.local/redirect?pid=$productId&mp=${prod.marketplace.name}&sub_id1=$sceneId&sub_id2=$userHash&sub_id3=$slotId&attr=$clickId",
            trackingMetadata = tracking,
            redirectDestination = RedirectDestination(
                destinationType = "SIMULATED_AFFILIATE_GATEWAY",
                targetMarketplaceName = prod.marketplace.displayName,
                simulatedReferenceCode = "SIM-${prod.marketplace.name}-${productId.uppercase()}-$clickId",
                isSimulatedRedirect = true
            ),
            status = AffiliateLinkStatus.SIMULATED,
            disclosureLabel = "RuangAI dapat memperoleh komisi dari pembelian melalui tautan afiliasi."
        )
        return Result.success(info)
    }

    override suspend fun recordAffiliateClick(productId: String, marketplace: String): Result<Unit> {
        val info = getAffiliateLink(productId, "general_catalog", null).getOrThrow()
        val prod = MockData.products.find { it.id == productId }
        recordStructuredAffiliateClick(info, prod?.title ?: productId)
        return Result.success(Unit)
    }

    override suspend fun recordStructuredAffiliateClick(
        info: AffiliateRedirectInfo,
        productTitle: String
    ): Result<AffiliateClickRecord> {
        val record = AffiliateClickRecord(
            clickAttributionId = info.clickAttributionId,
            productId = info.productId,
            productTitle = productTitle,
            marketplace = info.marketplace,
            sceneId = info.sceneId,
            placementId = info.placementId,
            subId1 = info.trackingMetadata.subId1,
            subId2 = info.trackingMetadata.subId2,
            subId3 = info.trackingMetadata.subId3,
            status = info.status,
            isSimulated = true
        )
        _clickHistory.value = listOf(record) + _clickHistory.value
        return Result.success(record)
    }

    override fun getAffiliateClickHistory(): Flow<List<AffiliateClickRecord>> = _clickHistory.asStateFlow()
}

class MockCreditWalletRepository : CreditWalletRepository {
    private val _balance = MutableStateFlow(CreditBalance(available = 3, reserved = 0, lifetimeConsumed = 1))
    private val _transactions = MutableStateFlow<List<CreditTransaction>>(
        listOf(
            CreditTransaction("tx_1", 3, TransactionType.WELCOME_BONUS, "Bonus Selamat Datang Pengguna Baru"),
            CreditTransaction("tx_2", -1, TransactionType.RENDER_CAPTURE, "Render Ruang Tamu Japandi")
        )
    )
    private val reservedKeys = mutableSetOf<String>()
    private val capturedKeys = mutableSetOf<String>()
    private val refundedKeys = mutableSetOf<String>()

    override fun getWalletBalance(): Flow<CreditBalance> = _balance.asStateFlow()

    override fun getTransactions(): Flow<List<CreditTransaction>> = _transactions.asStateFlow()

    override suspend fun reserveCredit(jobId: String): Result<Unit> = synchronized(this) {
        if (jobId.isBlank()) return Result.failure(IllegalArgumentException("jobId / idempotencyKey tidak boleh kosong"))
        // Idempotent guard: if already reserved (or already captured), do not deduct again
        if (reservedKeys.contains(jobId) || capturedKeys.contains(jobId)) {
            return Result.success(Unit)
        }
        if (_balance.value.available < 1) {
            return Result.failure(IllegalStateException("Saldo kredit tidak mencukupi"))
        }
        reservedKeys.add(jobId)
        _balance.value = _balance.value.copy(
            available = _balance.value.available - 1,
            reserved = _balance.value.reserved + 1
        )
        _transactions.value = listOf(
            CreditTransaction(
                id = "tx_res_${jobId.takeLast(8)}_${UUID.randomUUID().toString().take(4)}",
                amount = -1,
                type = TransactionType.RENDER_RESERVATION,
                description = "Reservasi Render AI (#$jobId)"
            )
        ) + _transactions.value
        return Result.success(Unit)
    }

    override suspend fun captureCredit(jobId: String): Result<Unit> = synchronized(this) {
        if (jobId.isBlank()) return Result.failure(IllegalArgumentException("jobId / idempotencyKey tidak boleh kosong"))
        if (capturedKeys.contains(jobId)) {
            return Result.success(Unit)
        }
        if (refundedKeys.contains(jobId)) {
            return Result.failure(IllegalStateException("Reservasi #$jobId sudah dikembalikan (refunded)"))
        }
        capturedKeys.add(jobId)
        _balance.value = _balance.value.copy(
            reserved = maxOf(0, _balance.value.reserved - 1),
            lifetimeConsumed = _balance.value.lifetimeConsumed + 1
        )
        _transactions.value = listOf(
            CreditTransaction(
                id = "tx_cap_${jobId.takeLast(8)}_${UUID.randomUUID().toString().take(4)}",
                amount = 0,
                type = TransactionType.RENDER_CAPTURE,
                description = "Kredit Terpakai — Render Selesai (#$jobId)"
            )
        ) + _transactions.value
        return Result.success(Unit)
    }

    override suspend fun refundCredit(jobId: String): Result<Unit> = synchronized(this) {
        if (jobId.isBlank()) return Result.failure(IllegalArgumentException("jobId / idempotencyKey tidak boleh kosong"))
        if (refundedKeys.contains(jobId)) {
            return Result.success(Unit)
        }
        if (capturedKeys.contains(jobId)) {
            return Result.failure(IllegalStateException("Kredit #$jobId sudah di-capture dan tidak dapat di-refund otomatis"))
        }
        refundedKeys.add(jobId)
        reservedKeys.remove(jobId)
        _balance.value = _balance.value.copy(
            available = _balance.value.available + 1,
            reserved = maxOf(0, _balance.value.reserved - 1)
        )
        _transactions.value = listOf(
            CreditTransaction(
                id = "tx_ref_${jobId.takeLast(8)}_${UUID.randomUUID().toString().take(4)}",
                amount = 1,
                type = TransactionType.AUTO_REFUND,
                description = "Pengembalian Kredit Gagal/Batal Render (#$jobId)"
            )
        ) + _transactions.value
        return Result.success(Unit)
    }

    override suspend fun purchaseCreditPackage(packageId: String): Result<Unit> {
        val pkg = MockData.creditPackages.find { it.id == packageId } ?: MockData.creditPackages.first()
        _balance.value = _balance.value.copy(available = _balance.value.available + pkg.credits)
        _transactions.value = listOf(
            CreditTransaction("tx_${UUID.randomUUID().toString().take(6)}", pkg.credits, TransactionType.TOP_UP, "Pembelian ${pkg.title}")
        ) + _transactions.value
        return Result.success(Unit)
    }
}

class MockAIChatRepository(
    private val recommendationEngine: ConsultantRecommendationEngine = ConsultantRecommendationEngine()
) : AIChatRepository {

    private val _conversations = MutableStateFlow<Map<String, ConsultationConversation>>(emptyMap())
    private val _messagesByScene = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

    init {
        val initialConvMap = mutableMapOf<String, ConsultationConversation>()
        val initialMsgMap = mutableMapOf<String, List<ChatMessage>>()
        for (scene in MockData.preloadedScenes) {
            val convId = "conv_${scene.id}"
            initialConvMap[scene.id] = ConsultationConversation(
                conversationId = convId,
                sceneId = scene.id,
                title = "Konsultasi ${scene.title}",
                selectedStyle = scene.preferredStyle ?: DesignStyle.JAPANDI
            )
            initialMsgMap[scene.id] = listOf(recommendationEngine.createInitialGreeting(scene, convId))
        }
        _conversations.value = initialConvMap
        _messagesByScene.value = initialMsgMap
    }

    override fun observeConversationForScene(sceneId: String): Flow<ConsultationConversation?> {
        return _conversations.map { it[sceneId] }
    }

    override fun observeMessagesForScene(sceneId: String): Flow<List<ChatMessage>> {
        return _messagesByScene.map { map ->
            map[sceneId] ?: run {
                val scene = MockData.preloadedScenes.find { it.id == sceneId }
                if (scene != null) {
                    listOf(recommendationEngine.createInitialGreeting(scene, "conv_$sceneId"))
                } else {
                    emptyList()
                }
            }
        }
    }

    override suspend fun getMessagesForSceneSync(sceneId: String): List<ChatMessage> {
        return _messagesByScene.value[sceneId] ?: emptyList()
    }

    override suspend fun getOrCreateConversation(scene: RoomScene): ConsultationConversation {
        val existing = _conversations.value[scene.id]
        if (existing != null) return existing

        val convId = "conv_${scene.id}"
        val created = ConsultationConversation(
            conversationId = convId,
            sceneId = scene.id,
            title = "Konsultasi ${scene.title}",
            selectedStyle = scene.preferredStyle ?: DesignStyle.JAPANDI
        )
        _conversations.value = _conversations.value + (scene.id to created)
        if (_messagesByScene.value[scene.id].isNullOrEmpty()) {
            _messagesByScene.value = _messagesByScene.value + (scene.id to listOf(
                recommendationEngine.createInitialGreeting(scene, convId)
            ))
        }
        return created
    }

    override suspend fun sendContextualMessage(
        context: ConsultationContext,
        userText: String,
        attachments: List<ChatAttachment>
    ): Result<ChatMessage> {
        val sceneId = context.scene.id
        val conv = getOrCreateConversation(context.scene)
        val now = System.currentTimeMillis()

        val userMsg = ChatMessage(
            id = "msg_usr_${UUID.randomUUID().toString().take(8)}",
            isFromUser = true,
            text = userText.ifBlank { "Melampirkan foto referensi ruangan" },
            timestamp = now,
            conversationId = conv.conversationId,
            senderRole = SenderRole.USER,
            attachments = attachments,
            isSimulated = false
        )

        val currentList = _messagesByScene.value[sceneId].orEmpty()
        _messagesByScene.value = _messagesByScene.value + (sceneId to (currentList + userMsg))

        val detectedStyle = recommendationEngine.parseStyleFromPrompt(userText)
        val detectedBudget = recommendationEngine.parseBudgetFromPrompt(userText)
        val updatedConv = conv.copy(
            selectedStyle = detectedStyle ?: conv.selectedStyle,
            customBudgetIdr = detectedBudget ?: conv.customBudgetIdr,
            updatedAt = now + 1
        )
        _conversations.value = _conversations.value + (sceneId to updatedConv)

        val aiReply = recommendationEngine.composeContextualAssistantMessage(
            conversationId = conv.conversationId,
            context = context.copy(conversation = updatedConv),
            userText = userText,
            attachments = attachments
        ).copy(timestamp = now + 2)

        val afterUser = _messagesByScene.value[sceneId].orEmpty()
        _messagesByScene.value = _messagesByScene.value + (sceneId to (afterUser + aiReply))
        return Result.success(aiReply)
    }

    override suspend fun updateConversationPreferences(
        sceneId: String,
        selectedStyle: DesignStyle?,
        customBudgetIdr: Long?
    ): Result<ConsultationConversation> {
        val current = _conversations.value[sceneId] ?: ConsultationConversation(
            conversationId = "conv_$sceneId",
            sceneId = sceneId,
            title = "Konsultasi Ruangan"
        )
        val updated = current.copy(
            selectedStyle = selectedStyle ?: current.selectedStyle,
            customBudgetIdr = customBudgetIdr ?: current.customBudgetIdr,
            updatedAt = System.currentTimeMillis()
        )
        _conversations.value = _conversations.value + (sceneId to updated)
        return Result.success(updated)
    }

    override suspend fun clearConversationForScene(sceneId: String): Result<Unit> {
        val scene = MockData.preloadedScenes.find { it.id == sceneId }
        val convId = _conversations.value[sceneId]?.conversationId ?: "conv_$sceneId"
        val resetMessages = if (scene != null) {
            listOf(recommendationEngine.createInitialGreeting(scene, convId))
        } else {
            emptyList()
        }
        _messagesByScene.value = _messagesByScene.value + (sceneId to resetMessages)
        return Result.success(Unit)
    }

    override suspend fun deleteConversation(conversationId: String): Result<Unit> {
        val targetSceneId = _conversations.value.entries.find { it.value.conversationId == conversationId }?.key
        if (targetSceneId != null) {
            _conversations.value = _conversations.value - targetSceneId
            _messagesByScene.value = _messagesByScene.value - targetSceneId
        }
        return Result.success(Unit)
    }
}

class MockMultimodalInputRepository : MultimodalInputRepository {
    override suspend fun attachRoomPhoto(
        conversationId: String,
        sourceUriOrPath: String,
        label: String
    ): Result<ChatAttachment> {
        val fileExists = sourceUriOrPath.startsWith("http") ||
            sourceUriOrPath.startsWith("content://") ||
            java.io.File(sourceUriOrPath.removePrefix("file://")).exists()
        return Result.success(
            ChatAttachment(
                id = "att_${UUID.randomUUID().toString().take(8)}",
                type = AttachmentType.ROOM_PHOTO,
                uriOrPath = sourceUriOrPath,
                label = label,
                fileExists = fileExists,
                isSimulatedAnalysis = true
            )
        )
    }

    override suspend fun verifyAttachmentAvailability(attachment: ChatAttachment): ChatAttachment {
        val exists = attachment.uriOrPath.startsWith("http") ||
            attachment.uriOrPath.startsWith("content://") ||
            java.io.File(attachment.uriOrPath.removePrefix("file://")).exists()
        return attachment.copy(fileExists = exists)
    }

    override suspend fun cleanupConversationAttachments(conversationId: String) = Unit
}

class MockAIInteriorDesignRepository(
    private val creditWalletRepository: CreditWalletRepository = MockCreditWalletRepository(),
    private val generator: DeterministicMakeoverGenerator = DeterministicMakeoverGenerator()
) : AIInteriorDesignRepository {

    private val _resultsByScene = MutableStateFlow<Map<String, List<DesignGenerationResult>>>(emptyMap())
    private val _requestsById = mutableMapOf<String, DesignGenerationRequest>()

    init {
        val initialMap = mutableMapOf<String, List<DesignGenerationResult>>()
        for (scene in MockData.preloadedScenes) {
            val seedReq = DesignGenerationRequest(
                requestId = "req_seed_${scene.id}",
                idempotencyKey = "idem_seed_${scene.id}",
                sceneId = scene.id,
                originalRoomImageUri = scene.originalImage.localUri,
                roomType = scene.roomType,
                selectedStyle = scene.preferredStyle ?: DesignStyle.JAPANDI,
                colorPaletteNames = listOf("Warm Linen", "Natural Oak", "Sage Mist"),
                desiredAtmosphere = DesignAtmosphere.WARM_COZY,
                furniturePreferences = listOf(ProductCategory.SOFA, ProductCategory.COFFEE_TABLE, ProductCategory.LIGHTING),
                targetBudgetIdr = scene.targetBudget,
                additionalInstructions = "Pertahankan pencahayaan alami dan sirkulasi ruang lega",
                variationCount = 3,
                creditCost = 1,
                createdAt = 1710000000000L
            )
            _requestsById[seedReq.requestId] = seedReq
            val seedRes = generator.generateMakeoverResult(
                request = seedReq,
                resultId = "res_seed_${scene.id}"
            )
            initialMap[scene.id] = listOf(seedRes)
        }
        _resultsByScene.value = initialMap
    }

    override fun validateConfiguration(request: DesignGenerationRequest): DesignConfigValidationResult {
        return request.validate()
    }

    override fun observeDesignHistoryForScene(sceneId: String): Flow<List<DesignGenerationResult>> {
        return _resultsByScene.map { map ->
            map[sceneId].orEmpty().sortedByDescending { it.createdAt }
        }
    }

    override suspend fun getDesignHistoryForSceneSync(sceneId: String): List<DesignGenerationResult> {
        return _resultsByScene.value[sceneId].orEmpty().sortedByDescending { it.createdAt }
    }

    override fun observeFavoriteVariations(sceneId: String?): Flow<List<DesignVariation>> {
        return _resultsByScene.map { map ->
            val allResults = if (sceneId != null) {
                map[sceneId].orEmpty()
            } else {
                map.values.flatten()
            }
            allResults.flatMap { it.variations }.filter { it.isFavorite }
        }
    }

    override suspend fun getResultByIdSync(resultId: String): DesignGenerationResult? {
        return _resultsByScene.value.values.flatten().find { it.resultId == resultId }
    }

    override suspend fun generateRoomMakeover(request: DesignGenerationRequest): Result<DesignGenerationResult> {
        val validation = request.validate()
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException(validation.errors.joinToString(" ")))
        }

        // Duplicate request protection via idempotencyKey
        val existingByIdempotency = _resultsByScene.value.values.flatten().find {
            it.idempotencyKey == request.idempotencyKey
        }
        if (existingByIdempotency != null && existingByIdempotency.status == DesignGenerationStatus.COMPLETED) {
            return Result.success(existingByIdempotency)
        }

        _requestsById[request.requestId] = request

        // 1. Reserve credit atomically using idempotencyKey
        val reserveRes = creditWalletRepository.reserveCredit(request.idempotencyKey)
        if (reserveRes.isFailure) {
            return Result.failure(
                reserveRes.exceptionOrNull() ?: IllegalStateException("Saldo kredit tidak mencukupi untuk makeover ruangan.")
            )
        }

        val now = System.currentTimeMillis()
        val resultId = existingByIdempotency?.resultId ?: "res_${request.requestId}"

        // 2. Record PENDING -> PROCESSING state
        val processingResult = DesignGenerationResult(
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
        upsertResultInMemory(processingResult)

        // 3. Handle simulated failure and automatic credit refund
        if (request.simulateFailure) {
            creditWalletRepository.refundCredit(request.idempotencyKey)
            val failedResult = processingResult.copy(
                status = DesignGenerationStatus.FAILED,
                creditCaptured = false,
                creditRefunded = true,
                errorMessage = "Simulasi kegagalan pemrosesan render AI. 1 Kredit telah dikembalikan otomatis ke dompet Anda.",
                updatedAt = now + 10
            )
            upsertResultInMemory(failedResult)
            return Result.failure(IllegalStateException(failedResult.errorMessage))
        }

        // 4. Deterministic generation & credit capture
        val completed = generator.generateMakeoverResult(
            request = request,
            resultId = resultId
        ).copy(
            updatedAt = now + 20
        )
        creditWalletRepository.captureCredit(request.idempotencyKey)
        upsertResultInMemory(completed)
        return Result.success(completed)
    }

    override suspend fun retryDesignGeneration(requestId: String): Result<DesignGenerationResult> {
        val existingResult = _resultsByScene.value.values.flatten().find {
            it.requestId == requestId || it.resultId == requestId
        } ?: return Result.failure(NoSuchElementException("Permintaan desain $requestId tidak ditemukan."))

        val baseRequest = _requestsById[existingResult.requestId] ?: DesignGenerationRequest(
            requestId = existingResult.requestId,
            idempotencyKey = existingResult.idempotencyKey,
            sceneId = existingResult.sceneId,
            originalRoomImageUri = existingResult.originalRoomImageUri,
            roomType = existingResult.roomType,
            selectedStyle = existingResult.selectedStyle,
            colorPaletteNames = existingResult.colorPaletteNames,
            desiredAtmosphere = existingResult.desiredAtmosphere,
            furniturePreferences = existingResult.furniturePreferences,
            targetBudgetIdr = existingResult.targetBudgetIdr,
            additionalInstructions = existingResult.additionalInstructions
        )

        val retryKey = "${baseRequest.idempotencyKey}_retry_${System.currentTimeMillis()}"
        val retryRequest = baseRequest.copy(
            idempotencyKey = retryKey,
            simulateFailure = false
        )

        val reserveRes = creditWalletRepository.reserveCredit(retryKey)
        if (reserveRes.isFailure) {
            return Result.failure(
                reserveRes.exceptionOrNull() ?: IllegalStateException("Saldo kredit tidak mencukupi untuk mencoba ulang.")
            )
        }

        val regenerated = generator.generateMakeoverResult(
            request = retryRequest,
            resultId = existingResult.resultId
        ).copy(
            requestId = existingResult.requestId,
            idempotencyKey = retryKey,
            createdAt = existingResult.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        creditWalletRepository.captureCredit(retryKey)
        upsertResultInMemory(regenerated)
        return Result.success(regenerated)
    }

    override suspend fun cancelDesignGeneration(requestId: String): Result<DesignGenerationResult> {
        val existing = _resultsByScene.value.values.flatten().find {
            it.requestId == requestId || it.resultId == requestId
        } ?: return Result.failure(NoSuchElementException("Permintaan desain $requestId tidak ditemukan."))

        if (existing.creditReserved && !existing.creditCaptured && !existing.creditRefunded) {
            creditWalletRepository.refundCredit(existing.idempotencyKey)
        }

        val cancelled = existing.copy(
            status = DesignGenerationStatus.CANCELLED,
            creditRefunded = existing.creditReserved && !existing.creditCaptured,
            errorMessage = "Generasi desain dibatalkan oleh pengguna.",
            updatedAt = System.currentTimeMillis()
        )
        upsertResultInMemory(cancelled)
        return Result.success(cancelled)
    }

    override suspend fun selectActiveVariation(
        resultId: String,
        variationId: String
    ): Result<DesignGenerationResult> {
        val existing = getResultByIdSync(resultId)
            ?: return Result.failure(NoSuchElementException("Hasil desain $resultId tidak ditemukan."))
        if (existing.variations.none { it.variationId == variationId }) {
            return Result.failure(IllegalArgumentException("Variasi $variationId tidak ditemukan pada hasil $resultId."))
        }
        val updated = existing.copy(
            selectedVariationId = variationId,
            updatedAt = System.currentTimeMillis()
        )
        upsertResultInMemory(updated)
        return Result.success(updated)
    }

    override suspend fun toggleFavoriteVariation(
        variationId: String,
        isFavorite: Boolean
    ): Result<DesignVariation> {
        val allResults = _resultsByScene.value.values.flatten()
        val parentResult = allResults.find { res -> res.variations.any { it.variationId == variationId } }
            ?: return Result.failure(NoSuchElementException("Variasi $variationId tidak ditemukan."))

        var updatedVariation: DesignVariation? = null
        val updatedVariations = parentResult.variations.map { v ->
            if (v.variationId == variationId) {
                v.copy(isFavorite = isFavorite).also { updatedVariation = it }
            } else v
        }
        val updatedResult = parentResult.copy(
            variations = updatedVariations,
            updatedAt = System.currentTimeMillis()
        )
        upsertResultInMemory(updatedResult)
        return Result.success(updatedVariation!!)
    }

    private fun upsertResultInMemory(result: DesignGenerationResult) {
        val currentList = _resultsByScene.value[result.sceneId].orEmpty()
        val updatedList = if (currentList.any { it.resultId == result.resultId }) {
            currentList.map { if (it.resultId == result.resultId) result else it }
        } else {
            listOf(result) + currentList
        }
        _resultsByScene.value = _resultsByScene.value + (result.sceneId to updatedList)
    }
}
