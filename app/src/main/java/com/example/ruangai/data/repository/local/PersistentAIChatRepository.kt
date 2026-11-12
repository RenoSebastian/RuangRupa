package com.example.ruangai.data.repository.local

import com.example.ruangai.data.local.db.dao.ConsultationDao
import com.example.ruangai.data.local.db.dao.RoomSceneDao
import com.example.ruangai.data.local.db.mapper.ConsultationMapper
import com.example.ruangai.data.local.db.mapper.RoomSceneMapper
import com.example.ruangai.data.local.storage.LocalImageStorageManager
import com.example.ruangai.data.repository.mock.ConsultantRecommendationEngine
import com.example.ruangai.domain.model.*
import com.example.ruangai.domain.repository.AIChatRepository
import com.example.ruangai.domain.repository.MultimodalInputRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PersistentAIChatRepository(
    private val consultationDao: ConsultationDao,
    private val roomSceneDao: RoomSceneDao,
    private val recommendationEngine: ConsultantRecommendationEngine = ConsultantRecommendationEngine(),
    private val storageManager: LocalImageStorageManager? = null
) : AIChatRepository {

    override fun observeConversationForScene(sceneId: String): Flow<ConsultationConversation?> {
        return consultationDao.observeConversationForScene(sceneId).map { entity ->
            entity?.let { ConsultationMapper.toDomain(it) }
        }
    }

    override fun observeMessagesForScene(sceneId: String): Flow<List<ChatMessage>> {
        return consultationDao.observeConversationForScene(sceneId).flatMapLatest { convEntity ->
            if (convEntity == null) {
                flowOf(emptyList())
            } else {
                consultationDao.observeMessagesForConversation(convEntity.conversationId).map { list ->
                    list.map { ConsultationMapper.toDomain(it) }
                }
            }
        }
    }

    override suspend fun getMessagesForSceneSync(sceneId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        val conv = consultationDao.getConversationForSceneSync(sceneId) ?: return@withContext emptyList()
        consultationDao.getMessagesForConversationSync(conv.conversationId).map {
            ConsultationMapper.toDomain(it)
        }
    }

    override suspend fun getOrCreateConversation(scene: RoomScene): ConsultationConversation = withContext(Dispatchers.IO) {
        // Ensure parent RoomScene exists in room_scenes to satisfy FOREIGN KEY (sceneId) REFERENCES room_scenes(id)
        val existingScene = roomSceneDao.getSceneByIdSync(scene.id)
        if (existingScene == null) {
            roomSceneDao.insertScene(RoomSceneMapper.toEntity(scene))
        }

        val existingConv = consultationDao.getConversationForSceneSync(scene.id)
        if (existingConv != null) {
            return@withContext ConsultationMapper.toDomain(existingConv)
        }

        val convId = "conv_${scene.id}"
        val now = System.currentTimeMillis()
        val newConv = ConsultationConversation(
            conversationId = convId,
            sceneId = scene.id,
            title = "Konsultasi ${scene.title}",
            selectedStyle = scene.preferredStyle ?: DesignStyle.JAPANDI,
            customBudgetIdr = null,
            status = ConversationStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )
        consultationDao.upsertConversation(ConsultationMapper.toEntity(newConv))

        // Seed initial contextual assistant welcome message if conversation has no messages
        val currentMsgs = consultationDao.getMessagesForConversationSync(convId)
        if (currentMsgs.isEmpty()) {
            val welcome = recommendationEngine.createInitialGreeting(scene, convId)
            consultationDao.insertMessage(ConsultationMapper.toEntity(welcome, convId))
        }

        newConv
    }

    override suspend fun sendContextualMessage(
        context: ConsultationContext,
        userText: String,
        attachments: List<ChatAttachment>
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val conv = getOrCreateConversation(context.scene)
            val now = System.currentTimeMillis()

            // Persist user message
            val userMessage = ChatMessage(
                id = "msg_usr_${UUID.randomUUID().toString().take(8)}",
                isFromUser = true,
                text = userText.ifBlank { "Melampirkan foto referensi ruangan" },
                timestamp = now,
                conversationId = conv.conversationId,
                senderRole = SenderRole.USER,
                attachments = attachments,
                isSimulated = false,
                deliveryStatus = MessageDeliveryStatus.DELIVERED
            )
            consultationDao.insertMessage(ConsultationMapper.toEntity(userMessage, conv.conversationId))

            // Detect style or budget update in prompt
            val detectedStyle = recommendationEngine.parseStyleFromPrompt(userText)
            val detectedBudget = recommendationEngine.parseBudgetFromPrompt(userText)
            var updatedConv = conv
            if (detectedStyle != null || detectedBudget != null) {
                updatedConv = conv.copy(
                    selectedStyle = detectedStyle ?: conv.selectedStyle,
                    customBudgetIdr = detectedBudget ?: conv.customBudgetIdr,
                    updatedAt = now + 1
                )
                consultationDao.upsertConversation(ConsultationMapper.toEntity(updatedConv))
                if (detectedStyle != null) {
                    roomSceneDao.updatePreferredStyle(context.scene.id, detectedStyle.name, now + 1)
                }
            }

            // Compose contextual simulated AI response with real catalog recommendations
            val updatedContext = context.copy(conversation = updatedConv)
            val assistantMessage = recommendationEngine.composeContextualAssistantMessage(
                conversationId = conv.conversationId,
                context = updatedContext,
                userText = userText,
                attachments = attachments
            ).copy(timestamp = now + 2)

            consultationDao.insertMessage(ConsultationMapper.toEntity(assistantMessage, conv.conversationId))
            consultationDao.upsertConversation(
                ConsultationMapper.toEntity(updatedConv.copy(updatedAt = now + 2))
            )

            Result.success(assistantMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateConversationPreferences(
        sceneId: String,
        selectedStyle: DesignStyle?,
        customBudgetIdr: Long?
    ): Result<ConsultationConversation> = withContext(Dispatchers.IO) {
        try {
            val sceneEntity = roomSceneDao.getSceneByIdSync(sceneId)
            val domainScene = sceneEntity?.let { RoomSceneMapper.toDomain(it) }
                ?: RoomScene(
                    id = sceneId,
                    title = "Proyek Ruangan",
                    roomType = RoomType.LIVING_ROOM,
                    originalImage = OriginalRoomImage("orig_$sceneId", "")
                )
            val current = getOrCreateConversation(domainScene)
            val now = System.currentTimeMillis()
            val updated = current.copy(
                selectedStyle = selectedStyle ?: current.selectedStyle,
                customBudgetIdr = customBudgetIdr ?: current.customBudgetIdr,
                updatedAt = now
            )
            consultationDao.upsertConversation(ConsultationMapper.toEntity(updated))
            if (selectedStyle != null) {
                roomSceneDao.updatePreferredStyle(sceneId, selectedStyle.name, now)
            }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearConversationForScene(sceneId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val conv = consultationDao.getConversationForSceneSync(sceneId)
            if (conv != null) {
                consultationDao.deleteMessagesForConversation(conv.conversationId)
                storageManager?.deleteConversationAttachments(conv.conversationId)
                val sceneEntity = roomSceneDao.getSceneByIdSync(sceneId)
                if (sceneEntity != null) {
                    val domainScene = RoomSceneMapper.toDomain(sceneEntity)
                    val welcome = recommendationEngine.createInitialGreeting(domainScene, conv.conversationId)
                    consultationDao.insertMessage(ConsultationMapper.toEntity(welcome, conv.conversationId))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteConversation(conversationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            consultationDao.deleteConversationById(conversationId)
            storageManager?.deleteConversationAttachments(conversationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class PersistentMultimodalInputRepository(
    private val storageManager: LocalImageStorageManager? = null
) : MultimodalInputRepository {

    override suspend fun attachRoomPhoto(
        conversationId: String,
        sourceUriOrPath: String,
        label: String
    ): Result<ChatAttachment> = withContext(Dispatchers.IO) {
        try {
            val saved = storageManager?.saveChatAttachmentImage(conversationId, sourceUriOrPath)
            val finalPath = saved?.localFilePath ?: sourceUriOrPath
            val exists = saved?.fileExists ?: ConsultationMapper.checkAttachmentFileExists(finalPath)

            val attachment = ChatAttachment(
                id = "att_${UUID.randomUUID().toString().take(8)}",
                type = AttachmentType.ROOM_PHOTO,
                uriOrPath = finalPath,
                label = label,
                mimeType = "image/jpeg",
                width = saved?.width ?: 1280,
                height = saved?.height ?: 960,
                fileExists = exists,
                isSimulatedAnalysis = true,
                simulationNote = "Simulasi — Foto disimpan di penyimpanan internal proyek (analisis AI cloud belum aktif)"
            )
            Result.success(attachment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyAttachmentAvailability(attachment: ChatAttachment): ChatAttachment = withContext(Dispatchers.IO) {
        val exists = ConsultationMapper.checkAttachmentFileExists(attachment.uriOrPath)
        attachment.copy(fileExists = exists)
    }

    override suspend fun cleanupConversationAttachments(conversationId: String) = withContext(Dispatchers.IO) {
        storageManager?.deleteConversationAttachments(conversationId)
        Unit
    }
}

