package com.shoppin_and_go.inventory_server.application

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.setExp
import com.ninjasquad.springmockk.MockkBean
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartInventoryRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.CartInventory
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.dto.CartInventoryStatus
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.UnauthorizedCartException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import java.util.*

class InventoryQueryServiceTest(
    @MockkBean private val cartRepository: CartRepository,
    @MockkBean private val cartConnectionRepository: CartConnectionRepository,
    @MockkBean private val cartInventoryRepository: CartInventoryRepository,
) : DescribeSpec({
    describe("InventoryQueryService#listInventory") {
        val service = InventoryQueryService(cartRepository, cartConnectionRepository, cartInventoryRepository)

        val fixtureMonkey = FixtureMonkey.builder().plugin(KotlinPlugin()).build()

        val deviceId = DeviceId("device-test_${UUID.randomUUID()}")
        val cartCode = CartCode("cart-test_${UUID.randomUUID()}")
        val cart = fixtureMonkey.giveMeBuilder<Cart>().setExp(Cart::code, cartCode).sample()
        val cartInventories = fixtureMonkey
            .giveMeBuilder<CartInventory>()
            .setExp(CartInventory::cart, cart)
            .sampleList(3)

        beforeEach {
            every { cartConnectionRepository.existsByDeviceIdAndCartAndDisconnectedAtIsNull(deviceId, cart) } returns true
            every { cartRepository.findByCode(cartCode) } returns cart
            every { cartInventoryRepository.findByCart(cart) } returns cartInventories
        }

        it("기기가 카트에 연결되었는지 확인한다") {
            service.listInventory(deviceId, cartCode)

            verify(exactly = 1) { cartConnectionRepository.existsByDeviceIdAndCartAndDisconnectedAtIsNull(deviceId, cart) }
        }

        it("카트의 재고 상태를 반환한다") {
            val result = service.listInventory(deviceId, cartCode)

            result shouldBe CartInventoryStatus(
                cartCode,
                cartInventories.map(CartInventoryStatus.CartItem::of)
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
