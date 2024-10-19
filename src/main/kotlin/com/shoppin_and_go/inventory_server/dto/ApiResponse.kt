package com.shoppin_and_go.inventory_server.dto

import com.shoppin_and_go.inventory_server.exception.LogicalException

sealed interface ApiResponse {
    val code: String
    val message: String
    val result: Map<String, *>
}

data class ErrorResponse(
    override val code: String,
    override val message: String,
    override val result: Map<String, Any>,
) : ApiResponse {
    constructor(e: LogicalException) : this(
        code = e.errorCode.toString(),
        message = e.message,
        result = e.payload
    )

    constructor(e: Exception) : this(
        code = "UnknownError",
        message = e.message ?: "Unknown error. Please contact the developer",
        result = emptyMap()
    )
}

class SuccessResponse<T>(
    override val code: String = "OK",
    override val message: String = "",
    override val result: Map<String, T> = emptyMap(),
) : ApiResponse {
    constructor(key: String, value: T) : this(
        result = mapOf(key to value)
    )

    companion object {
        fun empty() = SuccessResponse<Unit>()
    }
}
