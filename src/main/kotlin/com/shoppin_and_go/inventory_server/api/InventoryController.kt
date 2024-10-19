package com.shoppin_and_go.inventory_server.api

import com.shoppin_and_go.inventory_server.application.InventoryCommandService
import com.shoppin_and_go.inventory_server.application.InventoryQueryService
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.dto.CartInventoryStatus
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import com.shoppin_and_go.inventory_server.dto.SuccessResponse
import org.springframework.web.bind.annotation.*

@RestController
class InventoryController(
    private val inventoryCommandService: InventoryCommandService,
    private val inventoryQueryService: InventoryQueryService,
) {
    @PatchMapping("/carts/{cartCode}/inventories")
    fun updateCartInventory(
        @PathVariable cartCode: CartCode,
        @RequestBody request: InventoryUpdateRequest,
    ): SuccessResponse<Unit> {
        inventoryCommandService.updateCartInventory(cartCode, request)

        return SuccessResponse.empty()
    }

    @GetMapping("/devices/{deviceId}/carts/{cartCode}/inventories")
    fun listCartInventory(
        @PathVariable deviceId: DeviceId,
        @PathVariable cartCode: CartCode,
    ): SuccessResponse<CartInventoryStatus> {
        inventoryQueryService.listInventory(deviceId, cartCode).let {
            return SuccessResponse("inventory", it)
        }
    }
}
