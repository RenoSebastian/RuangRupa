package com.example.ruangai.domain.model

data class DimensionsCm(
    val width: Int,
    val depth: Int,
    val height: Int
) {
    fun toDisplayString(): String = "${width} x ${depth} x ${height} cm"
}

enum class RoomType(val displayName: String, val description: String) {
    LIVING_ROOM("Ruang Tamu", "Area berkumpul utama dan hiburan keluarga"),
    BEDROOM("Kamar Tidur", "Kamar tidur pribadi utama"),
    KOST_STUDIO("Kamar Kost / Studio", "Ruang tidur kompak serbaguna (3x4m)"),
    WORKSPACE("Ruang Kerja / Belajar", "Area fokus WFH dan produktivitas"),
    DINING_ROOM("Ruang Makan", "Area makan dan perjamuan"),
    KITCHEN("Dapur", "Area memasak dan kitchen set"),
    OTHER("Lainnya", "Area teras, koridor, atau balkon")
}

enum class DesignStyle(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val description: String,
    val primaryColors: List<Long>,
    val imageUrl: String
) {
    JAPANDI(
        id = "japandi",
        displayName = "Japandi",
        subtitle = "Harmoni Jepang & Skandinavia",
        description = "Kombinasi fungsionalitas Skandinavia dengan estetika minimalis Wabi-Sabi Jepang. Mengutamakan kayu alami, linen krem, dan tanaman indoor.",
        primaryColors = listOf(0xFFF3EFE0, 0xFFD4B393, 0xFF8B8278, 0xFF47866C),
        imageUrl = "https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=800&q=80"
    ),
    SCANDINAVIAN(
        id = "scandinavian",
        displayName = "Scandinavian",
        subtitle = "Terang, Bersih & Nyaman",
        description = "Gaya Nordik yang memaksimalkan pencahayaan alami, palet warna putih/abu terang, garis furnitur ramping, dan tekstur wol lembut.",
        primaryColors = listOf(0xFFFFFFFF, 0xFFE0E0E0, 0xFF8B8278, 0xFFB68C65),
        imageUrl = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=800&q=80"
    ),
    MINIMALIST(
        id = "minimalist",
        displayName = "Minimalist",
        subtitle = "Less is More",
        description = "Penataan tanpa clutter. Setiap furnitur memiliki fungsi tegas, garis geometris presisi, dan skema warna monokromatis menenangkan.",
        primaryColors = listOf(0xFFF8F7F4, 0xFF252525, 0xFF9E978E, 0xFFD8D3CA),
        imageUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80"
    ),
    INDUSTRIAL(
        id = "industrial",
        displayName = "Industrial",
        subtitle = "Tegas, Ekspos & Berkarakter",
        description = "Terinspirasi dari loteng perkotaan. Menampilkan elemen ekspos seperti besi hitam matte, pipa, kayu daur ulang, dan aksen bata/beton.",
        primaryColors = listOf(0xFF252525, 0xFF5C5248, 0xFFBA4444, 0xFF8B8278),
        imageUrl = "https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=800&q=80"
    ),
    TROPICAL(
        id = "tropical",
        displayName = "Tropical Modern",
        subtitle = "Sejuk & Terkoneksi Alam",
        description = "Dirancang untuk iklim tropis Indonesia. Sirkulasi udara terbuka, anyaman rotan, aksen kayu jati, dan vegetasi hijau segar.",
        primaryColors = listOf(0xFF47866C, 0xFFD4B393, 0xFFF8F7F4, 0xFF8B8278),
        imageUrl = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=800&q=80"
    ),
    RETRO_80S(
        id = "retro_80s",
        displayName = "Retro 80s",
        subtitle = "Nostalgia Pop & Berenergi",
        description = "Sentuhan era 80-an yang ceria dengan furnitur lengkung (curved furniture), lampu neon warm, dan kombinasi warna berani yang hangat.",
        primaryColors = listOf(0xFFBA4444, 0xFFB68C65, 0xFF252525, 0xFFE6A23C),
        imageUrl = "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=800&q=80"
    ),
    MODERN_LUXURY(
        id = "modern_luxury",
        displayName = "Modern Luxury",
        subtitle = "Elegan, Marmer & Aksen Emas",
        description = "Kemewahan kontemporer dengan finishing marmer, aksen kuningan/emas halus, kain velvet premium, dan pencahayaan aksen dramatis.",
        primaryColors = listOf(0xFF252525, 0xFFB68C65, 0xFFEFECE6, 0xFFFFFFFF),
        imageUrl = "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=800&q=80"
    ),
    MEDITERRANEAN(
        id = "mediterranean",
        displayName = "Mediterranean",
        subtitle = "Hangat, Terakota & Busur Alami",
        description = "Dinding plester bertekstur, lantai ubin terakota, lengkungan busur (arches), dan nuansa hangat pesisir Eropa selatan.",
        primaryColors = listOf(0xFFD4B393, 0xFFBA4444, 0xFFF8F7F4, 0xFF6B655E),
        imageUrl = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=800&q=80"
    ),
    MID_CENTURY(
        id = "mid_century",
        displayName = "Mid-Century Modern",
        subtitle = "Ikonik Abad 20",
        description = "Karakteristik kaki furnitur meruncing (tapered legs), kayu walnut hangat, dan bentuk organik yang timeless sepanjang zaman.",
        primaryColors = listOf(0xFF8B8278, 0xFFB68C65, 0xFF47866C, 0xFF252525),
        imageUrl = "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?auto=format&fit=crop&w=800&q=80"
    ),
    BOHEMIAN(
        id = "bohemian",
        displayName = "Bohemian Chic",
        subtitle = "Bebas, Eklektik & Berlapis Tekstur",
        description = "Tekstur berlapis makrame, karpet bermotif etnik, bantal hias berbagai corak, dan sentuhan tanaman rambat yang santai.",
        primaryColors = listOf(0xFFB68C65, 0xFFD4B393, 0xFFBA4444, 0xFF47866C),
        imageUrl = "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=800&q=80"
    )
}

