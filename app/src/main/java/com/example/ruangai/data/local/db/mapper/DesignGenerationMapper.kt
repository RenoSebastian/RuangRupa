package com.example.ruangai.data.local.db.mapper

import com.example.ruangai.data.local.db.entity.DesignGenerationRequestEntity
import com.example.ruangai.data.local.db.entity.DesignVariationEntity
import com.example.ruangai.domain.model.*
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import okio.Buffer

/**
 * Fault-tolerant serializer and mapper for [DesignGenerationRequestEntity] and [DesignVariationEntity].
 * Uses pure-JVM Moshi JsonReader/JsonWriter so it works identically in JVM unit tests, Robolectric, and Android.
 */
object DesignGenerationMapper {

    fun toDomainResult(
        entity: DesignGenerationRequestEntity,
        variationEntities: List<DesignVariationEntity>
    ): DesignGenerationResult {
        val roomType = try {
            RoomType.valueOf(entity.roomType)
        } catch (e: Exception) {
            RoomType.LIVING_ROOM
        }

        val selectedStyle = DesignStyle.values().find {
            it.name.equals(entity.selectedStyle, ignoreCase = true) ||
                it.id.equals(entity.selectedStyle, ignoreCase = true)
        } ?: DesignStyle.JAPANDI

        val atmosphere = try {
            DesignAtmosphere.valueOf(entity.desiredAtmosphere)
        } catch (e: Exception) {
            DesignAtmosphere.WARM_COZY
        }

        val status = try {
            DesignGenerationStatus.valueOf(entity.status)
        } catch (e: Exception) {
            DesignGenerationStatus.COMPLETED
        }

        val variations = variationEntities
            .sortedBy { it.variationIndex }
            .map { toDomainVariation(it) }

        return DesignGenerationResult(
            resultId = entity.resultId,
            requestId = entity.requestId,
            idempotencyKey = entity.idempotencyKey,
            sceneId = entity.sceneId,
            originalRoomImageUri = entity.originalRoomImageUri,
            roomType = roomType,
            selectedStyle = selectedStyle,
            colorPaletteNames = deserializeStringList(entity.colorPaletteNamesJson),
            desiredAtmosphere = atmosphere,
            furniturePreferences = deserializeCategoryList(entity.furniturePreferencesJson),
            targetBudgetIdr = entity.targetBudgetIdr,
            additionalInstructions = entity.additionalInstructions,
            status = status,
            variations = variations,
            selectedVariationId = entity.selectedVariationId ?: variations.firstOrNull()?.variationId,
            creditCost = entity.creditCost,
            creditReserved = entity.creditReserved,
            creditCaptured = entity.creditCaptured,
            creditRefunded = entity.creditRefunded,
            errorMessage = entity.errorMessage,
            isSimulated = entity.isSimulated,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toRequestEntity(domain: DesignGenerationResult): DesignGenerationRequestEntity {
        return DesignGenerationRequestEntity(
            resultId = domain.resultId,
            requestId = domain.requestId,
            idempotencyKey = domain.idempotencyKey,
            sceneId = domain.sceneId,
            originalRoomImageUri = domain.originalRoomImageUri,
            roomType = domain.roomType.name,
            selectedStyle = domain.selectedStyle.name,
            colorPaletteNamesJson = serializeStringList(domain.colorPaletteNames),
            desiredAtmosphere = domain.desiredAtmosphere.name,
            furniturePreferencesJson = serializeStringList(domain.furniturePreferences.map { it.name }),
            targetBudgetIdr = domain.targetBudgetIdr,
            additionalInstructions = domain.additionalInstructions,
            status = domain.status.name,
            selectedVariationId = domain.selectedVariationId,
            creditCost = domain.creditCost,
            creditReserved = domain.creditReserved,
            creditCaptured = domain.creditCaptured,
            creditRefunded = domain.creditRefunded,
            errorMessage = domain.errorMessage,
            isSimulated = domain.isSimulated,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    fun toDomainVariation(entity: DesignVariationEntity): DesignVariation {
        val style = DesignStyle.values().find {
            it.name.equals(entity.style, ignoreCase = true) ||
                it.id.equals(entity.style, ignoreCase = true)
        } ?: DesignStyle.JAPANDI

        val atmosphere = try {
            DesignAtmosphere.valueOf(entity.atmosphere)
        } catch (e: Exception) {
            DesignAtmosphere.WARM_COZY
        }

        val roomType = try {
            RoomType.valueOf(entity.roomType)
        } catch (e: Exception) {
            RoomType.LIVING_ROOM
        }

        return DesignVariation(
            variationId = entity.variationId,
            resultId = entity.resultId,
            requestId = entity.requestId,
            sceneId = entity.sceneId,
            variationIndex = entity.variationIndex,
            variationTitle = entity.variationTitle,
            style = style,
            colorPaletteNames = deserializeStringList(entity.colorPaletteNamesJson),
            colorPaletteHexes = deserializeLongList(entity.colorPaletteHexesJson),
            atmosphere = atmosphere,
            roomType = roomType,
            previewImageUrl = entity.previewImageUrl,
            designSummary = entity.designSummary,
            layoutAndLightingNotes = entity.layoutAndLightingNotes,
            estimatedFurnitureBudgetIdr = entity.estimatedFurnitureBudgetIdr,
            targetBudgetIdr = entity.targetBudgetIdr,
            catalogMatchedProducts = deserializeCatalogMatchedProducts(entity.catalogMatchedProductsJson),
            hotspotPins = deserializeHotspotPins(entity.hotspotPinsJson),
            isFavorite = entity.isFavorite,
            isSimulated = entity.isSimulated,
            createdAt = entity.createdAt
        )
    }

    fun toVariationEntity(domain: DesignVariation): DesignVariationEntity {
        return DesignVariationEntity(
            variationId = domain.variationId,
            resultId = domain.resultId,
            requestId = domain.requestId,
            sceneId = domain.sceneId,
            variationIndex = domain.variationIndex,
            variationTitle = domain.variationTitle,
            style = domain.style.name,
            colorPaletteNamesJson = serializeStringList(domain.colorPaletteNames),
            colorPaletteHexesJson = serializeLongList(domain.colorPaletteHexes),
            atmosphere = domain.atmosphere.name,
            roomType = domain.roomType.name,
            previewImageUrl = domain.previewImageUrl,
            designSummary = domain.designSummary,
            layoutAndLightingNotes = domain.layoutAndLightingNotes,
            estimatedFurnitureBudgetIdr = domain.estimatedFurnitureBudgetIdr,
            targetBudgetIdr = domain.targetBudgetIdr,
            catalogMatchedProductsJson = serializeCatalogMatchedProducts(domain.catalogMatchedProducts),
            hotspotPinsJson = serializeHotspotPins(domain.hotspotPins),
            isFavorite = domain.isFavorite,
            isSimulated = domain.isSimulated,
            createdAt = domain.createdAt
        )
    }

    fun serializeStringList(items: List<String>): String {
        val buffer = Buffer()
        JsonWriter.of(buffer).use { writer ->
            writer.beginArray()
            for (item in items) {
                writer.value(item)
            }
            writer.endArray()
        }
        return buffer.readUtf8()
    }

    fun deserializeStringList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return try {
            val result = mutableListOf<String>()
            val buffer = Buffer().writeUtf8(json)
            JsonReader.of(buffer).use { reader ->
                reader.beginArray()
                while (reader.hasNext()) {
                    result.add(reader.nextString())
                }
                reader.endArray()
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeLongList(items: List<Long>): String {
        val buffer = Buffer()
        JsonWriter.of(buffer).use { writer ->
            writer.beginArray()
            for (item in items) {
                writer.value(item)
            }
            writer.endArray()
        }
        return buffer.readUtf8()
    }

    fun deserializeLongList(json: String): List<Long> {
        if (json.isBlank()) return emptyList()
        return try {
            val result = mutableListOf<Long>()
            val buffer = Buffer().writeUtf8(json)
            JsonReader.of(buffer).use { reader ->
                reader.beginArray()
                while (reader.hasNext()) {
                    result.add(reader.nextLong())
                }
                reader.endArray()
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deserializeCategoryList(json: String): List<ProductCategory> {
        return deserializeStringList(json).mapNotNull { raw ->
            try {
                ProductCategory.valueOf(raw)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun serializeCatalogMatchedProducts(items: List<CatalogMatchedDesignProduct>): String {
        val buffer = Buffer()
        JsonWriter.of(buffer).use { writer ->
            writer.beginArray()
            for (item in items) {
                writer.beginObject()
                writer.name("productId").value(item.productId)
                writer.name("productTitle").value(item.productTitle)
                writer.name("category").value(item.category.name)
                writer.name("priceIdr").value(item.priceIdr)
                writer.name("style").value(item.style.name)
                writer.name("roleInDesign").value(item.roleInDesign)
                writer.name("compatibilityReason").value(item.compatibilityReason)
                writer.name("normalizedHotspotX").value(item.normalizedHotspotX.toDouble())
                writer.name("normalizedHotspotY").value(item.normalizedHotspotY.toDouble())
                writer.name("isExactVisualMatch").value(item.isExactVisualMatch)
                writer.name("matchDistinctionNotice").value(item.matchDistinctionNotice)
                writer.endObject()
            }
            writer.endArray()
        }
        return buffer.readUtf8()
    }

    fun deserializeCatalogMatchedProducts(json: String): List<CatalogMatchedDesignProduct> {
        if (json.isBlank()) return emptyList()
        return try {
            val list = mutableListOf<CatalogMatchedDesignProduct>()
            val buffer = Buffer().writeUtf8(json)
            JsonReader.of(buffer).use { reader ->
                reader.beginArray()
                while (reader.hasNext()) {
                    var productId = ""
                    var productTitle = ""
                    var categoryName = "SOFA"
                    var priceIdr = 0L
                    var styleName = "JAPANDI"
                    var roleInDesign = ""
                    var compatibilityReason = ""
                    var normalizedHotspotX = 0.5f
                    var normalizedHotspotY = 0.6f
                    var isExactVisualMatch = false
                    var matchDistinctionNotice = "Produk Katalog Serupa — cocok secara gaya, fungsi, dan anggaran dari katalog RuangAI."

                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "productId" -> productId = reader.nextString()
                            "productTitle" -> productTitle = reader.nextString()
                            "category" -> categoryName = reader.nextString()
                            "priceIdr" -> priceIdr = reader.nextLong()
                            "style" -> styleName = reader.nextString()
                            "roleInDesign" -> roleInDesign = reader.nextString()
                            "compatibilityReason" -> compatibilityReason = reader.nextString()
                            "normalizedHotspotX" -> normalizedHotspotX = reader.nextDouble().toFloat()
                            "normalizedHotspotY" -> normalizedHotspotY = reader.nextDouble().toFloat()
                            "isExactVisualMatch" -> isExactVisualMatch = reader.nextBoolean()
                            "matchDistinctionNotice" -> matchDistinctionNotice = reader.nextString()
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()

                    if (productId.isNotBlank()) {
                        val category = try {
                            ProductCategory.valueOf(categoryName)
                        } catch (e: Exception) {
                            ProductCategory.SOFA
                        }
                        val style = try {
                            DesignStyle.valueOf(styleName)
                        } catch (e: Exception) {
                            DesignStyle.JAPANDI
                        }
                        list.add(
                            CatalogMatchedDesignProduct(
                                productId = productId,
                                productTitle = productTitle,
                                category = category,
                                priceIdr = priceIdr,
                                style = style,
                                roleInDesign = roleInDesign,
                                compatibilityReason = compatibilityReason,
                                normalizedHotspotX = normalizedHotspotX,
                                normalizedHotspotY = normalizedHotspotY,
                                isExactVisualMatch = isExactVisualMatch,
                                matchDistinctionNotice = matchDistinctionNotice
                            )
                        )
                    }
                }
                reader.endArray()
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeHotspotPins(items: List<DesignHotspotPin>): String {
        val buffer = Buffer()
        JsonWriter.of(buffer).use { writer ->
            writer.beginArray()
            for (pin in items) {
                writer.beginObject()
                writer.name("pinId").value(pin.pinId)
                writer.name("productId").value(pin.productId)
                writer.name("label").value(pin.label)
                writer.name("priceIdr").value(pin.priceIdr)
                writer.name("normalizedX").value(pin.normalizedX.toDouble())
                writer.name("normalizedY").value(pin.normalizedY.toDouble())
                writer.endObject()
            }
            writer.endArray()
        }
        return buffer.readUtf8()
    }

    fun deserializeHotspotPins(json: String): List<DesignHotspotPin> {
        if (json.isBlank()) return emptyList()
        return try {
            val list = mutableListOf<DesignHotspotPin>()
            val buffer = Buffer().writeUtf8(json)
            JsonReader.of(buffer).use { reader ->
                reader.beginArray()
                while (reader.hasNext()) {
                    var pinId = ""
                    var productId = ""
                    var label = ""
                    var priceIdr = 0L
                    var normalizedX = 0.5f
                    var normalizedY = 0.5f

                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "pinId" -> pinId = reader.nextString()
                            "productId" -> productId = reader.nextString()
                            "label" -> label = reader.nextString()
                            "priceIdr" -> priceIdr = reader.nextLong()
                            "normalizedX" -> normalizedX = reader.nextDouble().toFloat()
                            "normalizedY" -> normalizedY = reader.nextDouble().toFloat()
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()

                    if (productId.isNotBlank()) {
                        list.add(
                            DesignHotspotPin(
                                pinId = pinId,
                                productId = productId,
                                label = label,
                                priceIdr = priceIdr,
                                normalizedX = normalizedX,
                                normalizedY = normalizedY
                            )
                        )
                    }
                }
                reader.endArray()
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}

