package com.shoppin_and_go.inventory_server.application

import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.dto.CartConnectionStatus
import com.shoppin_and_go.inventory_server.exception.AlreadyConnectedCartException
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.DuplicateCartConnectionException
import com.shoppin_and_go.inventory_server.extensions.throwIfTrue
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartSyncService(
    private val cartRepository: CartRepository,
    private val cartConnectionRepository: CartConnectionRepository,
    private val inventoryCommandService: InventoryCommandService,
) {
    fun connectToCart(code: CartCode, deviceId: DeviceId): CartConnectionStatus {
        val cart = getCart(code)

        checkDeviceConnected(deviceId).throwIfTrue { DuplicateCartConnectionException(deviceId) }
        checkCartConnected(cart).throwIfTrue { AlreadyConnectedCartException() }

        val cartConnection = cart.createConnection(deviceId)
        return cartConnectionRepository.save(cartConnection).let(CartConnectionStatus::of)
    }

    @Transactional
    fun disconnectFromAllCarts(deviceId: DeviceId): List<CartConnectionStatus> {
        val existConnections = cartConnectionRepository.findByDeviceIdAndDisconnectedAtIsNull(deviceId)

        return existConnections.map(::disconnectFromCart)
    }

    fun listCartConnections(deviceId: DeviceId): List<CartConnectionStatus> {
        val connections = cartConnectionRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId)

        return connections.map(CartConnectionStatus::of)
    }

    private fun getCart(code: CartCode): Cart {
        return cartRepository.findByCode(code) ?: throw CartNotFoundException(code)
    }

    private fun checkDeviceConnected(deviceId: DeviceId): Boolean {
        return cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId)
    }

    private fun checkCartConnected(cart: Cart): Boolean {
        return cartConnectionRepository.existsByCartAndDisconnectedAtIsNull(cart)
    }

    private fun disconnectFromCart(cartConnection: CartConnection): CartConnectionStatus {
        val cart = cartConnection.cart

        cartConnection.disconnect()
        inventoryCommandService.flushCartInventory(cart)

        return cartConnectionRepository.save(cartConnection).let(CartConnectionStatus::of)
    }
}