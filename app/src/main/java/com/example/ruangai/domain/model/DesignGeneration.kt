package com.example.ruangai.domain.model

/**
 * Provider-independent lifecycle status for full-room AI makeover generation requests.
 */
enum class DesignGenerationStatus(val displayName: String) {
    PENDING("Menunggu Antrean"),
    PROCESSING("Sedang Merender Desain (Simulasi)"),
    COMPLETED("Selesai"),
    FAILED("Gagal"),
    CANCELLED("Dibatalkan")
}

/**
 * Desired room mood/atmosphere for full-room makeover configuration.
 */
enum class DesignAtmosphere(
    val displayName: String,
    val subtitle: String,
    val lightingHint: String
) {
    WARM_COZY(
        displayName = "Hangat & Nyaman",
        subtitle = "Pencahayaan kuning hangat (2700K) dengan tekstur kain lembut",
        lightingHint = "Indirect warm LED 2700K & lampu lantai sudut"
    ),
    BRIGHT_AIRY(
        displayName = "Terang & Lapang",
        subtitle = "Cahaya alami maksimal dengan sirkulasi ruang terbuka",
        lightingHint = "Natural daylight 4000K & refleksi dinding terang"
    ),
    CALM_ZEN(
        displayName = "Tenang & Zen",
        subtitle = "Harmoni organik bebas distraksi visual untuk relaksasi",
        lightingHint = "Diffused warm-neutral 3000K dengan bayangan lembut"
    ),
    MODERN_ELEGANT(
        displayName = "Modern & Elegan",
        subtitle = "Aksen kontras terukur dengan siluet furnitur tegas",
        lightingHint = "Track spotlight 3500K & aksen lampu gantung fokus"
    ),
    CREATIVE_PRODUCTIVE(
        displayName = "Kreatif & Produktif",
        subtitle = "Ergonomis, fokus kerja tinggi, dan tata letak terorganisir",
        lightingHint = "Task lighting 4000K bebas silau untuk meja kerja"
    ),
    NATURAL_TROPICAL(
        displayName = "Tropis & Alami",
        subtitle = "Kesegaran tanaman indoor, serat rotan, dan kayu solid",
        lightingHint = "Bukaan jendela lebar dengan aksen lampu rotan hangat"
    )
}

/**
 * Represents a real catalog product matched to a generated design variation.
 * Explicitly distinguishes catalog-matched items from AI-generated visual preview elements.
 */
data class CatalogMatchedDesignProduct(
    val productId: String,
    val productTitle: String,
    val category: ProductCategory,
    val priceIdr: Long,
    val style: DesignStyle,
    val roleInDesign: String,
    val compatibilityReason: String,
    val normalizedHotspotX: Float = 0.5f,
    val normalizedHotspotY: Float = 0.6f,
    val isExactVisualMatch: Boolean = false,
    val matchDistinctionNotice: String = "Produk Katalog Serupa — cocok secara gaya, fungsi, dan anggaran dari katalog RuangAI (bukan objek piksel identik di dalam gambar simulasi)."
)

/**
 * A single generated design variation within a [DesignGenerationResult].
 * Each full-room makeover request produces multiple variations for comparison.
 */
data class DesignVariation(
    val variationId: String,
    val resultId: String,
    val requestId: String,
    val sceneId: String,
    val variationIndex: Int,
    val variationTitle: String,
    val style: DesignStyle,
    val colorPaletteNames: List<String>,
    val colorPaletteHexes: List<Long> = emptyList(),
    val atmosphere: DesignAtmosphere,
    val roomType: RoomType,
    val previewImageUrl: String,
    val designSummary: String,
    val layoutAndLightingNotes: String,
    val estimatedFurnitureBudgetIdr: Long,
    val targetBudgetIdr: Long,
    val catalogMatchedProducts: List<CatalogMatchedDesignProduct>,
    val hotspotPins: List<DesignHotspotPin> = emptyList(),
    val isFavorite: Boolean = false,
    val isSimulated: Boolean = true,
    val simulationDisclosure: String = "SIMULASI DESAIN AI: Visualisasi dan konsep tata ruang ini dihasilkan oleh mesin simulasi deterministik RuangAI untuk tujuan pratinjau konsep.",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Validation result for a [DesignGenerationRequest] before credit reservation or submission.
 */
data class DesignConfigValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

/**
 * Provider-independent request payload for Full Room Makeover AI Design Generation.
 */
data class DesignGenerationRequest(
    val requestId: String,
    val idempotencyKey: String,
    val sceneId: String,
    val originalRoomImageUri: String,
    val roomType: RoomType,
    val selectedStyle: DesignStyle,
    val colorPaletteNames: List<String>,
    val desiredAtmosphere: DesignAtmosphere = DesignAtmosphere.WARM_COZY,
    val furniturePreferences: List<ProductCategory> = emptyList(),
    val targetBudgetIdr: Long,
    val additionalInstructions: String = "",
    val variationCount: Int = 3,
    val creditCost: Int = 1,
    val simulateFailure: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun validate(): DesignConfigValidationResult {
        val errors = mutableListOf<String>()
        if (sceneId.isBlank()) {
            errors.add("Proyek ruangan harus dipilih.")
        }
        if (idempotencyKey.isBlank()) {
            errors.add("Kunci idempotensi (idempotencyKey) tidak boleh kosong.")
        }
        if (originalRoomImageUri.isBlank()) {
            errors.add("Foto asli ruangan tidak ditemukan.")
        }
        if (colorPaletteNames.isEmpty()) {
            errors.add("Pilih minimal satu palet warna desain.")
        }
        if (targetBudgetIdr < 500_000L) {
            errors.add("Target anggaran minimal Rp 500.000.")
        }
        if (variationCount !in 1..5) {
            errors.add("Jumlah variasi desain harus antara 1 hingga 5.")
        }
        if (creditCost < 1) {
            errors.add("Biaya kredit harus minimal 1 kredit.")
        }
        return DesignConfigValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

/**
 * Provider-independent result model for a Full Room Makeover generation job.
 * Preserves [originalRoomImageUri] without mutating the underlying room photo.
 */
data class DesignGenerationResult(
    val resultId: String,
    val requestId: String,
    val idempotencyKey: String,
    val sceneId: String,
    val originalRoomImageUri: String,
    val roomType: RoomType,
    val selectedStyle: DesignStyle,
    val colorPaletteNames: List<String>,
    val desiredAtmosphere: DesignAtmosphere,
    val furniturePreferences: List<ProductCategory>,
    val targetBudgetIdr: Long,
    val additionalInstructions: String,
    val status: DesignGenerationStatus,
    val variations: List<DesignVariation> = emptyList(),
    val selectedVariationId: String? = variations.firstOrNull()?.variationId,
    val creditCost: Int = 1,
    val creditReserved: Boolean = false,
    val creditCaptured: Boolean = false,
    val creditRefunded: Boolean = false,
    val errorMessage: String? = null,
    val isSimulated: Boolean = true,
    val simulationDisclosure: String = "Hasil Makeover Ruangan ini adalah simulasi lokal deterministik (Demo/Simulasi). Foto asli ruangan Anda tetap utuh dan produk yang direkomendasikan merupakan item katalog nyata yang kompatibel.",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val activeVariation: DesignVariation?
        get() = variations.find { it.variationId == selectedVariationId } ?: variations.firstOrNull()
}

