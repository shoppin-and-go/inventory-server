package com.shoppin_and_go.inventory_server.exception

import com.shoppin_and_go.inventory_server.domain.CartCode
import org.springframework.http.HttpStatus

class CartNotFoundException(code: CartCode) : LogicalException(
    HttpStatus.BAD_REQUEST,
    ErrorCode.CART_NOT_FOUND,
    "Cart not found",
    mapOf("cartCode" to code.toString())
)
