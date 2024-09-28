package com.shoppin_and_go.inventory_server.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CartTest : DescribeSpec({
    describe("Cart#createConnection") {
        it("returns a CartConnection") {
            val cart = Cart(CartCode("ABC123"))
            val deviceId = DeviceId("device-xyz")

            val connection = cart.createConnection(deviceId)

            connection.cart shouldBe cart
            connection.deviceId shouldBe deviceId
        }
    }
})
