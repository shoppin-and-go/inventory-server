package com.shoppin_and_go.inventory_server.domain

import com.shoppin_and_go.inventory_server.utils.FixtureBuilders
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CartConnectionTest : DescribeSpec({
    val cartBuilder = FixtureBuilders.get<Cart>()

    describe("CartConnection#disconnect") {
        it("sets disconnectedAt") {
            val cart = cartBuilder.sample()
            val deviceId = FixtureBuilders.deviceId()
            val connection = cart.createConnection(deviceId)

            connection.disconnect()

            connection.connected shouldBe false
        }
    }

    describe("CartConnection#connected") {
        lateinit var connection: CartConnection

        beforeEach {
            val cart = cartBuilder.sample()
            val deviceId = FixtureBuilders.deviceId()
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
