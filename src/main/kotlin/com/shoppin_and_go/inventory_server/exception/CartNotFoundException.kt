package com.shoppin_and_go.inventory_server.exception

import com.shoppin_and_go.inventory_server.domain.CartCode
import org.springframework.http.HttpStatus

class CartNotFoundException(code: CartCode) : LogicalException() {
    override val message = "Cart not found: $code"
    override val httpStatus = HttpStatus.BAD_REQUEST
}