package com.example.ruangai.data.repository.mock

import com.example.ruangai.domain.model.*
import com.example.ruangai.domain.repository.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MockAuthRepository : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(
        AuthState.Authenticated(
            UserSession(
                userId = "usr_demo_123",
                email = "dimas.pratama@gmail.com",
                displayName = "Dimas Pratama",
                photoUrl = null
            )
        )
    )

    override fun getAuthState(): Flow<AuthState> = _authState.asStateFlow()

    override suspend fun loginWithEmail(email: String, pass: String): Result<UserSession> {
        delay(600)
        val session = UserSession("usr_${UUID.randomUUID().toString().take(8)}", email, email.substringBefore('@'))
        _authState.value = AuthState.Authenticated(session)
        return Result.success(session)
    }

    override suspend fun registerWithEmail(email: String, pass: String, name: String): Result<UserSession> {
        delay(600)
        val session = UserSession("usr_${UUID.randomUUID().toString().take(8)}", email, name)
        _authState.value = AuthState.Authenticated(session)
        return Result.success(session)
    }

    override suspend fun signInWithGoogle(): Result<UserSession> {
        delay(800)
        val session = UserSession("usr_google_99", "dimas.pratama@gmail.com", "Dimas Pratama")
        _authState.value = AuthState.Authenticated(session)
        return Result.success(session)
    }

    override suspend fun logout(): Result<Unit> {
        _authState.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        delay(500)
        _authState.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }
}

class MockRoomSceneRepository : RoomSceneRepository {
    private val _scenes = MutableStateFlow<List<RoomScene>>(MockData.preloadedScenes)

    override fun getRoomScenes(): Flow<List<RoomScene>> = _scenes.asStateFlow()

    override fun getRoomSceneById(sceneId: String): Flow<RoomScene?> {
        return _scenes.map { list -> list.find { it.id == sceneId } }
    }

    override suspend fun createRoomScene(title: String, type: RoomType, imageUri: String): Result<RoomScene> {
        val newScene = RoomScene(
            id = "scene_${UUID.randomUUID().toString().take(8)}",
            title = title,
            roomType = type,
            originalImage = OriginalRoomImage(
                assetId = "orig_${UUID.randomUUID().toString().take(8)}",
                localUri = imageUri
            ),
            protectedRegions = listOf(
                ProtectedSceneRegion("pr_gen_1", "Dinding & Jendela", 0f, 0f, 1f, 0.65f),
                ProtectedSceneRegion("pr_gen_2", "Lantai Utama", 0f, 0.65f, 1f, 0.35f)
            ),
            placements = emptyList()
        )
        _scenes.value = listOf(newScene) + _scenes.value
        return Result.success(newScene)
    }

    override suspend fun updatePlacements(sceneId: String, placements: List<ProductPlacement>): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) scene.copy(placements = placements, updatedAt = System.currentTimeMillis())
            else scene
        }
        return Result.success(Unit)
    }

    override suspend fun addPlacement(sceneId: String, placement: ProductPlacement): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) {
                val updated = scene.placements + placement
                scene.copy(placements = updated, updatedAt = System.currentTimeMillis())
            } else scene
        }
        return Result.success(Unit)
    }

    override suspend fun removePlacement(sceneId: String, placementId: String): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) {
                scene.copy(placements = scene.placements.filterNot { it.placementId == placementId })
            } else scene
        }
        return Result.success(Unit)
    }

    override suspend fun updatePlacementTransform(
        sceneId: String,
        placementId: String,
        transform: PlacementTransform
    ): Result<Unit> {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) {
                val updated = scene.placements.map { p ->
                    if (p.placementId == placementId) p.copy(transform = transform) else p
                }
                scene.copy(placements = updated)
            } else scene
        }
        return Result.success(Unit)
    }

    override suspend fun deleteRoomScene(sceneId: String): Result<Unit> {
        _scenes.value = _scenes.value.filterNot { it.id == sceneId }
        return Result.success(Unit)
    }
}

class MockRoomAnalysisRepository : RoomAnalysisRepository {
    override suspend fun analyzeRoom(sceneId: String, imageUri: String): Result<RoomAnalysisResult> {
        delay(1200) // Simulate multi-step processing
        val result = RoomAnalysisResult(
            sceneId = sceneId,
            detectedType = RoomType.KOST_STUDIO,
            lighting = LightingInfo(
                naturalSource = "Jendela Kanan (Pencahayaan Alami 65%)",
                timeOfDay = "Siang Hari",
                intensity = "Cukup Terang"
            ),
            surface = SurfaceMaterial(
                wallColorHex = 0xFFF3EFE0,
                wallMaterial = "Plester Halus Cat Krem",
                floorType = "Keramik Putih Glossy 40x40cm"
            ),
            detectedItems = listOf(
                DetectedFurnitureItem("Kasur", "Ranjang Single 100x200cm", "Sisi Kiri", "Pertahankan ranjang, gunakan sprei beige netral"),
                DetectedFurnitureItem("Meja", "Meja Belajar Kayu Tua", "Sudut Depan", "Ganti dengan meja kerja kaki ramping agar lebih lapang"),
                DetectedFurnitureItem("Lampu", "Lampu Gantung Neon Putih", "Atas Ruangan", "Tambahkan standing lamp warm 3000K untuk suasana malam rileks")
            ),
            makeoverSuggestions = listOf(
                "Gunakan palet warna kayu alami dan kain linen untuk menciptakan tema Japandi yang bersih",
                "Pasang cermin gantung bulat untuk memantulkan cahaya dan memperluas visual kamar kost",
                "Gunakan karpet berukuran 120x160cm untuk menyatukan area duduk dan santai"
            )
        )
        return Result.success(result)
    }

