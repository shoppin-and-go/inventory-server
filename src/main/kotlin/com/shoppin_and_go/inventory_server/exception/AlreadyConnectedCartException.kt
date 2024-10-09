package com.shoppin_and_go.inventory_server.exception

import org.springframework.http.HttpStatus

class AlreadyConnectedCartException : LogicalException() {
    override val message = "This cart is already connected to another device"
    override val httpStatus = HttpStatus.CONFLICT
}