package com.shoppin_and_go.inventory_server.event

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

data class InventoryChangeEvent(
    val cartId: UUID,
    val inventoryUpdatedAt: LocalDateTime,
) : AbstractEvent {
    override val name: String = this::class.simpleName!!
    override val payload: Map<String, Any> = mapOf("cartId" to cartId)
    override val timestamp: Instant = inventoryUpdatedAt.toInstant(ZoneOffset.of("+09:00"))
}
