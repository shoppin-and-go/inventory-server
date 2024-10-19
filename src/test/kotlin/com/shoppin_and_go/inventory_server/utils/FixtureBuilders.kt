package com.shoppin_and_go.inventory_server.utils

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.setExp
import com.shoppin_and_go.inventory_server.domain.*
import net.jqwik.api.Arbitraries
import java.util.*

object FixtureBuilders {
    val fixtureMonkey: FixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .register(Cart::class.java) {
            it.giveMeBuilder<Cart>().setExp(Cart::code, Arbitraries.create(FixtureBuilders::cartCode))
        }
        .register(Product::class.java) {
            it.giveMeBuilder<Product>().setExp(Product::code, Arbitraries.create(FixtureBuilders::productCode))
        }
        .build()

    inline fun <reified T> builder() = fixtureMonkey.giveMeBuilder<T>()
    inline fun <reified T> sample(): T = builder<T>().sample()

    fun deviceId() = DeviceId("device-test_${UUID.randomUUID()}")
    fun cartCode() = CartCode("cart-test_${UUID.randomUUID()}")
    fun productCode() = ProductCode("product-test_${UUID.randomUUID()}")
    fun int() = Random().nextInt(1, 10)
}
