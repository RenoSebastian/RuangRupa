package com.example.ruangai.data.local.db.dao

import androidx.room.*
import com.example.ruangai.data.local.db.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomSceneDao {
    @Query("SELECT * FROM room_scenes ORDER BY updatedAt DESC")
    fun getAllScenes(): Flow<List<RoomSceneEntity>>

    @Query("SELECT * FROM room_scenes WHERE id = :id")
    fun getSceneById(id: String): Flow<RoomSceneEntity?>

    @Query("SELECT * FROM room_scenes WHERE id = :id")
    suspend fun getSceneByIdSync(id: String): RoomSceneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScene(scene: RoomSceneEntity)

    @Update
    suspend fun updateScene(scene: RoomSceneEntity)

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
