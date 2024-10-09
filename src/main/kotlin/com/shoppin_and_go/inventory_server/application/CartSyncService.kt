package com.shoppin_and_go.inventory_server.application

import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.dto.CartConnectionStatus
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.DuplicateCartConnectionException
import org.springframework.stereotype.Service

@Service
class CartSyncService(
    private val cartRepository: CartRepository,
    private val cartConnectionRepository: CartConnectionRepository,
) {
    fun connectToCart(code: CartCode, deviceId: DeviceId): CartConnectionStatus {
        val cart = cartRepository.findByCode(code) ?: throw CartNotFoundException(code)
        val connectionExistence = cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId)

        if (connectionExistence) throw DuplicateCartConnectionException(deviceId)

        val cartConnection = cart.createConnection(deviceId)
        return cartConnectionRepository.save(cartConnection).let(::convertToCartConnectionStatus)
    }

    fun disconnectFromAllCarts(deviceId: DeviceId): List<CartConnectionStatus> {
        val existConnections = cartConnectionRepository.findByDeviceIdAndDisconnectedAtIsNull(deviceId)
        if (existConnections.isEmpty()) return emptyList()

        existConnections.forEach(CartConnection::disconnect)

        cartConnectionRepository.saveAll(existConnections)

        return existConnections.map(::convertToCartConnectionStatus)
    }

    fun listCartConnections(deviceId: DeviceId): List<CartConnectionStatus> {
        val connections = cartConnectionRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId)

        return connections.map(::convertToCartConnectionStatus)
    }

    private fun convertToCartConnectionStatus(cartConnection: CartConnection): CartConnectionStatus {
        return CartConnectionStatus(cartConnection.cart.code, cartConnection.createdAt, cartConnection.connected)
    }
}