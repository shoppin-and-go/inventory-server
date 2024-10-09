package com.shoppin_and_go.inventory_server.api

import com.shoppin_and_go.inventory_server.application.CartSyncService
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.dto.ApiResponse
import com.shoppin_and_go.inventory_server.dto.CartConnectRequest
import com.shoppin_and_go.inventory_server.dto.CartConnectionStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class DeviceConnectionController(
    val cartSyncService: CartSyncService
) {
    @PostMapping("/cart-connections")
    fun connectToCart(
        @RequestBody request: CartConnectRequest
    ): ApiResponse<CartConnectionStatus> {
        val cartConnectionStatus = cartSyncService.connectToCart(request.cartCode, request.deviceId)

        return ApiResponse.success("connection", cartConnectionStatus)
    }

    @GetMapping("/devices/{deviceId}/cart-connections")
    fun listCartConnections(
        @PathVariable deviceId: DeviceId
    ): ApiResponse<List<CartConnectionStatus>> {
        val cartConnectionStatues = cartSyncService.listCartConnections(deviceId)

        return ApiResponse.success("connections", cartConnectionStatues)
    }

    @DeleteMapping("/devices/{deviceId}/cart-connections")
    fun disconnectAll(
        @PathVariable deviceId: DeviceId
    ): ApiResponse<List<CartConnectionStatus>> {
        val cartConnectionStatues = cartSyncService.disconnectFromAllCarts(deviceId)

        return ApiResponse.success("connections", cartConnectionStatues)
    }
}