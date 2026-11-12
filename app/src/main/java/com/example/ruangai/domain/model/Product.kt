package com.example.ruangai.domain.model

enum class ProductCategory(val displayName: String) {
    ALL("Semua"),
    SOFA("Sofa"),
    COFFEE_TABLE("Meja Kopi"),
    DINING_TABLE("Meja Makan"),
    TABLE("Meja Kerja"),
    CHAIR("Kursi Santai"),
    OFFICE_CHAIR("Kursi Kerja"),
    FLOOR_LAMP("Lampu Lantai"),
    CEILING_LAMP("Lampu Gantung"),
    LIGHTING("Lampu Meja"),
    RUG("Karpet"),
    SHELVES("Rak Ambalan"),
    STORAGE("Penyimpanan"),
    WALL_DECOR("Dekorasi Dinding"),
    DECOR("Tanaman & Dekor"),
    BEDROOM_FURNITURE("Kamar Tidur")
}

enum class MarketplaceProvider(val displayName: String, val badgeColor: Long) {
    SHOPEE("Shopee", 0xFFEE4D2D),
    TOKOPEDIA("Tokopedia", 0xFF03AC0E),
    LAZADA("Lazada", 0xFF0F146D),
    IKEA("IKEA", 0xFF0058A3),
    BLIBLI("Blibli", 0xFF0095DA)
}

enum class AffiliateLinkStatus(val displayLabel: String) {
    SIMULATED("Simulasi Afiliasi (Demo)"),
    ACTIVE_SIMULATED("Simulasi Afiliasi (Demo)"),
    LIVE_ACTIVE("Tautan Afiliasi Aktif"),
    EXPIRED("Kedaluwarsa")
}

enum class ProductAvailabilityStatus(val displayLabel: String) {
    SIMULATED_CATALOG("Simulasi Katalog"),
    IN_STOCK_DEMO("Tersedia (Data Sampel)"),
    MADE_TO_ORDER_DEMO("Pre-Order (Data Sampel)"),
    OUT_OF_STOCK_DEMO("Stok Habis (Data Sampel)")
}

enum class ProductSortOption(val displayName: String) {
    RELEVANCE("Relevansi"),
    PRICE_LOW_TO_HIGH("Harga: Rendah ke Tinggi"),
    PRICE_HIGH_TO_LOW("Harga: Tinggi ke Rendah"),
    POPULARITY("Popularitas")
}

enum class PriceRangeFilter(val label: String, val minIdr: Long?, val maxIdr: Long?) {
    ALL("Semua Harga", null, null),
    UNDER_300K("< Rp 300rb", null, 300_000L),
    RANGE_300K_TO_1M("Rp 300rb - 1 Juta", 300_000L, 1_000_000L),
    RANGE_1M_TO_2M("Rp 1 Juta - 2 Juta", 1_000_000L, 2_000_000L),
    ABOVE_2M("> Rp 2 Juta", 2_000_001L, null)
}

data class ProductCatalogFilter(
    val searchQuery: String = "",
    val category: ProductCategory = ProductCategory.ALL,
    val style: DesignStyle? = null,
    val priceRange: PriceRangeFilter = PriceRangeFilter.ALL,
    val minPriceIdr: Long? = null,
    val maxPriceIdr: Long? = null,
    val sortBy: ProductSortOption = ProductSortOption.RELEVANCE
)

data class TrackingMetadata(
    val projectId: String,
    val subId1: String, // Project / Scene ID
    val subId2: String, // Anonymous User Hash
    val subId3: String  // Placement Slot / Pin ID
)

data class RedirectDestination(
    val destinationType: String = "SIMULATED_PREVIEW",
    val targetMarketplaceName: String,
    val simulatedReferenceCode: String,
    val isSimulatedRedirect: Boolean = true
)

data class AffiliateRedirectInfo(
    val clickAttributionId: String = "attr_sim_001",
    val productId: String,
    val sceneId: String = "general_catalog",
    val placementId: String? = null,
    val marketplace: MarketplaceProvider,
    val originalProductUrl: String,
    val generatedAffiliateUrl: String,
    val trackingMetadata: TrackingMetadata,
    val redirectDestination: RedirectDestination = RedirectDestination(
        targetMarketplaceName = marketplace.displayName,
        simulatedReferenceCode = "SIM-${marketplace.name}-${productId.uppercase()}"
    ),
    val status: AffiliateLinkStatus = AffiliateLinkStatus.SIMULATED,
    val disclosureLabel: String = "RuangAI dapat memperoleh komisi dari pembelian melalui tautan afiliasi."
)

