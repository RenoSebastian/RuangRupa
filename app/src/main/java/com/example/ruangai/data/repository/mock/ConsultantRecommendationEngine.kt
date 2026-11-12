package com.example.ruangai.data.repository.mock

import com.example.ruangai.domain.model.*
import com.example.ruangai.domain.repository.AIRecommendationRepository
import com.example.ruangai.domain.repository.ProductCatalogRepository

class ConsultantRecommendationEngine(
    private val productCatalogRepository: ProductCatalogRepository = MockProductCatalogRepository()
) : AIRecommendationRepository {

    private val styleProfiles: Map<DesignStyle, StyleMoodboardProfile> = mapOf(
        DesignStyle.JAPANDI to StyleMoodboardProfile(
            style = DesignStyle.JAPANDI,
            colorPaletteNamed = listOf(
                "Linen Hangat" to 0xFFF3EFE0,
                "Kayu Pinus Alami" to 0xFFD4B393,
                "Taupe Abu Hangat" to 0xFF8B8278,
                "Hijau Sage Zen" to 0xFF47866C
            ),
            materialSuggestions = listOf(
                "Kayu pinus & oak natural matte",
                "Kain tenun linen & katun organik",
                "Anyaman rotan & bambu halus",
                "Keramik stoneware bertekstur doff"
            ),
            furnitureCharacteristics = listOf(
                "Profil rendah (low-profile) dengan kaki kayu ramping",
                "Garis bersih fungsional tanpa ornamen berlebihan",
                "Penyimpanan tertutup agar visual tetap rapi bebas clutter"
            ),
            lightingCharacteristics = listOf(
                "Warm white 2700K–3000K yang menenangkan",
                "Kap lampu kertas nasi / bambu difusi lembut",
                "Memaksimalkan cahaya matahari alami dari jendela"
            ),
            spaceUtilizationTip = "Sisakan minimal 35% area lantai terbuka dan gunakan furnitur berkaki ramping agar kamar kompak terasa 20% lebih lega.",
            exampleRoomImages = listOf(
                "https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=800&q=80"
            ),
            recommendedProductIds = listOf("prod_sofa_01", "prod_table_01", "prod_chair_01", "prod_light_01", "prod_bed_02")
        ),
        DesignStyle.SCANDINAVIAN to StyleMoodboardProfile(
            style = DesignStyle.SCANDINAVIAN,
            colorPaletteNamed = listOf(
                "Putih Nordik" to 0xFFFFFFFF,
                "Abu Kabut Terang" to 0xFFE0E0E0,
                "Abu Charcoal Lembut" to 0xFF8B8278,
                "Kayu Ash Terang" to 0xFFB68C65
            ),
            materialSuggestions = listOf(
                "Kayu ash & birch berwarna cerah",
                "Kain rajut wol & poly-cotton tahan noda",
                "Karpet bulu lembut motif geometris sederhana"
            ),
            furnitureCharacteristics = listOf(
                "Kaki kayu meruncing (tapered legs) yang ringan",
                "Sofa bantal empuk dengan sarung lepas-pasang",
                "Meja bundar atau oval ramah sirkulasi ruang"
            ),
            lightingCharacteristics = listOf(
                "Pencahayaan berlapis (ambient, task, dan floor lamp sudut)",
                "Cahaya netral-hangat 3000K untuk kesan bersih dan hygge"
            ),
            spaceUtilizationTip = "Gunakan warna dinding putih/krem terang dan cermin dinding untuk memantulkan cahaya alami ke seluruh sudut ruang.",
            exampleRoomImages = listOf(
                "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=800&q=80"
            ),
            recommendedProductIds = listOf("prod_sofa_02", "prod_table_01", "prod_rug_01", "prod_ochair_02", "prod_light_01")
        ),
        DesignStyle.MINIMALIST to StyleMoodboardProfile(
            style = DesignStyle.MINIMALIST,
            colorPaletteNamed = listOf(
                "Off-White Bersih" to 0xFFF8F7F4,
                "Hitam Monokrom" to 0xFF252525,
                "Greige Netral" to 0xFF9E978E,
                "Pasir Lembut" to 0xFFD8D3CA
            ),
            materialSuggestions = listOf(
                "MDF laminasi matte anti-sidik jari",
                "Baja bubuk hitam (powder-coated steel) tipis",
                "Kaca bening & kain tenun polos"
            ),
            furnitureCharacteristics = listOf(
                "Bentuk geometris tegas dan multifungsi",
                "Sistem rak modular vertikal hemat lantai",
                "Tanpa tarikan laci menonjol (handleless design)"
            ),
            lightingCharacteristics = listOf(
                "Downlight tersembunyi & lampu lantai siluet ramping",
                "Temperatur cahaya 3000K–4000K fokus pada bidang kerja"
            ),
            spaceUtilizationTip = "Batasi maksimal 3–4 furnitur utama per ruangan dan manfaatkan dinding vertikal dengan rak ambalan.",
            exampleRoomImages = listOf(
                "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80"
            ),
            recommendedProductIds = listOf("prod_sofa_01", "prod_shelf_02", "prod_chair_03", "prod_light_01", "prod_bed_02")
        ),
        DesignStyle.INDUSTRIAL to StyleMoodboardProfile(
            style = DesignStyle.INDUSTRIAL,
            colorPaletteNamed = listOf(
                "Besi Hitam Matte" to 0xFF252525,
                "Kayu Walnut Tua" to 0xFF5C5248,
                "Bata Terakota" to 0xFFBA4444,
                "Abu Beton Ekspos" to 0xFF8B8278
            ),
            materialSuggestions = listOf(
                "Rangka besi hollow hitam matte",
                "Kayu jati reclaimed bertekstur serat tegas",
                "Kulit sintetis / kanvas tebal & beton ekspos"
            ),
            furnitureCharacteristics = listOf(
                "Struktur rangka logam terlihat jelas (exposed frame)",
                "Meja dan rak terbuka kokoh berdaya tahan tinggi",
                "Kombinasi kayu gelap dan logam industrial"
            ),
            lightingCharacteristics = listOf(
                "Bohlam filamen Edison warm amber (2200K–2700K)",
                "Kap lampu gantung besi hitam atau track light"
            ),
            spaceUtilizationTip = "Gunakan rak dan meja rangka besi terbuka agar pandangan mata tetap tembus dan ruangan tidak terasa sempit.",
            exampleRoomImages = listOf(
                "https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=800&q=80"
            ),
            recommendedProductIds = listOf("prod_ctable_02", "prod_dtable_02", "prod_storage_01", "prod_chair_03", "prod_clamp_02")
        ),
        DesignStyle.TROPICAL to StyleMoodboardProfile(
            style = DesignStyle.TROPICAL,
            colorPaletteNamed = listOf(
                "Hijau Daun Tropis" to 0xFF47866C,
                "Kayu Jati Emas" to 0xFFD4B393,
                "Putih Gading" to 0xFFF8F7F4,
                "Cokelat Rotan" to 0xFF8B8278
            ),
            materialSuggestions = listOf(
                "Kayu jati solid tahan lembap",
                "Anyaman rotan alami & bambu Nusantara",
                "Serat jute alami & kain katun breathable"
            ),
            furnitureCharacteristics = listOf(
                "Kursi dan kabinet dengan panel anyaman rotan berpori",
                "Material organik yang sejuk untuk iklim tropis Indonesia",
                "Aksen tanaman indoor hidup atau artificial di sudut ruang"
            ),
            lightingCharacteristics = listOf(
                "Lampu gantung anyaman bambu dengan bayangan artistik",
                "Sirkulasi cahaya dan udara alami maksimal di siang hari"
            ),
            spaceUtilizationTip = "Tempatkan kursi rotan atau tanaman Monstera di dekat sumber cahaya jendela untuk menciptakan oase tropis yang segar.",
            exampleRoomImages = listOf(
                "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=800&q=80"
            ),
            recommendedProductIds = listOf("prod_chair_02", "prod_clamp_01", "prod_rug_02", "prod_storage_03", "prod_decor_01")
        ),
        DesignStyle.RETRO_80S to StyleMoodboardProfile(
            style = DesignStyle.RETRO_80S,
            colorPaletteNamed = listOf(
                "Terracotta Pop" to 0xFFBA4444,
                "Mustard Hangat" to 0xFFE6A23C,
                "Camel Vintage" to 0xFFB68C65,
                "Hitam Kontras" to 0xFF252525
            ),
            materialSuggestions = listOf(
                "Kain velvet & corduroy bertekstur lembut",
                "Aksen kuningan (brass) & kayu warna hangat",
                "Kanvas seni grafis geometris"
            ),
            furnitureCharacteristics = listOf(
                "Siluet lengkung (curved sofa & armchair) yang ekspresif",
                "Satu furnitur statement berwarna mencolok (focal point)",
                "Kaki furnitur silinder atau meruncing bergaya vintage"
            ),
            lightingCharacteristics = listOf(
                "Lampu lantai lengkung (arc lamp) kuningan hangat",
                "Pencahayaan aksen sudut untuk atmosfer hangat di malam hari"
            ),
            spaceUtilizationTip = "Jadikan satu sofa atau armchair berwarna Terracotta/Mustard sebagai titik fokus, lalu padukan dengan dinding netral agar tidak ramai.",
            exampleRoomImages = listOf(
                "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=800&q=80"
            ),
            recommendedProductIds = listOf("prod_sofa_03", "prod_chair_02", "prod_light_02", "prod_decor_02")
        ),
        DesignStyle.MODERN_LUXURY to StyleMoodboardProfile(
            style = DesignStyle.MODERN_LUXURY,
            colorPaletteNamed = listOf(
                "Onyx Charcoal" to 0xFF252525,
                "Champagne Gold" to 0xFFB68C65,
                "Krem Marmer" to 0xFFEFECE6,
                "Putih Mutiara" to 0xFFFFFFFF
            ),
            materialSuggestions = listOf(
                "Permukaan motif marmer Carrara & sintering stone",
                "Logam finishing brushed brass / emas lembut",
                "Kain beludru (velvet) & tenun berkerapatan tinggi"
            ),
            furnitureCharacteristics = listOf(
                "Proporsi simetris yang anggun dan berkelas",
                "Detail kaki atau bingkai logam emas/kuningan",
                "Kombinasi tekstur halus mengilap dan kain lembut"
            ),
            lightingCharacteristics = listOf(
                "Lampu gantung ring geometris modern atau lampu sudut brass",
                "Cahaya warm white 3000K yang menonjolkan kilau permukaan"
            ),
            spaceUtilizationTip = "Gunakan lampu lantai lengkung brass dan cermin bundar untuk menciptakan kesan mewah seperti suite hotel bintang lima tanpa merombak dinding.",
            exampleRoomImages = listOf(
                "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=800&q=80"
            ),
            recommendedProductIds = listOf("prod_sofa_02", "prod_light_02", "prod_clamp_02", "prod_rug_01", "prod_decor_02")
        ),
        DesignStyle.MEDITERRANEAN to StyleMoodboardProfile(
            style = DesignStyle.MEDITERRANEAN,
            colorPaletteNamed = listOf(
                "Pasir Kapur" to 0xFFD4B393,
                "Terakota Pesisir" to 0xFFBA4444,
                "Putih Kapur" to 0xFFF8F7F4,
                "Zaitun Tua" to 0xFF6B655E
            ),
            materialSuggestions = listOf(
                "Kayu solid bertekstur alami & rotan",
                "Serat jute, terakota, dan linen tenun kasar",
                "Dinding plester hangat warna krem pasir"
            ),
            furnitureCharacteristics = listOf(
                "Bentuk organik melengkung & meja bundar ramah berkumpul",
                "Nuansa bumi (earthy tones) yang hangat dan mengundang"
            ),
            lightingCharacteristics = listOf(
                "Cahaya keemasan hangat seperti matahari sore Mediterania",
                "Lampu gantung serat alami & lampu sudut lembut"
            ),
            spaceUtilizationTip = "Padukan karpet jute alami dengan furnitur kayu hangat dan aksen terakota untuk suasana santai khas vila pesisir.",
            exampleRoomImages = listOf(
                "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=800&q=80"
            ),
            recommendedProductIds = listOf("prod_rug_02", "prod_dtable_01", "prod_chair_02", "prod_decor_02")
        ),
        DesignStyle.MID_CENTURY to StyleMoodboardProfile(
            style = DesignStyle.MID_CENTURY,
            colorPaletteNamed = listOf(
                "Walnut Klasik" to 0xFF8B8278,
                "Amber Caramel" to 0xFFB68C65,
                "Olive Retro" to 0xFF47866C,
                "Espresso Gelap" to 0xFF252525
            ),
            materialSuggestions = listOf(
                "Kayu jati & walnut berwarna cokelat hangat",
                "Kain tweed, velvet mustard, dan aksen kuningan",
                "Besi hitam ramping berpadu kayu solid"
            ),
            furnitureCharacteristics = listOf(
                "Kaki meruncing miring (splayed tapered legs) ikonik abad 20",
                "Garis siluet tegas namun ergonomis dan timeless",
                "Kabinet sideboard rendah dengan penyimpanan fungsional"
            ),
            lightingCharacteristics = listOf(
                "Lampu lantai arc brass & lampu gantung bola/geometris",
                "Warm ambient lighting 2700K"
            ),
            spaceUtilizationTip = "Pilih satu kursi lounge ikonik berwarna mustard dan bufet rendah kayu agar dinding atas tetap terasa lapang.",
            exampleRoomImages = listOf(
                "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?auto=format&fit=crop&w=800&q=80"
            ),
            recommendedProductIds = listOf("prod_chair_03", "prod_ctable_02", "prod_storage_03", "prod_flamp_02", "prod_sofa_03")
        ),
        DesignStyle.BOHEMIAN to StyleMoodboardProfile(
            style = DesignStyle.BOHEMIAN,
            colorPaletteNamed = listOf(
                "Karamel Hangat" to 0xFFB68C65,
                "Krem Rotan" to 0xFFD4B393,
                "Merah Bata Etnik" to 0xFFBA4444,
                "Hijau Botani" to 0xFF47866C
            ),
            materialSuggestions = listOf(
                "Anyaman rotan, makrame, dan serat jute",
                "Tekstil berlapis (karpet motif & bantal tekstur)",
                "Kayu pinus/jati natural & tanaman gantung"
            ),
            furnitureCharacteristics = listOf(
                "Penataan santai, hangat, dan kaya tekstur",
                "Kursi santai rotan, cermin bundar, dan karpet tenun",
                "Kombinasi elemen seni dinding dan tanaman hijau"
            ),
            lightingCharacteristics = listOf(
                "Lampu gantung anyaman bambu & standing lamp sudut",
                "Cahaya temaram hangat yang artistik dan nyaman"
            ),
            spaceUtilizationTip = "Lapisi lantai dengan karpet jute alami dan tambahkan cermin rotan bundar agar tekstur kaya tetap terlihat rapi dan terang.",
            exampleRoomImages = listOf(
                "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=800&q=80"
            ),
            recommendedProductIds = listOf("prod_rug_02", "prod_chair_02", "prod_decor_02", "prod_clamp_01", "prod_decor_01")
        )
    )

    override fun getStyleMoodboardProfile(style: DesignStyle): StyleMoodboardProfile {
        return styleProfiles[style] ?: styleProfiles.getValue(DesignStyle.JAPANDI)
    }

    override fun getAllStyleMoodboardProfiles(): List<StyleMoodboardProfile> {
        return DesignStyle.values().map { getStyleMoodboardProfile(it) }
    }

    override fun parseBudgetFromPrompt(text: String): Long? {
        val lower = text.lowercase()

        // Match patterns like "1,5 juta", "1.5 juta", "2 juta", "2jt"
        val jutaRegex = Regex("""(\d+(?:[.,]\d+)?)\s*(?:juta|jt)\b""")
        val jutaMatch = jutaRegex.find(lower)
        if (jutaMatch != null) {
            val numStr = jutaMatch.groupValues[1].replace(',', '.')
            val value = numStr.toDoubleOrNull()
            if (value != null && value > 0) {
                return (value * 1_000_000).toLong()
            }
        }

        // Match patterns like "500 ribu", "750rb"
        val ribuRegex = Regex("""(\d+(?:[.,]\d+)?)\s*(?:ribu|rb)\b""")
        val ribuMatch = ribuRegex.find(lower)
        if (ribuMatch != null) {
            val numStr = ribuMatch.groupValues[1].replace(',', '.')
            val value = numStr.toDoubleOrNull()
            if (value != null && value > 0) {
                return (value * 1_000).toLong()
            }
        }

        // Match full IDR number like "Rp 1.500.000" or "Rp1500000"
        val rpRegex = Regex("""rp\.?\s*([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{6,9})""")
        val rpMatch = rpRegex.find(lower)
        if (rpMatch != null) {
            val digitsOnly = rpMatch.groupValues[1].replace(".", "").replace(",", "")
            val parsed = digitsOnly.toLongOrNull()
            if (parsed != null && parsed >= 100_000L) {
                return parsed
            }
        }

        return null
    }

    override fun parseStyleFromPrompt(text: String): DesignStyle? {
        val lower = text.lowercase()
        return when {
            lower.contains("japandi") -> DesignStyle.JAPANDI
            lower.contains("scandinavian") || lower.contains("skandinavia") || lower.contains("nordik") || lower.contains("nordic") -> DesignStyle.SCANDINAVIAN
            lower.contains("minimalis") || lower.contains("minimalist") -> DesignStyle.MINIMALIST
            lower.contains("industrial") -> DesignStyle.INDUSTRIAL
            lower.contains("tropis") || lower.contains("tropical") -> DesignStyle.TROPICAL
            lower.contains("retro") || lower.contains("80s") || lower.contains("80-an") -> DesignStyle.RETRO_80S
            lower.contains("luxury") || lower.contains("mewah") || lower.contains("elegan") || lower.contains("marmer") -> DesignStyle.MODERN_LUXURY
            lower.contains("mediterania") || lower.contains("mediterranean") -> DesignStyle.MEDITERRANEAN
            lower.contains("mid-century") || lower.contains("mid century") -> DesignStyle.MID_CENTURY
            lower.contains("bohemian") || lower.contains("boho") -> DesignStyle.BOHEMIAN
            else -> null
        }
    }

    fun parseCategoryFromPrompt(text: String): ProductCategory? {
        val lower = text.lowercase()
        return when {
            lower.contains("sofa") || lower.contains("couch") -> ProductCategory.SOFA
            lower.contains("meja kopi") || lower.contains("meja tamu") || lower.contains("coffee table") -> ProductCategory.COFFEE_TABLE
            lower.contains("meja makan") || lower.contains("dining") -> ProductCategory.DINING_TABLE
            lower.contains("kursi kerja") || lower.contains("kursi belajar") || lower.contains("ergonomis") || lower.contains("wfh") -> ProductCategory.OFFICE_CHAIR
            lower.contains("kursi") || lower.contains("armchair") || lower.contains("lounge") -> ProductCategory.CHAIR
            lower.contains("lampu lantai") || lower.contains("standing lamp") || lower.contains("floor lamp") || lower.contains("lampu sudut") -> ProductCategory.FLOOR_LAMP
            lower.contains("lampu gantung") || lower.contains("ceiling") || lower.contains("chandelier") -> ProductCategory.CEILING_LAMP
            lower.contains("lampu") || lower.contains("pencahayaan") -> ProductCategory.FLOOR_LAMP
            lower.contains("karpet") || lower.contains("rug") -> ProductCategory.RUG
            lower.contains("rak") || lower.contains("ambalan") || lower.contains("shelf") -> ProductCategory.SHELVES
            lower.contains("lemari") || lower.contains("bufet") || lower.contains("sideboard") || lower.contains("laci") || lower.contains("storage") -> ProductCategory.STORAGE
            lower.contains("cermin") || lower.contains("lukisan") || lower.contains("dekorasi") || lower.contains("tanaman") || lower.contains("dinding") -> ProductCategory.WALL_DECOR
            lower.contains("kasur") || lower.contains("dipan") || lower.contains("tempat tidur") || lower.contains("nakas") || lower.contains("bed") -> ProductCategory.BEDROOM_FURNITURE
            else -> null
        }
    }

    override suspend fun getContextualRecommendations(
        context: ConsultationContext,
        queryText: String,
        requestedStyle: DesignStyle?,
        budgetLimitIdr: Long?,
        limit: Int
    ): List<ProductRecommendationItem> {
        val catalog = if (context.availableCatalog.isNotEmpty()) {
            context.availableCatalog
        } else {
            MockData.products
        }

        val targetStyle = requestedStyle
            ?: parseStyleFromPrompt(queryText)
            ?: context.effectiveStyle
            ?: DesignStyle.JAPANDI

        val targetCategory = parseCategoryFromPrompt(queryText)
        val promptBudget = budgetLimitIdr ?: parseBudgetFromPrompt(queryText)
        val effectiveBudget = promptBudget ?: context.effectiveBudgetIdr

        val alreadyPlacedIds = context.scene.placements.map { it.productId }.toSet()
        val roomWidthCm = context.scene.userDimensionsCm?.width

        // 1. Filter by category if user explicitly asked for a category (e.g., "Cari sofa di bawah Rp1,5 juta")
        val categoryCandidates = if (targetCategory != null) {
            catalog.filter { it.category == targetCategory }
        } else {
            // Filter by room relevance & style compatibility
            val styleMatched = catalog.filter {
                it.designStyle == targetStyle || it.compatibleStyles.contains(targetStyle)
            }
            styleMatched.ifEmpty { catalog }
        }

        // 2. Score candidates based on style match, budget fit, room dimensions, and placement novelty
        val scored = categoryCandidates.map { product ->
            var score = 0
            if (product.designStyle == targetStyle) score += 40
            else if (product.compatibleStyles.contains(targetStyle)) score += 25

            if (product.priceIdr <= effectiveBudget) score += 35
            else score -= 20

            if (!alreadyPlacedIds.contains(product.id)) score += 10

            // Compact room awareness
            if (roomWidthCm != null && roomWidthCm <= 320 && product.dimensions.width <= 150) {
                score += 15
            }

            Pair(product, score)
        }.sortedWith(
            compareByDescending<Pair<Product, Int>> { it.second }
                .thenBy { it.first.priceIdr }
        )

        // 3. If budget is tight and user didn't lock a single category, pick a balanced bundle within budget
        val selectedProducts = mutableListOf<Product>()
        var runningCost = 0L

        for ((product, _) in scored) {
            if (selectedProducts.size >= limit) break
            if (targetCategory != null) {
                // Single category query: prefer items <= effectiveBudget first
                if (product.priceIdr <= effectiveBudget || selectedProducts.isEmpty()) {
                    selectedProducts.add(product)
                }
            } else {
                // Multi-item makeover / general advice: try to keep cumulative package <= effectiveBudget
                if (runningCost + product.priceIdr <= effectiveBudget || selectedProducts.isEmpty()) {
                    selectedProducts.add(product)
                    runningCost += product.priceIdr
                }
            }
        }

        return selectedProducts.map { product ->
            val fitsSingleBudget = product.priceIdr <= effectiveBudget
            val delta = if (fitsSingleBudget) {
                effectiveBudget - product.priceIdr
            } else {
                product.priceIdr - effectiveBudget
            }

            val styleLabel = if (product.designStyle == targetStyle) {
                "Utama: ${targetStyle.displayName}"
            } else if (product.compatibleStyles.contains(targetStyle)) {
                "Kompatibel: ${targetStyle.displayName}"
            } else {
                "Gaya: ${product.designStyle.displayName}"
            }

            val dimReason = if (roomWidthCm != null) {
                "Dimensi ${product.dimensions.toDisplayString()} proporsional untuk lebar ruangan ${roomWidthCm} cm."
            } else {
                "Dimensi ${product.dimensions.toDisplayString()} mudah ditata tanpa menghalangi sirkulasi."
            }

            val budgetReason = if (fitsSingleBudget) {
                "Sesuai batas anggaran Rp ${"%,d".format(effectiveBudget).replace(',', '.')}."
            } else {
                "Melebihi batas anggaran sebesar +Rp ${"%,d".format(delta).replace(',', '.')} (opsi terdekat di katalog)."
            }

            ProductRecommendationItem(
                productId = product.id,
                productTitle = product.title,
                categoryName = product.category.displayName,
                priceIdr = product.priceIdr,
                imageUrl = product.imageUrl,
                transparentCutoutUri = product.transparentCutoutUrl,
                dimensions = product.dimensions,
                primaryStyle = product.designStyle,
                compatibleStyles = product.compatibleStyles,
                reason = "${product.material}. $dimReason $budgetReason",
                styleCompatibilityLabel = styleLabel,
                fitsBudget = fitsSingleBudget,
                budgetDeltaIdr = delta
            )
        }
    }

    fun createInitialGreeting(scene: RoomScene, conversationId: String): ChatMessage {
        val styleName = scene.preferredStyle?.displayName ?: "Japandi / Minimalis"
        val dimText = scene.userDimensionsCm?.let { "${it.width}x${it.depth}x${it.height} cm" }
            ?: "belum diatur (dapat diubah di info proyek)"
        val budgetFormatted = "Rp " + "%,d".format(scene.targetBudget).replace(',', '.')

        val greeting = buildString {
            append("Halo! Saya Konsultan Interior AI RuangAI (Mode Simulasi) untuk proyek **${scene.title}** (${scene.roomType.displayName}).\n\n")
            append("• **Gaya Aktif:** $styleName\n")
            append("• **Dimensi Ruangan:** $dimText\n")
            append("• **Target Anggaran Ruangan:** $budgetFormatted\n")
            append("• **Furnitur Terpasang:** ${scene.placements.size} item\n\n")
            append("Silakan tanyakan rekomendasi furnitur, kombinasi warna, tata letak hemat ruang, atau penyesuaian budget!")
        }

        return ChatMessage(
            id = "msg_welcome_${scene.id}",
            isFromUser = false,
            text = greeting,
            conversationId = conversationId,
            senderRole = SenderRole.ASSISTANT,
            suggestedActions = listOf(
                "Cari sofa di bawah Rp1,5 juta",
                "Buat ruangan lebih mewah budget Rp2 juta",
                "Bagaimana cara membuat ruangan terlihat luas?",
                "Buka Style Explorer"
            ),
            isSimulated = true,
            deliveryStatus = MessageDeliveryStatus.DELIVERED
        )
    }

    suspend fun composeContextualAssistantMessage(
        conversationId: String,
        context: ConsultationContext,
        userText: String,
        attachments: List<ChatAttachment>
    ): ChatMessage {
        val scene = context.scene
        val promptStyle = parseStyleFromPrompt(userText)
        val promptBudget = parseBudgetFromPrompt(userText)
        val activeStyle = promptStyle ?: context.effectiveStyle ?: DesignStyle.JAPANDI
        val moodboard = getStyleMoodboardProfile(activeStyle)
        val evaluatedBudget = promptBudget ?: context.effectiveBudgetIdr

        val recommendations = getContextualRecommendations(
            context = context,
            queryText = userText,
            requestedStyle = activeStyle,
            budgetLimitIdr = evaluatedBudget,
            limit = 3
        )

        val fmtRoomBudget = "Rp " + "%,d".format(scene.targetBudget).replace(',', '.')
        val fmtEvaluatedBudget = "Rp " + "%,d".format(evaluatedBudget).replace(',', '.')
        val fmtPlacedTotal = "Rp " + "%,d".format(context.placedItemsTotalIdr).replace(',', '.')
        val recTotalIdr = recommendations.sumOf { it.priceIdr }
        val fmtRecTotal = "Rp " + "%,d".format(recTotalIdr).replace(',', '.')

        val dimensionStatement = scene.userDimensionsCm?.let {
            "Dimensi terukur ruangan Anda (**${it.toDisplayString()}**)"
        } ?: "Dimensi fisik pasti ruangan belum diinput (${context.analysisResult?.approximateLayoutObservations ?: "menggunakan proporsi visual ruangan"})"

        val lower = userText.lowercase()
        val responseBody = buildString {
            // 1. Honest Multimodal Attachment Notice (if user attached a photo)
            if (attachments.isNotEmpty()) {
                val attLabels = attachments.joinToString(", ") { "'${it.label}'" }
                append("📷 **[Simulasi Lampiran Foto]** Foto $attLabels telah tersimpan di penyimpanan internal proyek **${scene.title}**. ")
                append("Analisis visi AI cloud belum diaktifkan pada mode demo ini; evaluasi berikut mengacu pada data proyek lokal dan katalog terverifikasi.\n\n")
            }

            // 2. Project & Context Header
            append("Berdasarkan konteks proyek **${scene.title}** (${scene.roomType.displayName}) dengan preferensi gaya **${activeStyle.displayName}**:\n\n")

            // 3. Topic-specific Interior Design Consultation
            when {
                lower.contains("luas") || lower.contains("sempit") || lower.contains("tata letak") || lower.contains("layout") -> {
                    append("**Strategi Tata Letak & Optimasi Ruang:**\n")
                    append("• $dimensionStatement: ${moodboard.spaceUtilizationTip}\n")
                    append("• **Pencahayaan:** ${moodboard.lightingCharacteristics.firstOrNull() ?: "Maksimalkan bukaan jendela alami"}.\n")
                    append("• **Palet Warna:** Gunakan kombinasi ${moodboard.colorPaletteNamed.take(3).joinToString(", ") { it.first }} agar batas visual dinding tampak mundur dan lapang.\n\n")
                }
                lower.contains("warna") || lower.contains("material") || lower.contains("bahan") || lower.contains("kayu") -> {
                    append("**Rekomendasi Warna & Material (${activeStyle.displayName}):**\n")
                    append("• **Palet Utama:** ${moodboard.colorPaletteNamed.joinToString(", ") { it.first }}.\n")
                    append("• **Material Kunci:** ${moodboard.materialSuggestions.joinToString("; ")}.\n")
                    append("• **Karakter Furnitur:** ${moodboard.furnitureCharacteristics.firstOrNull()}.\n\n")
                }
                lower.contains("lampu") || lower.contains("cahaya") || lower.contains("lighting") -> {
                    append("**Panduan Pencahayaan (${activeStyle.displayName}):**\n")
                    moodboard.lightingCharacteristics.forEach { tip ->
                        append("• $tip\n")
                    }
                    append("• $dimensionStatement sangat terbantu dengan lampu sudut vertikal yang tidak memakan ruang lantai.\n\n")
                }
                else -> {
                    append("**Analisis Gaya & Proporsi Ruangan:**\n")
                    append("• $dimensionStatement dipadukan dengan karakter **${activeStyle.displayName}** (${activeStyle.subtitle}).\n")
                    append("• **Material & Warna:** ${moodboard.materialSuggestions.take(2).joinToString(" & ")} dengan aksen warna ${moodboard.colorPaletteNamed.take(2).joinToString(" / ") { it.first }}.\n")
                    append("• **Tips Ruang:** ${moodboard.spaceUtilizationTip}\n\n")
                }
            }

            // 4. Budget Transparency Breakdown
            append("**Rincian Anggaran (Simulasi Katalog):**\n")
            if (promptBudget != null) {
                append("• **Anggaran Spesifik Permintaan Anda:** $fmtEvaluatedBudget\n")
            }
            append("• **Target Anggaran Proyek Ruangan:** $fmtRoomBudget\n")
            append("• **Furnitur Terpasang di Kanvas (${scene.placements.size} item):** $fmtPlacedTotal\n")
            append("• **Estimasi Paket Rekomendasi (${recommendations.size} produk):** $fmtRecTotal\n")

            val overBudgetItems = recommendations.filter { !it.fitsBudget }
            if (overBudgetItems.isNotEmpty()) {
                val item = overBudgetItems.first()
                val overrunStr = "Rp " + "%,d".format(item.budgetDeltaIdr).replace(',', '.')
                append("⚠️ *Catatan Overrun Anggaran:* **${item.productTitle}** (${item.getFormattedPrice()}) melebihi batas $fmtEvaluatedBudget sebesar **+$overrunStr** karena merupakan opsi harga terdekat pada kategori tersebut di katalog saat ini.\n")
            } else if (recTotalIdr > evaluatedBudget && recommendations.size > 1) {
                val diffStr = "Rp " + "%,d".format(recTotalIdr - evaluatedBudget).replace(',', '.')
                append("💡 *Tips Prioritas Budget:*Setiap produk secara individual masuk dalam batas $fmtEvaluatedBudget. Jika ingin mengambil ketiganya sekaligus (total $fmtRecTotal, selisih +$diffStr), Anda dapat mencicil pembelian mulai dari **${recommendations.first().productTitle}** (${recommendations.first().getFormattedPrice()}).\n")
            }

            append("\nSilakan gunakan kartu produk di bawah untuk melihat detail, membandingkan, menambahkan ke Shopping List, atau mencoba langsung di **Virtual Try-On**!")
        }

        return ChatMessage(
            id = "msg_ai_${java.util.UUID.randomUUID().toString().take(8)}",
            isFromUser = false,
            text = responseBody,
            timestamp = System.currentTimeMillis(),
            suggestedActions = listOf(
                "Coba Furnitur di Ruangan",
                "Lihat Daftar Belanja",
                "Eksplorasi Gaya Interior"
            ),
            conversationId = conversationId,
            senderRole = SenderRole.ASSISTANT,
            attachments = emptyList(),
            recommendedProducts = recommendations,
            evaluatedBudgetIdr = evaluatedBudget,
            isSimulated = true,
            deliveryStatus = MessageDeliveryStatus.DELIVERED
        )
    }
}
