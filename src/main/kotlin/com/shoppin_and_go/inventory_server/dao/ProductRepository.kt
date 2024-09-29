package com.shoppin_and_go.inventory_server.dao

import com.shoppin_and_go.inventory_server.domain.Product
import com.shoppin_and_go.inventory_server.domain.ProductCode
import org.springframework.data.repository.Repository
import java.util.UUID

interface ProductRepository : Repository<Product, UUID> {
    fun findByCode(code: ProductCode): Product?
}