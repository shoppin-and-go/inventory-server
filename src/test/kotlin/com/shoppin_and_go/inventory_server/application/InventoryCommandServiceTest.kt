package com.shoppin_and_go.inventory_server.application

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.setExp
import com.ninjasquad.springmockk.MockkBean
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.ProductCode
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import com.shoppin_and_go.inventory_server.event.InventoryChangeEvent
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher

class InventoryCommandServiceTest(
    @MockkBean(relaxed = true) private val cartRepository: CartRepository,
) : DescribeSpec({
    val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    val service = InventoryCommandService(cartRepository, applicationEventPublisher)

    val cartCode = CartCode("cart-test_1")
    val productCode = ProductCode("product-test_1")
    val request = InventoryUpdateRequest(productCode, 1)

    val fixtureMonkey = FixtureMonkey.builder().plugin(KotlinPlugin()).build()

    val cart: Cart = fixtureMonkey.giveMeBuilder<Cart>().setExp(Cart::code, cartCode).sample()

    every { cartRepository.findByCode(cartCode) } returns cart

    describe("InventoryCommandService#updateCartInventory") {
        it("카트 인벤토리 업데이트 이벤트를 발행한다") {
            service.updateCartInventory(cartCode, request)

            verify(exactly = 1) {
                applicationEventPublisher.publishEvent(match< InventoryChangeEvent> { it.cartId == cart.id })
            }
        }

        context("코드에 해당하는 카트가 없을 때") {
            every { cartRepository.findByCode(cartCode) } returns null

            it("오류를 던진다") {
                shouldThrow<CartNotFoundException> {
                    service.updateCartInventory(cartCode, request)
                }
            }
        }
    }
})
