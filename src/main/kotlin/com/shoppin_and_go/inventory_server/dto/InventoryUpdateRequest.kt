package com.shoppin_and_go.inventory_server.dto

import com.shoppin_and_go.inventory_server.domain.ProductCode

data class InventoryUpdateRequest(
    val productCode: ProductCode,
    val quantity: Int,
)
