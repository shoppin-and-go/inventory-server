package com.shoppin_and_go.inventory_server.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Cart(
    @Column(unique = true, nullable = false)
    val code: CartCode,
) : BaseEntity() {
    fun createConnection(deviceId: DeviceId): CartConnection {
        return CartConnection(this, deviceId)
    }
}
