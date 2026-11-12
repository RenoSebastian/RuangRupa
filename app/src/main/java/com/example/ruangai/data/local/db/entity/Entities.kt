package com.example.ruangai.data.local.db.entity

import androidx.room.Entity
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
