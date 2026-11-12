package com.example.ruangai.domain.model

data class CreditBalance(
    val available: Int = 3,
    val reserved: Int = 0,
    val lifetimeConsumed: Int = 0
)

enum class TransactionType {
    WELCOME_BONUS,
    TOP_UP,
    RENDER_RESERVATION,
    RENDER_CAPTURE,
    AUTO_REFUND
}

data class CreditTransaction(
    val id: String,
    val amount: Int,
    val type: TransactionType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CreditPackage(
    val id: String,
    val title: String,
    val credits: Int,
    val priceIdr: Long,
    val isPopular: Boolean = false,
    val badgeLabel: String? = null
) {
    fun getFormattedPrice(): String = "Rp " + "%,d".format(priceIdr).replace(',', '.')
}
