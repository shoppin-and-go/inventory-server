package com.shoppin_and_go.inventory_server.domain

import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class CartConnection(
    @ManyToOne
    val cart: Cart,
    val deviceId: DeviceId,
) : BaseEntity() {
    private var disconnectedAt: LocalDateTime? = null

    val connected: Boolean
        get() = disconnectedAt == null

    fun disconnect() {
        disconnectedAt = LocalDateTime.now()
    }
}