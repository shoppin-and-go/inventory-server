package com.shoppin_and_go.inventory_server.api

import com.shoppin_and_go.inventory_server.application.InventoryCommandService
import com.shoppin_and_go.inventory_server.application.InventoryQueryService
import com.shoppin_and_go.inventory_server.application.OrderService
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.dto.ApiResponse
import com.shoppin_and_go.inventory_server.dto.CartInventoryStatus
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import com.shoppin_and_go.inventory_server.dto.OrderDetails
import org.springframework.web.bind.annotation.*

@RestController
class InventoryController(
    private val inventoryCommandService: InventoryCommandService,
    private val inventoryQueryService: InventoryQueryService,
    private val orderService: OrderService,
) {
    @PatchMapping("/carts/{cartCode}/inventories")
    fun updateCartInventory(
        @PathVariable cartCode: CartCode,
        @RequestBody request: InventoryUpdateRequest,
    ) {
        inventoryCommandService.updateCartInventory(cartCode, request)
    }

    @GetMapping("/devices/{deviceId}/carts/{cartCode}/inventories")
    fun listCartInventory(
        @PathVariable deviceId: DeviceId,
        @PathVariable cartCode: CartCode,
    ): ApiResponse<CartInventoryStatus> {
        inventoryQueryService.listInventory(deviceId, cartCode).let {
            return ApiResponse.success("inventory", it)
        }
    }

    @PostMapping("/devices/{deviceId}/carts/{cartCode}/orders")
    fun createOrder(
        @PathVariable deviceId: DeviceId,
        @PathVariable cartCode: CartCode,
    ): ApiResponse<OrderDetails> {
        orderService.createOrder(deviceId, cartCode).let {
            return ApiResponse.success("order", it)
        }
    }
}