    override suspend fun getCachedAnalysis(sceneId: String): RoomAnalysisResult? = null
}

class MockImageGenerationRepository : ImageGenerationRepository {
    private val _jobs = MutableStateFlow<Map<String, VisualizationJob>>(emptyMap())

    override suspend fun requestFullMakeover(sceneId: String, style: DesignStyle): Result<VisualizationJob> {
        val jobId = "job_makeover_${UUID.randomUUID().toString().take(6)}"
        val job = VisualizationJob(
            jobId = jobId,
            sceneId = sceneId,
            mode = JobMode.FULL_MAKEOVER,
            status = JobStatus.PROCESSING,
            creditsReserved = 1,
            message = "Sedang menghasilkan visualisasi ${style.displayName}..."
        )
        _jobs.value = _jobs.value + (jobId to job)
        return Result.success(job)
    }

    override suspend fun requestRealisticPlacement(
        sceneId: String,
        placements: List<ProductPlacement>
    ): Result<VisualizationJob> {
        val jobId = "job_tryon_${UUID.randomUUID().toString().take(6)}"
        val job = VisualizationJob(
            jobId = jobId,
            sceneId = sceneId,
            mode = JobMode.PRODUCT_PLACEMENT,
            status = JobStatus.PROCESSING,
            creditsReserved = 1,
            message = "Mengintegrasikan pencahayaan dan bayangan realistis..."
        )
        _jobs.value = _jobs.value + (jobId to job)
        return Result.success(job)
    }

    override fun observeJob(jobId: String): Flow<VisualizationJob> {
        return _jobs.map { map ->
            map[jobId] ?: VisualizationJob(jobId, "", JobMode.FULL_MAKEOVER, JobStatus.COMPLETED)
        }
    }
}

class MockProductCatalogRepository : ProductCatalogRepository {
    private val _products = MutableStateFlow(MockData.products)

