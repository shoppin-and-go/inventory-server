package com.shoppin_and_go.inventory_server.domain

import com.shoppin_and_go.inventory_server.utils.FixtureBuilders
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe


class CartTest : DescribeSpec({
    val cartFixtureBuilder = FixtureBuilders.builder<Cart>()
    val productFixtureBuilder = FixtureBuilders.builder<Product>()

    describe("Cart#createConnection") {
        it("returns a CartConnection") {
            val cart = cartFixtureBuilder.sample()
            val deviceId = FixtureBuilders.deviceId()

            val connection = cart.createConnection(deviceId)

            connection.cart shouldBe cart
            connection.deviceId shouldBe deviceId
        }
    }

    describe("Cart#changeProductQuantity") {
        lateinit var cart: Cart
        lateinit var product: Product
        val quantityChange = FixtureBuilders.quantity()

        beforeEach {
            cart = cartFixtureBuilder.sample()
            product = productFixtureBuilder.sample()
        }

        it("카트에 상품이 없는 경우 인벤토리가 새로 추가된다") {
            cart.inventories.size shouldBe 0

            cart.changeProductQuantity(product, quantityChange)

            cart.inventories.size shouldBe 1

            val inventory = cart.inventories.first()

            inventory.cart shouldBe cart
            inventory.product shouldBe product
            inventory.quantity shouldBe quantityChange
        }

        context("카트에 동일한 상품이 없는 경우") {
            beforeEach {
                cart.flushInventories()
            }

            it("인벤토리가 새로 추가된다") {
                cart.changeProductQuantity(product, quantityChange)

                cart.inventories.size shouldBe 1

                val inventory = cart.inventories.first()

                inventory.cart shouldBe cart
                inventory.product shouldBe product
                inventory.quantity shouldBe quantityChange
            }
        }

        context("카트에 동일한 상품이 있는 경우") {
            val initialQuantity = FixtureBuilders.quantity()

            beforeEach {
                cart.changeProductQuantity(product, initialQuantity)
            }

            it("기존 인벤토리의 수량이 변경된다") {
                cart.inventories.size shouldBe 1

                cart.changeProductQuantity(product, quantityChange)

                cart.inventories.size shouldBe 1

                val inventory = cart.inventories.first()

                inventory.cart shouldBe cart
                inventory.product shouldBe product
                inventory.quantity shouldBe initialQuantity + quantityChange
            }
        }
    }

    describe("Cart#flushInventories") {
        it("clears all inventories") {
            val cart = cartFixtureBuilder.sample()
            val product = productFixtureBuilder.sample()
            val quantityChange = 1

            cart.changeProductQuantity(product, quantityChange)

            cart.inventories.size shouldBe 1

            cart.flushInventories()

            cart.inventories.size shouldBe 0
        }
    }
})
