package com.shoppin_and_go.inventory_server.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
class Cart(
    @Column(unique = true, nullable = false)
    val code: CartCode,
) : BaseEntity() {
    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _inventories: MutableList<CartInventory> = mutableListOf()

    val inventories: List<CartInventory>
        get() = _inventories.toList()

    fun createConnection(deviceId: DeviceId): CartConnection {
        return CartConnection(this, deviceId)
    }

    fun changeProductQuantity(product: Product, quantityChange: Int): CartInventory {
        val inventory = _inventories.find { it.product == product }

        if (inventory == null) {
            val newInventory = CartInventory(this, product)
            newInventory.changeQuantity(quantityChange)

            _inventories.add(newInventory)

            return newInventory
        } else {
            inventory.changeQuantity(quantityChange)

            return inventory
        }
    }

    fun flushInventories() {
        _inventories.clear()
    }
}
