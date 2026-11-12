package com.example.ruangai.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ruangai.di.AppContainer
import com.example.ruangai.di.DefaultAppContainer
import com.example.ruangai.di.RuangAiViewModelFactory
import com.example.ruangai.ui.components.BottomTabBar
import com.example.ruangai.ui.navigation.BottomTab
import com.example.ruangai.ui.navigation.Screen
import com.example.ruangai.ui.screens.*
import com.example.ruangai.ui.viewmodel.RuangAiViewModel

@Composable
fun RuangAiApp(
    context: android.content.Context = androidx.compose.ui.platform.LocalContext.current,
    container: AppContainer = remember(context) { DefaultAppContainer(context = context) },
    viewModel: RuangAiViewModel = viewModel(factory = RuangAiViewModelFactory(container))
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val scenes by viewModel.roomScenes.collectAsStateWithLifecycle()
    val activeScene by viewModel.activeScene.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStyleFilter by viewModel.selectedStyleFilter.collectAsStateWithLifecycle()
    val selectedPriceRange by viewModel.selectedPriceRange.collectAsStateWithLifecycle()
    val selectedSortOption by viewModel.selectedSortOption.collectAsStateWithLifecycle()
    val comparedProductIds by viewModel.comparedProductIds.collectAsStateWithLifecycle()
    val comparedProducts by viewModel.comparedProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val totalCartPrice by viewModel.totalSelectedCartPrice.collectAsStateWithLifecycle()
    val activeAffiliatePreview by viewModel.activeAffiliatePreview.collectAsStateWithLifecycle()
    val balance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val activeConversation by viewModel.activeConversation.collectAsStateWithLifecycle()
    val pendingAttachments by viewModel.pendingAttachments.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val scannerStep by viewModel.scannerStep.collectAsStateWithLifecycle()
    val scannerProgress by viewModel.scannerProgressLabel.collectAsStateWithLifecycle()
    val scannerResult by viewModel.scannerAnalysisResult.collectAsStateWithLifecycle()
    val selectedPlacementId by viewModel.selectedPlacementId.collectAsStateWithLifecycle()
    val showCreditDialog by viewModel.showCreditDialog.collectAsStateWithLifecycle()
    val isGeneratingPreview by viewModel.isGeneratingPreview.collectAsStateWithLifecycle()
    val previewSuccessNotice by viewModel.previewSuccessNotice.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val isGridEnabled by viewModel.isGridEnabled.collectAsStateWithLifecycle()
    val lastExportResult by viewModel.lastExportResult.collectAsStateWithLifecycle()
    val userSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    val designHistory by viewModel.designHistoryForActiveScene.collectAsStateWithLifecycle()
    val activeDesignResult by viewModel.activeDesignResult.collectAsStateWithLifecycle()
    val favoriteVariations by viewModel.favoriteDesignVariations.collectAsStateWithLifecycle()
    val isGeneratingMakeover by viewModel.isGeneratingMakeover.collectAsStateWithLifecycle()
    val makeoverError by viewModel.makeoverError.collectAsStateWithLifecycle()
    val lastFailedMakeoverRequestId by viewModel.lastFailedMakeoverRequestId.collectAsStateWithLifecycle()

    // Back button handling to preserve clean backstack
    BackHandler(enabled = currentScreen !is Screen.Main || activeTab != BottomTab.HOME) {
        val handled = viewModel.navigateBack()
        if (!handled && activeTab != BottomTab.HOME) {
            viewModel.selectTab(BottomTab.HOME)
        }
    }

    when (val screen = currentScreen) {
        is Screen.Splash -> {
            SplashScreen(
                onTimeout = { viewModel.navigateTo(viewModel.resolveInitialRoute()) }
            )
        }

        is Screen.Onboarding -> {
            OnboardingScreen(
                onFinish = {
                    viewModel.completeOnboarding()
                    viewModel.navigateTo(viewModel.resolvePostOnboardingRoute())
                }
            )
        }

        is Screen.Auth -> {
            AuthScreen(
                onAuthSuccess = { next -> viewModel.navigateTo(next) },
                onLoginWithEmail = { email, pass, callback ->
                    viewModel.loginWithEmail(email, pass, callback)
                },
                onRegisterWithEmail = { email, pass, name, callback ->
                    viewModel.registerWithEmail(email, pass, name, callback)
                },
                onGoogleSignIn = { callback ->
                    viewModel.signInWithGoogle(callback)
                }
            )
        }

        is Screen.Main -> {
            Scaffold(
                bottomBar = {
                    BottomTabBar(
                        selectedTab = activeTab,
                        onTabSelected = { tab ->
                            if (tab == BottomTab.DESIGN) {
                                viewModel.navigateTo(Screen.RoomScanner)
                            } else {
                                viewModel.selectTab(tab)
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (activeTab) {
                        BottomTab.HOME -> {
                            HomeScreen(
                                userName = userSession?.displayName ?: "Pengguna RuangAI",
                                creditCount = balance.available,
                                scenes = scenes,
                                products = viewModel.allCatalogProducts,
                                onOpenScanner = { viewModel.navigateTo(Screen.RoomScanner) },
                                onOpenConsultation = { viewModel.navigateTo(Screen.Consultation()) },
                                onOpenStyleExplorer = { viewModel.navigateTo(Screen.StyleExplorer) },
                                onOpenTryOn = { sceneId ->
                                    viewModel.selectScene(sceneId)
                                    viewModel.navigateTo(Screen.VirtualTryOn(sceneId))
                                },
                                onOpenDesignResults = { sceneId ->
                                    viewModel.selectScene(sceneId)
                                    viewModel.navigateTo(Screen.DesignResults(sceneId))
                                },
                                onOpenProductDetail = { prod ->
                                    viewModel.navigateTo(Screen.ProductDetail(prod.id))
                                },
                                onOpenCreditWallet = { viewModel.navigateTo(Screen.CreditWallet) },
                                onOpenShopping = { viewModel.selectTab(BottomTab.SHOPPING) }
                            )
                        }

                        BottomTab.EXPLORE -> {
                            StyleExplorerScreen(
                                activeScene = activeScene,
                                styleMoodboards = remember { viewModel.getAllStyleMoodboards() },
                                allProducts = viewModel.allCatalogProducts,
                                onSelectStyle = { style ->
                                    viewModel.applyStylePreferenceToScene(style)
                                },
                                onApplyStyleToScene = { style ->
                                    viewModel.applyStylePreferenceToScene(style)
                                },
                                onDiscussStyleWithAi = { style ->
                                    viewModel.applyStylePreferenceToScene(style)
                                    val sId = activeScene?.id ?: "demo_scene_kost_01"
                                    viewModel.navigateTo(Screen.Consultation(sId))
                                    viewModel.sendMessage("Rekomendasikan penataan dan furnitur bergaya ${style.displayName} untuk ruangan ini.")
                                },
                                onViewProductDetail = { prod ->
                                    viewModel.navigateTo(Screen.ProductDetail(prod.id, sourceSceneId = activeScene?.id))
                                },
                                onTryProductInRoom = { prod ->
                                    val sId = activeScene?.id ?: scenes.firstOrNull()?.id ?: "demo_scene_kost_01"
                                    viewModel.addProductToScene(sId, prod)
                                }
                            )
                        }

                        BottomTab.DESIGN -> {
                            RoomScannerScreen(
                                scannerStep = scannerStep,
                                progressLabel = scannerProgress,
                                analysisResult = scannerResult,
                                onStartScan = { uri, type, title ->
                                    viewModel.startRoomScan(uri, type, title)
                                },
                                onReset = { viewModel.resetScanner() },
                                onFinish = { sceneId ->
                                    viewModel.selectScene(sceneId)
                                    viewModel.navigateTo(Screen.RoomDetail(sceneId))
                                },
                                onBack = { viewModel.selectTab(BottomTab.HOME) }
                            )
                        }

                        BottomTab.SHOPPING -> {
                            ShoppingScreen(
                                products = products,
                                selectedCategory = selectedCategory,
                                searchQuery = searchQuery,
                                selectedStyle = selectedStyleFilter,
                                selectedPriceRange = selectedPriceRange,
                                selectedSortOption = selectedSortOption,
                                comparedProductIds = comparedProductIds,
                                activeSceneTitle = activeScene?.title,
                                onCategorySelect = { viewModel.setCategory(it) },
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onStyleSelect = { viewModel.setStyleFilter(it) },
                                onPriceRangeSelect = { viewModel.setPriceRangeFilter(it) },
                                onSortSelect = { viewModel.setSortOption(it) },
                                onResetFilters = { viewModel.resetCatalogFilters() },
                                onToggleCompare = { viewModel.toggleProductComparison(it.id) },
                                onOpenComparison = { viewModel.navigateTo(Screen.ProductComparison) },
                                onClearComparison = { viewModel.clearProductComparison() },
                                onProductClick = { prod ->
                                    viewModel.navigateTo(Screen.ProductDetail(prod.id))
                                },
                                onTryOnClick = { prod ->
                                    val currentSceneId = activeScene?.id ?: scenes.firstOrNull()?.id ?: "demo_scene_kost_01"
                                    viewModel.addProductToScene(currentSceneId, prod)
                                },
                                onOpenCart = { viewModel.navigateTo(Screen.ShoppingList) },
                                cartItemCount = cartItems.sumOf { it.quantity }
                            )
                        }

                        BottomTab.PROFILE -> {
                            ProfileScreen(
                                userSession = userSession,
                                savedProjectsCount = scenes.size,
                                creditCount = balance.available,
                                onOpenWallet = { viewModel.navigateTo(Screen.CreditWallet) },
                                onLogout = { viewModel.logout() }
                            )
                        }
                    }
                }
            }
        }

        is Screen.RoomScanner -> {
            RoomScannerScreen(
                scannerStep = scannerStep,
                progressLabel = scannerProgress,
                analysisResult = scannerResult,
                onStartScan = { uri, type, title ->
                    viewModel.startRoomScan(uri, type, title)
                },
                onReset = { viewModel.resetScanner() },
                onFinish = { sceneId ->
                    viewModel.selectScene(sceneId)
                    viewModel.navigateTo(Screen.RoomDetail(sceneId))
                },
                onBack = { viewModel.navigateBack() }
            )
        }

        is Screen.Consultation -> {
            LaunchedEffect(screen.sceneId) {
                screen.sceneId?.let { viewModel.selectScene(it) }
            }
            ConsultationScreen(
                chatMessages = chatMessages,
                isAiThinking = isAiThinking,
                onSendMessage = { viewModel.sendMessage(it) },
                onBack = { viewModel.navigateBack() },
                onNavigateToTryOn = {
                    val sId = activeScene?.id ?: "demo_scene_kost_01"
                    viewModel.navigateTo(Screen.VirtualTryOn(sId))
                },
                onNavigateToShopping = {
                    viewModel.navigateTo(Screen.Main())
                    viewModel.selectTab(BottomTab.SHOPPING)
                },
                activeScene = activeScene,
                allScenes = scenes,
                activeConversation = activeConversation,
                pendingAttachments = pendingAttachments,
                chatError = chatError,
                comparedProductIds = comparedProductIds,
                onSelectScene = { sceneId -> viewModel.selectScene(sceneId) },
                onAttachPhoto = { uri, label -> viewModel.attachPhotoToConversation(uri, label) },
                onRemovePendingAttachment = { attId -> viewModel.removePendingAttachment(attId) },
                onClearConversation = { viewModel.clearCurrentConversation() },
                onDismissError = { viewModel.dismissChatError() },
                onOpenStyleExplorer = { viewModel.navigateTo(Screen.StyleExplorer) },
                onUpdateDimensionsAndBudget = { dims, budget ->
                    val sId = activeScene?.id ?: "demo_scene_kost_01"
                    viewModel.updateSceneDimensionsAndBudget(sId, dims, budget)
                },
                onViewProductDetail = { prodId ->
                    viewModel.navigateTo(Screen.ProductDetail(prodId, sourceSceneId = activeScene?.id))
                },
                onCompareProduct = { prodId ->
                    viewModel.toggleProductComparison(prodId)
                    viewModel.navigateTo(Screen.ProductComparison)
                },
                onAddProductToShoppingList = { prodId ->
                    viewModel.allCatalogProducts.find { it.id == prodId }?.let { prod ->
                        viewModel.addToCart(prod, activeScene?.id)
                    }
                },
                onTryProductInRoom = { prodId ->
                    viewModel.allCatalogProducts.find { it.id == prodId }?.let { prod ->
                        val sId = activeScene?.id ?: scenes.firstOrNull()?.id ?: "demo_scene_kost_01"
                        viewModel.addProductToScene(sId, prod)
                    }
                }
            )
        }

        is Screen.StyleExplorer -> {
            StyleExplorerScreen(
                activeScene = activeScene,
                styleMoodboards = remember { viewModel.getAllStyleMoodboards() },
                allProducts = viewModel.allCatalogProducts,
                onSelectStyle = { style ->
                    viewModel.applyStylePreferenceToScene(style)
                },
                onApplyStyleToScene = { style ->
                    viewModel.applyStylePreferenceToScene(style)
                },
                onDiscussStyleWithAi = { style ->
                    viewModel.applyStylePreferenceToScene(style)
                    val sId = activeScene?.id ?: "demo_scene_kost_01"
                    viewModel.navigateTo(Screen.Consultation(sId))
                    viewModel.sendMessage("Rekomendasikan penataan dan furnitur bergaya ${style.displayName} untuk ruangan ini.")
                },
                onViewProductDetail = { prod ->
                    viewModel.navigateTo(Screen.ProductDetail(prod.id, sourceSceneId = activeScene?.id))
                },
                onTryProductInRoom = { prod ->
                    val sId = activeScene?.id ?: scenes.firstOrNull()?.id ?: "demo_scene_kost_01"
                    viewModel.addProductToScene(sId, prod)
                },
                onBack = { viewModel.navigateBack() }
            )
        }

        is Screen.DesignResults -> {
            LaunchedEffect(screen.sceneId) {
                viewModel.selectScene(screen.sceneId)
            }
            val scene = scenes.find { it.id == screen.sceneId } ?: activeScene ?: scenes.firstOrNull()
            if (scene != null) {
                DesignResultsScreen(
                    scene = scene,
                    onBack = { viewModel.navigateBack() },
                    onOpenTryOn = { sId ->
                        viewModel.selectScene(sId)
                        viewModel.navigateTo(Screen.VirtualTryOn(sId))
                    },
                    onOpenShopping = { viewModel.selectTab(BottomTab.SHOPPING) },
                    onRegenerate = {
                        val currentStyle = activeDesignResult?.selectedStyle
                            ?: scene.preferredStyle
                            ?: com.example.ruangai.domain.model.DesignStyle.JAPANDI
                        val currentPalette = activeDesignResult?.colorPaletteNames
                            ?: viewModel.getStyleMoodboard(currentStyle).colorPaletteNamed.map { it.first }
                        val currentAtmosphere = activeDesignResult?.desiredAtmosphere
                            ?: com.example.ruangai.domain.model.DesignAtmosphere.WARM_COZY
                        val currentPrefs = activeDesignResult?.furniturePreferences.orEmpty()
                        viewModel.generateFullRoomMakeover(
                            sceneId = scene.id,
                            roomType = scene.roomType,
                            selectedStyle = currentStyle,
                            colorPaletteNames = currentPalette,
                            desiredAtmosphere = currentAtmosphere,
                            furniturePreferences = currentPrefs,
                            targetBudgetIdr = scene.targetBudget,
                            additionalInstructions = "Variasi regenerasi baru"
                        )
                    },
                    allScenes = scenes,
                    allCatalogProducts = viewModel.allCatalogProducts,
                    styleMoodboards = remember { viewModel.getAllStyleMoodboards() },
                    activeDesignResult = activeDesignResult,
                    designHistory = designHistory,
                    favoriteVariations = favoriteVariations,
                    creditAvailable = balance.available,
                    isGeneratingMakeover = isGeneratingMakeover,
                    makeoverError = makeoverError,
                    lastFailedRequestId = lastFailedMakeoverRequestId,
                    statusNotice = previewSuccessNotice,
                    onSelectScene = { sId -> viewModel.selectScene(sId) },
                    onSelectVariation = { resId, varId ->
                        viewModel.selectDesignVariation(resId, varId)
                    },
                    onToggleFavoriteVariation = { varId, isFav ->
                        viewModel.toggleFavoriteDesignVariation(varId, isFav)
                    },
                    onSelectHistoryResult = { resId ->
                        viewModel.selectDesignResultFromHistory(resId)
                    },
                    onSubmitMakeoverConfig = { sId, rType, style, palettes, atmosphere, prefs, budget, instructions ->
                        viewModel.generateFullRoomMakeover(
                            sceneId = sId,
                            roomType = rType,
                            selectedStyle = style,
                            colorPaletteNames = palettes,
                            desiredAtmosphere = atmosphere,
                            furniturePreferences = prefs,
                            targetBudgetIdr = budget,
                            additionalInstructions = instructions
                        )
                    },
                    onRetryFailedGeneration = { reqId ->
                        viewModel.retryLastFailedMakeover(reqId)
                    },
                    onCancelGeneration = { reqId ->
                        viewModel.cancelMakeoverGeneration(reqId)
                    },
                    onDismissError = {
                        viewModel.dismissMakeoverError()
                    },
                    onOpenProductDetail = { prodId ->
                        viewModel.navigateTo(Screen.ProductDetail(prodId, sourceSceneId = scene.id))
                    },
                    onAddProductToShoppingList = { prodId ->
                        viewModel.allCatalogProducts.find { it.id == prodId }?.let { prod ->
                            viewModel.addToCart(prod, scene.id)
                        }
                    },
                    onTryProductInRoom = { prodId ->
                        viewModel.allCatalogProducts.find { it.id == prodId }?.let { prod ->
                            viewModel.addProductToScene(scene.id, prod)
                        }
                    },
                    onOpenCreditWallet = {
                        viewModel.navigateTo(Screen.CreditWallet)
                    }
                )
            }
        }

        is Screen.RoomDetail -> {
            val scene = scenes.find { it.id == screen.sceneId } ?: scenes.firstOrNull()
            LaunchedEffect(screen.sceneId) {
                viewModel.loadRoomAnalysis(screen.sceneId)
            }
            RoomDetailScreen(
                scene = scene,
                analysisResult = scannerResult,
                onBack = { viewModel.navigateBack() },
                onOpenTryOn = { sId ->
                    viewModel.selectScene(sId)
                    viewModel.navigateTo(Screen.VirtualTryOn(sId))
                },
                onOpenConsultation = { sId ->
                    viewModel.selectScene(sId)
                    viewModel.navigateTo(Screen.Consultation(sId))
                },
                onUpdateMetadata = { sId, title, type, budget ->
                    viewModel.updateRoomSceneMetadata(sId, title, type, budget)
                },
                onDeleteScene = { sId ->
                    viewModel.deleteRoomScene(sId)
                },
                onOpenMakeoverResults = { sId ->
                    viewModel.selectScene(sId)
                    viewModel.navigateTo(Screen.DesignResults(sId))
                }
            )
        }

        is Screen.ProductDetail -> {
            val product = viewModel.allCatalogProducts.find { it.id == screen.productId } ?: viewModel.allCatalogProducts.first()
            val related = remember(product.id) { viewModel.getRelatedProducts(product) }
            val alternatives = remember(product.id) { viewModel.getAlternativeProducts(product) }

            ProductDetailScreen(
                product = product,
                relatedProducts = related,
                alternativeProducts = alternatives,
                isCompared = comparedProductIds.contains(product.id),
                sourceSceneId = screen.sourceSceneId,
                sourcePlacementId = screen.sourcePlacementId,
                activeAffiliatePreview = activeAffiliatePreview,
                onBack = { viewModel.navigateBack() },
                onTryOnInMyRoom = { prod ->
                    val sId = screen.sourceSceneId ?: activeScene?.id ?: scenes.firstOrNull()?.id ?: "demo_scene_kost_01"
                    viewModel.addProductToScene(sId, prod)
                },
                onAddToCart = { prod ->
                    val sId = screen.sourceSceneId ?: activeScene?.id
                    viewModel.addToCart(prod, sId, screen.sourcePlacementId)
                },
                onToggleCompare = { prod ->
                    viewModel.toggleProductComparison(prod.id)
                },
                onSelectOtherProduct = { other ->
                    viewModel.navigateTo(
                        Screen.ProductDetail(
                            productId = other.id,
                            sourceSceneId = screen.sourceSceneId,
                            sourcePlacementId = screen.sourcePlacementId
                        )
                    )
                },
                onReplaceInScene = { sId, plId, newProd ->
                    viewModel.replacePlacementProduct(sId, plId, newProd)
                    viewModel.navigateTo(Screen.VirtualTryOn(sId))
                },
                onTriggerAffiliateClick = { prod ->
                    viewModel.triggerAffiliateAttribution(
                        product = prod,
                        sceneId = screen.sourceSceneId ?: activeScene?.id,
                        placementId = screen.sourcePlacementId
                    )
                },
                onDismissAffiliateDialog = {
                    viewModel.dismissAffiliatePreview()
                }
            )
        }

        is Screen.ProductComparison -> {
            ProductComparisonScreen(
                comparedProducts = comparedProducts,
                onBack = { viewModel.navigateBack() },
                onRemoveFromComparison = { id -> viewModel.toggleProductComparison(id) },
                onClearAll = { viewModel.clearProductComparison() },
                onProductClick = { prod -> viewModel.navigateTo(Screen.ProductDetail(prod.id)) },
                onTryOnProduct = { prod ->
                    val sId = activeScene?.id ?: scenes.firstOrNull()?.id ?: "demo_scene_kost_01"
                    viewModel.addProductToScene(sId, prod)
                },
                onAddToCart = { prod ->
                    viewModel.addToCart(prod, activeScene?.id)
                }
            )
        }

        is Screen.VirtualTryOn -> {
            val scene = scenes.find { it.id == screen.sceneId } ?: scenes.firstOrNull()
            if (scene != null) {
                VirtualTryOnScreen(
                    scene = scene,
                    selectedPlacementId = selectedPlacementId,
                    allProducts = viewModel.allCatalogProducts,
                    walletBalance = balance.available,
                    isGeneratingPreview = isGeneratingPreview,
                    showCreditDialog = showCreditDialog,
                    successNotice = previewSuccessNotice,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    isGridEnabled = isGridEnabled,
                    lastExportResult = lastExportResult,
                    onSelectPlacement = { viewModel.selectPlacement(it) },
                    onTransformChange = { pId, trans ->
                        viewModel.updatePlacementTransform(pId, trans)
                    },
                    onRemovePlacement = { pId ->
                        viewModel.removePlacement(scene.id, pId)
                    },
                    onReplaceProduct = { pId, newProd ->
                        viewModel.replacePlacementProduct(scene.id, pId, newProd)
                    },
                    onAddProductToScene = { prod ->
                        viewModel.addProductToScene(scene.id, prod)
                    },
                    onOpenProductDetail = { prodId, placementId ->
                        viewModel.navigateTo(
                            Screen.ProductDetail(
                                productId = prodId,
                                sourceSceneId = scene.id,
                                sourcePlacementId = placementId
                            )
                        )
                    },
                    onSyncPlacementsToShoppingList = {
                        viewModel.syncScenePlacementsToShoppingList(scene)
                    },
                    onBringToFront = { pId ->
                        viewModel.bringPlacementToFront(scene.id, pId)
                    },
                    onSendToBack = { pId ->
                        viewModel.sendPlacementToBack(scene.id, pId)
                    },
                    onResetTransform = { pId ->
                        viewModel.resetPlacementTransform(scene.id, pId)
                    },
                    onToggleGrid = {
                        viewModel.toggleGrid()
                    },
                    onUndo = {
                        viewModel.undo()
                    },
                    onRedo = {
                        viewModel.redo()
                    },
                    onExportComposite = {
                        viewModel.exportFlattenedComposite(scene.id)
                    },
                    onRequestRealisticPreview = {
                        viewModel.openCreditConfirmation()
                    },
                    onConfirmCreditRender = {
                        viewModel.confirmRealisticPreview()
                    },
                    onDismissCreditDialog = {
                        viewModel.dismissCreditConfirmation()
                    },
                    onBack = { viewModel.navigateBack() },
                    onOpenShoppingList = { viewModel.navigateTo(Screen.ShoppingList) },
                    onOpenCreditWallet = { viewModel.navigateTo(Screen.CreditWallet) }
                )
            }
        }

        is Screen.ShoppingList -> {
            ShoppingListScreen(
                items = cartItems,
                scenes = scenes,
                totalSelectedPrice = totalCartPrice,
                targetBudget = activeScene?.targetBudget ?: 3000000L,
                activeAffiliatePreview = activeAffiliatePreview,
                onToggleItem = { viewModel.toggleCartItem(it) },
                onUpdateQuantity = { itemId, qty -> viewModel.updateCartQuantity(itemId, qty) },
                onRemoveItem = { itemId -> viewModel.removeCartItem(itemId) },
                onProductClick = { prod -> viewModel.navigateTo(Screen.ProductDetail(prod.id)) },
                onTriggerAffiliateClick = { prod, sId, plId ->
                    viewModel.triggerAffiliateAttribution(prod, sId, plId)
                },
                onDismissAffiliateDialog = {
                    viewModel.dismissAffiliatePreview()
                },
                onBack = { viewModel.navigateBack() }
            )
        }

        is Screen.CreditWallet -> {
            CreditWalletScreen(
                balance = balance,
                transactions = transactions,
                onBuyPackage = { pkgId -> viewModel.buyCredits(pkgId) },
                onBack = { viewModel.navigateBack() }
            )
        }
    }
}
