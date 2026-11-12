package com.example.ruangai.data.local.db.dao

import androidx.room.*
import com.example.ruangai.data.local.db.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomSceneDao {
    @Query("SELECT * FROM room_scenes ORDER BY updatedAt DESC")
    fun getAllScenes(): Flow<List<RoomSceneEntity>>

    @Query("SELECT * FROM room_scenes ORDER BY updatedAt DESC")
    suspend fun getAllScenesSync(): List<RoomSceneEntity>

    @Query("SELECT * FROM room_scenes WHERE id = :id")
    fun getSceneById(id: String): Flow<RoomSceneEntity?>

    @Query("SELECT * FROM room_scenes WHERE id = :id")
    suspend fun getSceneByIdSync(id: String): RoomSceneEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSceneIgnore(scene: RoomSceneEntity): Long

    @Update
    suspend fun updateScene(scene: RoomSceneEntity)

    @Transaction
    suspend fun insertScene(scene: RoomSceneEntity) {
        val rowId = insertSceneIgnore(scene)
        if (rowId == -1L) {
            updateScene(scene)
        }
    }

    @Query("UPDATE room_scenes SET preferredStyle = :styleName, updatedAt = :updatedAt WHERE id = :sceneId")
    suspend fun updatePreferredStyle(sceneId: String, styleName: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE room_scenes SET userDimWidthCm = :widthCm, userDimDepthCm = :depthCm, userDimHeightCm = :heightCm, updatedAt = :updatedAt WHERE id = :sceneId")
    suspend fun updateRoomDimensions(sceneId: String, widthCm: Int?, depthCm: Int?, heightCm: Int?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE room_scenes SET activeMakeoverId = :activeMakeoverId, previewImagePath = :previewImagePath, updatedAt = :updatedAt WHERE id = :sceneId")
    suspend fun updateActiveMakeover(
        sceneId: String,
        activeMakeoverId: String?,
        previewImagePath: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM room_scenes WHERE id = :id")
    suspend fun deleteScene(id: String)
}

@Dao
interface RoomPlacementDao {
    @Query("SELECT * FROM room_placements WHERE sceneId = :sceneId ORDER BY zIndex ASC")
    fun getPlacementsForScene(sceneId: String): Flow<List<RoomPlacementEntity>>

    @Query("SELECT * FROM room_placements WHERE sceneId = :sceneId ORDER BY zIndex ASC")
    suspend fun getPlacementsForSceneSync(sceneId: String): List<RoomPlacementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacement(placement: RoomPlacementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacements(placements: List<RoomPlacementEntity>)

    @Query("DELETE FROM room_placements WHERE placementId = :placementId")
    suspend fun deletePlacement(placementId: String)

    @Query("DELETE FROM room_placements WHERE sceneId = :sceneId")
    suspend fun deletePlacementsForScene(sceneId: String)
}

@Dao
interface RoomAnalysisDao {
    @Query("SELECT * FROM room_analysis_cache WHERE sceneId = :sceneId")
    suspend fun getAnalysisForScene(sceneId: String): RoomAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: RoomAnalysisEntity)

    @Query("DELETE FROM room_analysis_cache WHERE sceneId = :sceneId")
    suspend fun deleteAnalysisForScene(sceneId: String)
}

@Dao
interface SceneReferenceImageDao {
    @Query("SELECT * FROM scene_reference_images WHERE sceneId = :sceneId ORDER BY addedAt ASC")
    fun getReferenceImagesForScene(sceneId: String): Flow<List<SceneReferenceImageEntity>>

    @Query("SELECT * FROM scene_reference_images WHERE sceneId = :sceneId ORDER BY addedAt ASC")
    suspend fun getReferenceImagesForSceneSync(sceneId: String): List<SceneReferenceImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferenceImage(image: SceneReferenceImageEntity)

    @Query("DELETE FROM scene_reference_images WHERE sceneId = :sceneId")
    suspend fun deleteReferenceImagesForScene(sceneId: String)
}

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list_items ORDER BY addedAt DESC")
    fun getAllItems(): Flow<List<ShoppingListItemEntity>>

    @Query("SELECT * FROM shopping_list_items ORDER BY addedAt DESC")
    suspend fun getAllItemsSync(): List<ShoppingListItemEntity>

    @Query("SELECT * FROM shopping_list_items WHERE itemId = :itemId")
    suspend fun getItemById(itemId: String): ShoppingListItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: ShoppingListItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<ShoppingListItemEntity>)

    @Query("DELETE FROM shopping_list_items WHERE itemId = :itemId")
    suspend fun deleteItemById(itemId: String)

    @Query("DELETE FROM shopping_list_items WHERE associatedSceneId = :sceneId")
    suspend fun deleteItemsForScene(sceneId: String)

    @Query("DELETE FROM shopping_list_items")
    suspend fun clearAll()
}

@Dao
interface AffiliateClickDao {
    @Query("SELECT * FROM affiliate_click_logs ORDER BY clickedAt DESC")
    fun getAllClickLogs(): Flow<List<AffiliateClickEntity>>

    @Query("SELECT * FROM affiliate_click_logs ORDER BY clickedAt DESC")
    suspend fun getAllClickLogsSync(): List<AffiliateClickEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClickLog(log: AffiliateClickEntity)
}

@Dao
interface ConsultationDao {
    @Query("SELECT * FROM consultation_conversations WHERE sceneId = :sceneId ORDER BY updatedAt DESC LIMIT 1")
    fun observeConversationForScene(sceneId: String): Flow<ConsultationConversationEntity?>

    @Query("SELECT * FROM consultation_conversations WHERE sceneId = :sceneId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getConversationForSceneSync(sceneId: String): ConsultationConversationEntity?

    @Query("SELECT * FROM consultation_conversations WHERE conversationId = :conversationId")
    suspend fun getConversationByIdSync(conversationId: String): ConsultationConversationEntity?

    @Query("SELECT * FROM consultation_conversations WHERE sceneId = :sceneId")
    suspend fun getAllConversationsForSceneSync(sceneId: String): List<ConsultationConversationEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversationIgnore(conversation: ConsultationConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConsultationConversationEntity)

    @Transaction
    suspend fun upsertConversation(conversation: ConsultationConversationEntity) {
        val rowId = insertConversationIgnore(conversation)
        if (rowId == -1L) {
            updateConversation(conversation)
        }
    }

    @Query("DELETE FROM consultation_conversations WHERE conversationId = :conversationId")
    suspend fun deleteConversationById(conversationId: String)

    @Query("DELETE FROM consultation_conversations WHERE sceneId = :sceneId")
    suspend fun deleteConversationsForScene(sceneId: String)

    @Query("SELECT * FROM consultation_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessagesForConversation(conversationId: String): Flow<List<ConsultationMessageEntity>>

    @Query("SELECT * FROM consultation_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversationSync(conversationId: String): List<ConsultationMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConsultationMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ConsultationMessageEntity>)

    @Query("DELETE FROM consultation_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
}

@Dao
interface DesignGenerationDao {
    @Query("SELECT * FROM design_generation_requests WHERE sceneId = :sceneId ORDER BY createdAt DESC")
    fun observeRequestsForScene(sceneId: String): Flow<List<DesignGenerationRequestEntity>>

    @Query("SELECT * FROM design_generation_requests WHERE sceneId = :sceneId ORDER BY createdAt DESC")
    suspend fun getRequestsForSceneSync(sceneId: String): List<DesignGenerationRequestEntity>

    @Query("SELECT * FROM design_generation_requests WHERE resultId = :resultId LIMIT 1")
    suspend fun getRequestByResultIdSync(resultId: String): DesignGenerationRequestEntity?

    @Query("SELECT * FROM design_generation_requests WHERE requestId = :requestId LIMIT 1")
    suspend fun getRequestByRequestIdSync(requestId: String): DesignGenerationRequestEntity?

    @Query("SELECT * FROM design_generation_requests WHERE idempotencyKey = :idempotencyKey LIMIT 1")
    suspend fun getRequestByIdempotencyKeySync(idempotencyKey: String): DesignGenerationRequestEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRequestIgnore(entity: DesignGenerationRequestEntity): Long

    @Update
    suspend fun updateRequest(entity: DesignGenerationRequestEntity)

    @Transaction
    suspend fun upsertRequest(entity: DesignGenerationRequestEntity) {
        val rowId = insertRequestIgnore(entity)
        if (rowId == -1L) {
            updateRequest(entity)
        }
    }

    @Query("SELECT * FROM design_variations WHERE resultId = :resultId ORDER BY variationIndex ASC")
    fun observeVariationsForResult(resultId: String): Flow<List<DesignVariationEntity>>

    @Query("SELECT * FROM design_variations WHERE resultId = :resultId ORDER BY variationIndex ASC")
    suspend fun getVariationsForResultSync(resultId: String): List<DesignVariationEntity>

    @Query("SELECT * FROM design_variations WHERE sceneId = :sceneId ORDER BY createdAt DESC, variationIndex ASC")
    fun observeVariationsForScene(sceneId: String): Flow<List<DesignVariationEntity>>

    @Query("SELECT * FROM design_variations WHERE sceneId = :sceneId ORDER BY createdAt DESC, variationIndex ASC")
    suspend fun getVariationsForSceneSync(sceneId: String): List<DesignVariationEntity>

    @Query("SELECT * FROM design_variations WHERE variationId = :variationId LIMIT 1")
    suspend fun getVariationByIdSync(variationId: String): DesignVariationEntity?

    @Query("SELECT * FROM design_variations WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeAllFavoriteVariations(): Flow<List<DesignVariationEntity>>

    @Query("SELECT * FROM design_variations WHERE sceneId = :sceneId AND isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavoriteVariationsForScene(sceneId: String): Flow<List<DesignVariationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVariation(variation: DesignVariationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVariations(variations: List<DesignVariationEntity>)

    @Query("UPDATE design_variations SET isFavorite = :isFavorite WHERE variationId = :variationId")
    suspend fun updateVariationFavorite(variationId: String, isFavorite: Boolean)

    @Query("DELETE FROM design_generation_requests WHERE resultId = :resultId")
    suspend fun deleteResultById(resultId: String)
}
