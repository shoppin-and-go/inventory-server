package com.shoppin_and_go.inventory_server.domain

@JvmInline
value class DeviceId(private val value: String) {
    override fun toString(): String = value
}