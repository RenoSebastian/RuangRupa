package com.example.ruangai.domain.repository

import com.example.ruangai.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getAuthState(): Flow<AuthState>
    suspend fun loginWithEmail(email: String, pass: String): Result<UserSession>
    suspend fun registerWithEmail(email: String, pass: String, name: String): Result<UserSession>
    suspend fun signInWithGoogle(): Result<UserSession>
    suspend fun logout(): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}

interface RoomSceneRepository {
    fun getRoomScenes(): Flow<List<RoomScene>>
    fun getRoomSceneById(sceneId: String): Flow<RoomScene?>
    suspend fun createRoomScene(title: String, type: RoomType, imageUri: String): Result<RoomScene>
    suspend fun updatePlacements(sceneId: String, placements: List<ProductPlacement>): Result<Unit>
    suspend fun addPlacement(sceneId: String, placement: ProductPlacement): Result<Unit>
    suspend fun removePlacement(sceneId: String, placementId: String): Result<Unit>
    suspend fun updatePlacementTransform(sceneId: String, placementId: String, transform: PlacementTransform): Result<Unit>
    suspend fun updateRoomSceneMetadata(sceneId: String, title: String, type: RoomType, targetBudget: Long): Result<Unit> = Result.success(Unit)
    suspend fun updatePreferredStyle(sceneId: String, style: DesignStyle): Result<Unit> = Result.success(Unit)
    suspend fun updateRoomDimensions(sceneId: String, dimensionsCm: DimensionsCm?): Result<Unit> = Result.success(Unit)
    suspend fun deleteRoomScene(sceneId: String): Result<Unit>
}

interface RoomAnalysisRepository {
    suspend fun analyzeRoom(sceneId: String, imageUri: String): Result<RoomAnalysisResult>
    suspend fun getCachedAnalysis(sceneId: String): RoomAnalysisResult?
}

interface ImageGenerationRepository {
    suspend fun requestFullMakeover(sceneId: String, style: DesignStyle): Result<VisualizationJob>
    suspend fun requestRealisticPlacement(sceneId: String, placements: List<ProductPlacement>): Result<VisualizationJob>
    fun observeJob(jobId: String): Flow<VisualizationJob>
}

interface ProductCatalogRepository {
    fun getProducts(category: ProductCategory?, style: DesignStyle?, maxPrice: Long?): Flow<List<Product>>
    fun filterAndSortProducts(filter: ProductCatalogFilter): Flow<List<Product>>
    suspend fun getProductById(productId: String): Product?
    fun searchProducts(query: String): Flow<List<Product>>
    fun getRelatedProducts(product: Product, limit: Int = 4): List<Product>
    fun getAlternativeProducts(product: Product, limit: Int = 4): List<Product>
}

interface ShoppingListRepository {
    fun getShoppingListItems(): Flow<List<ShoppingCartItem>>
    suspend fun addOrIncrementProduct(
        product: Product,
        sceneId: String? = null,
        placementId: String? = null,
        quantityToAdd: Int = 1
    ): Result<ShoppingCartItem>
    suspend fun updateItemQuantity(itemId: String, newQuantity: Int): Result<Unit>
    suspend fun toggleItemSelection(itemId: String): Result<Unit>
    suspend fun removeItem(itemId: String): Result<Unit>
    suspend fun syncScenePlacementsToShoppingList(scene: RoomScene, allProducts: List<Product>): Result<Int>
}

interface AffiliateLinkRepository {
    suspend fun getAffiliateLink(productId: String, sceneId: String, placementId: String?): Result<AffiliateRedirectInfo>
    suspend fun recordAffiliateClick(productId: String, marketplace: String): Result<Unit>
    suspend fun recordStructuredAffiliateClick(
        info: AffiliateRedirectInfo,
        productTitle: String
    ): Result<AffiliateClickRecord>
    fun getAffiliateClickHistory(): Flow<List<AffiliateClickRecord>>
}

