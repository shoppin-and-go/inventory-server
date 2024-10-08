package com.shoppin_and_go.inventory_server.domain

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.shoppin_and_go.inventory_server.exception.InvalidQuantityException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CartInventoryTest : DescribeSpec({
    val fixtureMonkey = FixtureMonkey.builder().plugin(KotlinPlugin()).build()

    val cart = fixtureMonkey.giveMeBuilder<Cart>().sample()
    val product = fixtureMonkey.giveMeBuilder<Product>().sample()

    describe("CartInventoryTest#changeQuantity") {
        var quantityChange: Int

        context("변경 수량이 0인 경우") {
            quantityChange = 0

            it("상품 수량 오류를 던진다") {
                val inventory = CartInventory(cart, product)

                val exception = shouldThrow<InvalidQuantityException> { inventory.changeQuantity(quantityChange) }

                exception.message shouldBe "Quantity change must be different from 0"
            }
        }

        context("변경 수량이 0보다 큰 경우") {
            quantityChange = 3

            it("재고를 증가시킨다") {
                val inventory = CartInventory(cart, product)
                val beforeQuantity = inventory.quantity

                inventory.changeQuantity(quantityChange)

                inventory.quantity shouldBe beforeQuantity + quantityChange
            }
        }

        context("변경 수량이 0보다 작은 경우") {
            quantityChange = -3

            it("재고를 감소시킨다") {
                val inventory = CartInventory(cart, product).apply { changeQuantity(7) }

                val beforeQuantity = inventory.quantity

                inventory.changeQuantity(quantityChange)

                inventory.quantity shouldBe beforeQuantity + quantityChange
            }

            context("재고가 변경 수량보다 작은 경우") {
                quantityChange = -3

                it("재고 수량 오류를 던진다") {
                    val inventory = CartInventory(cart, product).apply { changeQuantity(2) }

                    val exception = shouldThrow<InvalidQuantityException> { inventory.changeQuantity(quantityChange) }

                    exception.message shouldBe "Quantity must be less than or equal to 3"
                }
            }
        }
    }
})
