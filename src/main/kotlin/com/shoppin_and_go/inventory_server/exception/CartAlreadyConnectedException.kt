package com.shoppin_and_go.inventory_server.exception

import com.shoppin_and_go.inventory_server.domain.Cart
import org.springframework.http.HttpStatus

class CartAlreadyConnectedException(cart: Cart) : LogicalException(
    HttpStatus.CONFLICT,
    ErrorCode.CART_ALREADY_CONNECTED,
    "This cart is already connected with another device",
    mapOf("cartCode" to cart.code.toString()),
)
