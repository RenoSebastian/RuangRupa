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
import com.example.ruangai.domain.model.Product
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
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val totalCartPrice by viewModel.totalSelectedCartPrice.collectAsStateWithLifecycle()
    val balance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
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
    val selectedStyle by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val selectedSortOption by viewModel.selectedSortOption.collectAsStateWithLifecycle()
    val maxPriceFilter by viewModel.maxPriceFilter.collectAsStateWithLifecycle()
    val comparisonProductIds by viewModel.comparisonProductIds.collectAsStateWithLifecycle()
    val comparisonProducts by viewModel.comparisonProducts.collectAsStateWithLifecycle()
    val activeAffiliateInfo by viewModel.activeAffiliateInfo.collectAsStateWithLifecycle()

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
                onTimeout = { next -> viewModel.navigateTo(next) }
            )
        }

        is Screen.Onboarding -> {
            OnboardingScreen(
                onFinish = { next -> viewModel.navigateTo(next) }
            )
        }

        is Screen.Auth -> {
            AuthScreen(
                onAuthSuccess = { next -> viewModel.navigateTo(next) }
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
                                userName = "Dimas",
                                creditCount = balance.available,
                                scenes = scenes,
                                products = products,
                                onOpenScanner = { viewModel.navigateTo(Screen.RoomScanner) },
                                onOpenConsultation = { viewModel.navigateTo(Screen.Consultation()) },
                                onOpenStyleExplorer = { viewModel.navigateTo(Screen.StyleExplorer) },
                                onOpenTryOn = { sceneId ->
                                    viewModel.selectScene(sceneId)
                                    viewModel.navigateTo(Screen.VirtualTryOn(sceneId))
                                },
                                onOpenDesignResults = { sceneId ->
                                    viewModel.selectScene(sceneId)
                                    viewModel.navigateTo(Screen.RoomDetail(sceneId))
                                },
                                onOpenProductDetail = { prod ->
                                    viewModel.navigateTo(Screen.ProductDetail(prod.id))
                                },
                                onOpenCreditWallet = { viewModel.navigateTo(Screen.CreditWallet) }
                            )
                        }

                        BottomTab.EXPLORE -> {
                            StyleExplorerScreen(
                                onSelectStyle = { style ->
                                    viewModel.navigateTo(Screen.RoomScanner)
                                }
                            )
                        }

                        BottomTab.DESIGN -> {
                            // Primary elevated tab directly opens scanner flow
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
                                selectedStyle = selectedStyle,
                                selectedSortOption = selectedSortOption,
                                maxPriceFilter = maxPriceFilter,
                                searchQuery = searchQuery,
                                comparisonProductIds = comparisonProductIds,
                                onCategorySelect = { viewModel.setCategory(it) },
                                onStyleSelect = { viewModel.setStyleFilter(it) },
                                onSortOptionSelect = { viewModel.setSortOption(it) },
                                onMaxPriceSelect = { viewModel.setMaxPriceFilter(it) },
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onProductClick = { prod ->
                                    viewModel.navigateTo(Screen.ProductDetail(prod.id))
                                },
                                onTryOnClick = { prod ->
                                    val currentSceneId = activeScene?.id ?: scenes.firstOrNull()?.id ?: "demo_scene_kost_01"
                                    viewModel.addProductToScene(currentSceneId, prod)
                                },
                                onToggleCompare = { prod -> viewModel.toggleCompareProduct(prod) },
                                onOpenComparison = { viewModel.navigateTo(Screen.ProductComparison(comparisonProductIds)) },
                                onClearComparison = { viewModel.clearComparison() },
                                onOpenCart = { viewModel.navigateTo(Screen.ShoppingList) },
                                cartItemCount = cartItems.size
                            )
                        }

                        BottomTab.PROFILE -> {
                            ProfileScreen(
                                userSession = null,
                                savedProjectsCount = scenes.size,
                                creditCount = balance.available,
                                onOpenWallet = { viewModel.navigateTo(Screen.CreditWallet) },
                                onLogout = { viewModel.navigateTo(Screen.Auth) },
                                onDeleteAccount = { viewModel.navigateTo(Screen.Auth) }
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
                    viewModel.selectTab(BottomTab.SHOPPING)
                }
            )
        }

        is Screen.StyleExplorer -> {
            StyleExplorerScreen(
                onSelectStyle = { style ->
                    viewModel.navigateTo(Screen.RoomScanner)
                },
                onBack = { viewModel.navigateBack() }
            )
        }

        is Screen.DesignResults -> {
            val scene = scenes.find { it.id == screen.sceneId } ?: scenes.firstOrNull()
            if (scene != null) {
                DesignResultsScreen(
                    scene = scene,
                    onBack = { viewModel.navigateBack() },
                    onOpenTryOn = { sId ->
                        viewModel.selectScene(sId)
                        viewModel.navigateTo(Screen.VirtualTryOn(sId))
                    },
                    onOpenShopping = { viewModel.selectTab(BottomTab.SHOPPING) },
                    onRegenerate = { viewModel.openCreditConfirmation() }
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
                }
            )
        }

        is Screen.ProductDetail -> {
            val product = products.find { it.id == screen.productId } ?: products.first()
            ProductDetailScreen(
                product = product,
                onBack = { viewModel.navigateBack() },
                onTryOnInMyRoom = { prod ->
                    val sId = activeScene?.id ?: scenes.firstOrNull()?.id ?: "demo_scene_kost_01"
                    viewModel.addProductToScene(sId, prod)
                },
                onAddToCart = { prod ->
                    viewModel.addToCart(prod)
                }
            )
        }

        is Screen.VirtualTryOn -> {
            val scene = scenes.find { it.id == screen.sceneId } ?: scenes.firstOrNull()
            if (scene != null) {
                VirtualTryOnScreen(
                    scene = scene,
                    selectedPlacementId = selectedPlacementId,
                    allProducts = products,
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
                    onOpenShoppingList = { viewModel.navigateTo(Screen.ShoppingList) }
                )
            }
        }

        is Screen.ShoppingList -> {
            ShoppingListScreen(
                items = cartItems,
                totalSelectedPrice = totalCartPrice,
                targetBudget = activeScene?.targetBudget ?: 5000000L,
                onToggleItem = { viewModel.toggleCartItem(it) },
                onUpdateQuantity = { pId, qty -> viewModel.updateCartQuantity(pId, qty) },
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