    override fun getProducts(
        category: ProductCategory?,
        style: DesignStyle?,
        minPrice: Long?,
        maxPrice: Long?,
        sortBy: ProductSortOption
    ): Flow<List<Product>> {
        return _products.map { list ->
            val filtered = list.filter { p ->
                (category == null || category == ProductCategory.ALL || p.category == category) &&
                (style == null || p.designStyle == style) &&
                (minPrice == null || p.priceIdr >= minPrice) &&
                (maxPrice == null || p.priceIdr <= maxPrice)
            }
            when (sortBy) {
                ProductSortOption.RELEVANCE -> filtered
                ProductSortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.priceIdr }
                ProductSortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.priceIdr }
                ProductSortOption.POPULARITY -> filtered.sortedByDescending { it.rating }
            }
        }
    }

    override suspend fun getProductById(productId: String): Product? {
        return _products.value.find { it.id == productId }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return _products.map { list ->
            if (query.isBlank()) list
            else list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.category.displayName.contains(query, ignoreCase = true) ||
                it.brand.contains(query, ignoreCase = true) ||
                it.material.contains(query, ignoreCase = true) ||
                it.color.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun getRelatedProducts(productId: String, limit: Int): List<Product> {
        val current = getProductById(productId) ?: return _products.value.take(limit)
        return _products.value
            .filter { it.id != productId && (it.category == current.category || it.designStyle == current.designStyle) }
            .take(limit)
            .ifEmpty { _products.value.filter { it.id != productId }.take(limit) }
    }
}

class MockShoppingCartRepository : ShoppingCartRepository {
    private val _items = MutableStateFlow<List<ShoppingCartItem>>(emptyList())

    override fun getCartItems(): Flow<List<ShoppingCartItem>> = _items.asStateFlow()

    override suspend fun addToCart(product: Product, sceneId: String?, quantity: Int): Result<Unit> {
        val existing = _items.value.find { it.product.id == product.id && it.associatedSceneId == sceneId }
        if (existing != null) {
            updateQuantity(product.id, existing.quantity + quantity)
        } else {
            _items.value = _items.value + ShoppingCartItem(product, quantity, isSelected = true, associatedSceneId = sceneId)
        }
        return Result.success(Unit)
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit> {
        if (quantity <= 0) {
            removeFromCart(productId)
        } else {
            _items.value = _items.value.map { item ->
                if (item.product.id == productId) item.copy(quantity = quantity) else item
            }
        }
        return Result.success(Unit)
    }

    override suspend fun toggleItemSelected(productId: String): Result<Unit> {
        _items.value = _items.value.map { item ->
            if (item.product.id == productId) item.copy(isSelected = !item.isSelected) else item
        }
        return Result.success(Unit)
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> {
        _items.value = _items.value.filterNot { it.product.id == productId }
        return Result.success(Unit)
    }

    override suspend fun clearCart(): Result<Unit> {
        _items.value = emptyList()
        return Result.success(Unit)
    }
}

class MockAffiliateLinkRepository : AffiliateLinkRepository {
    private val _clickedAttributions = MutableStateFlow<List<TrackingMetadata>>(emptyList())
    val clickedAttributions: Flow<List<TrackingMetadata>> = _clickedAttributions.asStateFlow()

    override suspend fun getAffiliateLink(
        productId: String,
        sceneId: String?,
        placementId: String?,
        userHash: String?
    ): Result<AffiliateRedirectInfo> {
        val prod = MockData.products.find { it.id == productId } ?: MockData.products.first()
        val sId = sceneId ?: "unassigned_project"
        val uHash = userHash ?: "usr_hash_anon_${productId.hashCode().toString().take(6)}"
        val slotId = placementId ?: "catalog_direct"

        val tracking = TrackingMetadata(
            projectId = sId,
            subId1 = sId,
            subId2 = uHash,
            subId3 = slotId
        )

        val affiliateUrl = "https://ruangai.id/r?p=${prod.id}&market=${prod.marketplace.name.lowercase()}&sub1=${tracking.subId1}&sub2=${tracking.subId2}&sub3=${tracking.subId3}"

        val info = AffiliateRedirectInfo(
            productId = prod.id,
            marketplace = prod.marketplace,
            originalProductUrl = "https://${prod.marketplace.name.lowercase()}.co.id/product/${prod.id}",
            generatedAffiliateUrl = affiliateUrl,
            trackingMetadata = tracking,
            status = AffiliateLinkStatus.ACTIVE_SIMULATED,
            disclosureLabel = "Tautan Afiliasi [Simulasi Demo]: RuangAI dapat memperoleh komisi tanpa biaya tambahan bagi Anda."
        )
        return Result.success(info)
    }

    override suspend fun recordAffiliateClick(
        productId: String,
        marketplace: String,
        trackingMetadata: TrackingMetadata?
    ): Result<Unit> {
        if (trackingMetadata != null) {
            _clickedAttributions.value = listOf(trackingMetadata) + _clickedAttributions.value
        }
        return Result.success(Unit)
    }
}

class MockCreditWalletRepository : CreditWalletRepository {
    private val _balance = MutableStateFlow(CreditBalance(available = 3, reserved = 0, lifetimeConsumed = 1))
    private val _transactions = MutableStateFlow<List<CreditTransaction>>(
        listOf(
            CreditTransaction("tx_1", 3, TransactionType.WELCOME_BONUS, "Bonus Selamat Datang Pengguna Baru"),
            CreditTransaction("tx_2", -1, TransactionType.RENDER_CAPTURE, "Render Ruang Tamu Japandi")
        )
    )

    override fun getWalletBalance(): Flow<CreditBalance> = _balance.asStateFlow()

    override fun getTransactions(): Flow<List<CreditTransaction>> = _transactions.asStateFlow()

    override suspend fun reserveCredit(jobId: String): Result<Unit> {
        if (_balance.value.available < 1) return Result.failure(IllegalStateException("Saldo kredit tidak mencukupi"))
        _balance.value = _balance.value.copy(
            available = _balance.value.available - 1,
            reserved = _balance.value.reserved + 1
        )
        _transactions.value = listOf(
            CreditTransaction("tx_${UUID.randomUUID().toString().take(6)}", -1, TransactionType.RENDER_RESERVATION, "Reservasi Render AI (#$jobId)")
        ) + _transactions.value
        return Result.success(Unit)
    }

    override suspend fun captureCredit(jobId: String): Result<Unit> {
        _balance.value = _balance.value.copy(
            reserved = maxOf(0, _balance.value.reserved - 1),
            lifetimeConsumed = _balance.value.lifetimeConsumed + 1
        )
        return Result.success(Unit)
    }

    override suspend fun refundCredit(jobId: String): Result<Unit> {
        _balance.value = _balance.value.copy(
            available = _balance.value.available + 1,
            reserved = maxOf(0, _balance.value.reserved - 1)
        )
        _transactions.value = listOf(
            CreditTransaction("tx_${UUID.randomUUID().toString().take(6)}", 1, TransactionType.AUTO_REFUND, "Pengembalian Kredit Gagal Render (#$jobId)")
        ) + _transactions.value
        return Result.success(Unit)
    }

    override suspend fun purchaseCreditPackage(packageId: String): Result<Unit> {
        val pkg = MockData.creditPackages.find { it.id == packageId } ?: MockData.creditPackages.first()
        _balance.value = _balance.value.copy(available = _balance.value.available + pkg.credits)
        _transactions.value = listOf(
            CreditTransaction("tx_${UUID.randomUUID().toString().take(6)}", pkg.credits, TransactionType.TOP_UP, "Pembelian ${pkg.title}")
        ) + _transactions.value
        return Result.success(Unit)
    }
}
