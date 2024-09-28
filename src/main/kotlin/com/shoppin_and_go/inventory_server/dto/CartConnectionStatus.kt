package com.shoppin_and_go.inventory_server.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.shoppin_and_go.inventory_server.domain.CartCode
import java.time.LocalDateTime

data class CartConnectionStatus(
    val cartCode: CartCode,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    val connectedAt: LocalDateTime,
    val connected: Boolean
)