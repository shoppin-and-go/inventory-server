package com.shoppin_and_go.inventory_server.application

import com.shoppin_and_go.inventory_server.dao.CartInventoryRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.dao.ProductRepository
import com.shoppin_and_go.inventory_server.domain.*
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
    private val cartInventoryRepository: CartInventoryRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun updateCartInventory(cartCode: CartCode, request: InventoryUpdateRequest) {
        val cart = getCart(cartCode)
        val product = getProduct(request.productCode)

        val changedInventory = changeCartInventory(cart, product, request.quantityChange)

        val event = InventoryChangeEvent(cart.id, changedInventory.updatedAt)
        eventPublisher.publishEvent(event)
    }

    fun flushCartInventory(cart: Cart) {
        cartInventoryRepository.deleteAllByCart(cart)
    }

    private fun getCart(code: CartCode): Cart {
        return cartRepository.findByCode(code) ?: throw CartNotFoundException(code)
    }

    private fun getProduct(code: ProductCode): Product {
        return productRepository.findByCode(code) ?: throw ProductNotFoundException(code)
    }

    private fun changeCartInventory(cart: Cart, product: Product, quantityChange: Int): CartInventory {
        val inventory = fetchCartInventory(cart, product)

        inventory.changeQuantity(quantityChange)

        return cartInventoryRepository.save(inventory)
    }

    private fun fetchCartInventory(cart: Cart, product: Product): CartInventory {
        return cartInventoryRepository.findByCartAndProduct(cart, product) ?: CartInventory(cart, product)
    }
}
