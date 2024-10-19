package com.shoppin_and_go.inventory_server.exception

enum class ErrorCode {
    CART_NOT_FOUND,
    INVALID_QUANTITY,
    PRODUCT_NOT_FOUND,
    CART_ALREADY_CONNECTED,
    DEVICE_ALREADY_CONNECTED,
    UNCONNECTED_CART,
}