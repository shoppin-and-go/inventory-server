package com.shoppin_and_go.inventory_server.dto

import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.CartInventory

data class CartInventoryStatus(
    val cartCode: CartCode,
    val items: List<CartItem>,
) {
    data class CartItem(
        val name: String,
        val quantity: Int,
        val price: Int
    ) {
        companion object {
            fun of(inventory: CartInventory) = CartItem(
                inventory.product.name,
                inventory.quantity,
                inventory.product.price
            )
        }
    }
}
