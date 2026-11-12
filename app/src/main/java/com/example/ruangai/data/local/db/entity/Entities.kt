package com.example.ruangai.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "room_scenes")
data class RoomSceneEntity(
    @PrimaryKey val id: String,
    val title: String,
    val roomType: String,
    val originalImagePath: String,
    val previewImagePath: String,
    val width: Int,
    val height: Int,
    val targetBudget: Long,
    val userDimWidthCm: Int? = null,
    val userDimDepthCm: Int? = null,
    val userDimHeightCm: Int? = null,
    val activeMakeoverId: String? = null,
    val preferredStyle: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "room_placements")
data class RoomPlacementEntity(
    @PrimaryKey val placementId: String,
    val sceneId: String,
    val productId: String,
    val productTitle: String,
    val productPriceIdr: Long,
    val categoryName: String,
    val cutoutUri: String,
    val dimWidthCm: Int,
    val dimDepthCm: Int,
    val dimHeightCm: Int,
    val translationX: Float,
    val translationY: Float,
    val scale: Float,
    val rotationDegrees: Float,
    val zIndex: Int
)

@Entity(tableName = "room_analysis_cache")
data class RoomAnalysisEntity(
    @PrimaryKey val sceneId: String,
    val detectedRoomType: String,
    val dominantColorsJson: String,
    val wallFloorObservations: String,
    val doorsWindowsObservations: String,
    val existingFurnitureJson: String,
    val lightingConditions: String,
    val approximateLayoutObservations: String,
    val suggestedStylesJson: String,
    val isSimulation: Boolean = true,
    val analyzedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scene_reference_images")
data class SceneReferenceImageEntity(
    @PrimaryKey val id: String,
    val sceneId: String,
    val imagePath: String,
    val label: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shopping_list_items")
data class ShoppingListItemEntity(
    @PrimaryKey val itemId: String, // e.g. "cart_${productId}_${sceneId ?: "general"}"
    val productId: String,
    val quantity: Int,
    val isSelected: Boolean,
    val associatedSceneId: String?,
    val associatedPlacementId: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "affiliate_click_logs")
data class AffiliateClickEntity(
    @PrimaryKey val clickAttributionId: String,
    val productId: String,
    val productTitle: String,
    val marketplace: String,
    val sceneId: String,
    val placementId: String?,
    val subId1: String,
    val subId2: String,
    val subId3: String,
    val status: String,
    val isSimulated: Boolean = true,
    val clickedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "consultation_conversations",
    foreignKeys = [
        ForeignKey(
            entity = RoomSceneEntity::class,
            parentColumns = ["id"],
            childColumns = ["sceneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sceneId"])
    ]
)
data class ConsultationConversationEntity(
    @PrimaryKey val conversationId: String,
    val sceneId: String,
    val title: String,
    val selectedStyleId: String? = null,
    val customBudgetIdr: Long? = null,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "consultation_messages",
    foreignKeys = [
        ForeignKey(
            entity = ConsultationConversationEntity::class,
            parentColumns = ["conversationId"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId", "timestamp"])
    ]
)
data class ConsultationMessageEntity(
    @PrimaryKey val messageId: String,
    val conversationId: String,
    val senderRole: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentsJson: String = "[]",
    val recommendedProductsJson: String = "[]",
    val suggestedActionsJson: String = "[]",
    val evaluatedBudgetIdr: Long? = null,
    val isSimulated: Boolean = true,
    val deliveryStatus: String = "DELIVERED"
)

@Entity(
    tableName = "design_generation_requests",
    foreignKeys = [
        ForeignKey(
            entity = RoomSceneEntity::class,
            parentColumns = ["id"],
            childColumns = ["sceneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sceneId", "createdAt"]),
        Index(value = ["idempotencyKey"], unique = true)
    ]
)
data class DesignGenerationRequestEntity(
    @PrimaryKey val resultId: String,
    val requestId: String,
    val idempotencyKey: String,
    val sceneId: String,
    val originalRoomImageUri: String,
    val roomType: String,
    val selectedStyle: String,
    val colorPaletteNamesJson: String = "[]",
    val desiredAtmosphere: String,
    val furniturePreferencesJson: String = "[]",
    val targetBudgetIdr: Long,
    val additionalInstructions: String = "",
    val status: String,
    val selectedVariationId: String? = null,
    val creditCost: Int = 1,
    val creditReserved: Boolean = false,
    val creditCaptured: Boolean = false,
    val creditRefunded: Boolean = false,
    val errorMessage: String? = null,
    val isSimulated: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "design_variations",
    foreignKeys = [
        ForeignKey(
            entity = DesignGenerationRequestEntity::class,
            parentColumns = ["resultId"],
            childColumns = ["resultId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["resultId", "variationIndex"]),
        Index(value = ["sceneId", "isFavorite"])
    ]
)
data class DesignVariationEntity(
    @PrimaryKey val variationId: String,
    val resultId: String,
    val requestId: String,
    val sceneId: String,
    val variationIndex: Int,
    val variationTitle: String,
    val style: String,
    val colorPaletteNamesJson: String = "[]",
    val colorPaletteHexesJson: String = "[]",
    val atmosphere: String,
    val roomType: String,
    val previewImageUrl: String,
    val designSummary: String,
    val layoutAndLightingNotes: String,
    val estimatedFurnitureBudgetIdr: Long,
    val targetBudgetIdr: Long,
    val catalogMatchedProductsJson: String = "[]",
    val hotspotPinsJson: String = "[]",
    val isFavorite: Boolean = false,
    val isSimulated: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
