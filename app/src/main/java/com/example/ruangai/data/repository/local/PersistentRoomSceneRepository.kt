package com.example.ruangai.data.repository.local

import android.content.Context
import android.net.Uri
import com.example.ruangai.data.local.db.dao.RoomPlacementDao
import com.example.ruangai.data.local.db.dao.RoomSceneDao
import com.example.ruangai.data.local.db.mapper.RoomSceneMapper
import com.example.ruangai.data.local.storage.LocalImageStorageManager
import com.example.ruangai.data.repository.mock.MockData
import com.example.ruangai.domain.model.*
import com.example.ruangai.domain.repository.RoomSceneRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PersistentRoomSceneRepository(
    private val context: Context,
    private val roomSceneDao: RoomSceneDao,
    private val placementDao: RoomPlacementDao,
    private val storageManager: LocalImageStorageManager = LocalImageStorageManager(context),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : RoomSceneRepository {

    init {
        // Seed initial demo rooms if database is empty
        scope.launch {
            seedInitialScenesIfNeeded()
        }
    }

    private suspend fun seedInitialScenesIfNeeded() = withContext(Dispatchers.IO) {
        val initialScenes = roomSceneDao.getAllScenes().first()
        if (initialScenes.isEmpty()) {
            for (scene in MockData.preloadedScenes) {
                roomSceneDao.insertScene(RoomSceneMapper.toEntity(scene))
                for (placement in scene.placements) {
                    placementDao.insertPlacement(RoomSceneMapper.toEntity(scene.id, placement))
                }
            }
        }
    }

    override fun getRoomScenes(): Flow<List<RoomScene>> {
        return roomSceneDao.getAllScenes().flatMapLatest { sceneEntities ->
            if (sceneEntities.isEmpty()) {
                flowOf(emptyList())
            } else {
                val sceneFlows = sceneEntities.map { entity ->
                    placementDao.getPlacementsForScene(entity.id).map { placementEntities ->
                        RoomSceneMapper.toDomain(entity, placementEntities)
                    }
                }
                combine(sceneFlows) { it.toList() }
            }
        }
    }

    override fun getRoomSceneById(sceneId: String): Flow<RoomScene?> {
        return roomSceneDao.getSceneById(sceneId).flatMapLatest { entity ->
            if (entity == null) {
                flowOf(null)
            } else {
                placementDao.getPlacementsForScene(sceneId).map { placements ->
                    RoomSceneMapper.toDomain(entity, placements)
                }
            }
        }
    }

    override suspend fun createRoomScene(
        title: String,
        type: RoomType,
        imageUri: String
    ): Result<RoomScene> = withContext(Dispatchers.IO) {
        try {
            val sceneId = "scene_${UUID.randomUUID().toString().take(8)}"
            var originalPath = imageUri
            var previewPath = imageUri
            var imgWidth = 1280
            var imgHeight = 960

            // If image is a local URI from Camera or Photo Picker, persist to durable internal storage
            if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
                try {
                    val savedResult = storageManager.saveRoomImage(sceneId, Uri.parse(imageUri))
                    originalPath = savedResult.originalFilePath
                    previewPath = savedResult.previewFilePath
                    imgWidth = savedResult.width
                    imgHeight = savedResult.height
                } catch (e: Exception) {
                    // Fallback to source URI if direct copy failed (e.g. test environment)
                    originalPath = imageUri
                    previewPath = imageUri
                }
            }

            val newScene = RoomScene(
                id = sceneId,
                title = title,
                roomType = type,
                originalImage = OriginalRoomImage(
                    assetId = "orig_$sceneId",
                    localUri = previewPath,
                    width = imgWidth,
                    height = imgHeight
                ),
                placements = emptyList(),
                targetBudget = 5_000_000L,
                updatedAt = System.currentTimeMillis()
            )

            roomSceneDao.insertScene(RoomSceneMapper.toEntity(newScene))
            Result.success(newScene)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePlacements(
        sceneId: String,
        placements: List<ProductPlacement>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            placementDao.deletePlacementsForScene(sceneId)
            val entities = placements.map { RoomSceneMapper.toEntity(sceneId, it) }
            placementDao.insertPlacements(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addPlacement(
        sceneId: String,
        placement: ProductPlacement
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            placementDao.insertPlacement(RoomSceneMapper.toEntity(sceneId, placement))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removePlacement(
        sceneId: String,
        placementId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            placementDao.deletePlacement(placementId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePlacementTransform(
        sceneId: String,
        placementId: String,
        transform: PlacementTransform
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val placements = placementDao.getPlacementsForSceneSync(sceneId)
            val target = placements.find { it.placementId == placementId }
            if (target != null) {
                val updated = target.copy(
                    translationX = transform.translationX,
                    translationY = transform.translationY,
                    scale = transform.scale,
                    rotationDegrees = transform.rotationDegrees,
                    zIndex = transform.zIndex
                )
                placementDao.insertPlacement(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRoomSceneMetadata(
        sceneId: String,
        title: String,
        type: RoomType,
        targetBudget: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val current = roomSceneDao.getSceneByIdSync(sceneId)
            if (current != null) {
                val updated = current.copy(
                    title = title,
                    roomType = type.name,
                    targetBudget = targetBudget,
                    updatedAt = System.currentTimeMillis()
                )
                roomSceneDao.updateScene(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePreferredStyle(
        sceneId: String,
        style: DesignStyle
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            roomSceneDao.updatePreferredStyle(sceneId, style.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRoomDimensions(
        sceneId: String,
        dimensionsCm: DimensionsCm?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            roomSceneDao.updateRoomDimensions(
                sceneId = sceneId,
                widthCm = dimensionsCm?.width,
                depthCm = dimensionsCm?.depth,
                heightCm = dimensionsCm?.height
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRoomScene(sceneId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            placementDao.deletePlacementsForScene(sceneId)
            roomSceneDao.deleteScene(sceneId)
            storageManager.deleteSceneAssets(sceneId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
