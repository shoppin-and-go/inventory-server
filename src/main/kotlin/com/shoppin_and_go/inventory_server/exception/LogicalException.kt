package com.shoppin_and_go.inventory_server.exception

import org.springframework.http.HttpStatus

abstract class LogicalException : Exception() {
    abstract override val message: String
    abstract val httpStatus: HttpStatus
}