data class AffiliateClickRecord(
    val clickAttributionId: String,
    val productId: String,
    val productTitle: String,
    val marketplace: MarketplaceProvider,
    val sceneId: String,
    val placementId: String?,
    val subId1: String,
    val subId2: String,
    val subId3: String,
    val status: AffiliateLinkStatus = AffiliateLinkStatus.SIMULATED,
    val isSimulated: Boolean = true,
    val clickedAt: Long = System.currentTimeMillis()
)

data class Product(
    val id: String,
    val title: String,
    val category: ProductCategory,
    val brand: String = "RuangAI Living Demo",
    val designStyle: DesignStyle,
    val compatibleStyles: List<DesignStyle> = listOf(designStyle),
    val priceIdr: Long,
    val originalPriceIdr: Long? = null,
    val marketplace: MarketplaceProvider,
    val storeName: String,
    val storeBadge: String = "Simulasi Katalog",
    val rating: Float = 4.8f,
    val reviewCount: Int = 100,
    val popularityScore: Int = 95,
    val imageUrl: String,
    val imageGallery: List<String> = listOf(imageUrl),
    val transparentCutoutUrl: String,
    val dimensions: DimensionsCm,
    val material: String,
    val color: String = "Natural",
    val colorHex: Long = 0xFFD4B393,
    val description: String,
    val compatibilityScore: Int = 95,
    val originalProductUrl: String = "simulated://catalog/product/$id",
    val affiliateStatus: AffiliateLinkStatus = AffiliateLinkStatus.SIMULATED,
    val availabilityStatus: ProductAvailabilityStatus = ProductAvailabilityStatus.SIMULATED_CATALOG,
    val lastPriceUpdateAt: Long = 1731369600000L,
    val isMockData: Boolean = true,
    val demoLabel: String = "Simulasi Katalog"
) {
    fun getFormattedPrice(): String {
        return "Rp " + "%,d".format(priceIdr).replace(',', '.')
    }

    fun getFormattedOriginalPrice(): String? {
        val orig = originalPriceIdr ?: return null
        if (orig <= priceIdr) return null
        return "Rp " + "%,d".format(orig).replace(',', '.')
    }

    fun hasDiscount(): Boolean {
        val orig = originalPriceIdr ?: return false
        return orig > priceIdr
    }

    fun getDiscountPercentage(): Int {
        val orig = originalPriceIdr ?: return 0
        if (orig <= priceIdr || orig == 0L) return 0
        return (((orig - priceIdr).toDouble() / orig.toDouble()) * 100.0).toInt()
    }
}

data class ShoppingCartItem(
    val itemId: String = "",
    val product: Product,
    val quantity: Int = 1,
    val isSelected: Boolean = true,
    val associatedSceneId: String? = null,
    val associatedPlacementId: String? = null,
    val addedAt: Long = System.currentTimeMillis()
) {
    val effectiveItemId: String
        get() = itemId.ifBlank { "cart_${product.id}_${associatedSceneId ?: "general"}" }
    val subtotalIdr: Long get() = product.priceIdr * quantity
}

data class BudgetSummary(
    val sceneId: String?,
    val sceneTitle: String,
    val targetBudgetIdr: Long,
    val selectedSubtotalIdr: Long,
    val totalItemCount: Int
) {
    val remainingBudgetIdr: Long get() = (targetBudgetIdr - selectedSubtotalIdr).coerceAtLeast(0L)
    val overrunIdr: Long get() = (selectedSubtotalIdr - targetBudgetIdr).coerceAtLeast(0L)
    val isOverBudget: Boolean get() = targetBudgetIdr > 0 && selectedSubtotalIdr > targetBudgetIdr
    val utilizationRatio: Float
        get() = if (targetBudgetIdr > 0) (selectedSubtotalIdr.toFloat() / targetBudgetIdr.toFloat()) else 0f
}
