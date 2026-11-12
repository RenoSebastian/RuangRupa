package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RuangAI", appName)
  }

  @Test
  fun `test deep link parsing for products, try-on and wallet`() {
    // 1. Product Detail Deep Link
    val productUri = android.net.Uri.parse("ruangai://product_detail/prod_sofa_japandi_01")
    val productScreen = com.example.ruangai.ui.navigation.Screen.parseDeepLinkUri(productUri)
    org.junit.Assert.assertTrue(productScreen is com.example.ruangai.ui.navigation.Screen.ProductDetail)
    assertEquals("prod_sofa_japandi_01", (productScreen as com.example.ruangai.ui.navigation.Screen.ProductDetail).productId)

    // 2. Virtual Try-On Deep Link
    val tryOnUri = android.net.Uri.parse("ruangai://virtual_try_on/demo_scene_kost_01?productId=prod_table_01")
    val tryOnScreen = com.example.ruangai.ui.navigation.Screen.parseDeepLinkUri(tryOnUri)
    org.junit.Assert.assertTrue(tryOnScreen is com.example.ruangai.ui.navigation.Screen.VirtualTryOn)
    val castedTryOn = tryOnScreen as com.example.ruangai.ui.navigation.Screen.VirtualTryOn
    assertEquals("demo_scene_kost_01", castedTryOn.sceneId)
    assertEquals("prod_table_01", castedTryOn.preselectedProductId)

    // 3. Wallet Deep Link
    val walletUri = android.net.Uri.parse("ruangai://credit_wallet")
    val walletScreen = com.example.ruangai.ui.navigation.Screen.parseDeepLinkUri(walletUri)
    org.junit.Assert.assertTrue(walletScreen is com.example.ruangai.ui.navigation.Screen.CreditWallet)
  }

  @Test
  fun `test room database persistence and project lifecycle`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(
        context,
        com.example.ruangai.data.local.db.RuangAiDatabase::class.java
    ).allowMainThreadQueries().build()

    val sceneDao = db.roomSceneDao()
    val placementDao = db.roomPlacementDao()
    val analysisDao = db.roomAnalysisDao()

    // 1. Insert Scene
    val testEntity = com.example.ruangai.data.local.db.entity.RoomSceneEntity(
        id = "scene_robo_01",
        title = "Kamar Tidur Utama Test",
        roomType = "BEDROOM",
        originalImagePath = "/data/user/0/com.example/files/room_images/scene_robo_01_orig.jpg",
        previewImagePath = "/data/user/0/com.example/files/room_images/scene_robo_01_prev.jpg",
        width = 1280,
        height = 960,
        targetBudget = 7500000L
    )
    sceneDao.insertScene(testEntity)

    val queried = sceneDao.getSceneByIdSync("scene_robo_01")
    org.junit.Assert.assertNotNull(queried)
    assertEquals("Kamar Tidur Utama Test", queried?.title)
    assertEquals(7500000L, queried?.targetBudget)

    // 2. Add Placement
    val placement = com.example.ruangai.data.local.db.entity.RoomPlacementEntity(
        placementId = "p_robo_1",
        sceneId = "scene_robo_01",
        productId = "prod_sofa_01",
        productTitle = "Sofa 2-Seater Japandi",
        productPriceIdr = 1450000L,
        categoryName = "Sofa",
        cutoutUri = "http://example.com/cutout.png",
        dimWidthCm = 140,
        dimDepthCm = 75,
        dimHeightCm = 80,
        translationX = 10f,
        translationY = 20f,
        scale = 1.0f,
        rotationDegrees = 0f,
        zIndex = 1
    )
    placementDao.insertPlacement(placement)
    val placements = placementDao.getPlacementsForSceneSync("scene_robo_01")
    assertEquals(1, placements.size)
    assertEquals("Sofa 2-Seater Japandi", placements.first().productTitle)

    // 3. Update Scene Metadata
    val updatedScene = queried!!.copy(title = "Kamar Tidur Modern Minimalis", targetBudget = 9000000L)
    sceneDao.updateScene(updatedScene)
    val afterUpdate = sceneDao.getSceneByIdSync("scene_robo_01")
    assertEquals("Kamar Tidur Modern Minimalis", afterUpdate?.title)
    assertEquals(9000000L, afterUpdate?.targetBudget)

    // 4. Test Analysis Cache
    val analysisEntity = com.example.ruangai.data.local.db.entity.RoomAnalysisEntity(
        sceneId = "scene_robo_01",
        detectedRoomType = "BEDROOM",
        dominantColorsJson = "Krem,Putih,Kayu",
        wallFloorObservations = "Dinding bersih",
        doorsWindowsObservations = "1 jendela",
        existingFurnitureJson = "KASUR|Kasur Queen|Tengah|Tetap",
        lightingConditions = "Terang alami",
        approximateLayoutObservations = "Persegi ~3.5x4m",
        suggestedStylesJson = "JAPANDI,SCANDINAVIAN",
        isSimulation = true
    )
    analysisDao.insertAnalysis(analysisEntity)
    val cachedAnalysis = analysisDao.getAnalysisForScene("scene_robo_01")
    org.junit.Assert.assertNotNull(cachedAnalysis)
    assertEquals("BEDROOM", cachedAnalysis?.detectedRoomType)
    org.junit.Assert.assertTrue(cachedAnalysis?.isSimulation == true)

    // 5. Delete Scene & Cascade cleanup
    placementDao.deletePlacementsForScene("scene_robo_01")
    sceneDao.deleteScene("scene_robo_01")
    analysisDao.deleteAnalysisForScene("scene_robo_01")

    val deletedScene = sceneDao.getSceneByIdSync("scene_robo_01")
    org.junit.Assert.assertNull(deletedScene)
    assertEquals(0, placementDao.getPlacementsForSceneSync("scene_robo_01").size)
    org.junit.Assert.assertNull(analysisDao.getAnalysisForScene("scene_robo_01"))

    db.close()
  }

  @Test
  fun `test scene compositor exports composite without modifying original photo`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testDir = java.io.File(context.cacheDir, "test_images").apply { mkdirs() }
    val originalFile = java.io.File(testDir, "test_original_room.jpg")

    // Create a real JPEG bitmap for original room
    val origBmp = android.graphics.Bitmap.createBitmap(400, 300, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(origBmp)
    canvas.drawColor(android.graphics.Color.LTGRAY)
    java.io.FileOutputStream(originalFile).use { out ->
      origBmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
    }
    val originalSize = originalFile.length()
    org.junit.Assert.assertTrue(originalSize > 0)

    val compositor = com.example.ruangai.data.local.graphics.SceneCompositor(context)
    val testScene = com.example.ruangai.domain.model.RoomScene(
        id = "scene_compositor_test",
        title = "Ruang Tamu Test Compositor",
        roomType = com.example.ruangai.domain.model.RoomType.LIVING_ROOM,
        originalImage = com.example.ruangai.domain.model.OriginalRoomImage("img_test", originalFile.absolutePath, 400, 300),
        placements = listOf(
            com.example.ruangai.domain.model.ProductPlacement(
                placementId = "p_comp_1",
                productId = "prod_table_01",
                productTitle = "Meja Kopi Bundar Minimalis",
                productPriceIdr = 420000L,
                categoryName = "Meja",
                referenceImage = com.example.ruangai.domain.model.ProductReferenceImage(
                    "ref_table",
                    "prod_table_01",
                    "http://example.com/cutout.png",
                    com.example.ruangai.domain.model.DimensionsCm(60, 60, 45)
                ),
                transform = com.example.ruangai.domain.model.PlacementTransform(
                    translationX = 20f,
                    translationY = 30f,
                    scale = 1.1f,
                    rotationDegrees = 5f,
                    zIndex = 1
                )
            )
        )
    )

    val exportResult = compositor.compositeScene(testScene)

    // 1. Verify composite and mask output files exist
    org.junit.Assert.assertTrue(exportResult.compositeFile.exists())
    org.junit.Assert.assertTrue(exportResult.maskFile.exists())
    org.junit.Assert.assertEquals(400, exportResult.width)
    org.junit.Assert.assertEquals(300, exportResult.height)
    org.junit.Assert.assertTrue(exportResult.fileSizeBytes > 0)

    // 2. Strict Scene Preservation: Original image is 100% immutable and unchanged on disk
    org.junit.Assert.assertTrue(originalFile.exists())
    org.junit.Assert.assertEquals(originalSize, originalFile.length())
  }

  @Test
  fun `test try on undo redo and layer ordering lifecycle in viewmodel`() = kotlinx.coroutines.runBlocking {
    val viewModel = com.example.ruangai.ui.viewmodel.RuangAiViewModel()
    val product = com.example.ruangai.data.repository.mock.MockData.products.first()

    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertFalse(viewModel.canUndo.value)
    org.junit.Assert.assertFalse(viewModel.canRedo.value)

    viewModel.addProductToScene("demo_scene_kost_01", product)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    org.junit.Assert.assertTrue(viewModel.canUndo.value)
    org.junit.Assert.assertFalse(viewModel.canRedo.value)

    viewModel.undo()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertTrue(viewModel.canRedo.value)

    viewModel.redo()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertTrue(viewModel.canUndo.value)

    val scene = viewModel.activeScene.value
    org.junit.Assert.assertNotNull(scene)
    val placement = scene!!.placements.first()
    val originalZ = placement.transform.zIndex

    viewModel.bringPlacementToFront(scene.id, placement.placementId)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    val afterFront = viewModel.activeScene.value?.placements?.find { it.placementId == placement.placementId }
    org.junit.Assert.assertNotNull(afterFront)
    org.junit.Assert.assertTrue(afterFront!!.transform.zIndex > originalZ)
  }

  @Test
  fun `test shopping list and affiliate click persistence in room database`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(
        context,
        com.example.ruangai.data.local.db.RuangAiDatabase::class.java
    ).allowMainThreadQueries().build()

    val shoppingRepo = com.example.ruangai.data.repository.local.PersistentShoppingListRepository(
        shoppingListDao = db.shoppingListDao(),
        seedInitialItemsIfNeeded = false
    )
    val affiliateRepo = com.example.ruangai.data.repository.local.PersistentAffiliateLinkRepository(
        affiliateClickDao = db.affiliateClickDao()
    )

    val sofa = com.example.ruangai.data.repository.mock.MockData.products.first { it.id == "prod_sofa_01" }
    val table = com.example.ruangai.data.repository.mock.MockData.products.first { it.id == "prod_table_01" }

    // 1. Add product twice to same room project -> deduplicates by incrementing quantity
    shoppingRepo.addOrIncrementProduct(sofa, sceneId = "demo_scene_kost_01", placementId = "place_kost_1", quantityToAdd = 1)
    shoppingRepo.addOrIncrementProduct(sofa, sceneId = "demo_scene_kost_01", placementId = "place_kost_1", quantityToAdd = 2)
    shoppingRepo.addOrIncrementProduct(table, sceneId = "demo_scene_kost_01", placementId = null, quantityToAdd = 1)

    val items = db.shoppingListDao().getAllItemsSync()
    assertEquals(2, items.size)
    val sofaRow = items.first { it.productId == "prod_sofa_01" }
    assertEquals(3, sofaRow.quantity)

    // 2. Verify budget summary calculation against room budget (4,000,000 IDR)
    val expectedSubtotal = (sofa.priceIdr * 3) + (table.priceIdr * 1)
    val summary = com.example.ruangai.domain.model.BudgetSummary(
        sceneId = "demo_scene_kost_01",
        sceneTitle = "Kamar Kost",
        targetBudgetIdr = 4_000_000L,
        selectedSubtotalIdr = expectedSubtotal,
        totalItemCount = 4
    )
    assertEquals(expectedSubtotal, summary.selectedSubtotalIdr)
    org.junit.Assert.assertTrue(summary.isOverBudget)
    org.junit.Assert.assertTrue(summary.overrunIdr > 0L)

    // 3. Verify Affiliate Click logging and persistence in Room
    val redirectInfo = affiliateRepo.getAffiliateLink(
        productId = sofa.id,
        sceneId = "demo_scene_kost_01",
        placementId = "place_kost_1"
    ).getOrThrow()
    assertEquals(com.example.ruangai.domain.model.AffiliateLinkStatus.SIMULATED, redirectInfo.status)
    assertEquals("demo_scene_kost_01", redirectInfo.trackingMetadata.subId1)
    org.junit.Assert.assertTrue(redirectInfo.disclosureLabel.contains("komisi"))

    affiliateRepo.recordStructuredAffiliateClick(redirectInfo, sofa.title)
    val clickLogs = db.affiliateClickDao().getAllClickLogsSync()
    assertEquals(1, clickLogs.size)
    assertEquals("prod_sofa_01", clickLogs.first().productId)
    assertEquals("place_kost_1", clickLogs.first().placementId)

    db.close()
  }

  @Test
  fun `test design to buy product replacement preserves other scene placements and transparent cutout has alpha zero background`() = kotlinx.coroutines.runBlocking<Unit> {
    // 1. Verify FurnitureCutoutBitmapFactory produces true transparent background (alpha == 0 at corners, ARGB_8888)
    val sofa = com.example.ruangai.data.repository.mock.MockData.products.first { it.id == "prod_sofa_01" }
    val cutoutBmp = com.example.ruangai.data.local.graphics.FurnitureCutoutBitmapFactory.createTransparentCutoutBitmap(
        productId = sofa.id,
        categoryName = sofa.category.displayName,
        colorHex = sofa.colorHex,
        dimensions = sofa.dimensions,
        targetWidthPx = 320
    )
    org.junit.Assert.assertTrue(cutoutBmp.width > 0 && cutoutBmp.height > 0)
    assertEquals(android.graphics.Bitmap.Config.ARGB_8888, cutoutBmp.config)
    // Corner pixel (0, 0) must be 100% transparent (alpha == 0)
    val cornerPixel = cutoutBmp.getPixel(0, 0)
    assertEquals(0, android.graphics.Color.alpha(cornerPixel))

    // 2. Verify Design-to-Buy product replacement preserves all other placements in the scene
    val viewModel = com.example.ruangai.ui.viewmodel.RuangAiViewModel()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val initialScene = viewModel.roomScenes.value.first { it.id == "demo_scene_kost_01" }
    val initialCount = initialScene.placements.size
    org.junit.Assert.assertTrue(initialCount >= 2)

    val targetPlacement = initialScene.placements.first()
    val untouchedPlacement = initialScene.placements.last()
    val replacementSofa = com.example.ruangai.data.repository.mock.MockData.products.first { it.id == "prod_sofa_02" }

    viewModel.replacePlacementProduct(
        sceneId = "demo_scene_kost_01",
        placementId = targetPlacement.placementId,
        newProduct = replacementSofa
    )
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val updatedScene = viewModel.roomScenes.value.first { it.id == "demo_scene_kost_01" }
    assertEquals(initialCount, updatedScene.placements.size)

    val replaced = updatedScene.placements.first { it.placementId == targetPlacement.placementId }
    assertEquals("prod_sofa_02", replaced.productId)
    assertEquals(replacementSofa.title, replaced.productTitle)
    // Original transform coordinates are preserved when replacing product
    assertEquals(targetPlacement.transform.translationX, replaced.transform.translationX, 0.01f)
    assertEquals(targetPlacement.transform.translationY, replaced.transform.translationY, 0.01f)

    // Untouched placement remains intact
    val stillIntact = updatedScene.placements.first { it.placementId == untouchedPlacement.placementId }
    assertEquals(untouchedPlacement.productId, stillIntact.productId)
  }

  @Test
  fun `test room database migration 2 to 3 preserves all six milestone 5 tables and enforces v3 schema`() = kotlinx.coroutines.runBlocking<Unit> {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbFile = context.getDatabasePath("test_migration_v2_to_v3.db")
    if (dbFile.exists()) dbFile.delete()
    dbFile.parentFile?.mkdirs()

    // 1. Create a real SQLite v2 database fixture with all 6 Milestone 5 tables (exact v2 column definitions) and sample records
    val rawV2 = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
    rawV2.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
    rawV2.execSQL("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, 'legacy_v2_hash')")

    rawV2.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `room_scenes` (
            `id` TEXT NOT NULL,
            `title` TEXT NOT NULL,
            `roomType` TEXT NOT NULL,
            `originalImagePath` TEXT NOT NULL,
            `previewImagePath` TEXT NOT NULL,
            `width` INTEGER NOT NULL,
            `height` INTEGER NOT NULL,
            `targetBudget` INTEGER NOT NULL,
            `userDimWidthCm` INTEGER,
            `userDimDepthCm` INTEGER,
            `userDimHeightCm` INTEGER,
            `activeMakeoverId` TEXT,
            `createdAt` INTEGER NOT NULL,
            `updatedAt` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent()
    )
    rawV2.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `room_placements` (
            `placementId` TEXT NOT NULL,
            `sceneId` TEXT NOT NULL,
            `productId` TEXT NOT NULL,
            `productTitle` TEXT NOT NULL,
            `productPriceIdr` INTEGER NOT NULL,
            `categoryName` TEXT NOT NULL,
            `cutoutUri` TEXT NOT NULL,
            `dimWidthCm` INTEGER NOT NULL,
            `dimDepthCm` INTEGER NOT NULL,
            `dimHeightCm` INTEGER NOT NULL,
            `translationX` REAL NOT NULL,
            `translationY` REAL NOT NULL,
            `scale` REAL NOT NULL,
            `rotationDegrees` REAL NOT NULL,
            `zIndex` INTEGER NOT NULL,
            PRIMARY KEY(`placementId`)
        )
        """.trimIndent()
    )
    rawV2.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `room_analysis_cache` (
            `sceneId` TEXT NOT NULL,
            `detectedRoomType` TEXT NOT NULL,
            `dominantColorsJson` TEXT NOT NULL,
            `wallFloorObservations` TEXT NOT NULL,
            `doorsWindowsObservations` TEXT NOT NULL,
            `existingFurnitureJson` TEXT NOT NULL,
            `lightingConditions` TEXT NOT NULL,
            `approximateLayoutObservations` TEXT NOT NULL,
            `suggestedStylesJson` TEXT NOT NULL,
            `isSimulation` INTEGER NOT NULL,
            `analyzedAt` INTEGER NOT NULL,
            PRIMARY KEY(`sceneId`)
        )
        """.trimIndent()
    )
    rawV2.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `scene_reference_images` (
            `id` TEXT NOT NULL,
            `sceneId` TEXT NOT NULL,
            `imagePath` TEXT NOT NULL,
            `label` TEXT NOT NULL,
            `addedAt` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent()
    )
    rawV2.execSQL(
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
    rawV2.execSQL(
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

    // Populate all 6 v2 tables with realistic Milestone 5 user data
    rawV2.execSQL(
        "INSERT INTO room_scenes VALUES ('v2_scene_01', 'Kamar Kost v2', 'KOST_STUDIO', '/path/orig.jpg', '/path/prev.jpg', 1280, 960, 3500000, 300, 400, 280, NULL, 1700000000000, 1700000000000)"
    )
    rawV2.execSQL(
        "INSERT INTO room_placements VALUES ('v2_place_01', 'v2_scene_01', 'prod_sofa_01', 'Sofa 2-Seater Fabric Japandi Beige', 1450000, 'Sofa', 'http://cutout', 140, 75, 80, 15.0, 25.0, 1.0, 0.0, 1)"
    )
    rawV2.execSQL(
        "INSERT INTO room_analysis_cache VALUES ('v2_scene_01', 'KOST_STUDIO', 'Krem,Putih', 'Dinding bersih', '1 jendela', 'KASUR|Single|Kiri|Ganti', 'Terang', '3x4m', 'JAPANDI', 1, 1700000000000)"
    )
    rawV2.execSQL(
        "INSERT INTO scene_reference_images VALUES ('v2_ref_01', 'v2_scene_01', '/path/ref.jpg', 'Inspirasi Japandi', 1700000000000)"
    )
    rawV2.execSQL(
        "INSERT INTO shopping_list_items VALUES ('v2_cart_01', 'prod_sofa_01', 2, 1, 'v2_scene_01', 'v2_place_01', 1700000000000)"
    )
    rawV2.execSQL(
        "INSERT INTO affiliate_click_logs VALUES ('v2_click_01', 'prod_sofa_01', 'Sofa 2-Seater Fabric Japandi Beige', 'SHOPEE', 'v2_scene_01', 'v2_place_01', 'v2_scene_01', 'user_demo', 'v2_place_01', 'SIMULATED', 1, 1700000000000)"
    )
    rawV2.version = 2
    rawV2.close()

    // 2. Open with Room v4 and explicit migrations (NO fallbackToDestructiveMigration)
    val migratedDb = androidx.room.Room.databaseBuilder(
        context,
        com.example.ruangai.data.local.db.RuangAiDatabase::class.java,
        "test_migration_v2_to_v3.db"
    )
        .setJournalMode(androidx.room.RoomDatabase.JournalMode.TRUNCATE)
        .addMigrations(
            com.example.ruangai.data.local.db.RuangAiDatabase.MIGRATION_1_2,
            com.example.ruangai.data.local.db.RuangAiDatabase.MIGRATION_2_3,
            com.example.ruangai.data.local.db.RuangAiDatabase.MIGRATION_3_4
        )
        .allowMainThreadQueries()
        .build()

    // 3. Verify all 6 Milestone 5 tables preserved 100% of their rows
    val migratedScene = migratedDb.roomSceneDao().getSceneByIdSync("v2_scene_01")
    org.junit.Assert.assertNotNull(migratedScene)
    assertEquals("Kamar Kost v2", migratedScene?.title)
    assertEquals(3500000L, migratedScene?.targetBudget)
    org.junit.Assert.assertNull(migratedScene?.preferredStyle)

    val migratedPlacements = migratedDb.roomPlacementDao().getPlacementsForSceneSync("v2_scene_01")
    assertEquals(1, migratedPlacements.size)
    assertEquals("v2_place_01", migratedPlacements.first().placementId)

    val migratedAnalysis = migratedDb.roomAnalysisDao().getAnalysisForScene("v2_scene_01")
    org.junit.Assert.assertNotNull(migratedAnalysis)
    assertEquals("KOST_STUDIO", migratedAnalysis?.detectedRoomType)

    val migratedRefs = migratedDb.sceneReferenceImageDao().getReferenceImagesForSceneSync("v2_scene_01")
    assertEquals(1, migratedRefs.size)
    assertEquals("Inspirasi Japandi", migratedRefs.first().label)

    val migratedCart = migratedDb.shoppingListDao().getAllItemsSync()
    assertEquals(1, migratedCart.size)
    assertEquals(2, migratedCart.first().quantity)

    val migratedClicks = migratedDb.affiliateClickDao().getAllClickLogsSync()
    assertEquals(1, migratedClicks.size)
    assertEquals("v2_click_01", migratedClicks.first().clickAttributionId)

    // 4. Verify new v3 column & tables work properly after migration
    migratedDb.roomSceneDao().updatePreferredStyle("v2_scene_01", "JAPANDI", System.currentTimeMillis())
    assertEquals("JAPANDI", migratedDb.roomSceneDao().getSceneByIdSync("v2_scene_01")?.preferredStyle)

    migratedDb.consultationDao().upsertConversation(
        com.example.ruangai.data.local.db.entity.ConsultationConversationEntity(
            conversationId = "conv_after_mig",
            sceneId = "v2_scene_01",
            title = "Konsultasi Kamar Kost v2",
            selectedStyleId = "JAPANDI",
            customBudgetIdr = 3500000L,
            status = "ACTIVE"
        )
    )
    val convAfterMig = migratedDb.consultationDao().getConversationForSceneSync("v2_scene_01")
    org.junit.Assert.assertNotNull(convAfterMig)
    assertEquals("conv_after_mig", convAfterMig?.conversationId)

    migratedDb.close()
    dbFile.delete()
  }

  @Test
  fun `test conversation and message persistence and restoration across repository restarts`() = kotlinx.coroutines.runBlocking<Unit> {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbName = "test_chat_persistence.db"
    val dbFile = context.getDatabasePath(dbName)
    if (dbFile.exists()) dbFile.delete()
    dbFile.parentFile?.mkdirs()

    val storageManager = com.example.ruangai.data.local.storage.LocalImageStorageManager(context)
    val kostScene = com.example.ruangai.data.repository.mock.MockData.preloadedScenes.first { it.id == "demo_scene_kost_01" }

    // Session 1: Create DB, seed scene, send contextual chat message
    val db1 = androidx.room.Room.databaseBuilder(
        context,
        com.example.ruangai.data.local.db.RuangAiDatabase::class.java,
        dbName
    ).allowMainThreadQueries().build()

    val chatRepo1 = com.example.ruangai.data.repository.local.PersistentAIChatRepository(
        consultationDao = db1.consultationDao(),
        roomSceneDao = db1.roomSceneDao(),
        storageManager = storageManager
    )

    val conv1 = chatRepo1.getOrCreateConversation(kostScene)
    val consultContext = com.example.ruangai.domain.model.ConsultationContext(
        scene = kostScene,
        conversation = conv1,
        analysisResult = null,
        shoppingListItems = emptyList(),
        availableCatalog = com.example.ruangai.data.repository.mock.MockData.products
    )

    val replyResult = chatRepo1.sendContextualMessage(
        context = consultContext,
        userText = "Rekomendasikan meja kerja dan lampu untuk kamar kost gaya Japandi dengan budget Rp 1,5 juta",
        attachments = emptyList()
    )
    org.junit.Assert.assertTrue(replyResult.isSuccess)
    val session1Messages = db1.consultationDao().getMessagesForConversationSync("conv_demo_scene_kost_01")
    // 1 initial greeting + 1 user message + 1 AI response = 3 messages
    assertEquals(3, session1Messages.size)
    db1.close()

    // Session 2: Re-open DB from disk and verify full conversation & recommendation restoration
    val db2 = androidx.room.Room.databaseBuilder(
        context,
        com.example.ruangai.data.local.db.RuangAiDatabase::class.java,
        dbName
    ).allowMainThreadQueries().build()

    val restoredConv = db2.consultationDao().getConversationForSceneSync("demo_scene_kost_01")
    org.junit.Assert.assertNotNull(restoredConv)
    assertEquals(1_500_000L, restoredConv?.customBudgetIdr)

    val restoredEntities = db2.consultationDao().getMessagesForConversationSync("conv_demo_scene_kost_01")
    assertEquals(3, restoredEntities.size)

    val restoredAiMessage = com.example.ruangai.data.local.db.mapper.ConsultationMapper.toDomain(restoredEntities.last())
    org.junit.Assert.assertFalse(restoredAiMessage.isFromUser)
    org.junit.Assert.assertTrue(restoredAiMessage.recommendedProducts.isNotEmpty())
    org.junit.Assert.assertTrue(restoredAiMessage.isSimulated)

    db2.close()
    dbFile.delete()
  }

  @Test
  fun `test foreign key on delete cascade removes conversations and messages when room scene deleted`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(
        context,
        com.example.ruangai.data.local.db.RuangAiDatabase::class.java
    ).allowMainThreadQueries().build()

    val sceneDao = db.roomSceneDao()
    val consultationDao = db.consultationDao()

    sceneDao.insertScene(
        com.example.ruangai.data.local.db.entity.RoomSceneEntity(
            id = "scene_cascade_01",
            title = "Ruang Kerja Cascade",
            roomType = "WORKSPACE",
            originalImagePath = "/path/orig.jpg",
            previewImagePath = "/path/prev.jpg",
            width = 300,
            height = 300,
            targetBudget = 4000000L,
            preferredStyle = "MINIMALIST"
        )
    )

    consultationDao.upsertConversation(
        com.example.ruangai.data.local.db.entity.ConsultationConversationEntity(
            conversationId = "conv_cascade_01",
            sceneId = "scene_cascade_01",
            title = "Diskusi Ruang Kerja",
            selectedStyleId = "MINIMALIST",
            customBudgetIdr = 4000000L,
            status = "ACTIVE"
        )
    )

    consultationDao.insertMessage(
        com.example.ruangai.data.local.db.entity.ConsultationMessageEntity(
            messageId = "msg_cascade_01",
            conversationId = "conv_cascade_01",
            senderRole = "USER",
            text = "Tolong sarankan meja kerja minimalis",
            attachmentsJson = "[]",
            recommendedProductsJson = "[]",
            suggestedActionsJson = "[]",
            evaluatedBudgetIdr = 4000000L,
            isSimulated = true,
            deliveryStatus = "DELIVERED",
            timestamp = 1700000000000L
        )
    )

    org.junit.Assert.assertNotNull(consultationDao.getConversationForSceneSync("scene_cascade_01"))
    assertEquals(1, consultationDao.getMessagesForConversationSync("conv_cascade_01").size)

    // Delete parent room scene -> FOREIGN KEY ON DELETE CASCADE removes conversation and messages automatically
    sceneDao.deleteScene("scene_cascade_01")

    org.junit.Assert.assertNull(consultationDao.getConversationForSceneSync("scene_cascade_01"))
    assertEquals(0, consultationDao.getMessagesForConversationSync("conv_cascade_01").size)

    db.close()
  }

  @Test
  fun `test chat photo attachment storage metadata persistence and missing file handling`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storageManager = com.example.ruangai.data.local.storage.LocalImageStorageManager(context)

    // Create a valid source image file to attach
    val sourceFile = java.io.File(context.cacheDir, "sample_room_upload.jpg")
    val bmp = android.graphics.Bitmap.createBitmap(240, 180, android.graphics.Bitmap.Config.ARGB_8888)
    android.graphics.Canvas(bmp).drawColor(android.graphics.Color.CYAN)
    java.io.FileOutputStream(sourceFile).use { out ->
      bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
    }

    val savedResult = storageManager.saveChatAttachmentImage(
        conversationId = "conv_att_test",
        sourceUriOrPath = sourceFile.absolutePath
    )
    val savedFile = java.io.File(savedResult.localFilePath)
    org.junit.Assert.assertTrue(savedResult.fileExists)
    org.junit.Assert.assertTrue(savedFile.exists())
    assertEquals(240, savedResult.width)
    assertEquals(180, savedResult.height)

    val attachment = com.example.ruangai.domain.model.ChatAttachment(
        id = "att_test_01",
        uriOrPath = savedFile.absolutePath,
        label = savedFile.name,
        mimeType = "image/jpeg",
        width = savedResult.width,
        height = savedResult.height,
        fileExists = true
    )

    // Serialize & deserialize while file exists
    val json = com.example.ruangai.data.local.db.mapper.ConsultationMapper.serializeAttachments(listOf(attachment))
    val restoredWithFile = com.example.ruangai.data.local.db.mapper.ConsultationMapper.deserializeAttachments(json)
    assertEquals(1, restoredWithFile.size)
    org.junit.Assert.assertTrue(restoredWithFile.first().fileExists)

    // Delete conversation attachments -> file is cleaned up from internal storage
    storageManager.deleteConversationAttachments("conv_att_test")
    org.junit.Assert.assertFalse(savedFile.exists())

    // Deserializing after file deletion gracefully sets fileExists = false without crashing
    val restoredAfterDelete = com.example.ruangai.data.local.db.mapper.ConsultationMapper.deserializeAttachments(json)
    assertEquals(1, restoredAfterDelete.size)
    org.junit.Assert.assertFalse(restoredAfterDelete.first().fileExists)
  }

  @Test
  fun `test context switching between room projects isolates conversation history and style preferences`() = kotlinx.coroutines.runBlocking {
    val viewModel = com.example.ruangai.ui.viewmodel.RuangAiViewModel()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    // 1. Default scene is demo_scene_kost_01
    viewModel.selectScene("demo_scene_kost_01")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertEquals("demo_scene_kost_01", viewModel.activeScene.value?.id)

    // Apply Modern Luxury style & send message in Kost project
    viewModel.applyStylePreferenceToScene(com.example.ruangai.domain.model.DesignStyle.MODERN_LUXURY, "demo_scene_kost_01")
    viewModel.sendMessage("Sarankan lampu meja untuk kamar kost saya")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val kostMessages = viewModel.chatMessages.value
    org.junit.Assert.assertTrue(kostMessages.any { it.text.contains("lampu meja", ignoreCase = true) })
    assertEquals(
        com.example.ruangai.domain.model.DesignStyle.MODERN_LUXURY,
        viewModel.activeScene.value?.preferredStyle
    )

    // 2. Switch to Living Room project (demo_scene_living_02)
    viewModel.selectScene("demo_scene_living_02")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertEquals("demo_scene_living_02", viewModel.activeScene.value?.id)

    val livingMessages = viewModel.chatMessages.value
    // Living room conversation does NOT contain the Kost message
    org.junit.Assert.assertFalse(livingMessages.any { it.text.contains("lampu meja", ignoreCase = true) })
    assertEquals(
        com.example.ruangai.domain.model.DesignStyle.SCANDINAVIAN,
        viewModel.activeScene.value?.preferredStyle
    )

    // 3. Switch back to Kost project -> Kost conversation and preferred style are intact
    viewModel.selectScene("demo_scene_kost_01")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertEquals(
        com.example.ruangai.domain.model.DesignStyle.MODERN_LUXURY,
        viewModel.activeScene.value?.preferredStyle
    )
    org.junit.Assert.assertTrue(
        viewModel.chatMessages.value.any { it.text.contains("lampu meja", ignoreCase = true) }
    )
  }

  @Test
  fun `test style preference persistence dimensions update clear conversation and chat product actions`() = kotlinx.coroutines.runBlocking {
    val viewModel = com.example.ruangai.ui.viewmodel.RuangAiViewModel()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    // 1. Update dimensions & budget for active scene
    viewModel.updateSceneDimensionsAndBudget(
        sceneId = "demo_scene_kost_01",
        dimensionsCm = com.example.ruangai.domain.model.DimensionsCm(320, 420, 290),
        targetBudgetIdr = 4_500_000L
    )
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    val updatedDims = viewModel.activeScene.value?.userDimensionsCm
    org.junit.Assert.assertNotNull(updatedDims)
    assertEquals(320, updatedDims?.width)
    assertEquals(420, updatedDims?.depth)
    assertEquals(290, updatedDims?.height)
    assertEquals(4_500_000L, viewModel.activeScene.value?.targetBudget)

    // 2. Send a consultation prompt and verify recommended products can be added to cart or compared
    viewModel.sendMessage("Rekomendasikan sofa minimalis untuk ruangan ini")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    val lastAiMsg = viewModel.chatMessages.value.last { !it.isFromUser }
    org.junit.Assert.assertTrue(lastAiMsg.recommendedProducts.isNotEmpty())

    val recProductId = lastAiMsg.recommendedProducts.first().productId
    val catalogProduct = viewModel.allCatalogProducts.first { it.id == recProductId }

    // Action 1: Compare product
    viewModel.toggleProductComparison(catalogProduct.id)
    org.junit.Assert.assertTrue(viewModel.comparedProductIds.value.contains(catalogProduct.id))

    // Action 2: Add to shopping list
    val cartCountBefore = viewModel.cartItems.value.sumOf { it.quantity }
    viewModel.addToCart(catalogProduct, "demo_scene_kost_01")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    val cartCountAfter = viewModel.cartItems.value.sumOf { it.quantity }
    assertEquals(cartCountBefore + 1, cartCountAfter)

    // Action 3: Navigate to Consultation with sceneId
    viewModel.navigateTo(com.example.ruangai.ui.navigation.Screen.Consultation("demo_scene_kost_01"))
    org.junit.Assert.assertTrue(viewModel.currentScreen.value is com.example.ruangai.ui.navigation.Screen.Consultation)

    // Action 4: Clear conversation resets to initial greeting message
    viewModel.clearCurrentConversation("demo_scene_kost_01")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertEquals(1, viewModel.chatMessages.value.size)
    org.junit.Assert.assertFalse(viewModel.chatMessages.value.first().isFromUser)
  }

  // =========================================================================
  // MILESTONE 7: FULL ROOM MAKEOVER & PERSISTENT DESIGN HISTORY ROBOLECTRIC TESTS
  // =========================================================================

  @Test
  fun `test room migration v3 to v4 preserves all 8 milestone 1 to 6 tables`() = kotlinx.coroutines.runBlocking<Unit> {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbFile = context.getDatabasePath("test_migration_v3_to_v4.db")
    if (dbFile.exists()) dbFile.delete()
    dbFile.parentFile?.mkdirs()

    // 1. Create a genuine v3 SQLite database with all 8 Milestone 1-6 tables populated
    val rawV3 = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
    rawV3.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `room_scenes` (
            `id` TEXT NOT NULL,
            `title` TEXT NOT NULL,
            `roomType` TEXT NOT NULL,
            `originalImagePath` TEXT NOT NULL,
            `previewImagePath` TEXT NOT NULL,
            `width` INTEGER NOT NULL,
            `height` INTEGER NOT NULL,
            `targetBudget` INTEGER NOT NULL,
            `userDimWidthCm` INTEGER,
            `userDimDepthCm` INTEGER,
            `userDimHeightCm` INTEGER,
            `activeMakeoverId` TEXT,
            `preferredStyle` TEXT DEFAULT NULL,
            `createdAt` INTEGER NOT NULL,
            `updatedAt` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent()
    )
    rawV3.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `room_placements` (
            `placementId` TEXT NOT NULL,
            `sceneId` TEXT NOT NULL,
            `productId` TEXT NOT NULL,
            `productTitle` TEXT NOT NULL,
            `productPriceIdr` INTEGER NOT NULL,
            `categoryName` TEXT NOT NULL,
            `cutoutUri` TEXT NOT NULL,
            `dimWidthCm` INTEGER NOT NULL,
            `dimDepthCm` INTEGER NOT NULL,
            `dimHeightCm` INTEGER NOT NULL,
            `translationX` REAL NOT NULL,
            `translationY` REAL NOT NULL,
            `scale` REAL NOT NULL,
            `rotationDegrees` REAL NOT NULL,
            `zIndex` INTEGER NOT NULL,
            PRIMARY KEY(`placementId`)
        )
        """.trimIndent()
    )
    rawV3.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `room_analysis_cache` (
            `sceneId` TEXT NOT NULL,
            `detectedRoomType` TEXT NOT NULL,
            `dominantColorsJson` TEXT NOT NULL,
            `wallFloorObservations` TEXT NOT NULL,
            `doorsWindowsObservations` TEXT NOT NULL,
            `existingFurnitureJson` TEXT NOT NULL,
            `lightingConditions` TEXT NOT NULL,
            `approximateLayoutObservations` TEXT NOT NULL,
            `suggestedStylesJson` TEXT NOT NULL,
            `isSimulation` INTEGER NOT NULL,
            `analyzedAt` INTEGER NOT NULL,
            PRIMARY KEY(`sceneId`)
        )
        """.trimIndent()
    )
    rawV3.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `scene_reference_images` (
            `id` TEXT NOT NULL,
            `sceneId` TEXT NOT NULL,
            `imagePath` TEXT NOT NULL,
            `label` TEXT NOT NULL,
            `addedAt` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent()
    )
    rawV3.execSQL(
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
    rawV3.execSQL(
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
    rawV3.execSQL(
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
    rawV3.execSQL(
        "CREATE INDEX IF NOT EXISTS `index_consultation_conversations_sceneId` ON `consultation_conversations` (`sceneId`)"
    )
    rawV3.execSQL(
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
    rawV3.execSQL(
        "CREATE INDEX IF NOT EXISTS `index_consultation_messages_conversationId_timestamp` ON `consultation_messages` (`conversationId`, `timestamp`)"
    )

    // Populate all 8 v3 tables
    rawV3.execSQL(
        "INSERT INTO room_scenes VALUES ('v3_scene_01', 'Ruang Tamu v3', 'LIVING_ROOM', '/path/v3_orig.jpg', '/path/v3_prev.jpg', 1280, 960, 6000000, 400, 500, 300, NULL, 'JAPANDI', 1710000000000, 1710000000000)"
    )
    rawV3.execSQL(
        "INSERT INTO room_placements VALUES ('v3_place_01', 'v3_scene_01', 'prod_sofa_01', 'Sofa 2-Seater Fabric Japandi Beige', 1450000, 'Sofa', 'http://cutout', 140, 75, 80, 10.0, 20.0, 1.0, 0.0, 1)"
    )
    rawV3.execSQL(
        "INSERT INTO room_analysis_cache VALUES ('v3_scene_01', 'LIVING_ROOM', 'Putih,Kayu', 'Dinding bersih', '2 jendela', 'SOFA|2-Seater|Tengah|Pertahankan', 'Terang', '4x5m', 'JAPANDI', 1, 1710000000000)"
    )
    rawV3.execSQL(
        "INSERT INTO scene_reference_images VALUES ('v3_ref_01', 'v3_scene_01', '/path/ref_v3.jpg', 'Referensi v3', 1710000000000)"
    )
    rawV3.execSQL(
        "INSERT INTO shopping_list_items VALUES ('v3_cart_01', 'prod_sofa_01', 1, 1, 'v3_scene_01', 'v3_place_01', 1710000000000)"
    )
    rawV3.execSQL(
        "INSERT INTO affiliate_click_logs VALUES ('v3_click_01', 'prod_sofa_01', 'Sofa 2-Seater Fabric Japandi Beige', 'TOKOPEDIA', 'v3_scene_01', 'v3_place_01', 'v3_scene_01', 'usr_v3', 'v3_place_01', 'SIMULATED', 1, 1710000000000)"
    )
    rawV3.execSQL(
        "INSERT INTO consultation_conversations VALUES ('v3_conv_01', 'v3_scene_01', 'Diskusi Ruang Tamu v3', 'JAPANDI', 6000000, 'ACTIVE', 1710000000000, 1710000000000)"
    )
    rawV3.execSQL(
        "INSERT INTO consultation_messages VALUES ('v3_msg_01', 'v3_conv_01', 'ASSISTANT', 'Halo dari v3', 1710000000005, '[]', '[]', '[]', 6000000, 1, 'DELIVERED')"
    )
    rawV3.version = 3
    rawV3.close()

    // 2. Open with Room v4 and MIGRATION_3_4 (NO fallbackToDestructiveMigration)
    val migratedDb = androidx.room.Room.databaseBuilder(
        context,
        com.example.ruangai.data.local.db.RuangAiDatabase::class.java,
        "test_migration_v3_to_v4.db"
    )
        .setJournalMode(androidx.room.RoomDatabase.JournalMode.TRUNCATE)
        .addMigrations(
            com.example.ruangai.data.local.db.RuangAiDatabase.MIGRATION_1_2,
            com.example.ruangai.data.local.db.RuangAiDatabase.MIGRATION_2_3,
            com.example.ruangai.data.local.db.RuangAiDatabase.MIGRATION_3_4
        )
        .allowMainThreadQueries()
        .build()

    // 3. Verify all 8 Milestone 1-6 tables preserved 100% of their rows
    val sceneAfter = migratedDb.roomSceneDao().getSceneByIdSync("v3_scene_01")
    org.junit.Assert.assertNotNull(sceneAfter)
    assertEquals("Ruang Tamu v3", sceneAfter?.title)
    assertEquals("/path/v3_orig.jpg", sceneAfter?.originalImagePath)
    assertEquals("JAPANDI", sceneAfter?.preferredStyle)

    assertEquals(1, migratedDb.roomPlacementDao().getPlacementsForSceneSync("v3_scene_01").size)
    org.junit.Assert.assertNotNull(migratedDb.roomAnalysisDao().getAnalysisForScene("v3_scene_01"))
    assertEquals(1, migratedDb.sceneReferenceImageDao().getReferenceImagesForSceneSync("v3_scene_01").size)
    assertEquals(1, migratedDb.shoppingListDao().getAllItemsSync().size)
    assertEquals(1, migratedDb.affiliateClickDao().getAllClickLogsSync().size)
    assertEquals("v3_conv_01", migratedDb.consultationDao().getConversationForSceneSync("v3_scene_01")?.conversationId)
    assertEquals(1, migratedDb.consultationDao().getMessagesForConversationSync("v3_conv_01").size)

    // 4. Verify new v4 design_generation_requests & design_variations tables work on the migrated DB
    val persistentDesignRepo = com.example.ruangai.data.repository.local.PersistentAIInteriorDesignRepository(
        designGenerationDao = migratedDb.designGenerationDao(),
        roomSceneDao = migratedDb.roomSceneDao()
    )
    val genReq = com.example.ruangai.domain.model.DesignGenerationRequest(
        requestId = "req_v4_after_mig",
        idempotencyKey = "idem_v4_after_mig",
        sceneId = "v3_scene_01",
        originalRoomImageUri = "/path/v3_orig.jpg",
        roomType = com.example.ruangai.domain.model.RoomType.LIVING_ROOM,
        selectedStyle = com.example.ruangai.domain.model.DesignStyle.JAPANDI,
        colorPaletteNames = listOf("Warm Linen", "Natural Oak"),
        targetBudgetIdr = 6_000_000L
    )
    val genResult = persistentDesignRepo.generateRoomMakeover(genReq).getOrThrow()
    assertEquals(3, genResult.variations.size)

    // Verify originalImagePath on room_scenes was NOT overwritten
    assertEquals("/path/v3_orig.jpg", migratedDb.roomSceneDao().getSceneByIdSync("v3_scene_01")?.originalImagePath)

    migratedDb.close()
    dbFile.delete()
  }

  @Test
  fun `test persistent design history variations favorites and cascade delete`() = kotlinx.coroutines.runBlocking<Unit> {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbName = "test_design_history_persistence.db"
    val dbFile = context.getDatabasePath(dbName)
    if (dbFile.exists()) dbFile.delete()
    dbFile.parentFile?.mkdirs()

    val creditRepo = com.example.ruangai.data.repository.mock.MockCreditWalletRepository()

    // Session 1: Generate makeover, select variation 2, favorite variation 2
    val db1 = androidx.room.Room.databaseBuilder(
        context,
        com.example.ruangai.data.local.db.RuangAiDatabase::class.java,
        dbName
    ).allowMainThreadQueries().build()

    val designRepo1 = com.example.ruangai.data.repository.local.PersistentAIInteriorDesignRepository(
        designGenerationDao = db1.designGenerationDao(),
        roomSceneDao = db1.roomSceneDao(),
        creditWalletRepository = creditRepo
    )

    val req = com.example.ruangai.domain.model.DesignGenerationRequest(
        requestId = "req_persist_01",
        idempotencyKey = "idem_persist_01",
        sceneId = "demo_scene_kost_01",
        originalRoomImageUri = "file:///immutable_kost_photo.jpg",
        roomType = com.example.ruangai.domain.model.RoomType.KOST_STUDIO,
        selectedStyle = com.example.ruangai.domain.model.DesignStyle.MID_CENTURY,
        colorPaletteNames = listOf("Teak Wood", "Olive Green"),
        desiredAtmosphere = com.example.ruangai.domain.model.DesignAtmosphere.WARM_COZY,
        furniturePreferences = listOf(com.example.ruangai.domain.model.ProductCategory.CHAIR),
        targetBudgetIdr = 4_000_000L,
        additionalInstructions = "Sudut baca retro"
    )

    val createdResult = designRepo1.generateRoomMakeover(req).getOrThrow()
    assertEquals(3, createdResult.variations.size)

    val secondVariationId = createdResult.variations[1].variationId
    designRepo1.selectActiveVariation(createdResult.resultId, secondVariationId)
    designRepo1.toggleFavoriteVariation(secondVariationId, true)
    db1.close()

    // Session 2: Re-open DB from disk and verify history, variations, active variation, and favorite status
    val db2 = androidx.room.Room.databaseBuilder(
        context,
        com.example.ruangai.data.local.db.RuangAiDatabase::class.java,
        dbName
    ).allowMainThreadQueries().build()

    val designRepo2 = com.example.ruangai.data.repository.local.PersistentAIInteriorDesignRepository(
        designGenerationDao = db2.designGenerationDao(),
        roomSceneDao = db2.roomSceneDao(),
        creditWalletRepository = creditRepo
    )

    val history = designRepo2.getDesignHistoryForSceneSync("demo_scene_kost_01")
    assertEquals(1, history.size)
    val restored = history.first()
    assertEquals(createdResult.resultId, restored.resultId)
    assertEquals(secondVariationId, restored.selectedVariationId)
    assertEquals(3, restored.variations.size)
    org.junit.Assert.assertTrue(restored.variations[1].isFavorite)
    assertEquals("file:///immutable_kost_photo.jpg", restored.originalRoomImageUri)

    // Delete parent room scene -> FOREIGN KEY ON DELETE CASCADE removes design requests and variations
    db2.roomSceneDao().deleteScene("demo_scene_kost_01")
    assertEquals(0, designRepo2.getDesignHistoryForSceneSync("demo_scene_kost_01").size)
    assertEquals(0, db2.designGenerationDao().getVariationsForSceneSync("demo_scene_kost_01").size)

    db2.close()
    dbFile.delete()
  }

  @Test
  fun `test makeover viewmodel end to end workflow with credit gate and retry`() = kotlinx.coroutines.runBlocking<Unit> {
    val viewModel = com.example.ruangai.ui.viewmodel.RuangAiViewModel()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val initialCredits = viewModel.walletBalance.value.available
    assertEquals(3, initialCredits)

    // 1. Trigger a simulated failure -> credit is reserved then automatically refunded
    viewModel.generateFullRoomMakeover(
        sceneId = "demo_scene_kost_01",
        roomType = com.example.ruangai.domain.model.RoomType.KOST_STUDIO,
        selectedStyle = com.example.ruangai.domain.model.DesignStyle.TROPICAL,
        colorPaletteNames = listOf("Palm Green", "Rattan"),
        desiredAtmosphere = com.example.ruangai.domain.model.DesignAtmosphere.NATURAL_TROPICAL,
        furniturePreferences = listOf(com.example.ruangai.domain.model.ProductCategory.CHAIR),
        targetBudgetIdr = 3_500_000L,
        additionalInstructions = "Nuansa resort Bali",
        idempotencyKey = "idem_vm_fail_01",
        simulateFailure = true
    )
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    org.junit.Assert.assertNotNull(viewModel.makeoverError.value)
    assertEquals(3, viewModel.walletBalance.value.available)
    org.junit.Assert.assertNotNull(viewModel.lastFailedMakeoverRequestId.value)

    // 2. Retry the failed generation -> succeeds, captures 1 credit, and produces 3 variations
    viewModel.retryLastFailedMakeover()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    org.junit.Assert.assertNull(viewModel.makeoverError.value)
    assertEquals(2, viewModel.walletBalance.value.available)
    val activeRes = viewModel.activeDesignResult.value
    org.junit.Assert.assertNotNull(activeRes)
    assertEquals(com.example.ruangai.domain.model.DesignStyle.TROPICAL, activeRes?.selectedStyle)
    assertEquals(3, activeRes?.variations?.size)

    // 3. Favorite a variation and verify favoriteDesignVariations StateFlow updates
    val targetVar = activeRes!!.variations.first()
    viewModel.toggleFavoriteDesignVariation(targetVar.variationId, true)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertTrue(viewModel.favoriteDesignVariations.value.any { it.variationId == targetVar.variationId })
  }
}
