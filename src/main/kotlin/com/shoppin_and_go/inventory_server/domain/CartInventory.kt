package com.shoppin_and_go.inventory_server.domain

import com.shoppin_and_go.inventory_server.exception.InvalidQuantityException
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne

@Entity
class CartInventory(
    @ManyToOne
    val cart: Cart,
    @ManyToOne
    val product: Product
) : BaseEntity() {
    var quantity: Int = 0
        private set

    fun changeQuantity(quantityChange: Int) {
        when {
            quantityChange < 0 -> decreaseQuantity(-quantityChange)
            quantityChange > 0 -> increaseQuantity(quantityChange)
            else -> throw InvalidQuantityException("Quantity change must be different from 0")
        }
    }

    private fun increaseQuantity(quantity: Int) {
        assert(quantity > 0) { "Quantity must be greater than 0" }

        this.quantity += quantity
    }

    private fun decreaseQuantity(quantity: Int) {
        assert(quantity > 0) { "Quantity must be greater than 0" }
        if (this.quantity < quantity) throw InvalidQuantityException("Quantity must be less than or equal to $quantity")

        this.quantity -= quantity
    }
}
