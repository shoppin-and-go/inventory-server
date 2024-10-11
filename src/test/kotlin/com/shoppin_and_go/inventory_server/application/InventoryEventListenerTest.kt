package com.shoppin_and_go.inventory_server.application

import com.navercorp.fixturemonkey.kotlin.setExp
import com.ninjasquad.springmockk.MockkBean
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.event.InventoryChangeEvent
import com.shoppin_and_go.inventory_server.utils.FixtureBuilders
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.verify
import java.time.LocalDateTime

class InventoryEventListenerTest(
    @MockkBean(relaxed = true) private val eventMessagingTemplate: EventMessagingTemplate,
    @MockkBean private val cartConnectionRepository: CartConnectionRepository,
) : DescribeSpec({
    val inventoryEventListener = InventoryEventListener(cartConnectionRepository, eventMessagingTemplate)

    val cartBuilder = FixtureBuilders.builder<Cart>()

    val cartOne = cartBuilder.sample()
    val cartTwo = cartBuilder.sample()

    val cartOneConnectionBuilder = FixtureBuilders.builder<CartConnection>().setExp(CartConnection::cart, cartOne)
    val cartTwoConnectionBuilder = FixtureBuilders.builder<CartConnection>().setExp(CartConnection::cart, cartTwo)

    val cartOneFirstConn = cartOneConnectionBuilder.sample()
    val cartOneSecondConn = cartOneConnectionBuilder.sample()
    val cartTwoFirstConn = cartTwoConnectionBuilder.sample()
    val cartTwoSecondConn = cartTwoConnectionBuilder.sample()

    beforeEach {
        every { cartConnectionRepository.findByCartIdAndDisconnectedAtIsNull(cartOne.id) } returns listOf(
            cartOneFirstConn,
            cartOneSecondConn
        )
        every { cartConnectionRepository.findByCartIdAndDisconnectedAtIsNull(cartTwo.id) } returns listOf(
            cartTwoFirstConn,
            cartTwoSecondConn
        )
    }

    describe("InventoryEventListener#noticeInventoryChange") {
        it("인벤토리가 변경된 카트에 연결된 디바이스에 이벤트를 전송한다") {
            val event = InventoryChangeEvent(cartOne.id, LocalDateTime.now())
            inventoryEventListener.noticeInventoryChange(event)

            verify(exactly = 1) { eventMessagingTemplate.sendToDevice(cartOneFirstConn.deviceId, event) }
            verify(exactly = 1) { eventMessagingTemplate.sendToDevice(cartOneSecondConn.deviceId, event) }
        }
    }
})
