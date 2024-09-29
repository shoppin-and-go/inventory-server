package com.shoppin_and_go.inventory_server.application

import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import com.shoppin_and_go.inventory_server.event.InventoryChangeEvent
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class InventoryCommandService(
    private val cartRepository: CartRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun updateCartInventory(cartCode: CartCode, request: InventoryUpdateRequest) {
        val cart = getCart(cartCode)

        // TODO: CartInventory 업데이트 처리
        val cartInventoryUpdatedAt = LocalDateTime.now()

        val event = InventoryChangeEvent(cart.id, cartInventoryUpdatedAt)
        eventPublisher.publishEvent(event)
    }

    private fun getCart(code: CartCode): Cart {
        return cartRepository.findByCode(code) ?: throw CartNotFoundException(code)
    }
}
