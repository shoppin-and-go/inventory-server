package com.shoppin_and_go.inventory_server.dao

import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.domain.DeviceId
import org.springframework.data.repository.Repository
import java.util.*

interface CartConnectionRepository : Repository<CartConnection, UUID> {
    fun findByDeviceIdAndDisconnectedAtIsNull(deviceId: DeviceId): List<CartConnection>
    fun findByDeviceIdOrderByCreatedAtDesc(deviceId: DeviceId): List<CartConnection>
    fun findByCartIdAndDisconnectedAtIsNull(cartId: UUID): List<CartConnection>
    fun existsByDeviceIdAndDisconnectedAtIsNull(deviceId: DeviceId): Boolean
    fun existsByDeviceIdAndCartAndDisconnectedAtIsNull(deviceId: DeviceId, cart: Cart): Boolean
    fun save(cartConnection: CartConnection): CartConnection
    fun saveAll(cartConnections: Iterable<CartConnection>): List<CartConnection>
}