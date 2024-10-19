package com.shoppin_and_go.inventory_server.exception

import com.shoppin_and_go.inventory_server.domain.ProductCode
import org.springframework.http.HttpStatus

class ProductNotFoundException(code: ProductCode) : LogicalException(
    HttpStatus.BAD_REQUEST,
    ErrorCode.PRODUCT_NOT_FOUND,
    "Product not found",
    mapOf("productCode" to code.toString())
)
