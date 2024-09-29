package com.shoppin_and_go.inventory_server.application

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.setExp
import com.ninjasquad.springmockk.MockkBean
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.event.InventoryChangeEvent
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.verify
import java.time.LocalDateTime

class InventoryEventListenerTest(
    @MockkBean(relaxed = true) private val eventMessagingTemplate: EventMessagingTemplate,
    @MockkBean private val cartConnectionRepository: CartConnectionRepository,
) : DescribeSpec({
    val inventoryEventListener = InventoryEventListener(cartConnectionRepository, eventMessagingTemplate)

    val fixtureMonkey = FixtureMonkey.builder().plugin(KotlinPlugin()).build()

    val cartOne = fixtureMonkey.giveMeBuilder<Cart>().sample()
    val cartTwo = fixtureMonkey.giveMeBuilder<Cart>().sample()

    val cartOneFirstConn = fixtureMonkey.giveMeBuilder<CartConnection>().setExp(CartConnection::cart, cartOne).sample()
    val cartOneSecondConn = fixtureMonkey.giveMeBuilder<CartConnection>().setExp(CartConnection::cart, cartOne).sample()
    val cartTwoFirstConn = fixtureMonkey.giveMeBuilder<CartConnection>().setExp(CartConnection::cart, cartTwo).sample()
    val cartTwoSecondConn = fixtureMonkey.giveMeBuilder<CartConnection>().setExp(CartConnection::cart, cartTwo).sample()

    every { cartConnectionRepository.findByCartIdAndDisconnectedAtIsNull(cartOne.id) } returns listOf(cartOneFirstConn, cartOneSecondConn)
    every { cartConnectionRepository.findByCartIdAndDisconnectedAtIsNull(cartTwo.id) } returns listOf(cartTwoFirstConn, cartTwoSecondConn)

    describe("InventoryEventListener#noticeInventoryChange") {
        it("인벤토리가 변경된 카트에 연결된 디바이스에 이벤트를 전송한다") {
            val event = InventoryChangeEvent(cartOne.id, LocalDateTime.now())
            inventoryEventListener.noticeInventoryChange(event)

            verify(exactly = 1) { eventMessagingTemplate.sendToDevice(cartOneFirstConn.deviceId, event) }
            verify(exactly = 1) { eventMessagingTemplate.sendToDevice(cartOneSecondConn.deviceId, event) }
        }
    }
})
