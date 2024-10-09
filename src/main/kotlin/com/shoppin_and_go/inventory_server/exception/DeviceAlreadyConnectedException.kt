package com.shoppin_and_go.inventory_server.exception

import org.springframework.http.HttpStatus

class DeviceAlreadyConnectedException : LogicalException() {
    override val message = "This device is already connected with another cart"
    override val httpStatus = HttpStatus.CONFLICT
}
