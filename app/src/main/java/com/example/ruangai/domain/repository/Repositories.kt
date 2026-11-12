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
    fun getProducts(
        category: ProductCategory? = null,
        style: DesignStyle? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        sortBy: ProductSortOption = ProductSortOption.RELEVANCE
    ): Flow<List<Product>>
    suspend fun getProductById(productId: String): Product?
    fun searchProducts(query: String): Flow<List<Product>>
    suspend fun getRelatedProducts(productId: String, limit: Int = 4): List<Product>
}

interface ShoppingCartRepository {
    fun getCartItems(): Flow<List<ShoppingCartItem>>
    suspend fun addToCart(product: Product, sceneId: String? = null, quantity: Int = 1): Result<Unit>
    suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit>
    suspend fun toggleItemSelected(productId: String): Result<Unit>
    suspend fun removeFromCart(productId: String): Result<Unit>
    suspend fun clearCart(): Result<Unit>
}

interface AffiliateLinkRepository {
    suspend fun getAffiliateLink(
        productId: String,
        sceneId: String?,
        placementId: String?,
        userHash: String? = null
    ): Result<AffiliateRedirectInfo>
    suspend fun recordAffiliateClick(
        productId: String,
        marketplace: String,
        trackingMetadata: TrackingMetadata? = null
    ): Result<Unit>
}

interface CreditWalletRepository {
    fun getWalletBalance(): Flow<CreditBalance>
    fun getTransactions(): Flow<List<CreditTransaction>>
    suspend fun reserveCredit(jobId: String): Result<Unit>
    suspend fun captureCredit(jobId: String): Result<Unit>
    suspend fun refundCredit(jobId: String): Result<Unit>
    suspend fun purchaseCreditPackage(packageId: String): Result<Unit>
}
