package com.shoppin_and_go.inventory_server.application

import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.dto.CartInventoryStatus
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.UnauthorizedCartException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryQueryService(
    private val cartRepository: CartRepository,
    private val cartConnectionRepository: CartConnectionRepository,
) {
    @Transactional
    fun listInventory(deviceId: DeviceId, cartCode: CartCode): CartInventoryStatus {
        val cart = getAuthorizedCart(deviceId, cartCode)

        return CartInventoryStatus(
            cart.code,
            cart.inventories.map(CartInventoryStatus.CartItem::of)
        )
    }

    fun getAuthorizedCart(deviceId: DeviceId, cartCode: CartCode): Cart {
        return getCart(cartCode).also { checkAuthority(deviceId, it) }
    }

    private fun getCart(cartCode: CartCode): Cart {
        return cartRepository.findByCode(cartCode) ?: throw CartNotFoundException(cartCode)
    }

    private fun checkAuthority(deviceId: DeviceId, cart: Cart) {
        val connectionPresence =  cartConnectionRepository.existsByDeviceIdAndCartAndDisconnectedAtIsNull(deviceId, cart)

        if (connectionPresence.not()) throw UnauthorizedCartException()
    }
}