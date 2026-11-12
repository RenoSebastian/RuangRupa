package com.example.ruangai.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ruangai.data.local.db.dao.*
import com.example.ruangai.data.local.db.entity.*

@Database(
    entities = [
        RoomSceneEntity::class,
        RoomPlacementEntity::class,
        RoomAnalysisEntity::class,
        SceneReferenceImageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RuangAiDatabase : RoomDatabase() {
    abstract fun roomSceneDao(): RoomSceneDao
    abstract fun roomPlacementDao(): RoomPlacementDao
    abstract fun roomAnalysisDao(): RoomAnalysisDao
    abstract fun sceneReferenceImageDao(): SceneReferenceImageDao

    companion object {
        @Volatile
        private var INSTANCE: RuangAiDatabase? = null

        fun getInstance(context: Context): RuangAiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RuangAiDatabase::class.java,
                    "ruangai_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
