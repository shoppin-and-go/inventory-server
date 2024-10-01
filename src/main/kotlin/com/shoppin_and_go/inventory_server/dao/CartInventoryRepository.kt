package com.shoppin_and_go.inventory_server.dao

import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartInventory
import com.shoppin_and_go.inventory_server.domain.Product
import org.springframework.data.repository.Repository
import java.util.UUID


interface CartInventoryRepository : Repository<CartInventory, UUID> {
    fun findByCartAndProduct(cart: Cart, product: Product): CartInventory?
    fun findByCart(cart: Cart): List<CartInventory>
    fun save(cartInventory: CartInventory): CartInventory
    fun saveAll(cartInventories: Iterable<CartInventory>): List<CartInventory>
}