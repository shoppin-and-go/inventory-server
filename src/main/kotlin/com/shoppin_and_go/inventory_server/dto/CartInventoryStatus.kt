package com.shoppin_and_go.inventory_server.dto

import com.shoppin_and_go.inventory_server.domain.CartCode

data class CartInventoryStatus(
    val cartCode: CartCode,
    val items: List<CartItem>,
) {
    data class CartItem(
        val name: String,
        val quantity: Int,
        val price: Int
    )
}
