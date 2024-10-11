package com.shoppin_and_go.inventory_server.application

import com.navercorp.fixturemonkey.kotlin.setExp
import com.ninjasquad.springmockk.MockkBean
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.Product
import com.shoppin_and_go.inventory_server.dto.CartInventoryStatus
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.UnauthorizedCartException
import com.shoppin_and_go.inventory_server.utils.FixtureBuilders
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify

class InventoryQueryServiceTest(
    @MockkBean private val cartRepository: CartRepository,
    @MockkBean private val cartConnectionRepository: CartConnectionRepository,
) : DescribeSpec({
    describe("InventoryQueryService#listInventory") {
        val service = InventoryQueryService(cartRepository, cartConnectionRepository)

        val deviceId = FixtureBuilders.deviceId()
        val cartCode = FixtureBuilders.cartCode()
        val cart = FixtureBuilders.builder<Cart>().setExp(Cart::code, cartCode).sample().apply {
            changeProductQuantity(FixtureBuilders.builder<Product>().sample(), 1)
            changeProductQuantity(FixtureBuilders.builder<Product>().sample(), 2)
            changeProductQuantity(FixtureBuilders.builder<Product>().sample(), 3)
        }

        beforeEach {
            every { cartConnectionRepository.existsByDeviceIdAndCartAndDisconnectedAtIsNull(deviceId, cart) } returns true
            every { cartRepository.findByCode(cartCode) } returns cart
        }

        it("기기가 카트에 연결되었는지 확인한다") {
            service.listInventory(deviceId, cartCode)

            verify(exactly = 1) { cartConnectionRepository.existsByDeviceIdAndCartAndDisconnectedAtIsNull(deviceId, cart) }
        }

        it("카트의 재고 상태를 반환한다") {
            val result = service.listInventory(deviceId, cartCode)

            result shouldBe CartInventoryStatus(
                cartCode,
                cart.inventories.map(CartInventoryStatus.CartItem::of)
            )
        }

        context("코드에 해당하는 카트가 없는 경우") {
            beforeEach {
                every { cartRepository.findByCode(cartCode) } returns null
            }

            it("카트 존재 오류를 던진다") {
                shouldThrow<CartNotFoundException> {
                    service.listInventory(deviceId, cartCode)
                }
            }
        }

        context("기기와 카트가 연결되지 않은 경우") {
            beforeEach {
                every { cartConnectionRepository.existsByDeviceIdAndCartAndDisconnectedAtIsNull(deviceId, cart) } returns false
            }

            it("권한 오류를 던진다") {
                shouldThrow<UnauthorizedCartException> {
                    service.listInventory(deviceId, cartCode)
                }
            }
        }
    }
})
