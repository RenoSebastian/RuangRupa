package com.example.ruangai.data.remote.dto

import com.example.ruangai.domain.model.CreditBalance
import com.example.ruangai.domain.model.CreditTransaction
import com.example.ruangai.domain.model.TransactionType

data class WalletResponseDto(
    val availableCredits: Int,
    val reservedCredits: Int,
    val lifetimeConsumed: Int
) {
    fun toDomain(): CreditBalance = CreditBalance(
        available = availableCredits,
        reserved = reservedCredits,
        lifetimeConsumed = lifetimeConsumed
    )
}

data class TransactionResponseDto(
    val id: String,
    val amount: Int,
    val transactionType: String,
    val description: String,
    val timestamp: Long
) {
    fun toDomain(): CreditTransaction = CreditTransaction(
        id = id,
        amount = amount,
        type = runCatching { TransactionType.valueOf(transactionType) }.getOrDefault(TransactionType.RENDER_CAPTURE),
        description = description,
        timestamp = timestamp
    )
}

data class ReserveCreditRequestDto(
    val jobId: String,
    val idempotencyKey: String,
    val amount: Int = 1
)
