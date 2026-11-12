package com.example.ruangai.domain.model

data class UserSession(
    val userId: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
)

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val session: UserSession) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

data class ChatMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedActions: List<String> = emptyList(),
    val conversationId: String = "",
    val senderRole: SenderRole = if (isFromUser) SenderRole.USER else SenderRole.ASSISTANT,
    val attachments: List<ChatAttachment> = emptyList(),
    val recommendedProducts: List<ProductRecommendationItem> = emptyList(),
    val evaluatedBudgetIdr: Long? = null,
    val isSimulated: Boolean = true,
    val deliveryStatus: MessageDeliveryStatus = MessageDeliveryStatus.DELIVERED
)
