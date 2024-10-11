package com.shoppin_and_go.inventory_server.utils

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.domain.ProductCode
import java.util.*

object FixtureBuilders {
    fun builder(): FixtureMonkey = FixtureMonkey.builder().plugin(KotlinPlugin()).build()

    inline fun <reified T> get() = builder().giveMeBuilder<T>()
    inline fun <reified T> sample(): T = get<T>().sample()

    fun deviceId() = DeviceId("device-test_${UUID.randomUUID()}")
    fun cartCode() = CartCode("cart-test_${UUID.randomUUID()}")
    fun productCode() = ProductCode("product-test_${UUID.randomUUID()}")
    fun int() = Random().nextInt(1, 10)
}