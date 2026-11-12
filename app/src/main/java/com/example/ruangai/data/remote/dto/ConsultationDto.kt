package com.example.ruangai.data.remote.dto

import com.example.ruangai.domain.model.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConsultationChatRequestDto(
    @Json(name = "conversation_id") val conversationId: String,
    @Json(name = "scene_id") val sceneId: String,
    @Json(name = "room_type") val roomType: String,
    @Json(name = "preferred_style") val preferredStyle: String?,
    @Json(name = "target_budget_idr") val targetBudgetIdr: Long,
    @Json(name = "user_message") val userMessage: String,
    @Json(name = "attachment_uris") val attachmentUris: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RecommendedProductRefDto(
    @Json(name = "product_id") val productId: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "style_fit") val styleFit: String,
    @Json(name = "fits_budget") val fitsBudget: Boolean
)

@JsonClass(generateAdapter = true)
data class ConsultationChatResponseDto(
    @Json(name = "message_id") val messageId: String,
    @Json(name = "conversation_id") val conversationId: String,
    @Json(name = "reply_text") val replyText: String,
    @Json(name = "suggested_actions") val suggestedActions: List<String> = emptyList(),
    @Json(name = "recommended_products") val recommendedProducts: List<RecommendedProductRefDto> = emptyList(),
    @Json(name = "is_simulated") val isSimulated: Boolean = true,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

