package com.shoppin_and_go.inventory_server.exception

import com.shoppin_and_go.inventory_server.domain.DeviceId
import org.springframework.http.HttpStatus

class DuplicateCartConnectionException(deviceId: DeviceId) : LogicalException() {
    override val message = "This device is already connected to a cart: $deviceId"
    override val httpStatus = HttpStatus.CONFLICT
}