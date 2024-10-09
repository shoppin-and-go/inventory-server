package com.shoppin_and_go.inventory_server.application

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.setExp
import com.ninjasquad.springmockk.MockkBean
import com.shoppin_and_go.inventory_server.dao.CartInventoryRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.dao.ProductRepository
import com.shoppin_and_go.inventory_server.domain.*
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import com.shoppin_and_go.inventory_server.event.InventoryChangeEvent
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.ProductNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import java.util.UUID

class InventoryCommandServiceTest(
    @MockkBean(relaxed = true) private val cartRepository: CartRepository,
    @MockkBean(relaxed = true) private val productRepository: ProductRepository,
    @MockkBean(relaxed = true) private val cartInventoryRepository: CartInventoryRepository,
) : DescribeSpec({
    val fixtureMonkey = FixtureMonkey.builder().plugin(KotlinPlugin()).build()

    fun buildService() = InventoryCommandService(
        cartRepository,
        productRepository,
        cartInventoryRepository,
        mockk(relaxed = true)
    )

    val cartCode = CartCode("cart-test_${UUID.randomUUID()}")
    val productCode = ProductCode("product-test_${UUID.randomUUID()}")
    val request = InventoryUpdateRequest(productCode, 1)

    val cart: Cart = fixtureMonkey.giveMeBuilder<Cart>().setExp(Cart::code, cartCode).sample()
    val product: Product = fixtureMonkey.giveMeBuilder<Product>().setExp(Product::code, productCode).sample()

    every { cartRepository.findByCode(cartCode) } returns cart
    every { productRepository.findByCode(productCode) } returns product
    every { cartInventoryRepository.findByCartAndProduct(cart, product) } returns null

    describe("InventoryCommandService#updateCartInventory") {
        it("새로운 재고를 추가한다") {
            val service = buildService()

            service.updateCartInventory(cartCode, request)

            verify(exactly = 1) {
                cartInventoryRepository.save(match { it.cart == cart && it.product == product && it.quantity == request.quantityChange })
            }
        }

        context("기존에 담아뒀던 상품인 경우") {
            val cartInventory = fixtureMonkey.giveMeBuilder<CartInventory>()
                .setExp(CartInventory::cart, cart)
                .setExp(CartInventory::product, product)
                .sample()
                .let(::spyk)

            every { cartInventoryRepository.findByCartAndProduct(cart, product) } returns cartInventory

            it("기존의 재고에서 수량을 변경한다") {
                val service = buildService()

                service.updateCartInventory(cartCode, request)

                verify(exactly = 1) { cartInventory.changeQuantity(request.quantityChange) }
                verify(exactly = 1) { cartInventoryRepository.save(cartInventory) }
            }
        }

        it("카트 인벤토리 업데이트 이벤트를 발행한다") {
            val applicationEventPublisher: ApplicationEventPublisher = mockk(relaxed = true)
            val service = InventoryCommandService(
                cartRepository,
                productRepository,
                cartInventoryRepository,
                applicationEventPublisher
            )

            service.updateCartInventory(cartCode, request)

            verify(exactly = 1) {
                applicationEventPublisher.publishEvent(match<InventoryChangeEvent> { it.cartId == cart.id })
            }
        }

        context("코드에 해당하는 카트가 없을 때") {
            every { cartRepository.findByCode(cartCode) } returns null
            every { productRepository.findByCode(productCode) } returns product

            it("오류를 던진다") {
                val service = buildService()

                shouldThrow<CartNotFoundException> {
                    service.updateCartInventory(cartCode, request)
                }
            }
        }

        context("코드에 해당하는 제품이 없을 때") {
            every { cartRepository.findByCode(cartCode) } returns cart
            every { productRepository.findByCode(productCode) } returns null

            it("오류를 던진다") {
                val service = buildService()

                shouldThrow<ProductNotFoundException> {
                    service.updateCartInventory(cartCode, request)
                }
            }
        }
    }

    describe("InventoryCommandService#flushCartInventory") {
        it("카트 인벤토리를 모두 삭제한다") {
            val service = buildService()

            service.flushCartInventory(cart)

            verify(exactly = 1) { cartInventoryRepository.deleteAllByCart(cart) }
        }
    }
})
