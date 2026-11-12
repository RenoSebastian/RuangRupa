package com.example.ruangai.data.remote.util

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class HttpError(val code: Int, val message: String, val errorBody: String? = null) : NetworkResult<Nothing>()
    data class NetworkError(val exception: Throwable) : NetworkResult<Nothing>()
    object Timeout : NetworkResult<Nothing>()

    fun toResult(): Result<T> = when (this) {
        is Success -> Result.success(data)
        is HttpError -> Result.failure(Exception("HTTP $code: $message"))
        is NetworkError -> Result.failure(exception)
        is Timeout -> Result.failure(Exception("Permintaan jaringan melebihi batas waktu (Timeout)"))
    }
}
