package com.example.ruangai.data.remote.config

enum class Environment(val baseUrl: String) {
    DEV("https://api-dev.ruangai.id/v1"),
    STAGING("https://api-staging.ruangai.id/v1"),
    PRODUCTION("https://api.ruangai.id/v1")
}

data class ApiConfig(
    val environment: Environment = Environment.DEV,
    val connectTimeoutSeconds: Long = 15L,
    val readTimeoutSeconds: Long = 30L,
    val writeTimeoutSeconds: Long = 30L,
    val appVersion: String = "1.0.0",
    val platform: String = "android"
) {
    val currentBaseUrl: String
        get() = environment.baseUrl

    fun defaultHeaders(): Map<String, String> = mapOf(
        "Content-Type" to "application/json",
        "Accept" to "application/json",
        "X-App-Version" to appVersion,
        "X-Platform" to platform
    )
}
