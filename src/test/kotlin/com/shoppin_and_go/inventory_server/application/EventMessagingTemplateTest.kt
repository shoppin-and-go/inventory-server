package com.shoppin_and_go.inventory_server.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.event.AbstractEvent
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.mockk
import io.mockk.verify
import org.springframework.messaging.Message
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.Instant

class EventMessagingTemplateTest : DescribeSpec({
    lateinit var simpMessagingTemplate: SimpMessagingTemplate
    lateinit var eventMessagingTemplate: EventMessagingTemplate

    beforeTest {
        simpMessagingTemplate = mockk(relaxed = true)
        eventMessagingTemplate = EventMessagingTemplate(simpMessagingTemplate)
    }

    describe("EventMessagingTemplate#sendToDevice") {
        val deviceId = DeviceId("device-xyz")
        val event = object : AbstractEvent {
            override val name: String = "AnyTestEvent"
            override val timestamp: Instant = Instant.now()
            override val payload: Map<String, Any> = mapOf("cartId" to "cart-test_1")
        }

        it("이벤트를 메세지 포맷으로 변경하여 전송한다") {
            eventMessagingTemplate.sendToDevice(deviceId, event)

            val expectedPayload = mapOf(
                "name" to event.name,
                "timestamp" to event.timestamp.toEpochMilli(),
                "payload" to event.payload
            )

            verify(exactly = 1) {
                simpMessagingTemplate.send(any(), match { message: Message<ByteArray> ->
                    message.payload contentEquals ObjectMapper().writeValueAsString(expectedPayload).toByteArray()
                })
            }
        }

        it("\"queue/device/{deviceId}\"로 메세지를 전송한다") {
            eventMessagingTemplate.sendToDevice(deviceId, event)

            verify(exactly = 1) { simpMessagingTemplate.send("/queue/device/${deviceId}", any()) }
        }
    }
})
