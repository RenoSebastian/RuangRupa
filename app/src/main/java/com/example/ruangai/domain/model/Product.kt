package com.example.ruangai.domain.model

enum class ProductCategory(val displayName: String) {
    ALL("Semua"),
    SOFA("Sofa"),
    COFFEE_TABLE("Meja Kopi"),
    DINING_TABLE("Meja Makan"),
    TABLE("Meja"),
    CHAIR("Kursi"),
    OFFICE_CHAIR("Kursi Kerja"),
    FLOOR_LAMP("Lampu Lantai"),
    CEILING_LAMP("Lampu Gantung"),
    LIGHTING("Lampu"),
    RUG("Karpet"),
    SHELVES("Rak & Ambalan"),
    STORAGE("Lemari & Storage"),
    WALL_DECORATION("Hiasan Dinding"),
    BEDROOM_FURNITURE("Furnitur Kamar"),
    DECOR("Dekorasi")
}

enum class MarketplaceProvider(val displayName: String, val badgeColor: Long) {
    SHOPEE("Shopee", 0xFFEE4D2D),
    TOKOPEDIA("Tokopedia", 0xFF03AC0E),
    LAZADA("Lazada", 0xFF0F146D),
    IKEA("IKEA", 0xFF0058A3)
}

enum class AffiliateLinkStatus {
    ACTIVE_SIMULATED,
    LIVE_ACTIVE,
    EXPIRED
}

enum class ProductSortOption(val displayName: String) {
    RELEVANCE("Paling Relevan"),
    PRICE_LOW_HIGH("Harga: Terendah"),
    PRICE_HIGH_LOW("Harga: Tertinggi"),
    POPULARITY("Rating Tertinggi")
}

data class TrackingMetadata(
    val projectId: String,
    val subId1: String, // Project / Scene ID
    val subId2: String, // Anonymous User Hash
    val subId3: String  // Placement Slot / Pin ID
)

data class AffiliateRedirectInfo(
    val productId: String,
    val marketplace: MarketplaceProvider,
    val originalProductUrl: String,
    val generatedAffiliateUrl: String,
    val trackingMetadata: TrackingMetadata,
    val status: AffiliateLinkStatus = AffiliateLinkStatus.ACTIVE_SIMULATED,
    val disclosureLabel: String = "Tautan Afiliasi [Simulasi Demo]"
)

data class Product(
    val id: String,
    val title: String,
    val category: ProductCategory,
    val designStyle: DesignStyle,
    val priceIdr: Long,
    val originalPriceIdr: Long? = null,
    val marketplace: MarketplaceProvider,
    val storeName: String,
    val storeBadge: String, // e.g. "Official Store", "Star+"
    val brand: String = "RuangAI Curated",
    val rating: Float,
    val reviewCount: Int,
    val imageUrl: String,
    val galleryImages: List<String> = emptyList(),
    val transparentCutoutUrl: String,
    val dimensions: DimensionsCm,
    val material: String,
    val color: String = "Natural",
    val description: String,
    val originalProductUrl: String = "",
    val affiliateStatus: AffiliateLinkStatus = AffiliateLinkStatus.ACTIVE_SIMULATED,
    val isAvailable: Boolean = true,
    val lastPriceUpdate: Long = System.currentTimeMillis(),
    val compatibilityScore: Int = 95,
    val isMockData: Boolean = true
) {
    fun getFormattedPrice(): String {
        return "Rp " + "%,d".format(priceIdr).replace(',', '.')
    }

    fun getFormattedOriginalPrice(): String? {
        return originalPriceIdr?.let { "Rp " + "%,d".format(it).replace(',', '.') }
    }

    fun getDiscountPercentage(): Int? {
        if (originalPriceIdr == null || originalPriceIdr <= priceIdr) return null
        return (((originalPriceIdr - priceIdr).toDouble() / originalPriceIdr.toDouble()) * 100).toInt()
    }
}

data class ShoppingCartItem(
    val product: Product,
    val quantity: Int = 1,
    val isSelected: Boolean = true,
    val associatedSceneId: String? = null
)
