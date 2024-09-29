package com.shoppin_and_go.inventory_server.application

import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.event.InventoryChangeEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.*

@Service
class InventoryEventListener(
    private val cartConnectionRepository: CartConnectionRepository,
    private val messagingTemplate: EventMessagingTemplate
) {
    @EventListener
    fun noticeInventoryChange(event: InventoryChangeEvent) {
        val deviceIds = getDeviceIds(event.cartId)

        deviceIds.forEach { messagingTemplate.sendToDevice(it, event) }
    }

    private fun getDeviceIds(cartId: UUID): List<DeviceId> = cartConnectionRepository
        .findByCartIdAndDisconnectedAtIsNull(cartId)
        .map(CartConnection::deviceId)
}