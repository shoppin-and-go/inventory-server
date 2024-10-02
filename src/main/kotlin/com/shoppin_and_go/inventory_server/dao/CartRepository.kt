package com.shoppin_and_go.inventory_server.dao

import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartCode
import org.springframework.data.repository.Repository
import java.util.UUID

interface CartRepository : Repository<Cart, UUID> {
    fun findByCode(code: CartCode): Cart?
    fun save(cart: Cart): Cart
    fun delete(cart: Cart)
}