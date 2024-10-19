package com.shoppin_and_go.inventory_server.api

import com.shoppin_and_go.inventory_server.application.CartSyncService
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.dto.CartConnectRequest
import com.shoppin_and_go.inventory_server.dto.CartConnectionStatus
import com.shoppin_and_go.inventory_server.dto.SuccessResponse
import org.springframework.web.bind.annotation.*

@RestController
class DeviceConnectionController(
    val cartSyncService: CartSyncService
) {
    @PostMapping("/cart-connections")
    fun connectToCart(
        @RequestBody request: CartConnectRequest,
    ): SuccessResponse<CartConnectionStatus> {
        val cartConnectionStatus = cartSyncService.connectToCart(request.cartCode, request.deviceId)

        return SuccessResponse("connection", cartConnectionStatus)
    }

    @GetMapping("/devices/{deviceId}/cart-connections")
    fun listCartConnections(
        @PathVariable deviceId: DeviceId,
    ): SuccessResponse<List<CartConnectionStatus>> {
        val cartConnectionStatues = cartSyncService.listCartConnections(deviceId)

        return SuccessResponse("connections", cartConnectionStatues)
    }

    @DeleteMapping("/devices/{deviceId}/cart-connections")
    fun disconnectAll(
        @PathVariable deviceId: DeviceId,
    ): SuccessResponse<List<CartConnectionStatus>> {
        val cartConnectionStatues = cartSyncService.disconnectFromAllCarts(deviceId)

        return SuccessResponse("connections", cartConnectionStatues)
    }
}