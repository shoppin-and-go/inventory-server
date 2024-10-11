package com.shoppin_and_go.inventory_server.utils

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.domain.ProductCode
import java.util.*

object FixtureBuilders {
    fun fixtureMonkey(): FixtureMonkey = FixtureMonkey.builder().plugin(KotlinPlugin()).build()

    inline fun <reified T> builder() = fixtureMonkey().giveMeBuilder<T>()
    inline fun <reified T> sample(): T = builder<T>().sample()

    fun deviceId() = DeviceId("device-test_${UUID.randomUUID()}")
    fun cartCode() = CartCode("cart-test_${UUID.randomUUID()}")
    fun productCode() = ProductCode("product-test_${UUID.randomUUID()}")
    fun int() = Random().nextInt(1, 10)
}