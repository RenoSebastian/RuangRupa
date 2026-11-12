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

    @Test
    fun testCatalogHasMinimum20ProductsAndUniqueCutouts() {
        val products = MockData.products
        assertTrue("Expected at least 20 demo products, got ${products.size}", products.size >= 20)

        // Verify unique IDs and unique transparent cutout URLs
        val uniqueIds = products.map { it.id }.toSet()
        val uniqueCutouts = products.map { it.transparentCutoutUrl }.toSet()
        assertEquals(products.size, uniqueIds.size)
        assertEquals(products.size, uniqueCutouts.size)

        // Verify Simulasi Katalog disclosure label on all demo items
        assertTrue(products.all { it.demoLabel == "Simulasi Katalog" && it.isMockData })
    }

    @Test
    fun testCatalogFilteringByCategoryStylePriceAndSorting() = runBlocking {
        val repo = MockProductCatalogRepository()

        // 1. Filter by category SOFA
        val sofas = repo.filterAndSortProducts(
            ProductCatalogFilter(category = ProductCategory.SOFA)
        ).first()
        assertEquals(3, sofas.size)
        assertTrue(sofas.all { it.category == ProductCategory.SOFA })

        // 2. Filter by style JAPANDI and price range under 1M
        val japandiAffordable = repo.filterAndSortProducts(
            ProductCatalogFilter(
                style = DesignStyle.JAPANDI,
                priceRange = PriceRangeFilter.RANGE_300K_TO_1M,
                sortBy = ProductSortOption.PRICE_LOW_TO_HIGH
            )
        ).first()
        assertTrue(japandiAffordable.isNotEmpty())
        assertTrue(japandiAffordable.all { it.priceIdr in 300_000L..1_000_000L })
        // Check ascending price sort
        for (i in 0 until japandiAffordable.size - 1) {
            assertTrue(japandiAffordable[i].priceIdr <= japandiAffordable[i + 1].priceIdr)
        }

        // 3. Search by keyword "rotan"
        val rotanResults = repo.filterAndSortProducts(
            ProductCatalogFilter(searchQuery = "rotan")
        ).first()
        assertTrue(rotanResults.isNotEmpty())
        assertTrue(rotanResults.all {
            it.title.contains("rotan", ignoreCase = true) ||
            it.material.contains("rotan", ignoreCase = true)
        })
    }

    @Test
    fun testProductComparisonUpToThreeProducts() {
        val viewModel = com.example.ruangai.ui.viewmodel.RuangAiViewModel()
        assertEquals(0, viewModel.comparedProductIds.value.size)

        viewModel.toggleProductComparison("prod_sofa_01")
        viewModel.toggleProductComparison("prod_sofa_02")
        viewModel.toggleProductComparison("prod_sofa_03")
        assertEquals(3, viewModel.comparedProductIds.value.size)

        // Adding a 4th replaces the oldest so max remains 3
        viewModel.toggleProductComparison("prod_table_01")
        assertEquals(3, viewModel.comparedProductIds.value.size)
        assertFalse(viewModel.comparedProductIds.value.contains("prod_sofa_01"))
        assertTrue(viewModel.comparedProductIds.value.contains("prod_table_01"))

        // Removing one
        viewModel.toggleProductComparison("prod_table_01")
        assertEquals(2, viewModel.comparedProductIds.value.size)

        // Clear all
        viewModel.clearProductComparison()
        assertTrue(viewModel.comparedProductIds.value.isEmpty())
    }

    @Test
    fun testShoppingListDeduplicationAndBudgetCalculations() = runBlocking {
        val shoppingRepo = MockShoppingListRepository()
        val sofa = MockData.products.first { it.id == "prod_sofa_01" }

        // Adding prod_sofa_01 again to demo_scene_kost_01 increments quantity instead of duplicating row
        val initialItems = shoppingRepo.getShoppingListItems().first()
        val initialCount = initialItems.size
        val res = shoppingRepo.addOrIncrementProduct(sofa, "demo_scene_kost_01", "place_kost_1", 1)
        assertTrue(res.isSuccess)
        assertEquals(2, res.getOrNull()?.quantity)

        val afterAddItems = shoppingRepo.getShoppingListItems().first()
        assertEquals(initialCount, afterAddItems.size) // No duplicate entry created

        // Test BudgetSummary remaining vs overrun
        val withinBudget = BudgetSummary(
            sceneId = "demo_scene_kost_01",
            sceneTitle = "Kamar Kost",
            targetBudgetIdr = 3_000_000L,
            selectedSubtotalIdr = 1_870_000L,
            totalItemCount = 2
        )
        assertFalse(withinBudget.isOverBudget)
        assertEquals(1_130_000L, withinBudget.remainingBudgetIdr)
        assertEquals(0L, withinBudget.overrunIdr)

        val overBudget = BudgetSummary(
            sceneId = "demo_scene_kost_01",
            sceneTitle = "Kamar Kost",
            targetBudgetIdr = 3_000_000L,
            selectedSubtotalIdr = 3_320_000L,
            totalItemCount = 3
        )
        assertTrue(overBudget.isOverBudget)
        assertEquals(0L, overBudget.remainingBudgetIdr)
        assertEquals(320_000L, overBudget.overrunIdr)
    }

    @Test
    fun testAffiliateAttributionMetadataAndDisclosure() = runBlocking {
        val affiliateRepo = MockAffiliateLinkRepository()
        val linkRes = affiliateRepo.getAffiliateLink(
            productId = "prod_sofa_01",
            sceneId = "demo_scene_kost_01",
            placementId = "place_kost_1"
        )
        assertTrue(linkRes.isSuccess)
        val info = linkRes.getOrThrow()

        assertEquals("prod_sofa_01", info.productId)
        assertEquals("demo_scene_kost_01", info.sceneId)
        assertEquals("place_kost_1", info.placementId)
        assertEquals("demo_scene_kost_01", info.trackingMetadata.subId1)
        assertEquals("place_kost_1", info.trackingMetadata.subId3)
        assertEquals(AffiliateLinkStatus.SIMULATED, info.status)
        assertTrue(info.redirectDestination.isSimulatedRedirect)
        assertEquals(
            "RuangAI dapat memperoleh komisi dari pembelian melalui tautan afiliasi.",
            info.disclosureLabel
        )

        // Record click and verify history
        affiliateRepo.recordStructuredAffiliateClick(info, "Sofa 2-Seater Fabric Japandi Beige")
        val history = affiliateRepo.getAffiliateClickHistory().first()
        assertEquals(1, history.size)
        assertTrue(history.first().isSimulated)
        assertEquals(info.clickAttributionId, history.first().clickAttributionId)
    }

    @Test
    fun testContextualConsultationEngineAndProductRecommendationValidity() = runBlocking {
        val engine = ConsultantRecommendationEngine()
        val kostScene = MockData.preloadedScenes.first { it.id == "demo_scene_kost_01" }
        val context = ConsultationContext(
            scene = kostScene,
            conversation = null,
            analysisResult = null,
            shoppingListItems = emptyList(),
            availableCatalog = MockData.products
        )

        val aiMessage = engine.composeContextualAssistantMessage(
            conversationId = "conv_demo_scene_kost_01",
            context = context,
            userText = "Rekomendasikan meja kerja dan kursi untuk kamar kost 3x4m gaya Japandi",
            attachments = emptyList()
        )

        assertTrue(aiMessage.isSimulated)
        assertTrue(aiMessage.text.contains("Kamar Kost"))
        assertTrue(aiMessage.recommendedProducts.isNotEmpty())
        assertTrue(aiMessage.suggestedActions.isNotEmpty())

        val catalogIds = MockData.products.map { it.id }.toSet()
        for (rec in aiMessage.recommendedProducts) {
            assertTrue("Recommended product ${rec.productId} must exist in catalog", catalogIds.contains(rec.productId))
            assertTrue(rec.priceIdr > 0L)
            assertTrue(rec.reason.isNotBlank())
        }
    }

    @Test
    fun testBudgetAwareRecommendationsAndOverrunWarning() = runBlocking {
        val engine = ConsultantRecommendationEngine()
        val kostScene = MockData.preloadedScenes.first { it.id == "demo_scene_kost_01" }
        val context = ConsultationContext(
            scene = kostScene,
            conversation = null,
            analysisResult = null,
            shoppingListItems = emptyList(),
            availableCatalog = MockData.products
        )

        // User explicitly specifies a very tight budget (Rp 200.000) for a sofa where all catalog sofas cost > Rp 1.000.000
        val tightBudgetMsg = engine.composeContextualAssistantMessage(
            conversationId = "conv_demo_scene_kost_01",
            context = context,
            userText = "Carikan sofa dengan budget Rp 200rb",
            attachments = emptyList()
        )

        assertEquals(200_000L, tightBudgetMsg.evaluatedBudgetIdr)
        assertTrue(tightBudgetMsg.recommendedProducts.isNotEmpty())
        val firstSofaRec = tightBudgetMsg.recommendedProducts.first()
        assertFalse("Sofa should be flagged as over the 200k budget", firstSofaRec.fitsBudget)
        assertTrue(firstSofaRec.budgetDeltaIdr > 0L)
        assertTrue(tightBudgetMsg.text.contains("Overrun Anggaran"))
    }

    @Test
    fun testStyleMoodboardProfilesForAllTenStyles() {
        val engine = ConsultantRecommendationEngine()
        val profiles = engine.getAllStyleMoodboardProfiles()
        assertEquals(10, profiles.size)
        val allStyles = DesignStyle.values().toSet()
        assertEquals(allStyles, profiles.map { it.style }.toSet())

        val catalogIds = MockData.products.map { it.id }.toSet()
        for (profile in profiles) {
            assertTrue(profile.colorPaletteNamed.isNotEmpty())
            assertTrue(profile.materialSuggestions.isNotEmpty())
            assertTrue(profile.furnitureCharacteristics.isNotEmpty())
            assertTrue(profile.lightingCharacteristics.isNotEmpty())
            assertTrue(profile.spaceUtilizationTip.isNotBlank())
            assertTrue(profile.recommendedProductIds.isNotEmpty())
            assertTrue(profile.recommendedProductIds.all { catalogIds.contains(it) })
        }
    }

    @Test
    fun testFaultTolerantConsultationMapperHandlesMalformedJsonAndMissingFiles() {
        val mapper = com.example.ruangai.data.local.db.mapper.ConsultationMapper
        // 1. Malformed JSON strings should never throw; they return empty lists safely
        assertTrue(mapper.deserializeAttachments("{corrupted_json").isEmpty())
        assertTrue(mapper.deserializeRecommendations("[invalid").isEmpty())
        assertTrue(mapper.deserializeActions("not_a_json_array").isEmpty())

        // 2. Attachment pointing to non-existent local file is flagged with fileExists = false instead of crashing
        val attachment = ChatAttachment(
            id = "att_missing_1",
            uriOrPath = "C:/non_existent_path_12345/missing_photo.jpg",
            label = "missing_photo.jpg",
            mimeType = "image/jpeg",
            width = 800,
            height = 600,
            fileExists = true
        )
        val json = mapper.serializeAttachments(listOf(attachment))
        val decoded = mapper.deserializeAttachments(json)
        assertEquals(1, decoded.size)
        assertEquals("att_missing_1", decoded.first().id)
        assertFalse("Missing local file must be safely marked fileExists = false", decoded.first().fileExists)
    }

    @Test
    fun testConsultationDtoRoundtripMapping() {
        val kostScene = MockData.preloadedScenes.first()
        val reqDto = ConsultationChatRequestDto(
            conversationId = "conv_test_1",
            sceneId = kostScene.id,
            roomType = kostScene.roomType.name,
            preferredStyle = "JAPANDI",
            targetBudgetIdr = 3_000_000L,
            userMessage = "Halo AI",
            attachmentUris = emptyList()
        )
        assertEquals("conv_test_1", reqDto.conversationId)
        assertEquals("JAPANDI", reqDto.preferredStyle)
        assertEquals(3_000_000L, reqDto.targetBudgetIdr)

        val respDto = ConsultationChatResponseDto(
            messageId = "msg_dto_1",
            conversationId = "conv_test_1",
            replyText = "Saran desain Japandi [Simulasi AI]",
            suggestedActions = listOf("Coba di Ruangan"),
            recommendedProducts = listOf(
                RecommendedProductRefDto(
                    productId = "prod_sofa_01",
                    reason = "Pas untuk kamar 3x4m",
                    styleFit = "Japandi",
                    fitsBudget = true
                )
            ),
            isSimulated = true,
            timestamp = 1730000000000L
        )
        assertEquals("msg_dto_1", respDto.messageId)
        assertEquals("conv_test_1", respDto.conversationId)
        assertEquals(1, respDto.recommendedProducts.size)
        assertEquals("prod_sofa_01", respDto.recommendedProducts.first().productId)
        assertTrue(respDto.isSimulated)
    }

    // =========================================================================
    // MILESTONE 7: FULL ROOM MAKEOVER & AI DESIGN GENERATION UNIT TESTS
    // =========================================================================

    @Test
    fun testMilestone7DesignConfigurationValidation() {
        val validReq = DesignGenerationRequest(
            requestId = "req_val_01",
            idempotencyKey = "idem_val_01",
            sceneId = "demo_scene_kost_01",
            originalRoomImageUri = "https://images.unsplash.com/photo-1555854877-bab0e564b8d5",
            roomType = RoomType.KOST_STUDIO,
            selectedStyle = DesignStyle.JAPANDI,
            colorPaletteNames = listOf("Warm Linen", "Natural Oak"),
            desiredAtmosphere = DesignAtmosphere.WARM_COZY,
            furniturePreferences = listOf(ProductCategory.TABLE, ProductCategory.OFFICE_CHAIR),
            targetBudgetIdr = 3_000_000L,
            additionalInstructions = "Tambahkan lampu meja hangat"
        )
        val validCheck = validReq.validate()
        assertTrue(validCheck.isValid)
        assertTrue(validCheck.errors.isEmpty())

        // Invalid configuration: empty sceneId, empty palettes, budget below minimum Rp 500.000
        val invalidReq = validReq.copy(
            sceneId = "",
            colorPaletteNames = emptyList(),
            targetBudgetIdr = 100_000L,
            variationCount = 0
        )
        val invalidCheck = invalidReq.validate()
        assertFalse(invalidCheck.isValid)
        assertEquals(4, invalidCheck.errors.size)
    }

    @Test
    fun testMilestone7DeterministicMockGeneration() {
        val generator = DeterministicMakeoverGenerator()
        val req1 = DesignGenerationRequest(
            requestId = "req_det_A",
            idempotencyKey = "idem_det_A",
            sceneId = "demo_scene_kost_01",
            originalRoomImageUri = "https://images.unsplash.com/photo-1555854877-bab0e564b8d5",
            roomType = RoomType.KOST_STUDIO,
            selectedStyle = DesignStyle.SCANDINAVIAN,
            colorPaletteNames = listOf("Nordic White", "Birch Wood", "Dusty Blue"),
            desiredAtmosphere = DesignAtmosphere.BRIGHT_AIRY,
            furniturePreferences = listOf(ProductCategory.SOFA, ProductCategory.LIGHTING),
            targetBudgetIdr = 4_500_000L,
            additionalInstructions = "Fokus pencahayaan terang",
            createdAt = 1720000000000L
        )
        val req2 = req1.copy(requestId = "req_det_B", idempotencyKey = "idem_det_B")

        val res1 = generator.generateMakeoverResult(req1, resultId = "res_det")
        val res2 = generator.generateMakeoverResult(req2, resultId = "res_det")

        assertTrue(res1.isSimulated)
        assertEquals(DesignGenerationStatus.COMPLETED, res1.status)
        assertEquals(res1.variations.size, res2.variations.size)

        for (i in res1.variations.indices) {
            val v1 = res1.variations[i]
            val v2 = res2.variations[i]
            assertTrue(v1.isSimulated)
            assertEquals(v1.variationTitle, v2.variationTitle)
            assertEquals(v1.previewImageUrl, v2.previewImageUrl)
            assertEquals(v1.designSummary, v2.designSummary)
            assertEquals(v1.estimatedFurnitureBudgetIdr, v2.estimatedFurnitureBudgetIdr)
            assertEquals(
                v1.catalogMatchedProducts.map { it.productId },
                v2.catalogMatchedProducts.map { it.productId }
            )
        }
    }

    @Test
    fun testMilestone7DesignVariationsCountAndMetadata() {
        val generator = DeterministicMakeoverGenerator()
        for (style in DesignStyle.values()) {
            val req = DesignGenerationRequest(
                requestId = "req_style_${style.name}",
                idempotencyKey = "idem_style_${style.name}",
                sceneId = "demo_scene_living_02",
                originalRoomImageUri = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7",
                roomType = RoomType.LIVING_ROOM,
                selectedStyle = style,
                colorPaletteNames = listOf("Warm Neutral", "Accent Wood"),
                desiredAtmosphere = DesignAtmosphere.MODERN_ELEGANT,
                targetBudgetIdr = 6_000_000L,
                variationCount = 3
            )
            val result = generator.generateMakeoverResult(req)
            assertEquals(3, result.variations.size)
            assertEquals(style, result.selectedStyle)
            result.variations.forEachIndexed { idx, variation ->
                assertEquals(idx, variation.variationIndex)
                assertEquals(style, variation.style)
                assertEquals(DesignAtmosphere.MODERN_ELEGANT, variation.atmosphere)
                assertEquals(RoomType.LIVING_ROOM, variation.roomType)
                assertTrue(variation.previewImageUrl.startsWith("https://"))
                assertTrue(variation.designSummary.isNotBlank())
                assertTrue(variation.simulationDisclosure.contains("SIMULASI", ignoreCase = true))
            }
        }
    }

    @Test
    fun testMilestone7CatalogMatchedProductsReferenceRealCatalog() {
        val catalogIds = MockData.products.map { it.id }.toSet()
        val generator = DeterministicMakeoverGenerator()
        val req = DesignGenerationRequest(
            requestId = "req_cat_01",
            idempotencyKey = "idem_cat_01",
            sceneId = "demo_scene_kost_01",
            originalRoomImageUri = "https://images.unsplash.com/photo-1555854877-bab0e564b8d5",
            roomType = RoomType.KOST_STUDIO,
            selectedStyle = DesignStyle.JAPANDI,
            colorPaletteNames = listOf("Warm Linen", "Natural Oak"),
            desiredAtmosphere = DesignAtmosphere.CALM_ZEN,
            furniturePreferences = listOf(ProductCategory.SOFA, ProductCategory.COFFEE_TABLE, ProductCategory.LIGHTING),
            targetBudgetIdr = 5_000_000L
        )
        val result = generator.generateMakeoverResult(req)
        for (variation in result.variations) {
            assertTrue(variation.catalogMatchedProducts.isNotEmpty())
            for (matched in variation.catalogMatchedProducts) {
                assertTrue(
                    "Matched product ${matched.productId} must exist in MockData.products",
                    catalogIds.contains(matched.productId)
                )
                val realProd = MockData.products.first { it.id == matched.productId }
                assertEquals(realProd.priceIdr, matched.priceIdr)
                assertEquals(realProd.title, matched.productTitle)
                assertFalse(
                    "Catalog matched product must explicitly set isExactVisualMatch = false",
                    matched.isExactVisualMatch
                )
                assertTrue(matched.matchDistinctionNotice.contains("bukan objek piksel identik", ignoreCase = true))
            }
        }
    }

    @Test
    fun testMilestone7OriginalRoomImageImmutability() = runBlocking {
        val creditRepo = MockCreditWalletRepository()
        val designRepo = MockAIInteriorDesignRepository(creditWalletRepository = creditRepo)
        val originalPhotoUri = "file:///data/user/0/com.example.ruangai/files/rooms/immutable_room_orig.jpg"

        val req = DesignGenerationRequest(
            requestId = "req_imm_01",
            idempotencyKey = "idem_imm_01",
            sceneId = "demo_scene_kost_01",
            originalRoomImageUri = originalPhotoUri,
            roomType = RoomType.KOST_STUDIO,
            selectedStyle = DesignStyle.INDUSTRIAL,
            colorPaletteNames = listOf("Raw Concrete", "Matte Black"),
            desiredAtmosphere = DesignAtmosphere.CREATIVE_PRODUCTIVE,
            targetBudgetIdr = 4_000_000L
        )

        val generated = designRepo.generateRoomMakeover(req).getOrThrow()
        assertEquals(originalPhotoUri, generated.originalRoomImageUri)
        for (v in generated.variations) {
            assertNotEquals(
                "Generated variation preview must not overwrite or equal local original URI",
                originalPhotoUri,
                v.previewImageUrl
            )
        }
    }

    @Test
    fun testMilestone7CreditReservationAndCaptureLifecycle() = runBlocking {
        val creditRepo = MockCreditWalletRepository()
        val designRepo = MockAIInteriorDesignRepository(creditWalletRepository = creditRepo)

        val initialBalance = creditRepo.getWalletBalance().first()
        assertEquals(3, initialBalance.available)
        assertEquals(0, initialBalance.reserved)
        assertEquals(1, initialBalance.lifetimeConsumed)

        val req = DesignGenerationRequest(
            requestId = "req_cred_01",
            idempotencyKey = "idem_cred_01",
            sceneId = "demo_scene_kost_01",
            originalRoomImageUri = "https://images.unsplash.com/photo-1555854877-bab0e564b8d5",
            roomType = RoomType.KOST_STUDIO,
            selectedStyle = DesignStyle.JAPANDI,
            colorPaletteNames = listOf("Warm Linen"),
            targetBudgetIdr = 3_000_000L
        )

        val res = designRepo.generateRoomMakeover(req)
        assertTrue(res.isSuccess)
        val generated = res.getOrThrow()
        assertTrue(generated.creditReserved)
        assertTrue(generated.creditCaptured)
        assertFalse(generated.creditRefunded)

        val afterBalance = creditRepo.getWalletBalance().first()
        assertEquals(2, afterBalance.available)
        assertEquals(0, afterBalance.reserved)
        assertEquals(2, afterBalance.lifetimeConsumed)

        val txs = creditRepo.getTransactions().first()
        assertTrue(txs.any { it.type == TransactionType.RENDER_RESERVATION && it.description.contains("idem_cred_01") })
        assertTrue(txs.any { it.type == TransactionType.RENDER_CAPTURE && it.description.contains("idem_cred_01") })
    }

    @Test
    fun testMilestone7CreditRefundOnFailureAndDuplicateIdempotencyProtection() = runBlocking {
        val creditRepo = MockCreditWalletRepository()
        val designRepo = MockAIInteriorDesignRepository(creditWalletRepository = creditRepo)

        // 1. Simulated generation failure -> reserves credit then automatically refunds it
        val failingReq = DesignGenerationRequest(
            requestId = "req_fail_01",
            idempotencyKey = "idem_fail_01",
            sceneId = "demo_scene_kost_01",
            originalRoomImageUri = "https://images.unsplash.com/photo-1555854877-bab0e564b8d5",
            roomType = RoomType.KOST_STUDIO,
            selectedStyle = DesignStyle.BOHEMIAN,
            colorPaletteNames = listOf("Terracotta", "Sand"),
            targetBudgetIdr = 3_500_000L,
            simulateFailure = true
        )

        val failRes = designRepo.generateRoomMakeover(failingReq)
        assertTrue(failRes.isFailure)

        val balanceAfterFailure = creditRepo.getWalletBalance().first()
        assertEquals("Available credits must be refunded back to 3 on failure", 3, balanceAfterFailure.available)
        assertEquals(0, balanceAfterFailure.reserved)

        val txsAfterFailure = creditRepo.getTransactions().first()
        assertTrue(txsAfterFailure.any { it.type == TransactionType.AUTO_REFUND && it.description.contains("idem_fail_01") })

        // 2. Retry the failed request -> succeeds and captures 1 credit
        val retryRes = designRepo.retryDesignGeneration("req_fail_01")
        assertTrue(retryRes.isSuccess)
        assertEquals(DesignGenerationStatus.COMPLETED, retryRes.getOrThrow().status)
        assertEquals(2, creditRepo.getWalletBalance().first().available)

        // 3. Duplicate submission with the same idempotencyKey -> returns existing result without deducting credit again
        val dupKey = "idem_unique_once_99"
        val successReq = failingReq.copy(
            requestId = "req_dup_01",
            idempotencyKey = dupKey,
            simulateFailure = false
        )
        val firstRun = designRepo.generateRoomMakeover(successReq).getOrThrow()
        assertEquals(1, creditRepo.getWalletBalance().first().available)

        val secondDuplicateRun = designRepo.generateRoomMakeover(successReq.copy(requestId = "req_dup_02")).getOrThrow()
        assertEquals(firstRun.resultId, secondDuplicateRun.resultId)
        assertEquals(
            "Duplicate request with identical idempotencyKey must NOT deduct another credit",
            1,
            creditRepo.getWalletBalance().first().available
        )
    }
}
