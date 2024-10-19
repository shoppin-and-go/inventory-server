package com.shoppin_and_go.inventory_server.exception

import org.springframework.http.HttpStatus

abstract class LogicalException(
    val httpStatus: HttpStatus,
    val errorCode: ErrorCode,
    override val message: String,
    val payload: Map<String, Any>,
) : Exception()