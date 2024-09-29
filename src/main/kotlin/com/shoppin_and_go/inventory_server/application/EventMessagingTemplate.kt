package com.shoppin_and_go.inventory_server.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.event.AbstractEvent
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Component

@Component
class EventMessagingTemplate(
    private val simpMessagingTemplate: SimpMessagingTemplate,
) {
    fun sendToDevice(deviceId: DeviceId, event: AbstractEvent) {
        simpMessagingTemplate.send("/queue/device/${deviceId}", toMessage(event))
    }

    private fun toMessage(event: AbstractEvent): Message<ByteArray> {
        val payload = mapOf(
            "name" to event.name,
            "timestamp" to event.timestamp.toEpochMilli(),
            "payload" to event.payload
        )

        val headers = mapOf(
            "name" to event.name,
            "timestamp" to event.timestamp.toEpochMilli(),
        )

        return GenericMessage(serializeAsJson(payload), MessageHeaders(headers))
    }

    private fun serializeAsJson(value: Any): ByteArray {
        return ObjectMapper().writeValueAsString(value).toByteArray()
    }
}