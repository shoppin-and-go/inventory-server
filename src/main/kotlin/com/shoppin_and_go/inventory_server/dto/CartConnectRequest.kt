package com.shoppin_and_go.inventory_server.dto

import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.DeviceId

data class CartConnectRequest(
    val deviceId: DeviceId,
    val cartCode: CartCode
)
