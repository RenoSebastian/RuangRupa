package com.example.ruangai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruangai.data.repository.mock.*
import com.example.ruangai.domain.model.*
import com.example.ruangai.domain.repository.*
import com.example.ruangai.ui.navigation.BottomTab
import com.example.ruangai.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class RuangAiViewModel(
    private val authRepo: AuthRepository = MockAuthRepository(),
    private val roomSceneRepo: RoomSceneRepository = MockRoomSceneRepository(),
    private val analysisRepo: RoomAnalysisRepository = MockRoomAnalysisRepository(),
    private val imageGenRepo: ImageGenerationRepository = MockImageGenerationRepository(),
    private val productRepo: ProductCatalogRepository = MockProductCatalogRepository(),
    private val affiliateRepo: AffiliateLinkRepository = MockAffiliateLinkRepository(),
    private val creditRepo: CreditWalletRepository = MockCreditWalletRepository(),
    private val shoppingCartRepo: ShoppingCartRepository = MockShoppingCartRepository(),
    private val compositor: com.example.ruangai.data.local.graphics.SceneCompositor? = null
) : ViewModel() {

    // Navigation Stack
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeTab = MutableStateFlow(BottomTab.HOME)
    val activeTab: StateFlow<BottomTab> = _activeTab.asStateFlow()

    private val screenBackStack = mutableListOf<Screen>()
    private val tabHistory = mutableListOf<BottomTab>()

    // Navigation Methods
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun selectTab(tab: BottomTab) {
        if (_activeTab.value != tab) {
            tabHistory.add(_activeTab.value)
            _activeTab.value = tab
        }
        _currentScreen.value = Screen.Main(tab)
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            val prev = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = prev
            if (prev is Screen.Main) {
                _activeTab.value = prev.activeTab
            }
            return true
        } else if (tabHistory.isNotEmpty()) {
            val prevTab = tabHistory.removeAt(tabHistory.size - 1)
            _activeTab.value = prevTab
            _currentScreen.value = Screen.Main(prevTab)
            return true
        } else if (_activeTab.value != BottomTab.HOME) {
            _activeTab.value = BottomTab.HOME
            _currentScreen.value = Screen.Main(BottomTab.HOME)
            return true
        }
        return false
    }

    fun handleDeepLink(uri: android.net.Uri): Boolean {
        val destination = Screen.parseDeepLinkUri(uri)
        if (destination != null) {
            navigateTo(destination)
            return true
        }
        return false
    }

    val authState = authRepo.getAuthState().stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Initial)
    val roomScenes = roomSceneRepo.getRoomScenes().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val walletBalance = creditRepo.getWalletBalance().stateIn(viewModelScope, SharingStarted.Eagerly, CreditBalance())
    val transactions = creditRepo.getTransactions().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Product Filter & Catalog State
    private val _selectedCategory = MutableStateFlow(ProductCategory.ALL)
    val selectedCategory: StateFlow<ProductCategory> = _selectedCategory.asStateFlow()

    private val _selectedStyle = MutableStateFlow<DesignStyle?>(null)
    val selectedStyle: StateFlow<DesignStyle?> = _selectedStyle.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(ProductSortOption.RELEVANCE)
    val selectedSortOption: StateFlow<ProductSortOption> = _selectedSortOption.asStateFlow()

    private val _maxPriceFilter = MutableStateFlow<Long?>(null)
    val maxPriceFilter: StateFlow<Long?> = _maxPriceFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val products: StateFlow<List<Product>> = combine(
        _selectedCategory,
        _selectedStyle,
        _selectedSortOption,
        _maxPriceFilter,
        _searchQuery
    ) { category, style, sort, maxPrice, query ->
        val all = MockData.products
        val filtered = all.filter { p ->
            (category == ProductCategory.ALL || p.category == category) &&
            (style == null || p.designStyle == style) &&
            (maxPrice == null || p.priceIdr <= maxPrice) &&
            (query.isBlank() ||
             p.title.contains(query, ignoreCase = true) ||
             p.category.displayName.contains(query, ignoreCase = true) ||
             p.brand.contains(query, ignoreCase = true) ||
             p.material.contains(query, ignoreCase = true) ||
             p.color.contains(query, ignoreCase = true))
        }
        when (sort) {
            ProductSortOption.RELEVANCE -> filtered
            ProductSortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.priceIdr }
            ProductSortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.priceIdr }
            ProductSortOption.POPULARITY -> filtered.sortedByDescending { it.rating }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MockData.products)

    // Product Comparison State (Max 3 items)
    private val _comparisonProductIds = MutableStateFlow<List<String>>(emptyList())
    val comparisonProductIds: StateFlow<List<String>> = _comparisonProductIds.asStateFlow()

    val comparisonProducts: StateFlow<List<Product>> = combine(
        _comparisonProductIds
    ) { (ids) ->
        ids.mapNotNull { id -> MockData.products.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Affiliate Link & Outbound Tracking State
    private val _activeAffiliateInfo = MutableStateFlow<AffiliateRedirectInfo?>(null)
    val activeAffiliateInfo: StateFlow<AffiliateRedirectInfo?> = _activeAffiliateInfo.asStateFlow()

    // Active Room Scene for Try-On & Details
    private val _activeSceneId = MutableStateFlow<String>("demo_scene_kost_01")
    val activeSceneId: StateFlow<String> = _activeSceneId.asStateFlow()

    val activeScene: StateFlow<RoomScene?> = combine(
        roomScenes,
        _activeSceneId
    ) { scenes, id ->
        scenes.find { it.id == id } ?: scenes.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Shopping Cart / Project Shopping List
    private val _cartItems = MutableStateFlow<List<ShoppingCartItem>>(
        listOf(
            ShoppingCartItem(MockData.products[0], 1, isSelected = true, associatedSceneId = "demo_scene_kost_01"),
            ShoppingCartItem(MockData.products[2], 1, isSelected = true, associatedSceneId = "demo_scene_kost_01")
        )
    )
    val cartItems: StateFlow<List<ShoppingCartItem>> = _cartItems.asStateFlow()

    val totalSelectedCartPrice: StateFlow<Long> = _cartItems.map { list ->
        list.filter { it.isSelected }.sumOf { it.product.priceIdr * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    // AI Consultation Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "msg_1",
                isFromUser = false,
                text = "Halo! Saya Konsultan Desain RuangAI. Saya siap membantu menata kamar atau memilih furnitur yang pas untuk budget dan ruangan Anda. Apa yang ingin Anda ubah hari ini?",
                suggestedActions = listOf(
                    "Rekomendasi sofa kamar kost",
                    "Tema Japandi budget Rp 2 juta",
                    "Pilih warna dinding cerah"
                )
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Scanner / Upload Flow State
    private val _scannerStep = MutableStateFlow(0) // 0: select/camera, 1: preview & type, 2: analyzing, 3: done
    val scannerStep: StateFlow<Int> = _scannerStep.asStateFlow()

    private val _scannerAnalysisResult = MutableStateFlow<RoomAnalysisResult?>(null)
    val scannerAnalysisResult: StateFlow<RoomAnalysisResult?> = _scannerAnalysisResult.asStateFlow()

    private val _scannerProgressLabel = MutableStateFlow("Mengunggah foto ruangan...")
    val scannerProgressLabel: StateFlow<String> = _scannerProgressLabel.asStateFlow()

    // Selected placement ID in Try-On canvas
    private val _selectedPlacementId = MutableStateFlow<String?>(null)
    val selectedPlacementId: StateFlow<String?> = _selectedPlacementId.asStateFlow()

    // Credit Confirmation Dialog State
    private val _showCreditDialog = MutableStateFlow(false)
    val showCreditDialog: StateFlow<Boolean> = _showCreditDialog.asStateFlow()

    private val _isGeneratingPreview = MutableStateFlow(false)
    val isGeneratingPreview: StateFlow<Boolean> = _isGeneratingPreview.asStateFlow()

    private val _previewSuccessNotice = MutableStateFlow<String?>(null)
    val previewSuccessNotice: StateFlow<String?> = _previewSuccessNotice.asStateFlow()

    // Active Scene Selection
    fun selectScene(sceneId: String) {
        _activeSceneId.value = sceneId
    }

    // Try-On Multi-Placement Management
    fun selectPlacement(placementId: String?) {
        _selectedPlacementId.value = placementId
    }

    fun updatePlacementTransform(placementId: String, transform: PlacementTransform) {
        val sceneId = _activeSceneId.value
        viewModelScope.launch {
            roomSceneRepo.updatePlacementTransform(sceneId, placementId, transform)
        }
    }

    // Undo / Redo History Stack for Scene Placements
    private val placementUndoStack = mutableListOf<List<ProductPlacement>>()
    private val placementRedoStack = mutableListOf<List<ProductPlacement>>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _isGridEnabled = MutableStateFlow(false)
    val isGridEnabled: StateFlow<Boolean> = _isGridEnabled.asStateFlow()

    private val _lastExportResult = MutableStateFlow<com.example.ruangai.data.local.graphics.CompositeExportResult?>(null)
    val lastExportResult: StateFlow<com.example.ruangai.data.local.graphics.CompositeExportResult?> = _lastExportResult.asStateFlow()

    private fun pushUndoSnapshot() {
        val current = activeScene.value?.placements ?: emptyList()
        placementUndoStack.add(current)
        placementRedoStack.clear()
        _canUndo.value = placementUndoStack.isNotEmpty()
        _canRedo.value = false
    }

    fun undo() {
        val sceneId = _activeSceneId.value
        if (placementUndoStack.isNotEmpty()) {
            val current = activeScene.value?.placements ?: emptyList()
            placementRedoStack.add(current)
            val prev = placementUndoStack.removeAt(placementUndoStack.size - 1)
            viewModelScope.launch {
                roomSceneRepo.updatePlacements(sceneId, prev)
                _canUndo.value = placementUndoStack.isNotEmpty()
                _canRedo.value = true
            }
        }
    }

    fun redo() {
        val sceneId = _activeSceneId.value
        if (placementRedoStack.isNotEmpty()) {
            val current = activeScene.value?.placements ?: emptyList()
            placementUndoStack.add(current)
            val next = placementRedoStack.removeAt(placementRedoStack.size - 1)
            viewModelScope.launch {
                roomSceneRepo.updatePlacements(sceneId, next)
                _canUndo.value = true
                _canRedo.value = placementRedoStack.isNotEmpty()
            }
        }
    }

    fun toggleGrid() {
        _isGridEnabled.value = !_isGridEnabled.value
    }

    fun bringPlacementToFront(sceneId: String, placementId: String) {
        val scene = activeScene.value ?: return
        pushUndoSnapshot()
        val maxZ = (scene.placements.maxOfOrNull { it.transform.zIndex } ?: 0) + 1
        val updated = scene.placements.map {
            if (it.placementId == placementId) it.copy(transform = it.transform.copy(zIndex = maxZ)) else it
        }
        viewModelScope.launch {
            roomSceneRepo.updatePlacements(sceneId, updated)
        }
    }

    fun sendPlacementToBack(sceneId: String, placementId: String) {
        val scene = activeScene.value ?: return
        pushUndoSnapshot()
        val minZ = (scene.placements.minOfOrNull { it.transform.zIndex } ?: 0) - 1
        val updated = scene.placements.map {
            if (it.placementId == placementId) it.copy(transform = it.transform.copy(zIndex = minZ)) else it
        }
        viewModelScope.launch {
            roomSceneRepo.updatePlacements(sceneId, updated)
        }
    }

    fun resetPlacementTransform(sceneId: String, placementId: String) {
        pushUndoSnapshot()
        updatePlacementTransform(placementId, PlacementTransform(translationX = 0f, translationY = 0f, scale = 1.0f, rotationDegrees = 0f))
    }

    fun exportFlattenedComposite(sceneId: String) {
        val comp = compositor ?: return
        val scene = activeScene.value ?: return
        viewModelScope.launch {
            try {
                val res = comp.compositeScene(scene)
                _lastExportResult.value = res
                _previewSuccessNotice.value = "Komposisi berhasil diekspor & disimpan ke galeri lokal!"
                delay(3500)
                _previewSuccessNotice.value = null
            } catch (e: Exception) {
                _previewSuccessNotice.value = "Gagal mengekspor komposisi: ${e.message}"
                delay(3500)
                _previewSuccessNotice.value = null
            }
        }
    }

    fun addProductToScene(sceneId: String, product: Product) {
        viewModelScope.launch {
            pushUndoSnapshot()
            val existingPlacements = activeScene.value?.placements ?: emptyList()
            val nextZ = (existingPlacements.maxOfOrNull { it.transform.zIndex } ?: 0) + 1
            val placement = ProductPlacement(
                placementId = "place_${UUID.randomUUID().toString().take(6)}",
                productId = product.id,
                productTitle = product.title,
                productPriceIdr = product.priceIdr,
                categoryName = product.category.displayName,
                referenceImage = ProductReferenceImage(
                    "ref_${UUID.randomUUID().toString().take(6)}",
                    product.id,
                    product.transparentCutoutUrl,
                    product.dimensions
                ),
                transform = PlacementTransform(translationX = 0f, translationY = 40f, scale = 1.0f, rotationDegrees = 0f, zIndex = nextZ)
            )
            roomSceneRepo.addPlacement(sceneId, placement)
            _selectedPlacementId.value = placement.placementId
            _activeSceneId.value = sceneId
            navigateTo(Screen.VirtualTryOn(sceneId))
        }
    }

    fun removePlacement(sceneId: String, placementId: String) {
        viewModelScope.launch {
            pushUndoSnapshot()
            roomSceneRepo.removePlacement(sceneId, placementId)
            if (_selectedPlacementId.value == placementId) {
                _selectedPlacementId.value = null
            }
        }
    }

    fun replacePlacementProduct(sceneId: String, placementId: String, newProduct: Product) {
        viewModelScope.launch {
            val scene = activeScene.value ?: return@launch
            pushUndoSnapshot()
            val updated = scene.placements.map { p ->
                if (p.placementId == placementId) {
                    p.copy(
                        productId = newProduct.id,
                        productTitle = newProduct.title,
                        productPriceIdr = newProduct.priceIdr,
                        categoryName = newProduct.category.displayName,
                        referenceImage = ProductReferenceImage(
                            "ref_${UUID.randomUUID().toString().take(6)}",
                            newProduct.id,
                            newProduct.transparentCutoutUrl,
                            newProduct.dimensions
                        )
                    )
                } else p
            }
            roomSceneRepo.updatePlacements(sceneId, updated)
        }
    }

    // Realistic AI Inpainting Confirmation Flow
    fun openCreditConfirmation() {
        _showCreditDialog.value = true
    }

    fun dismissCreditConfirmation() {
        _showCreditDialog.value = false
    }

    fun confirmRealisticPreview(jobMode: JobMode = JobMode.PRODUCT_PLACEMENT) {
        _showCreditDialog.value = false
        _isGeneratingPreview.value = true
        val jobId = "job_${UUID.randomUUID().toString().take(6)}"

        viewModelScope.launch {
            creditRepo.reserveCredit(jobId)
            delay(1800) // Simulate compositing and inpainting
            creditRepo.captureCredit(jobId)
            _isGeneratingPreview.value = false
            _previewSuccessNotice.value = "Pratinjau AI Realistis berhasil dibuat! (1 Kredit digunakan)"
            delay(3500)
            _previewSuccessNotice.value = null
        }
    }

    // Cart Management
    fun toggleCartItem(productId: String) {
        _cartItems.value = _cartItems.value.map { item ->
            if (item.product.id == productId) item.copy(isSelected = !item.isSelected) else item
        }
    }

    fun updateCartQuantity(productId: String, newQty: Int) {
        if (newQty <= 0) {
            _cartItems.value = _cartItems.value.filterNot { it.product.id == productId }
        } else {
            _cartItems.value = _cartItems.value.map { item ->
                if (item.product.id == productId) item.copy(quantity = newQty) else item
            }
        }
    }

    fun addToCart(product: Product) {
        val existing = _cartItems.value.find { it.product.id == product.id }
        if (existing != null) {
            updateCartQuantity(product.id, existing.quantity + 1)
        } else {
            _cartItems.value = _cartItems.value + ShoppingCartItem(product, 1, true, _activeSceneId.value)
        }
    }

    // AI Chat (Context-Aware Interior Consultant with Real Product & Style Recommendations)
    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage("msg_${UUID.randomUUID().toString().take(6)}", true, userText)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            delay(800)
            val currentScene = activeScene.value
            val roomLabel = currentScene?.title ?: "Ruangan Anda"
            val allProds = MockData.products

            val (replyText, recIds, recStyle) = when {
                userText.contains("sofa", ignoreCase = true) -> {
                    val sofaProds = allProds.filter { it.category == ProductCategory.SOFA }.take(2).map { it.id }
                    Triple(
                        "Untuk $roomLabel, saya merekomendasikan Sofa 2-Seater Fabric Japandi Beige (140x75x80 cm). Kaki kayunya ramping sehingga menjaga proporsi lantai tetap lega. Harganya Rp 1.450.000.",
                        sofaProds,
                        DesignStyle.JAPANDI
                    )
                }
                userText.contains("budget", ignoreCase = true) || userText.contains("juta", ignoreCase = true) -> {
                    val budgetProds = allProds.filter { it.priceIdr <= 500_000L }.take(3).map { it.id }
                    Triple(
                        "Untuk optimasi budget di $roomLabel, prioritaskan 3 elemen fungsional: Meja Kopi Bundar (Rp 420.000), Karpet Nordik Lembut (Rp 310.000), dan Lampu Lantai Standing (Rp 285.000). Total paket hanya Rp 1.015.000.",
                        budgetProds,
                        DesignStyle.SCANDINAVIAN
                    )
                }
                userText.contains("tropis", ignoreCase = true) || userText.contains("tropical", ignoreCase = true) -> {
                    val tropProds = allProds.filter { it.designStyle == DesignStyle.TROPICAL || it.material.contains("Rotan", ignoreCase = true) || it.material.contains("Jati", ignoreCase = true) }.take(2).map { it.id }.ifEmpty { allProds.take(2).map { it.id } }
                    Triple(
                        "Gaya Tropical Modern sangat cocok untuk iklim Indonesia! Kombinasikan material rotan alami, kayu jati hangat, dan sirkulasi cahaya jendela yang lega di $roomLabel.",
                        tropProds,
                        DesignStyle.TROPICAL
                    )
                }
                userText.contains("minimalis", ignoreCase = true) || userText.contains("minimalist", ignoreCase = true) -> {
                    val minProds = allProds.filter { it.designStyle == DesignStyle.MINIMALIST || it.category == ProductCategory.TABLE }.take(2).map { it.id }
                    Triple(
                        "Prinsip Minimalist di $roomLabel mengutamakan garis geometris bersih dan palet warna monokromatis hangat (#F8F7F4) tanpa clutter visual.",
                        minProds,
                        DesignStyle.MINIMALIST
                    )
                }
                userText.contains("industrial", ignoreCase = true) -> {
                    val indProds = allProds.filter { it.designStyle == DesignStyle.INDUSTRIAL || it.category == ProductCategory.LIGHTING }.take(2).map { it.id }
                    Triple(
                        "Untuk aksen Industrial yang berkarakter di $roomLabel, padukan rangka besi hitam matte dengan kayu bertekstur hangat dan pencahayaan warm 3000K.",
                        indProds,
                        DesignStyle.INDUSTRIAL
                    )
                }
                userText.contains("japandi", ignoreCase = true) -> {
                    val japProds = allProds.filter { it.designStyle == DesignStyle.JAPANDI }.take(2).map { it.id }
                    Triple(
                        "Gaya Japandi memadukan kehangatan kayu pinus alami dengan tekstil linen krem. Anda bisa langsung mencoba penataan produk Japandi di $roomLabel melalui Virtual Try-On 2D!",
                        japProds,
                        DesignStyle.JAPANDI
                    )
                }
                else -> {
                    val defaultProds = allProds.take(2).map { it.id }
                    Triple(
                        "Berdasarkan analisis pencahayaan dan proporsi $roomLabel, kombinasi warna netral hangat dengan furnitur berkaki ramping akan membuat ruangan terasa 30% lebih lapang. Berikut produk terkurasi yang cocok:",
                        defaultProds,
                        DesignStyle.JAPANDI
                    )
                }
            }

            val aiMsg = ChatMessage(
                id = "msg_${UUID.randomUUID().toString().take(6)}",
                isFromUser = false,
                text = replyText,
                suggestedActions = listOf("Coba Furnitur di Ruangan", "Lihat Makeover Penuh", "Lihat Daftar Belanja"),
                recommendedProductIds = recIds,
                recommendedStyle = recStyle
            )
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiThinking.value = false
        }
    }

    fun applyStyleAndOpenMakeover(style: DesignStyle) {
        val targetSceneId = _activeSceneId.value.ifBlank { "demo_scene_kost_01" }
        _activeSceneId.value = targetSceneId
        navigateTo(Screen.DesignResults(targetSceneId))
    }

    // Scanner Simulation
    fun startRoomScan(imageUri: String, roomType: RoomType, title: String) {
        _scannerStep.value = 2 // Analyzing
        viewModelScope.launch {
            _scannerProgressLabel.value = "Mengunggah foto resolusi optimal (1280px)..."
            delay(700)
            _scannerProgressLabel.value = "Memetakan batas dinding, lantai & jendela..."
            delay(800)
            _scannerProgressLabel.value = "Mendeteksi furnitur & sumber pencahayaan..."
            delay(800)
            val sceneRes = roomSceneRepo.createRoomScene(title, roomType, imageUri)
            val scene = sceneRes.getOrNull()
            if (scene != null) {
                _activeSceneId.value = scene.id
                val analysis = analysisRepo.analyzeRoom(scene.id, imageUri)
                _scannerAnalysisResult.value = analysis.getOrNull()
                _scannerStep.value = 3 // Done
            }
        }
    }

    fun deleteRoomScene(sceneId: String) {
        viewModelScope.launch {
            roomSceneRepo.deleteRoomScene(sceneId)
            if (_activeSceneId.value == sceneId) {
                val remaining = roomScenes.value.filterNot { it.id == sceneId }
                _activeSceneId.value = remaining.firstOrNull()?.id ?: ""
            }
            navigateBack()
        }
    }

    fun updateRoomSceneMetadata(sceneId: String, title: String, type: RoomType, targetBudget: Long) {
        viewModelScope.launch {
            roomSceneRepo.updateRoomSceneMetadata(sceneId, title, type, targetBudget)
        }
    }

    fun loadRoomAnalysis(sceneId: String) {
        viewModelScope.launch {
            val cached = analysisRepo.getCachedAnalysis(sceneId)
            if (cached != null) {
                _scannerAnalysisResult.value = cached
            } else {
                val scene = roomScenes.value.find { it.id == sceneId }
                val uri = scene?.originalImage?.localUri ?: ""
                val analysis = analysisRepo.analyzeRoom(sceneId, uri)
                _scannerAnalysisResult.value = analysis.getOrNull()
            }
        }
    }

    fun resetScanner() {
        _scannerStep.value = 0
        _scannerAnalysisResult.value = null
    }

    // Credit Packages
    fun buyCredits(pkgId: String) {
        viewModelScope.launch {
            creditRepo.purchaseCreditPackage(pkgId)
            _previewSuccessNotice.value = "Top up kredit berhasil! Saldo telah ditambahkan."
            delay(3000)
            _previewSuccessNotice.value = null
        }
    }

    // Filter & Sort Products
    fun setCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    fun setStyleFilter(style: DesignStyle?) {
        _selectedStyle.value = style
    }

    fun setSortOption(option: ProductSortOption) {
        _selectedSortOption.value = option
    }

    fun setMaxPriceFilter(price: Long?) {
        _maxPriceFilter.value = price
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Product Comparison Management (Max 3 items)
    fun toggleCompareProduct(product: Product) {
        val current = _comparisonProductIds.value
        if (product.id in current) {
            _comparisonProductIds.value = current - product.id
        } else {
            if (current.size >= 3) {
                // Replace the oldest item if already 3
                _comparisonProductIds.value = current.drop(1) + product.id
            } else {
                _comparisonProductIds.value = current + product.id
            }
        }
    }

    fun removeFromComparison(productId: String) {
        _comparisonProductIds.value = _comparisonProductIds.value - productId
    }

    fun clearComparison() {
        _comparisonProductIds.value = emptyList()
    }

    // Related Products Helper
    fun getRelatedProducts(productId: String): List<Product> {
        val current = MockData.products.find { it.id == productId } ?: return MockData.products.take(4)
        return MockData.products
            .filter { it.id != productId && (it.category == current.category || it.designStyle == current.designStyle) }
            .take(4)
            .ifEmpty { MockData.products.filter { it.id != productId }.take(4) }
    }

    // Affiliate Attribution Management
    fun prepareAffiliateRedirect(product: Product, placementId: String? = null) {
        viewModelScope.launch {
            val res = affiliateRepo.getAffiliateLink(product.id, _activeSceneId.value, placementId)
            _activeAffiliateInfo.value = res.getOrNull()
        }
    }

    fun recordAffiliateClick(product: Product) {
        viewModelScope.launch {
            affiliateRepo.recordAffiliateClick(
                productId = product.id,
                marketplace = product.marketplace.name,
                trackingMetadata = _activeAffiliateInfo.value?.trackingMetadata
            )
        }
    }
}
