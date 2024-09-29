package com.shoppin_and_go.inventory_server.event

import java.time.Instant

interface AbstractEvent {
    val name: String
    val payload: Map<String, Any>
    val timestamp: Instant
}
