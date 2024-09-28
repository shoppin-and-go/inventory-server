package com.shoppin_and_go.inventory_server.api

import com.shoppin_and_go.inventory_server.dto.ApiResponse
import com.shoppin_and_go.inventory_server.dto.ErrorResponse
import com.shoppin_and_go.inventory_server.exception.LogicalException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ApiResponse.error(e), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(LogicalException::class)
    fun handleCartNotFoundException(e: LogicalException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ApiResponse.error(e), e.httpStatus)
    }
}