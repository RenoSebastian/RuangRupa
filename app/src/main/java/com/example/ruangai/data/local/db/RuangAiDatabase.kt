package com.example.ruangai.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ruangai.data.local.db.dao.*
import com.example.ruangai.data.local.db.entity.*

@Database(
    entities = [
        RoomSceneEntity::class,
        RoomPlacementEntity::class,
        RoomAnalysisEntity::class,
        SceneReferenceImageEntity::class,
        ShoppingListItemEntity::class,
        AffiliateClickEntity::class,
        ConsultationConversationEntity::class,
        ConsultationMessageEntity::class,
        DesignGenerationRequestEntity::class,
        DesignVariationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class RuangAiDatabase : RoomDatabase() {
    abstract fun roomSceneDao(): RoomSceneDao
    abstract fun roomPlacementDao(): RoomPlacementDao
    abstract fun roomAnalysisDao(): RoomAnalysisDao
    abstract fun sceneReferenceImageDao(): SceneReferenceImageDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun affiliateClickDao(): AffiliateClickDao
    abstract fun consultationDao(): ConsultationDao
    abstract fun designGenerationDao(): DesignGenerationDao

    companion object {
        @Volatile
        private var INSTANCE: RuangAiDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `shopping_list_items` (
                        `itemId` TEXT NOT NULL,
                        `productId` TEXT NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `isSelected` INTEGER NOT NULL,
                        `associatedSceneId` TEXT,
                        `associatedPlacementId` TEXT,
                        `addedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`itemId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `affiliate_click_logs` (
                        `clickAttributionId` TEXT NOT NULL,
                        `productId` TEXT NOT NULL,
                        `productTitle` TEXT NOT NULL,
                        `marketplace` TEXT NOT NULL,
                        `sceneId` TEXT NOT NULL,
                        `placementId` TEXT,
                        `subId1` TEXT NOT NULL,
                        `subId2` TEXT NOT NULL,
                        `subId3` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `isSimulated` INTEGER NOT NULL,
                        `clickedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`clickAttributionId`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add preferredStyle to room_scenes preserving all existing Milestone 1-5 room projects
                db.execSQL(
                    "ALTER TABLE `room_scenes` ADD COLUMN `preferredStyle` TEXT DEFAULT NULL"
                )

                // 2. Create consultation_conversations with foreign key CASCADE to room_scenes(id)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `consultation_conversations` (
                        `conversationId` TEXT NOT NULL,
                        `sceneId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `selectedStyleId` TEXT,
                        `customBudgetIdr` INTEGER,
                        `status` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`conversationId`),
                        FOREIGN KEY(`sceneId`) REFERENCES `room_scenes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_consultation_conversations_sceneId` ON `consultation_conversations` (`sceneId`)"
                )

                // 3. Create consultation_messages with foreign key CASCADE to consultation_conversations(conversationId)
                // and composite index on (conversationId, timestamp)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `consultation_messages` (
                        `messageId` TEXT NOT NULL,
                        `conversationId` TEXT NOT NULL,
                        `senderRole` TEXT NOT NULL,
                        `text` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `attachmentsJson` TEXT NOT NULL,
                        `recommendedProductsJson` TEXT NOT NULL,
                        `suggestedActionsJson` TEXT NOT NULL,
                        `evaluatedBudgetIdr` INTEGER,
                        `isSimulated` INTEGER NOT NULL,
                        `deliveryStatus` TEXT NOT NULL,
                        PRIMARY KEY(`messageId`),
                        FOREIGN KEY(`conversationId`) REFERENCES `consultation_conversations`(`conversationId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_consultation_messages_conversationId_timestamp` ON `consultation_messages` (`conversationId`, `timestamp`)"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create design_generation_requests with foreign key CASCADE to room_scenes(id)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `design_generation_requests` (
                        `resultId` TEXT NOT NULL,
                        `requestId` TEXT NOT NULL,
                        `idempotencyKey` TEXT NOT NULL,
                        `sceneId` TEXT NOT NULL,
                        `originalRoomImageUri` TEXT NOT NULL,
                        `roomType` TEXT NOT NULL,
                        `selectedStyle` TEXT NOT NULL,
                        `colorPaletteNamesJson` TEXT NOT NULL,
                        `desiredAtmosphere` TEXT NOT NULL,
                        `furniturePreferencesJson` TEXT NOT NULL,
                        `targetBudgetIdr` INTEGER NOT NULL,
                        `additionalInstructions` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `selectedVariationId` TEXT,
                        `creditCost` INTEGER NOT NULL,
                        `creditReserved` INTEGER NOT NULL,
                        `creditCaptured` INTEGER NOT NULL,
                        `creditRefunded` INTEGER NOT NULL,
                        `errorMessage` TEXT,
                        `isSimulated` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`resultId`),
                        FOREIGN KEY(`sceneId`) REFERENCES `room_scenes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_design_generation_requests_sceneId_createdAt` ON `design_generation_requests` (`sceneId`, `createdAt`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_design_generation_requests_idempotencyKey` ON `design_generation_requests` (`idempotencyKey`)"
                )

                // 2. Create design_variations with foreign key CASCADE to design_generation_requests(resultId)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `design_variations` (
                        `variationId` TEXT NOT NULL,
                        `resultId` TEXT NOT NULL,
                        `requestId` TEXT NOT NULL,
                        `sceneId` TEXT NOT NULL,
                        `variationIndex` INTEGER NOT NULL,
                        `variationTitle` TEXT NOT NULL,
                        `style` TEXT NOT NULL,
                        `colorPaletteNamesJson` TEXT NOT NULL,
                        `colorPaletteHexesJson` TEXT NOT NULL,
                        `atmosphere` TEXT NOT NULL,
                        `roomType` TEXT NOT NULL,
                        `previewImageUrl` TEXT NOT NULL,
                        `designSummary` TEXT NOT NULL,
                        `layoutAndLightingNotes` TEXT NOT NULL,
                        `estimatedFurnitureBudgetIdr` INTEGER NOT NULL,
                        `targetBudgetIdr` INTEGER NOT NULL,
                        `catalogMatchedProductsJson` TEXT NOT NULL,
                        `hotspotPinsJson` TEXT NOT NULL,
                        `isFavorite` INTEGER NOT NULL,
                        `isSimulated` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`variationId`),
                        FOREIGN KEY(`resultId`) REFERENCES `design_generation_requests`(`resultId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_design_variations_resultId_variationIndex` ON `design_variations` (`resultId`, `variationIndex`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_design_variations_sceneId_isFavorite` ON `design_variations` (`sceneId`, `isFavorite`)"
                )
            }
        }

        fun getInstance(context: Context): RuangAiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RuangAiDatabase::class.java,
                    "ruangai_database.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
