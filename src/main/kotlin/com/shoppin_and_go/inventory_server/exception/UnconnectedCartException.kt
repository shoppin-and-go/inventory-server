package com.shoppin_and_go.inventory_server.exception

import com.shoppin_and_go.inventory_server.domain.Cart
import org.springframework.http.HttpStatus

class UnconnectedCartException(cart: Cart) : LogicalException(
    HttpStatus.FORBIDDEN,
    ErrorCode.UNCONNECTED_CART,
    "This cart is not connected to this device",
    mapOf("cartCode" to cart.code.toString())
)