interface CreditWalletRepository {
    fun getWalletBalance(): Flow<CreditBalance>
    fun getTransactions(): Flow<List<CreditTransaction>>
    suspend fun reserveCredit(jobId: String): Result<Unit>
    suspend fun captureCredit(jobId: String): Result<Unit>
    suspend fun refundCredit(jobId: String): Result<Unit>
    suspend fun purchaseCreditPackage(packageId: String): Result<Unit>
}

/**
 * Provider-independent AI Interior Design Generation repository interface (Milestone 7 Part B).
 * Supports full-room makeover configuration validation, idempotent generation requests,
 * asynchronous lifecycle states (pending, processing, completed, failed, cancelled),
 * retry/cancellation, variation selection, favorites, and persistent design history.
 */
interface AIInteriorDesignRepository {
    fun validateConfiguration(request: DesignGenerationRequest): DesignConfigValidationResult
    fun observeDesignHistoryForScene(sceneId: String): Flow<List<DesignGenerationResult>>
    suspend fun getDesignHistoryForSceneSync(sceneId: String): List<DesignGenerationResult>
    fun observeFavoriteVariations(sceneId: String? = null): Flow<List<DesignVariation>>
    suspend fun getResultByIdSync(resultId: String): DesignGenerationResult?
    suspend fun generateRoomMakeover(request: DesignGenerationRequest): Result<DesignGenerationResult>
    suspend fun retryDesignGeneration(requestId: String): Result<DesignGenerationResult>
    suspend fun cancelDesignGeneration(requestId: String): Result<DesignGenerationResult>
    suspend fun selectActiveVariation(resultId: String, variationId: String): Result<DesignGenerationResult>
    suspend fun toggleFavoriteVariation(variationId: String, isFavorite: Boolean): Result<DesignVariation>
}

/**
 * Provider-independent AI Chat & Consultation repository interface (Milestone 6 Part I).
 * Supports room-project-scoped persistent conversations and simulated/mock responses.
 */
interface AIChatRepository {
    fun observeConversationForScene(sceneId: String): Flow<ConsultationConversation?>
    fun observeMessagesForScene(sceneId: String): Flow<List<ChatMessage>>
    suspend fun getMessagesForSceneSync(sceneId: String): List<ChatMessage>
    suspend fun getOrCreateConversation(scene: RoomScene): ConsultationConversation
    suspend fun sendContextualMessage(
        context: ConsultationContext,
        userText: String,
        attachments: List<ChatAttachment> = emptyList()
    ): Result<ChatMessage>
    suspend fun updateConversationPreferences(
        sceneId: String,
        selectedStyle: DesignStyle?,
        customBudgetIdr: Long?
    ): Result<ConsultationConversation>
    suspend fun clearConversationForScene(sceneId: String): Result<Unit>
    suspend fun deleteConversation(conversationId: String): Result<Unit>
}

/**
 * Provider-independent AI Product Recommendation & Style Moodboard repository interface (Milestone 6 Part D, E, F, I).
 * Strictly recommends verified catalog products from ProductCatalogRepository.
 */
interface AIRecommendationRepository {
    suspend fun getContextualRecommendations(
        context: ConsultationContext,
        queryText: String,
        requestedStyle: DesignStyle? = null,
        budgetLimitIdr: Long? = null,
        limit: Int = 3
    ): List<ProductRecommendationItem>
    fun getStyleMoodboardProfile(style: DesignStyle): StyleMoodboardProfile
    fun getAllStyleMoodboardProfiles(): List<StyleMoodboardProfile>
    fun parseBudgetFromPrompt(text: String): Long?
    fun parseStyleFromPrompt(text: String): DesignStyle?
}

/**
 * Provider-independent Multimodal Input repository interface (Milestone 6 Part G, I).
 * Handles room photo attachments stored in app internal storage with honest simulation disclosures.
 */
interface MultimodalInputRepository {
    suspend fun attachRoomPhoto(
        conversationId: String,
        sourceUriOrPath: String,
        label: String = "Foto Referensi Ruangan"
    ): Result<ChatAttachment>
    suspend fun verifyAttachmentAvailability(attachment: ChatAttachment): ChatAttachment
    suspend fun cleanupConversationAttachments(conversationId: String)
}
