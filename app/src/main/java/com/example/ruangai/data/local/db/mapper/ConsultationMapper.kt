package com.example.ruangai.data.local.db.mapper

import com.example.ruangai.data.local.db.entity.ConsultationConversationEntity
import com.example.ruangai.data.local.db.entity.ConsultationMessageEntity
import com.example.ruangai.domain.model.*
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import okio.Buffer
import java.io.File

/**
 * Fault-tolerant serializer and mapper for ConsultationConversationEntity & ConsultationMessageEntity.
 * Uses pure-JVM Moshi JsonReader/JsonWriter so it works identically in JVM unit tests, Robolectric, and Android.
 * Handles malformed JSON and missing local attachment files gracefully without crashing conversation history.
 */
object ConsultationMapper {

    fun toDomain(entity: ConsultationConversationEntity): ConsultationConversation {
        val style = entity.selectedStyleId?.let { raw ->
            DesignStyle.values().find { it.name.equals(raw, ignoreCase = true) || it.id.equals(raw, ignoreCase = true) }
        }
        val status = try {
            ConversationStatus.valueOf(entity.status)
        } catch (e: Exception) {
            ConversationStatus.ACTIVE
        }
        return ConsultationConversation(
            conversationId = entity.conversationId,
            sceneId = entity.sceneId,
            title = entity.title,
            selectedStyle = style,
            customBudgetIdr = entity.customBudgetIdr,
            status = status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: ConsultationConversation): ConsultationConversationEntity {
        return ConsultationConversationEntity(
            conversationId = domain.conversationId,
            sceneId = domain.sceneId,
            title = domain.title,
            selectedStyleId = domain.selectedStyle?.name,
            customBudgetIdr = domain.customBudgetIdr,
            status = domain.status.name,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    fun toDomain(entity: ConsultationMessageEntity): ChatMessage {
        val role = try {
            SenderRole.valueOf(entity.senderRole)
        } catch (e: Exception) {
            SenderRole.ASSISTANT
        }
        val delivery = try {
            MessageDeliveryStatus.valueOf(entity.deliveryStatus)
        } catch (e: Exception) {
            MessageDeliveryStatus.DELIVERED
        }

        val attachments = deserializeAttachments(entity.attachmentsJson)
        val recommendations = deserializeRecommendations(entity.recommendedProductsJson)
        val actions = deserializeActions(entity.suggestedActionsJson)

        return ChatMessage(
            id = entity.messageId,
            isFromUser = (role == SenderRole.USER),
            text = entity.text,
            timestamp = entity.timestamp,
            suggestedActions = actions,
            conversationId = entity.conversationId,
            senderRole = role,
            attachments = attachments,
            recommendedProducts = recommendations,
            evaluatedBudgetIdr = entity.evaluatedBudgetIdr,
            isSimulated = entity.isSimulated,
            deliveryStatus = delivery
        )
    }

    fun toEntity(domain: ChatMessage, conversationId: String = domain.conversationId): ConsultationMessageEntity {
        return ConsultationMessageEntity(
            messageId = domain.id,
            conversationId = conversationId,
            senderRole = domain.senderRole.name,
            text = domain.text,
            timestamp = domain.timestamp,
            attachmentsJson = serializeAttachments(domain.attachments),
            recommendedProductsJson = serializeRecommendations(domain.recommendedProducts),
            suggestedActionsJson = serializeActions(domain.suggestedActions),
            evaluatedBudgetIdr = domain.evaluatedBudgetIdr,
            isSimulated = domain.isSimulated,
            deliveryStatus = domain.deliveryStatus.name
        )
    }

    fun serializeAttachments(attachments: List<ChatAttachment>): String {
        if (attachments.isEmpty()) return "[]"
        return try {
            val buffer = Buffer()
            val writer = JsonWriter.of(buffer)
            writer.beginArray()
            for (att in attachments) {
                writer.beginObject()
                writer.name("id").value(att.id)
                writer.name("type").value(att.type.name)
                writer.name("uriOrPath").value(att.uriOrPath)
                writer.name("label").value(att.label)
                writer.name("mimeType").value(att.mimeType)
                writer.name("width").value(att.width)
                writer.name("height").value(att.height)
                writer.name("isSimulatedAnalysis").value(att.isSimulatedAnalysis)
                writer.name("simulationNote").value(att.simulationNote)
                writer.endObject()
            }
            writer.endArray()
            buffer.readUtf8()
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializeAttachments(json: String?): List<ChatAttachment> {
        if (json.isNullOrBlank() || json.trim() == "[]") return emptyList()
        return try {
            val buffer = Buffer().writeUtf8(json)
            val reader = JsonReader.of(buffer)
            val result = mutableListOf<ChatAttachment>()
            reader.beginArray()
            while (reader.hasNext()) {
                var id = ""
                var typeName = AttachmentType.ROOM_PHOTO.name
                var uriOrPath = ""
                var label = "Lampiran Foto Ruangan"
                var mimeType = "image/jpeg"
                var width = 0
                var height = 0
                var isSim = true
                var simNote = "Simulasi — Foto dilampirkan sebagai referensi visual lokal"

                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "id" -> id = reader.nextString()
                        "type" -> typeName = reader.nextString()
                        "uriOrPath" -> uriOrPath = reader.nextString()
                        "label" -> label = reader.nextString()
                        "mimeType" -> mimeType = reader.nextString()
                        "width" -> width = reader.nextInt()
                        "height" -> height = reader.nextInt()
                        "isSimulatedAnalysis" -> isSim = reader.nextBoolean()
                        "simulationNote" -> simNote = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()

                if (id.isNotBlank() && uriOrPath.isNotBlank()) {
                    val attType = try {
                        AttachmentType.valueOf(typeName)
                    } catch (e: Exception) {
                        AttachmentType.ROOM_PHOTO
                    }
                    val exists = checkAttachmentFileExists(uriOrPath)
                    result.add(
                        ChatAttachment(
                            id = id,
                            type = attType,
                            uriOrPath = uriOrPath,
                            label = label,
                            mimeType = mimeType,
                            width = width,
                            height = height,
                            fileExists = exists,
                            isSimulatedAnalysis = isSim,
                            simulationNote = simNote
                        )
                    )
                }
            }
            reader.endArray()
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun checkAttachmentFileExists(uriOrPath: String): Boolean {
        if (uriOrPath.isBlank()) return false
        if (uriOrPath.startsWith("http://") || uriOrPath.startsWith("https://") || uriOrPath.startsWith("content://") || uriOrPath.startsWith("cutout://")) {
            return true
        }
        val cleanPath = if (uriOrPath.startsWith("file://")) uriOrPath.removePrefix("file://") else uriOrPath
        return try {
            val f = File(cleanPath)
            f.exists() && f.length() > 0L
        } catch (e: Exception) {
            false
        }
    }

    fun serializeRecommendations(items: List<ProductRecommendationItem>): String {
        if (items.isEmpty()) return "[]"
        return try {
            val buffer = Buffer()
            val writer = JsonWriter.of(buffer)
            writer.beginArray()
            for (item in items) {
                writer.beginObject()
                writer.name("productId").value(item.productId)
                writer.name("productTitle").value(item.productTitle)
                writer.name("categoryName").value(item.categoryName)
                writer.name("priceIdr").value(item.priceIdr)
                writer.name("imageUrl").value(item.imageUrl)
                writer.name("transparentCutoutUri").value(item.transparentCutoutUri)
                writer.name("dimWidth").value(item.dimensions.width)
                writer.name("dimDepth").value(item.dimensions.depth)
                writer.name("dimHeight").value(item.dimensions.height)
                writer.name("primaryStyle").value(item.primaryStyle.name)
                writer.name("compatibleStyles").value(item.compatibleStyles.joinToString(",") { it.name })
                writer.name("reason").value(item.reason)
                writer.name("styleCompatibilityLabel").value(item.styleCompatibilityLabel)
                writer.name("fitsBudget").value(item.fitsBudget)
                writer.name("budgetDeltaIdr").value(item.budgetDeltaIdr)
                writer.endObject()
            }
            writer.endArray()
            buffer.readUtf8()
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializeRecommendations(json: String?): List<ProductRecommendationItem> {
        if (json.isNullOrBlank() || json.trim() == "[]") return emptyList()
        return try {
            val buffer = Buffer().writeUtf8(json)
            val reader = JsonReader.of(buffer)
            val result = mutableListOf<ProductRecommendationItem>()
            reader.beginArray()
            while (reader.hasNext()) {
                var productId = ""
                var productTitle = ""
                var categoryName = ""
                var priceIdr = 0L
                var imageUrl = ""
                var cutoutUri = ""
                var dimW = 60
                var dimD = 60
                var dimH = 60
                var primaryStyleName = DesignStyle.JAPANDI.name
                var compatibleStylesRaw = ""
                var reason = ""
                var styleCompatibilityLabel = ""
                var fitsBudget = true
                var budgetDeltaIdr = 0L

                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "productId" -> productId = reader.nextString()
                        "productTitle" -> productTitle = reader.nextString()
                        "categoryName" -> categoryName = reader.nextString()
                        "priceIdr" -> priceIdr = reader.nextLong()
                        "imageUrl" -> imageUrl = reader.nextString()
                        "transparentCutoutUri" -> cutoutUri = reader.nextString()
                        "dimWidth" -> dimW = reader.nextInt()
                        "dimDepth" -> dimD = reader.nextInt()
                        "dimHeight" -> dimH = reader.nextInt()
                        "primaryStyle" -> primaryStyleName = reader.nextString()
                        "compatibleStyles" -> compatibleStylesRaw = reader.nextString()
                        "reason" -> reason = reader.nextString()
                        "styleCompatibilityLabel" -> styleCompatibilityLabel = reader.nextString()
                        "fitsBudget" -> fitsBudget = reader.nextBoolean()
                        "budgetDeltaIdr" -> budgetDeltaIdr = reader.nextLong()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()

                if (productId.isNotBlank()) {
                    val primaryStyle = try {
                        DesignStyle.valueOf(primaryStyleName)
                    } catch (e: Exception) {
                        DesignStyle.JAPANDI
                    }
                    val compatibleStyles = compatibleStylesRaw.split(",")
                        .mapNotNull { token ->
                            try {
                                if (token.isBlank()) null else DesignStyle.valueOf(token.trim())
                            } catch (e: Exception) {
                                null
                            }
                        }
                    result.add(
                        ProductRecommendationItem(
                            productId = productId,
                            productTitle = productTitle,
                            categoryName = categoryName,
                            priceIdr = priceIdr,
                            imageUrl = imageUrl,
                            transparentCutoutUri = cutoutUri,
                            dimensions = DimensionsCm(dimW, dimD, dimH),
                            primaryStyle = primaryStyle,
                            compatibleStyles = compatibleStyles,
                            reason = reason,
                            styleCompatibilityLabel = styleCompatibilityLabel,
                            fitsBudget = fitsBudget,
                            budgetDeltaIdr = budgetDeltaIdr
                        )
                    )
                }
            }
            reader.endArray()
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeActions(actions: List<String>): String {
        if (actions.isEmpty()) return "[]"
        return try {
            val buffer = Buffer()
            val writer = JsonWriter.of(buffer)
            writer.beginArray()
            for (act in actions) {
                writer.value(act)
            }
            writer.endArray()
            buffer.readUtf8()
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializeActions(json: String?): List<String> {
        if (json.isNullOrBlank() || json.trim() == "[]") return emptyList()
        return try {
            val buffer = Buffer().writeUtf8(json)
            val reader = JsonReader.of(buffer)
            val result = mutableListOf<String>()
            reader.beginArray()
            while (reader.hasNext()) {
                result.add(reader.nextString())
            }
            reader.endArray()
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
}

