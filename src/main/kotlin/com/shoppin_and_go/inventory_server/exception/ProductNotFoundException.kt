package com.shoppin_and_go.inventory_server.exception

import com.shoppin_and_go.inventory_server.domain.ProductCode
import org.springframework.http.HttpStatus

class ProductNotFoundException(code: ProductCode) : LogicalException() {
    override val message = "Product not found: $code"
    override val httpStatus = HttpStatus.BAD_REQUEST
}