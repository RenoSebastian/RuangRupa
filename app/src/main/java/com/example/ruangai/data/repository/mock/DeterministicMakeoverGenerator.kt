package com.example.ruangai.data.repository.mock

import com.example.ruangai.domain.model.*
import kotlin.math.abs

/**
 * Deterministic mock engine for Full Room Makeover AI Design Generation (Milestone 7 Part B, C, D).
 *
 * Guarantees:
 * - Clearly labels all generated results and variations as simulated (`isSimulated = true`).
 * - Never claims real cloud AI image generation or fabricates external provider payloads.
 * - Never mutates or overwrites the original room photo URI (`request.originalRoomImageUri`).
 * - Produces deterministic multiple variations (default 3) for any given configuration.
 * - Matches only real, verified products from [MockData.products] and explicitly distinguishes
 *   catalog-matched furniture from simulated visual preview elements.
 */
class DeterministicMakeoverGenerator(
    private val catalogProducts: List<Product> = MockData.products
) {

    private val styleVariationPreviewPool: Map<DesignStyle, List<String>> = mapOf(
        DesignStyle.JAPANDI to listOf(
            "https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=1000&q=80"
        ),
        DesignStyle.SCANDINAVIAN to listOf(
            "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=1000&q=80"
        ),
        DesignStyle.MINIMALIST to listOf(
            "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=1000&q=80"
        ),
        DesignStyle.INDUSTRIAL to listOf(
            "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1540518614846-7ede433c457?auto=format&fit=crop&w=1000&q=80"
        ),
        DesignStyle.MODERN_LUXURY to listOf(
            "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=1000&q=80"
        ),
        DesignStyle.TROPICAL to listOf(
            "https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1615873968403-89e068629265?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=1000&q=80"
        ),
        DesignStyle.MEDITERRANEAN to listOf(
            "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=1000&q=80"
        ),
        DesignStyle.MID_CENTURY to listOf(
            "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1000&q=80"
        ),
        DesignStyle.BOHEMIAN to listOf(
            "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?auto=format&fit=crop&w=1000&q=80"
        ),
        DesignStyle.RETRO_80S to listOf(
            "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1000&q=80"
        )
    )

    private val stylePaletteHexLookup: Map<DesignStyle, List<Long>> = mapOf(
        DesignStyle.JAPANDI to listOf(0xFFF6F2EC, 0xFFD9C3A9, 0xFF8A9A86, 0xFF3E3A35),
        DesignStyle.SCANDINAVIAN to listOf(0xFFFAFAFA, 0xFFE5DFD5, 0xFFA7B8C4, 0xFFD4A373),
        DesignStyle.MINIMALIST to listOf(0xFFF4F4F2, 0xFFDAD7CD, 0xFF525252, 0xFF1F1F1F),
        DesignStyle.INDUSTRIAL to listOf(0xFF4A4A48, 0xFF8C4A32, 0xFFA39B8B, 0xFF1E1E1E),
        DesignStyle.MODERN_LUXURY to listOf(0xFFEDEAE4, 0xFF2B2D2F, 0xFFC59B6D, 0xFF4A5D4E),
        DesignStyle.TROPICAL to listOf(0xFFF7F4EB, 0xFF3A5A40, 0xFFC68B59, 0xFFD4C5A9),
        DesignStyle.MEDITERRANEAN to listOf(0xFFF5EFE6, 0xFF5C3A21, 0xFFC8A96E, 0xFF2C3E35),
        DesignStyle.MID_CENTURY to listOf(0xFFF4EFE6, 0xFFB55D35, 0xFF5E7153, 0xFFD99B26),
        DesignStyle.BOHEMIAN to listOf(0xFFF3ECE1, 0xFFC86D51, 0xFF7A8B69, 0xFFD4A359),
        DesignStyle.RETRO_80S to listOf(0xFFF7E7CE, 0xFF2A9D8F, 0xFFE76F51, 0xFFE9C46A)
    )

    fun generateMakeoverResult(
        request: DesignGenerationRequest,
        resultId: String = "res_${deterministicToken(request)}"
    ): DesignGenerationResult {
        val count = request.variationCount.coerceIn(1, 5)
        val variations = (0 until count).map { idx ->
            buildVariation(
                request = request,
                resultId = resultId,
                variationIndex = idx
            )
        }

        return DesignGenerationResult(
            resultId = resultId,
            requestId = request.requestId,
            idempotencyKey = request.idempotencyKey,
            sceneId = request.sceneId,
            originalRoomImageUri = request.originalRoomImageUri,
            roomType = request.roomType,
            selectedStyle = request.selectedStyle,
            colorPaletteNames = request.colorPaletteNames,
            desiredAtmosphere = request.desiredAtmosphere,
            furniturePreferences = request.furniturePreferences,
            targetBudgetIdr = request.targetBudgetIdr,
            additionalInstructions = request.additionalInstructions,
            status = DesignGenerationStatus.COMPLETED,
            variations = variations,
            selectedVariationId = variations.firstOrNull()?.variationId,
            creditCost = request.creditCost,
            creditReserved = true,
            creditCaptured = true,
            creditRefunded = false,
            errorMessage = null,
            isSimulated = true,
            createdAt = request.createdAt,
            updatedAt = request.createdAt
        )
    }

    private fun buildVariation(
        request: DesignGenerationRequest,
        resultId: String,
        variationIndex: Int
    ): DesignVariation {
        val style = request.selectedStyle
        val previews = styleVariationPreviewPool[style] ?: listOf(style.imageUrl)
        val previewUrl = previews[variationIndex % previews.size]

        val archetypeName = when (variationIndex) {
            0 -> "Variasi 1: Harmoni Utama ${style.displayName}"
            1 -> "Variasi 2: Aksen Kontras & Tekstur ${style.displayName}"
            2 -> "Variasi 3: Tata Ruang Efisien & Hemat Budget"
            else -> "Variasi ${variationIndex + 1}: Eksplorasi ${style.displayName}"
        }

        val matchedProducts = selectCatalogMatchesForVariation(
            request = request,
            variationIndex = variationIndex
        )

        val estimatedBudget = matchedProducts.sumOf { it.priceIdr }
        val instructionNote = if (request.additionalInstructions.isNotBlank()) {
            " Instruksi khusus diterapkan: \"${request.additionalInstructions.trim()}\"."
        } else {
            ""
        }

        val paletteText = request.colorPaletteNames.joinToString(", ")
        val summary = when (variationIndex) {
            0 -> "Transformasi menyeluruh ${request.roomType.displayName} bergaya ${style.displayName} dengan suasana ${request.desiredAtmosphere.displayName.lowercase()}. Memadukan palet warna ($paletteText) dan titik fokus furnitur proporsional.$instructionNote"
            1 -> "Eksplorasi lapisan tekstur dan pencahayaan aksen untuk ${request.roomType.displayName} bergaya ${style.displayName}. Menonjolkan kontras material hangat dengan nuansa ${request.desiredAtmosphere.displayName.lowercase()}.$instructionNote"
            else -> "Penataan ${request.roomType.displayName} bergaya ${style.displayName} yang memaksimalkan sirkulasi lantai terbuka dan efisiensi anggaran (estimasi Rp ${formatIdr(estimatedBudget)} dari target Rp ${formatIdr(request.targetBudgetIdr)}).$instructionNote"
        }

        val layoutNotes = "${request.desiredAtmosphere.lightingHint} • Palet: $paletteText • Estimasi 3 furnitur utama: Rp ${formatIdr(estimatedBudget)}"

        val hotspots = matchedProducts.map { item ->
            DesignHotspotPin(
                pinId = "pin_${resultId}_${variationIndex}_${item.productId}",
                productId = item.productId,
                label = item.productTitle,
                priceIdr = item.priceIdr,
                normalizedX = item.normalizedHotspotX,
                normalizedY = item.normalizedHotspotY
            )
        }

        val varToken = deterministicToken(request)
        return DesignVariation(
            variationId = "var_${varToken}_${variationIndex + 1}",
            resultId = resultId,
            requestId = request.requestId,
            sceneId = request.sceneId,
            variationIndex = variationIndex,
            variationTitle = archetypeName,
            style = style,
            colorPaletteNames = request.colorPaletteNames,
            colorPaletteHexes = stylePaletteHexLookup[style] ?: listOf(0xFFF6F2EC, 0xFFD9C3A9, 0xFF3E3A35),
            atmosphere = request.desiredAtmosphere,
            roomType = request.roomType,
            previewImageUrl = previewUrl,
            designSummary = summary,
            layoutAndLightingNotes = layoutNotes,
            estimatedFurnitureBudgetIdr = estimatedBudget,
            targetBudgetIdr = request.targetBudgetIdr,
            catalogMatchedProducts = matchedProducts,
            hotspotPins = hotspots,
            isFavorite = false,
            isSimulated = true,
            createdAt = request.createdAt + variationIndex
        )
    }

    private fun selectCatalogMatchesForVariation(
        request: DesignGenerationRequest,
        variationIndex: Int
    ): List<CatalogMatchedDesignProduct> {
        val preferredCategories = request.furniturePreferences.filter { it != ProductCategory.ALL }

        // Score every real catalog product deterministically
        val scored = catalogProducts.map { product ->
            var score = 0
            if (product.designStyle == request.selectedStyle) score += 60
            else if (product.compatibleStyles.contains(request.selectedStyle)) score += 35

            if (preferredCategories.isNotEmpty() && product.category in preferredCategories) {
                score += 45
            }

            // Variation-specific differentiation so Variations 1, 2, and 3 highlight distinct combinations
            when (variationIndex) {
                0 -> {
                    score += product.compatibilityScore / 4
                    if (product.priceIdr <= request.targetBudgetIdr / 2) score += 20
                }
                1 -> {
                    score += product.popularityScore / 4
                    if (product.category in listOf(ProductCategory.LIGHTING, ProductCategory.FLOOR_LAMP, ProductCategory.DECOR, ProductCategory.RUG, ProductCategory.CHAIR, ProductCategory.COFFEE_TABLE)) {
                        score += 25
                    }
                }
                else -> {
                    // Budget efficiency variation favors lower price items
                    val budgetBonus = ((3_000_000L - product.priceIdr.coerceAtMost(3_000_000L)) / 80_000L).toInt()
                    score += budgetBonus
                }
            }

            product to score
        }.sortedWith(
            compareByDescending<Pair<Product, Int>> { it.second }
                .thenBy { it.first.priceIdr }
                .thenBy { it.first.id }
        )

        // Pick 3 distinct categories when possible, shifting selection slightly per variationIndex
        val chosen = mutableListOf<Product>()
        val usedCategories = mutableSetOf<ProductCategory>()

        val rotatedCandidates = if (variationIndex > 0 && scored.size > 3) {
            scored.drop(variationIndex) + scored.take(variationIndex)
        } else {
            scored
        }

        for ((candidate, _) in rotatedCandidates) {
            if (chosen.size >= 3) break
            if (candidate.category !in usedCategories) {
                chosen.add(candidate)
                usedCategories.add(candidate.category)
            }
        }

        // Fill up to 3 if fewer categories matched
        for ((candidate, _) in rotatedCandidates) {
            if (chosen.size >= 3) break
            if (candidate.id !in chosen.map { it.id }) {
                chosen.add(candidate)
            }
        }

        val hotspotPositions = listOf(
            0.35f to 0.62f,
            0.68f to 0.56f,
            0.52f to 0.78f
        )

        return chosen.mapIndexed { idx, prod ->
            val (hx, hy) = hotspotPositions[idx % hotspotPositions.size]
            val role = when (idx) {
                0 -> "Titik Fokus Utama (${prod.category.displayName})"
                1 -> "Pendukung Fungsi & Estetika (${prod.category.displayName})"
                else -> "Pelengkap Suasana & Pencahayaan (${prod.category.displayName})"
            }
            val reason = buildString {
                if (prod.designStyle == request.selectedStyle) {
                    append("Gaya utama ${request.selectedStyle.displayName}")
                } else {
                    append("Kompatibel dengan ${request.selectedStyle.displayName}")
                }
                append(" • Dimensi ${prod.dimensions.width}x${prod.dimensions.depth}x${prod.dimensions.height} cm")
            }

            CatalogMatchedDesignProduct(
                productId = prod.id,
                productTitle = prod.title,
                category = prod.category,
                priceIdr = prod.priceIdr,
                style = prod.designStyle,
                roleInDesign = role,
                compatibilityReason = reason,
                normalizedHotspotX = hx,
                normalizedHotspotY = hy,
                isExactVisualMatch = false
            )
        }
    }

    fun deterministicToken(request: DesignGenerationRequest): String {
        val raw = listOf(
            request.sceneId,
            request.roomType.name,
            request.selectedStyle.name,
            request.colorPaletteNames.sorted().joinToString(","),
            request.desiredAtmosphere.name,
            request.furniturePreferences.map { it.name }.sorted().joinToString(","),
            request.targetBudgetIdr.toString(),
            request.additionalInstructions.trim().lowercase()
        ).joinToString("|")
        return abs(raw.hashCode()).toString(16)
    }

    private fun formatIdr(amount: Long): String = "%,d".format(amount).replace(',', '.')
}