// Immutable user upload
data class OriginalRoomImage(
    val assetId: String,
    val localUri: String,
    val width: Int = 1280,
    val height: Int = 960,
    val capturedAt: Long = System.currentTimeMillis()
)

// Marketplace cutout asset
data class ProductReferenceImage(
    val imageId: String,
    val productId: String,
    val transparentCutoutUri: String,
    val trueDimensionsCm: DimensionsCm
)

// Local touch transform
data class PlacementTransform(
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val scale: Float = 1.0f,
    val rotationDegrees: Float = 0f,
    val zIndex: Int = 0
)

// Individual furniture placement on room
data class ProductPlacement(
    val placementId: String,
    val productId: String,
    val productTitle: String,
    val productPriceIdr: Long,
    val categoryName: String,
    val referenceImage: ProductReferenceImage,
    val transform: PlacementTransform = PlacementTransform(),
    val isSelected: Boolean = false
)

data class ProtectedSceneRegion(
    val regionId: String,
    val label: String,
    val normalizedX: Float,
    val normalizedY: Float,
    val normalizedWidth: Float,
    val normalizedHeight: Float
)

data class EditablePlacementRegion(
    val regionId: String,
    val normalizedX: Float,
    val normalizedY: Float,
    val normalizedWidth: Float,
    val normalizedHeight: Float
)

data class DesignHotspotPin(
    val pinId: String,
    val productId: String,
    val label: String,
    val priceIdr: Long,
    val normalizedX: Float,
    val normalizedY: Float
)

data class GeneratedDesignResult(
    val resultId: String,
    val style: DesignStyle,
    val renderImageUrl: String,
    val designSummary: String,
    val estimatedBudgetTotalIdr: Long,
    val hotspotPins: List<DesignHotspotPin> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis()
)

data class RoomScene(
    val id: String,
    val title: String,
    val roomType: RoomType,
    val originalImage: OriginalRoomImage,
    val protectedRegions: List<ProtectedSceneRegion> = emptyList(),
    val placements: List<ProductPlacement> = emptyList(),
    val fullMakeovers: List<GeneratedDesignResult> = emptyList(),
    val activeMakeoverId: String? = null,
    val targetBudget: Long = 5_000_000L,
    val preferredStyle: DesignStyle? = null,
    val userDimensionsCm: DimensionsCm? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class JobMode { FULL_MAKEOVER, PRODUCT_PLACEMENT }
enum class JobStatus { PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED }

data class VisualizationJob(
    val jobId: String,
    val sceneId: String,
    val mode: JobMode,
    val status: JobStatus = JobStatus.PENDING,
    val creditsReserved: Int = 1,
    val message: String = "Menyiapkan render..."
)
