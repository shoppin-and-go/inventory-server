package com.shoppin_and_go.inventory_server.application

import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.dao.ProductRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.Product
import com.shoppin_and_go.inventory_server.domain.ProductCode
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import com.shoppin_and_go.inventory_server.event.InventoryChangeEvent
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.ProductNotFoundException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryCommandService(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun updateCartInventory(cartCode: CartCode, request: InventoryUpdateRequest) {
        val cart = getCart(cartCode)
        val product = getProduct(request.productCode)

        val changedInventory = cart.changeProductQuantity(product, request.quantityChange)
        cartRepository.save(cart)

        val event = InventoryChangeEvent(cart.id, changedInventory.updatedAt)
        eventPublisher.publishEvent(event)
    }

    fun flushCartInventory(cart: Cart) {
        cart.flushInventories()

        cartRepository.save(cart)
    }

    private fun getCart(code: CartCode): Cart {
        return cartRepository.findByCode(code) ?: throw CartNotFoundException(code)
    }

    private fun getProduct(code: ProductCode): Product {
        return productRepository.findByCode(code) ?: throw ProductNotFoundException(code)
    }
}
