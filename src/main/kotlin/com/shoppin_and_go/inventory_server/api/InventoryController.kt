package com.shoppin_and_go.inventory_server.api

import com.shoppin_and_go.inventory_server.application.InventoryCommandService
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InventoryController(
    private val inventoryCommandService: InventoryCommandService
) {
    @PatchMapping("/carts/{cartCode}/inventories")
    fun updateCartInventory(
        @PathVariable cartCode: CartCode,
        @RequestBody request: InventoryUpdateRequest,
    ) {
        inventoryCommandService.updateCartInventory(cartCode, request)
    }
}
