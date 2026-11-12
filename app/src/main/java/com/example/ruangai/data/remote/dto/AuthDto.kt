package com.example.ruangai.data.remote.dto

import com.example.ruangai.domain.model.UserSession

data class LoginRequestDto(
    val email: String,
    val passwordHash: String,
    val deviceHardwareHash: String,
    val platform: String = "android"
)

data class RegisterRequestDto(
    val email: String,
    val passwordHash: String,
    val displayName: String,
    val deviceHardwareHash: String,
    val platform: String = "android"
)

data class UserSessionResponseDto(
    val userId: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val token: String? = null
) {
    fun toDomain(): UserSession = UserSession(
        userId = userId,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl
    )
}
