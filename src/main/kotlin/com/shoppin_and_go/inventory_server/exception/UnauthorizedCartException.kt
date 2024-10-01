package com.shoppin_and_go.inventory_server.exception

import org.springframework.http.HttpStatus

class UnauthorizedCartException : LogicalException() {
    override val message = "This cart is not connected to this device"
    override val httpStatus = HttpStatus.UNAUTHORIZED
}