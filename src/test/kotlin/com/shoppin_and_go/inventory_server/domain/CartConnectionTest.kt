package com.shoppin_and_go.inventory_server.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CartConnectionTest : DescribeSpec({
    describe("CartConnection#disconnect") {
        it("sets disconnectedAt") {
            val cart = Cart(CartCode("ABC123"))
            val deviceId = DeviceId("device-xyz")
            val connection = cart.createConnection(deviceId)

            connection.disconnect()

            connection.connected shouldBe false
        }
    }

    describe("CartConnection#connected") {
        lateinit var connection: CartConnection

        beforeEach {
            val cart = Cart(CartCode("ABC123"))
            val deviceId = DeviceId("device-xyz")
            connection = cart.createConnection(deviceId)
        }

        it("returns true") {
            connection.connected shouldBe true
        }

        context("when disconnectedAt is not null") {
            beforeEach { connection.disconnect() }

            it("returns false") {
                connection.connected shouldBe false
            }
        }
    }
})
