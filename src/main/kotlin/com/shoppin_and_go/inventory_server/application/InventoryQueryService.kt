package com.shoppin_and_go.inventory_server.application

import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartInventoryRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.*
import com.shoppin_and_go.inventory_server.dto.CartInventoryStatus
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.UnauthorizedCartException
import org.springframework.stereotype.Service

@Service
class InventoryQueryService(
    private val cartRepository: CartRepository,
    private val cartConnectionRepository: CartConnectionRepository,
    private val cartInventoryRepository: CartInventoryRepository,
) {
    fun listInventory(deviceId: DeviceId, cartCode: CartCode): CartInventoryStatus {
        val cart = getCart(cartCode)
        checkAuthority(deviceId, cart)

        val inventories = cartInventoryRepository.findByCart(cart)

        return CartInventoryStatus(
            cart.code,
            inventories.map {
                CartInventoryStatus.CartItem(
                    it.product.name,
                    it.product.price,
                    it.quantity,
                )
            }
        )
    }

    private fun getCart(cartCode: CartCode): Cart {
        return cartRepository.findByCode(cartCode) ?: throw CartNotFoundException(cartCode)
    }

    private fun checkAuthority(deviceId: DeviceId, cart: Cart) {
        val connectionPresence =  cartConnectionRepository.existsByDeviceIdAndCartAndDisconnectedAtIsNull(deviceId, cart)

        if (connectionPresence.not()) throw UnauthorizedCartException()
    }
}