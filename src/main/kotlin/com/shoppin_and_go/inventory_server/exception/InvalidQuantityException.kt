package com.shoppin_and_go.inventory_server.exception

import org.springframework.http.HttpStatus

class InvalidQuantityException(override val message: String = "Invalid inventory quantity") : LogicalException(
    HttpStatus.BAD_REQUEST,
    ErrorCode.INVALID_QUANTITY,
    message,
    emptyMap()
)
