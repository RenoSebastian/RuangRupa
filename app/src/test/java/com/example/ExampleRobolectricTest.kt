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
}

