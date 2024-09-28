package com.shoppin_and_go.inventory_server.dto

sealed class ApiResponse<T>(
    val code: ResponseCode,
    val message: String,
    val result: Map<String, T>
) {
    companion object {
        fun <T> success(key: String, value: T) = SuccessResponse(key, value)
        fun error(e: Exception) = ErrorResponse(e)
    }
}

enum class ResponseCode {
    OK,
    ERROR,
}

class SuccessResponse<T>(key: String, value: T) : ApiResponse<T>(
    code = ResponseCode.OK,
    message = "Success",
    result = mapOf(key to value)
)
class ErrorResponse(e: Exception) : ApiResponse<String>(
    code = ResponseCode.ERROR,
    message = e.message ?: "Internal server error",
    result = emptyMap()
)
