package com.shoppin_and_go.inventory_server.exception

import com.shoppin_and_go.inventory_server.domain.DeviceId
import org.springframework.http.HttpStatus

class DeviceAlreadyConnectedException(deviceId: DeviceId) : LogicalException(
    HttpStatus.CONFLICT,
    ErrorCode.DEVICE_ALREADY_CONNECTED,
    "This device is already connected with another cart",
    mapOf("deviceId" to deviceId.toString())
)
