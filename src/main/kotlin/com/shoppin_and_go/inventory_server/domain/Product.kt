package com.shoppin_and_go.inventory_server.domain

import jakarta.persistence.Entity

@Entity
class Product(
    val code: ProductCode,
    val name: String,
    val price: Int,
) : BaseEntity()
