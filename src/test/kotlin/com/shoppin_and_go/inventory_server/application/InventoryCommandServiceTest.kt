package com.shoppin_and_go.inventory_server.application

import com.navercorp.fixturemonkey.kotlin.setExp
import com.ninjasquad.springmockk.MockkBean
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.dao.ProductRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.Product
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import com.shoppin_and_go.inventory_server.event.InventoryChangeEvent
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.ProductNotFoundException
import com.shoppin_and_go.inventory_server.utils.FixtureBuilders
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher

class InventoryCommandServiceTest(
    @MockkBean(relaxed = true) private val cartRepository: CartRepository,
    @MockkBean(relaxed = true) private val productRepository: ProductRepository,
) : DescribeSpec({
    fun buildService() = InventoryCommandService(
        cartRepository,
        productRepository,
        mockk(relaxed = true)
    )

    val cartCode = FixtureBuilders.cartCode()
    val productCode = FixtureBuilders.productCode()
    val request = InventoryUpdateRequest(productCode, 1)

    val cartFixtureBuilder = FixtureBuilders.get<Cart>().setExp(Cart::code, cartCode)
    val productFixtureBuilder = FixtureBuilders.get<Product>().setExp(Product::code, productCode)

    beforeEach {
        every { cartRepository.findByCode(cartCode) } answers { cartFixtureBuilder.sample() }
        every { productRepository.findByCode(productCode) } answers { productFixtureBuilder.sample() }
    }

    describe("InventoryCommandService#updateCartInventory") {
        it("새로운 재고를 추가한다") {
            val service = buildService()

            val cart = spyk(cartFixtureBuilder.sample())
            val product = productFixtureBuilder.sample()

            every { cartRepository.findByCode(cartCode) } returns cart
            every { productRepository.findByCode(productCode) } returns product

            service.updateCartInventory(cartCode, request)

            verify(exactly = 1) { cart.changeProductQuantity(product, request.quantityChange) }
            verify(exactly = 1) { cartRepository.save(cart) }
        }

        it("카트 인벤토리 업데이트 이벤트를 발행한다") {
            val applicationEventPublisher: ApplicationEventPublisher = mockk(relaxed = true)
            val service = InventoryCommandService(
                cartRepository,
                productRepository,
                applicationEventPublisher
            )

            val cart = cartFixtureBuilder.sample()

            every { cartRepository.findByCode(cartCode) } returns cart

            service.updateCartInventory(cartCode, request)

            verify(exactly = 1) {
                applicationEventPublisher.publishEvent(match<InventoryChangeEvent> { it.cartId == cart.id })
            }
        }

        context("코드에 해당하는 카트가 없을 때") {
            beforeEach {
                every { cartRepository.findByCode(cartCode) } returns null
            }

            it("오류를 던진다") {
                val service = buildService()

                shouldThrow<CartNotFoundException> {
                    service.updateCartInventory(cartCode, request)
                }
            }
        }

        context("코드에 해당하는 제품이 없을 때") {
            beforeEach {
                every { productRepository.findByCode(productCode) } returns null
            }

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

            val cart = spyk(cartFixtureBuilder.sample())
            service.flushCartInventory(cart)

            verify(exactly = 1) { cart.flushInventories() }
            verify(exactly = 1) { cartRepository.save(cart) }
        }
    }
})
