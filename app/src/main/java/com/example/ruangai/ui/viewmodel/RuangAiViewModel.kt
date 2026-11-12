package com.example.ruangai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruangai.data.repository.mock.*
import com.example.ruangai.domain.model.*
import com.example.ruangai.domain.repository.*
import com.example.ruangai.ui.navigation.BottomTab
import com.example.ruangai.ui.navigation.Screen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class RuangAiViewModel(
    private val authRepo: AuthRepository = MockAuthRepository(),
    private val roomSceneRepo: RoomSceneRepository = MockRoomSceneRepository(),
    private val analysisRepo: RoomAnalysisRepository = MockRoomAnalysisRepository(),
    private val imageGenRepo: ImageGenerationRepository = MockImageGenerationRepository(),
    private val productRepo: ProductCatalogRepository = MockProductCatalogRepository(),
    private val shoppingListRepo: ShoppingListRepository = MockShoppingListRepository(),
    private val affiliateRepo: AffiliateLinkRepository = MockAffiliateLinkRepository(),
    private val creditRepo: CreditWalletRepository = MockCreditWalletRepository(),
    private val aiChatRepo: AIChatRepository = MockAIChatRepository(),
    private val aiRecommendationRepo: AIRecommendationRepository = ConsultantRecommendationEngine(productRepo),
    private val multimodalRepo: MultimodalInputRepository = MockMultimodalInputRepository(),
    private val aiInteriorDesignRepo: AIInteriorDesignRepository = MockAIInteriorDesignRepository(creditRepo),
    private val compositor: com.example.ruangai.data.local.graphics.SceneCompositor? = null,
    initialOnboardingCompleted: Boolean = false,
    private val onSaveOnboardingCompleted: ((Boolean) -> Unit)? = null
) : ViewModel() {

    // Onboarding State
    private val _isOnboardingCompleted = MutableStateFlow(initialOnboardingCompleted)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
        onSaveOnboardingCompleted?.invoke(true)
    }

    fun resetOnboardingState(isCompleted: Boolean = false) {
        _isOnboardingCompleted.value = isCompleted
        onSaveOnboardingCompleted?.invoke(isCompleted)
    }

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

    fun resolveInitialRoute(): Screen {
        val isAuthenticated = authState.value is AuthState.Authenticated
        return when {
            !_isOnboardingCompleted.value -> Screen.Onboarding
            isAuthenticated -> Screen.Main(BottomTab.HOME)
            else -> Screen.Auth
        }
    }

    fun resolvePostOnboardingRoute(): Screen {
        val isAuthenticated = authState.value is AuthState.Authenticated
        return if (isAuthenticated) Screen.Main(BottomTab.HOME) else Screen.Auth
    }

    val authState: StateFlow<AuthState> = authRepo.getAuthState().stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Initial)
    val currentUserSession: StateFlow<UserSession?> = authState.map {
        (it as? AuthState.Authenticated)?.session
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun loginWithEmail(email: String, pass: String, onResult: (Result<UserSession>) -> Unit = {}) {
        viewModelScope.launch {
            val res = authRepo.loginWithEmail(email, pass)
            onResult(res)
        }
    }

    fun registerWithEmail(email: String, pass: String, name: String, onResult: (Result<UserSession>) -> Unit = {}) {
        viewModelScope.launch {
            val res = authRepo.registerWithEmail(email, pass, name)
            onResult(res)
        }
    }

    fun signInWithGoogle(onResult: (Result<UserSession>) -> Unit = {}) {
        viewModelScope.launch {
            val res = authRepo.signInWithGoogle()
            onResult(res)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            screenBackStack.clear()
            tabHistory.clear()
            _activeTab.value = BottomTab.HOME
            _currentScreen.value = Screen.Auth
        }
    }
    val roomScenes = roomSceneRepo.getRoomScenes().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val walletBalance = creditRepo.getWalletBalance().stateIn(viewModelScope, SharingStarted.Eagerly, CreditBalance())
    val transactions = creditRepo.getTransactions().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Product Catalog Filter & Sort State
    private val _selectedCategory = MutableStateFlow(ProductCategory.ALL)
    val selectedCategory: StateFlow<ProductCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStyleFilter = MutableStateFlow<DesignStyle?>(null)
    val selectedStyleFilter: StateFlow<DesignStyle?> = _selectedStyleFilter.asStateFlow()

    private val _selectedPriceRange = MutableStateFlow(PriceRangeFilter.ALL)
    val selectedPriceRange: StateFlow<PriceRangeFilter> = _selectedPriceRange.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(ProductSortOption.RELEVANCE)
    val selectedSortOption: StateFlow<ProductSortOption> = _selectedSortOption.asStateFlow()

    val catalogFilter: StateFlow<ProductCatalogFilter> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedStyleFilter,
        _selectedPriceRange,
        _selectedSortOption
    ) { query, cat, style, priceRange, sort ->
        ProductCatalogFilter(
            searchQuery = query,
            category = cat,
            style = style,
            priceRange = priceRange,
            sortBy = sort
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ProductCatalogFilter())

    val products: StateFlow<List<Product>> = catalogFilter.flatMapLatest { filter ->
        productRepo.filterAndSortProducts(filter)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MockData.products)

    val allCatalogProducts: List<Product> = MockData.products

    // Product Comparison State (Up to 3 products)
    private val _comparedProductIds = MutableStateFlow<List<String>>(emptyList())
    val comparedProductIds: StateFlow<List<String>> = _comparedProductIds.asStateFlow()

    val comparedProducts: StateFlow<List<Product>> = _comparedProductIds.map { ids ->
        ids.mapNotNull { id -> MockData.products.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleProductComparison(productId: String) {
        val current = _comparedProductIds.value
        if (current.contains(productId)) {
            _comparedProductIds.value = current.filterNot { it == productId }
        } else if (current.size < 3) {
            _comparedProductIds.value = current + productId
        } else {
            // Replace oldest compared item if already at 3
            _comparedProductIds.value = current.drop(1) + productId
        }
    }

    fun clearProductComparison() {
        _comparedProductIds.value = emptyList()
    }

    fun getRelatedProducts(product: Product): List<Product> = productRepo.getRelatedProducts(product, 4)

    fun getAlternativeProducts(product: Product): List<Product> = productRepo.getAlternativeProducts(product, 4)

    // Active Room Scene for Try-On & Details
    private val _activeSceneId = MutableStateFlow("demo_scene_kost_01")
    val activeSceneId: StateFlow<String> = _activeSceneId.asStateFlow()

    val activeScene: StateFlow<RoomScene?> = combine(
        roomScenes,
        _activeSceneId
    ) { scenes, id ->
        scenes.find { it.id == id } ?: scenes.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Persistent Shopping Cart / Project Shopping List
    val cartItems: StateFlow<List<ShoppingCartItem>> = shoppingListRepo.getShoppingListItems()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val totalSelectedCartPrice: StateFlow<Long> = cartItems.map { list ->
        list.filter { it.isSelected }.sumOf { it.subtotalIdr }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    // Affiliate Attribution State
    private val _activeAffiliatePreview = MutableStateFlow<AffiliateRedirectInfo?>(null)
    val activeAffiliatePreview: StateFlow<AffiliateRedirectInfo?> = _activeAffiliatePreview.asStateFlow()

    val affiliateClickHistory: StateFlow<List<AffiliateClickRecord>> = affiliateRepo.getAffiliateClickHistory()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun triggerAffiliateAttribution(
        product: Product,
        sceneId: String? = null,
        placementId: String? = null
    ) {
        val resolvedSceneId = sceneId ?: _activeSceneId.value
        viewModelScope.launch {
            val res = affiliateRepo.getAffiliateLink(product.id, resolvedSceneId, placementId)
            val info = res.getOrNull()
            if (info != null) {
                affiliateRepo.recordStructuredAffiliateClick(info, product.title)
                _activeAffiliatePreview.value = info
            }
        }
    }

    fun dismissAffiliatePreview() {
        _activeAffiliatePreview.value = null
    }

    // AI Consultation Chat State (Room-Project Scoped & Persistent)
    val activeConversation: StateFlow<ConsultationConversation?> = _activeSceneId.flatMapLatest { sceneId ->
        aiChatRepo.observeConversationForScene(sceneId)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val chatMessages: StateFlow<List<ChatMessage>> = combine(
        _activeSceneId.flatMapLatest { sceneId -> aiChatRepo.observeMessagesForScene(sceneId) },
        activeScene
    ) { messages, scene ->
        if (messages.isNotEmpty()) {
            messages
        } else if (scene != null) {
            listOf(
                ConsultantRecommendationEngine().createInitialGreeting(scene, "conv_${scene.id}")
            )
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _pendingAttachments = MutableStateFlow<List<ChatAttachment>>(emptyList())
    val pendingAttachments: StateFlow<List<ChatAttachment>> = _pendingAttachments.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    init {
        viewModelScope.launch {
            activeScene.filterNotNull().collect { scene ->
                aiChatRepo.getOrCreateConversation(scene)
            }
        }
    }

    // Scanner / Upload Flow State
    private val _scannerStep = MutableStateFlow(0)
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

    // Active Scene Selection (Switches project & consultation context cleanly)
    fun selectScene(sceneId: String) {
        if (_activeSceneId.value != sceneId) {
            _pendingAttachments.value = emptyList()
            _chatError.value = null
            _activeSceneId.value = sceneId
        }
        viewModelScope.launch {
            val targetScene = roomScenes.value.find { it.id == sceneId }
            if (targetScene != null) {
                aiChatRepo.getOrCreateConversation(targetScene)
                loadRoomAnalysis(sceneId)
            }
        }
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
            val scene = roomScenes.value.find { it.id == sceneId } ?: activeScene.value ?: return@launch
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
            _selectedPlacementId.value = placementId
            _activeSceneId.value = sceneId
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
            delay(1800)
            creditRepo.captureCredit(jobId)
            _isGeneratingPreview.value = false
            _previewSuccessNotice.value = "Pratinjau AI Realistis berhasil dibuat! (1 Kredit digunakan)"
            delay(3500)
            _previewSuccessNotice.value = null
        }
    }

    // Persistent Shopping List Management
    fun toggleCartItem(itemIdOrProductId: String) {
        viewModelScope.launch {
            shoppingListRepo.toggleItemSelection(itemIdOrProductId)
        }
    }

    fun updateCartQuantity(itemIdOrProductId: String, newQty: Int) {
        viewModelScope.launch {
            shoppingListRepo.updateItemQuantity(itemIdOrProductId, newQty)
        }
    }

    fun removeCartItem(itemIdOrProductId: String) {
        viewModelScope.launch {
            shoppingListRepo.removeItem(itemIdOrProductId)
        }
    }

    fun addToCart(
        product: Product,
        sceneId: String? = _activeSceneId.value,
        placementId: String? = null
    ) {
        viewModelScope.launch {
            shoppingListRepo.addOrIncrementProduct(
                product = product,
                sceneId = sceneId,
                placementId = placementId,
                quantityToAdd = 1
            )
            _previewSuccessNotice.value = "${product.title} ditambahkan ke Shopping List"
            delay(2500)
            _previewSuccessNotice.value = null
        }
    }

    fun syncScenePlacementsToShoppingList(scene: RoomScene) {
        viewModelScope.launch {
            val res = shoppingListRepo.syncScenePlacementsToShoppingList(scene, MockData.products)
            val count = res.getOrDefault(0)
            _previewSuccessNotice.value = "$count furnitur ruangan disinkronkan ke Shopping List!"
            delay(3000)
            _previewSuccessNotice.value = null
        }
    }

    // AI Consultation & Contextual Multimodal Chat (Milestone 6)
    fun attachPhotoToConversation(
        sourceUriOrPath: String,
        label: String = "Foto Referensi Ruangan"
    ) {
        val sceneId = _activeSceneId.value
        val convId = activeConversation.value?.conversationId ?: "conv_$sceneId"
        viewModelScope.launch {
            val res = multimodalRepo.attachRoomPhoto(convId, sourceUriOrPath, label)
            val att = res.getOrNull()
            if (att != null) {
                _pendingAttachments.value = _pendingAttachments.value + att
            } else {
                _chatError.value = "Gagal melampirkan foto referensi."
            }
        }
    }

    fun removePendingAttachment(attachmentId: String) {
        _pendingAttachments.value = _pendingAttachments.value.filterNot { it.id == attachmentId }
    }

    fun dismissChatError() {
        _chatError.value = null
    }

    fun sendMessage(
        userText: String,
        explicitAttachments: List<ChatAttachment>? = null
    ) {
        val attachmentsToSend = explicitAttachments ?: _pendingAttachments.value
        if (userText.isBlank() && attachmentsToSend.isEmpty()) return

        val scene = activeScene.value ?: roomScenes.value.firstOrNull() ?: MockData.preloadedScenes.first()
        _isAiThinking.value = true
        _chatError.value = null
        _pendingAttachments.value = emptyList()

        viewModelScope.launch {
            try {
                // Detect style preference in prompt and persist on RoomScene as well
                val detectedStyle = aiRecommendationRepo.parseStyleFromPrompt(userText)
                if (detectedStyle != null) {
                    roomSceneRepo.updatePreferredStyle(scene.id, detectedStyle)
                }

                val cachedAnalysis = analysisRepo.getCachedAnalysis(scene.id) ?: _scannerAnalysisResult.value
                val currentScene = roomScenes.value.find { it.id == scene.id } ?: scene
                val context = ConsultationContext(
                    scene = currentScene,
                    conversation = activeConversation.value,
                    analysisResult = cachedAnalysis,
                    shoppingListItems = cartItems.value,
                    availableCatalog = allCatalogProducts
                )

                val result = aiChatRepo.sendContextualMessage(
                    context = context,
                    userText = userText,
                    attachments = attachmentsToSend
                )
                if (result.isFailure) {
                    _chatError.value = result.exceptionOrNull()?.message ?: "Gagal mengirim pesan konsultasi."
                }
            } catch (e: Exception) {
                _chatError.value = e.message ?: "Terjadi kesalahan pada simulasi konsultasi."
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun applyStylePreferenceToScene(
        style: DesignStyle,
        sceneId: String = _activeSceneId.value
    ) {
        viewModelScope.launch {
            roomSceneRepo.updatePreferredStyle(sceneId, style)
            aiChatRepo.updateConversationPreferences(
                sceneId = sceneId,
                selectedStyle = style,
                customBudgetIdr = null
            )
            _previewSuccessNotice.value = "Gaya ${style.displayName} disimpan sebagai preferensi ruangan!"
            delay(2500)
            _previewSuccessNotice.value = null
        }
    }

    fun updateSceneDimensionsAndBudget(
        sceneId: String,
        dimensionsCm: DimensionsCm?,
        targetBudgetIdr: Long
    ) {
        viewModelScope.launch {
            val current = roomScenes.value.find { it.id == sceneId }
            if (current != null) {
                roomSceneRepo.updateRoomSceneMetadata(sceneId, current.title, current.roomType, targetBudgetIdr)
            }
            roomSceneRepo.updateRoomDimensions(sceneId, dimensionsCm)
            aiChatRepo.updateConversationPreferences(
                sceneId = sceneId,
                selectedStyle = null,
                customBudgetIdr = targetBudgetIdr
            )
            _previewSuccessNotice.value = "Konteks ukuran & anggaran ruangan diperbarui!"
            delay(2500)
            _previewSuccessNotice.value = null
        }
    }

    fun clearCurrentConversation(sceneId: String = _activeSceneId.value) {
        viewModelScope.launch {
            aiChatRepo.clearConversationForScene(sceneId)
            _pendingAttachments.value = emptyList()
            _chatError.value = null
        }
    }

    fun deleteCurrentConversation(sceneId: String = _activeSceneId.value) {
        viewModelScope.launch {
            val convId = activeConversation.value?.conversationId ?: "conv_$sceneId"
            aiChatRepo.deleteConversation(convId)
            _pendingAttachments.value = emptyList()
            _chatError.value = null
        }
    }

    fun getStyleMoodboard(style: DesignStyle): StyleMoodboardProfile {
        return aiRecommendationRepo.getStyleMoodboardProfile(style)
    }

    fun getAllStyleMoodboards(): List<StyleMoodboardProfile> {
        return aiRecommendationRepo.getAllStyleMoodboardProfiles()
    }

    fun getRecommendedProductsForStyle(style: DesignStyle): List<Product> {
        val profile = aiRecommendationRepo.getStyleMoodboardProfile(style)
        val byId = profile.recommendedProductIds.mapNotNull { id ->
            allCatalogProducts.find { it.id == id }
        }
        return if (byId.isNotEmpty()) {
            byId
        } else {
            allCatalogProducts.filter { it.designStyle == style || it.compatibleStyles.contains(style) }
        }
    }

    // Scanner Simulation
    fun startRoomScan(imageUri: String, roomType: RoomType, title: String) {
        _scannerStep.value = 2
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
                _scannerStep.value = 3
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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStyleFilter(style: DesignStyle?) {
        _selectedStyleFilter.value = style
    }

    fun setPriceRangeFilter(range: PriceRangeFilter) {
        _selectedPriceRange.value = range
    }

    fun setSortOption(sort: ProductSortOption) {
        _selectedSortOption.value = sort
    }

    fun resetCatalogFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = ProductCategory.ALL
        _selectedStyleFilter.value = null
        _selectedPriceRange.value = PriceRangeFilter.ALL
        _selectedSortOption.value = ProductSortOption.RELEVANCE
    }

    // =========================================================================
    // MILESTONE 7: FULL ROOM MAKEOVER & AI DESIGN GENERATION
    // =========================================================================

    val designHistoryForActiveScene: StateFlow<List<DesignGenerationResult>> = _activeSceneId.flatMapLatest { sceneId ->
        aiInteriorDesignRepo.observeDesignHistoryForScene(sceneId)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteDesignVariations: StateFlow<List<DesignVariation>> = _activeSceneId.flatMapLatest { sceneId ->
        aiInteriorDesignRepo.observeFavoriteVariations(sceneId)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedDesignResultId = MutableStateFlow<String?>(null)
    val selectedDesignResultId: StateFlow<String?> = _selectedDesignResultId.asStateFlow()

    val activeDesignResult: StateFlow<DesignGenerationResult?> = combine(
        designHistoryForActiveScene,
        _selectedDesignResultId
    ) { history, selectedId ->
        history.find { it.resultId == selectedId } ?: history.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isGeneratingMakeover = MutableStateFlow(false)
    val isGeneratingMakeover: StateFlow<Boolean> = _isGeneratingMakeover.asStateFlow()

    private val _makeoverError = MutableStateFlow<String?>(null)
    val makeoverError: StateFlow<String?> = _makeoverError.asStateFlow()

    private val _lastFailedMakeoverRequestId = MutableStateFlow<String?>(null)
    val lastFailedMakeoverRequestId: StateFlow<String?> = _lastFailedMakeoverRequestId.asStateFlow()

    fun selectDesignResultFromHistory(resultId: String) {
        _selectedDesignResultId.value = resultId
    }

    fun dismissMakeoverError() {
        _makeoverError.value = null
    }

    fun generateFullRoomMakeover(
        sceneId: String = _activeSceneId.value,
        roomType: RoomType? = null,
        selectedStyle: DesignStyle,
        colorPaletteNames: List<String>,
        desiredAtmosphere: DesignAtmosphere = DesignAtmosphere.WARM_COZY,
        furniturePreferences: List<ProductCategory> = emptyList(),
        targetBudgetIdr: Long,
        additionalInstructions: String = "",
        idempotencyKey: String = "idem_${sceneId}_${selectedStyle.name}_${System.currentTimeMillis()}",
        simulateFailure: Boolean = false
    ) {
        val scene = roomScenes.value.find { it.id == sceneId }
            ?: activeScene.value
            ?: MockData.preloadedScenes.first()

        val resolvedRoomType = roomType ?: scene.roomType
        val resolvedPalette = colorPaletteNames.ifEmpty {
            getStyleMoodboard(selectedStyle).colorPaletteNamed.map { it.first }
        }

        val request = DesignGenerationRequest(
            requestId = "req_${UUID.randomUUID().toString().take(8)}",
            idempotencyKey = idempotencyKey,
            sceneId = scene.id,
            originalRoomImageUri = scene.originalImage.localUri,
            roomType = resolvedRoomType,
            selectedStyle = selectedStyle,
            colorPaletteNames = resolvedPalette,
            desiredAtmosphere = desiredAtmosphere,
            furniturePreferences = furniturePreferences,
            targetBudgetIdr = targetBudgetIdr,
            additionalInstructions = additionalInstructions,
            variationCount = 3,
            creditCost = 1,
            simulateFailure = simulateFailure
        )

        val validation = aiInteriorDesignRepo.validateConfiguration(request)
        if (!validation.isValid) {
            _makeoverError.value = validation.errors.joinToString(" ")
            return
        }

        _isGeneratingMakeover.value = true
        _makeoverError.value = null
        _lastFailedMakeoverRequestId.value = null

        viewModelScope.launch {
            try {
                // Persist preferred style & updated budget on scene context
                roomSceneRepo.updatePreferredStyle(scene.id, selectedStyle)
                roomSceneRepo.updateRoomSceneMetadata(scene.id, scene.title, resolvedRoomType, targetBudgetIdr)

                val res = aiInteriorDesignRepo.generateRoomMakeover(request)
                val generated = res.getOrNull()
                if (generated != null) {
                    _selectedDesignResultId.value = generated.resultId
                    _previewSuccessNotice.value = "Makeover AI (${selectedStyle.displayName}) selesai dengan ${generated.variations.size} variasi!"
                    navigateTo(Screen.DesignResults(scene.id))
                    delay(3000)
                    _previewSuccessNotice.value = null
                } else {
                    _lastFailedMakeoverRequestId.value = request.requestId
                    _makeoverError.value = res.exceptionOrNull()?.message
                        ?: "Gagal memproses makeover ruangan."
                }
            } catch (e: Exception) {
                _lastFailedMakeoverRequestId.value = request.requestId
                _makeoverError.value = e.message ?: "Terjadi kesalahan saat memproses makeover."
            } finally {
                _isGeneratingMakeover.value = false
            }
        }
    }

    fun retryLastFailedMakeover(requestId: String? = _lastFailedMakeoverRequestId.value) {
        val targetReqId = requestId ?: return
        _isGeneratingMakeover.value = true
        _makeoverError.value = null

        viewModelScope.launch {
            try {
                val res = aiInteriorDesignRepo.retryDesignGeneration(targetReqId)
                val generated = res.getOrNull()
                if (generated != null) {
                    _selectedDesignResultId.value = generated.resultId
                    _lastFailedMakeoverRequestId.value = null
                    _previewSuccessNotice.value = "Percobaan ulang Makeover AI berhasil!"
                    delay(3000)
                    _previewSuccessNotice.value = null
                } else {
                    _makeoverError.value = res.exceptionOrNull()?.message ?: "Gagal mencoba ulang makeover."
                }
            } finally {
                _isGeneratingMakeover.value = false
            }
        }
    }

    fun cancelMakeoverGeneration(requestId: String) {
        viewModelScope.launch {
            aiInteriorDesignRepo.cancelDesignGeneration(requestId)
            _isGeneratingMakeover.value = false
            _previewSuccessNotice.value = "Generasi desain dibatalkan & kredit telah dikembalikan."
            delay(2500)
            _previewSuccessNotice.value = null
        }
    }

    fun selectDesignVariation(resultId: String, variationId: String) {
        viewModelScope.launch {
            aiInteriorDesignRepo.selectActiveVariation(resultId, variationId)
        }
    }

    fun toggleFavoriteDesignVariation(variationId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            aiInteriorDesignRepo.toggleFavoriteVariation(variationId, isFavorite)
            _previewSuccessNotice.value = if (isFavorite) {
                "Variasi desain disimpan ke Favorit!"
            } else {
                "Variasi desain dihapus dari Favorit."
            }
            delay(2000)
            _previewSuccessNotice.value = null
        }
    }
}
