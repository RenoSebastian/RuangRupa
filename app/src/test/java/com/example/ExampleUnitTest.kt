package com.example

import com.example.ruangai.data.remote.config.ApiConfig
import com.example.ruangai.data.remote.config.Environment
import com.example.ruangai.data.remote.dto.*
import com.example.ruangai.data.remote.util.NetworkResult
import com.example.ruangai.data.repository.mock.*
import com.example.ruangai.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPreloadedRoomsAndProductsExist() {
        assertEquals(2, MockData.preloadedScenes.size)
        assertTrue(MockData.products.size >= 10)

        val kostScene = MockData.preloadedScenes.find { it.roomType == RoomType.KOST_STUDIO }
        assertNotNull(kostScene)
        assertEquals(2, kostScene?.placements?.size) // Multiple placements in single scene
    }

    @Test
    fun testMultiProductPlacementCrud() = runBlocking {
        val repo = MockRoomSceneRepository()
        val scenes = repo.getRoomScenes().first()
        val testScene = scenes.first()

        // 1. Initial count
        val initialCount = testScene.placements.size

        // 2. Add product
        val newPlacement = ProductPlacement(
            placementId = "test_p1",
            productId = "prod_light_01",
            productTitle = "Lampu Lantai Standing Minimalis",
            productPriceIdr = 285000,
            categoryName = "Lampu",
            referenceImage = ProductReferenceImage("ref_test", "prod_light_01", "uri", DimensionsCm(25, 25, 150)),
            transform = PlacementTransform(10f, 20f, 1.2f, 15f, 3)
        )
        repo.addPlacement(testScene.id, newPlacement)

        val updatedScene = repo.getRoomSceneById(testScene.id).first()
        assertNotNull(updatedScene)
        assertEquals(initialCount + 1, updatedScene?.placements?.size)

        // 3. Update transform
        repo.updatePlacementTransform(testScene.id, "test_p1", PlacementTransform(50f, 50f, 1.5f, 0f, 4))
        val sceneAfterTransform = repo.getRoomSceneById(testScene.id).first()
        val movedPlacement = sceneAfterTransform?.placements?.find { it.placementId == "test_p1" }
        assertEquals(50f, movedPlacement?.transform?.translationX)
        assertEquals(1.5f, movedPlacement?.transform?.scale)

        // 4. Remove placement
        repo.removePlacement(testScene.id, "test_p1")
        val sceneAfterRemove = repo.getRoomSceneById(testScene.id).first()
        assertEquals(initialCount, sceneAfterRemove?.placements?.size)
    }

    @Test
    fun testCreditWalletReservationAndCapture() = runBlocking {
        val walletRepo = MockCreditWalletRepository()
        val initialBalance = walletRepo.getWalletBalance().first()
        assertEquals(3, initialBalance.available)
        assertEquals(0, initialBalance.reserved)

        // Reserve 1 credit
        val reserveRes = walletRepo.reserveCredit("job_123")
        assertTrue(reserveRes.isSuccess)

        val afterReserve = walletRepo.getWalletBalance().first()
        assertEquals(2, afterReserve.available)
        assertEquals(1, afterReserve.reserved)

        // Capture credit on completion
        walletRepo.captureCredit("job_123")
        val afterCapture = walletRepo.getWalletBalance().first()
        assertEquals(2, afterCapture.available)
        assertEquals(0, afterCapture.reserved)
        assertEquals(2, afterCapture.lifetimeConsumed)
    }

    @Test
    fun testProductPriceFormattingAndDiscount() {
        val sofa = MockData.products.first()
        assertEquals("Rp 1.450.000", sofa.getFormattedPrice())
    }

    @Test
    fun testApiConfigEnvironments() {
        val devConfig = ApiConfig(environment = Environment.DEV)
        assertEquals("https://api-dev.ruangai.id/v1", devConfig.currentBaseUrl)

        val prodConfig = ApiConfig(environment = Environment.PRODUCTION)
        assertEquals("https://api.ruangai.id/v1", prodConfig.currentBaseUrl)

        assertTrue(devConfig.defaultHeaders().containsKey("X-Platform"))
        assertEquals("android", devConfig.defaultHeaders()["X-Platform"])
    }

    @Test
    fun testDtoToDomainMapping() {
        val dto = ProductResponseDto(
            id = "dto_prod_1",
            title = "Meja Kopi Minimalis DTO",
            category = "TABLE",
            style = "japandi",
            priceIdr = 450000,
            marketplace = "SHOPEE",
            storeName = "Store DTO",
            storeBadge = "Official Store",
            rating = 4.8f,
            reviewCount = 120,
            imageUrl = "http://example.com/img.jpg",
            cutoutUrl = "http://example.com/cutout.png",
            dimensions = listOf(60, 60, 45),
            material = "Kayu Pinus",
            description = "Deskripsi DTO"
        )

        val domain = dto.toDomain()
        assertEquals("dto_prod_1", domain.id)
        assertEquals(ProductCategory.TABLE, domain.category)
        assertEquals(DesignStyle.JAPANDI, domain.designStyle)
        assertEquals(450000L, domain.priceIdr)
        assertEquals("Rp 450.000", domain.getFormattedPrice())
    }

    @Test
    fun testNetworkResultConversion() {
        val success = NetworkResult.Success("data_ok")
        assertTrue(success.toResult().isSuccess)
        assertEquals("data_ok", success.toResult().getOrNull())

        val error = NetworkResult.HttpError(404, "Not Found")
        assertTrue(error.toResult().isFailure)
    }

    @Test
    fun testTabSelectionAndBackstackRestoration() {
        val viewModel = com.example.ruangai.ui.viewmodel.RuangAiViewModel()

        // Initial tab is HOME
        assertEquals(com.example.ruangai.ui.navigation.BottomTab.HOME, viewModel.activeTab.value)

        // Switch to SHOPPING tab
        viewModel.selectTab(com.example.ruangai.ui.navigation.BottomTab.SHOPPING)
        assertEquals(com.example.ruangai.ui.navigation.BottomTab.SHOPPING, viewModel.activeTab.value)

        // Switch to PROFILE tab
        viewModel.selectTab(com.example.ruangai.ui.navigation.BottomTab.PROFILE)
        assertEquals(com.example.ruangai.ui.navigation.BottomTab.PROFILE, viewModel.activeTab.value)

        // Back action should restore previous tab (SHOPPING)
        val handled1 = viewModel.navigateBack()
        assertTrue(handled1)
        assertEquals(com.example.ruangai.ui.navigation.BottomTab.SHOPPING, viewModel.activeTab.value)

        // Back action should restore initial tab (HOME)
        val handled2 = viewModel.navigateBack()
        assertTrue(handled2)
        assertEquals(com.example.ruangai.ui.navigation.BottomTab.HOME, viewModel.activeTab.value)
    }

    @Test
    fun testScreenNavigationAndBackStack() {
        val viewModel = com.example.ruangai.ui.viewmodel.RuangAiViewModel()

        // Navigate from Main to Product Detail
        viewModel.navigateTo(com.example.ruangai.ui.navigation.Screen.ProductDetail("prod_sofa_01"))
        assertTrue(viewModel.currentScreen.value is com.example.ruangai.ui.navigation.Screen.ProductDetail)

        // Navigate back
        val handled = viewModel.navigateBack()
        assertTrue(handled)
        assertTrue(viewModel.currentScreen.value is com.example.ruangai.ui.navigation.Screen.Splash || viewModel.currentScreen.value is com.example.ruangai.ui.navigation.Screen.Main)
    }

    @Test
    fun testRoomSceneMapperBidirectional() {
        val domainScene = MockData.preloadedScenes.first()
        val entity = com.example.ruangai.data.local.db.mapper.RoomSceneMapper.toEntity(domainScene)

        assertEquals(domainScene.id, entity.id)
        assertEquals(domainScene.title, entity.title)
        assertEquals(domainScene.roomType.name, entity.roomType)
        assertEquals(domainScene.targetBudget, entity.targetBudget)

        val restoredDomain = com.example.ruangai.data.local.db.mapper.RoomSceneMapper.toDomain(entity, emptyList())
        assertEquals(domainScene.id, restoredDomain.id)
        assertEquals(domainScene.title, restoredDomain.title)
        assertEquals(domainScene.roomType, restoredDomain.roomType)
    }

    @Test
    fun testDeterministicVisionAnalysisStructuredOutput() {
        val analysis = RoomAnalysisResult(
            sceneId = "scene_test_kost",
            detectedType = RoomType.KOST_STUDIO,
            lighting = LightingInfo("Jendela timur", "Siang", "Terang"),
            surface = SurfaceMaterial(0xFFF8F7F4, "Dinding off-white", "Vinyl pinus"),
            detectedItems = listOf(
                DetectedFurnitureItem("KASUR", "Kasur Single 100x200cm", "Kiri", "Ganti dipan kayu")
            ),
            makeoverSuggestions = listOf("Gunakan dipan kayu berkaki ramping"),
            dominantColors = listOf("Krem Hangat", "Putih", "Kayu"),
            wallAndFloorObservations = "Dinding bersih, lantai vinyl",
            doorsWindows = "1 jendela geser, 1 pintu kayu",
            approximateLayoutObservations = "Bentuk persegi panjang ~3x4m",
            suggestedDesignStyles = listOf(DesignStyle.JAPANDI, DesignStyle.SCANDINAVIAN)
        )

        assertTrue(analysis.isSimulation)
        assertTrue(analysis.simulationNotice.contains("Simulasi Analisis"))
        assertEquals(2, analysis.suggestedDesignStyles.size)
        assertTrue(analysis.suggestedDesignStyles.contains(DesignStyle.JAPANDI))
    }

    @Test
    fun testAlignmentGridToggle() {
        val viewModel = com.example.ruangai.ui.viewmodel.RuangAiViewModel()
        assertFalse(viewModel.isGridEnabled.value)
        viewModel.toggleGrid()
        assertTrue(viewModel.isGridEnabled.value)
        viewModel.toggleGrid()
        assertFalse(viewModel.isGridEnabled.value)
    }
}


