package com.shoppin_and_go.inventory_server.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class InventoryChangeEventTest : DescribeSpec({
    it("클래스의 이름과 동일한 이벤트 이름을 가진다") {
        val event = InventoryChangeEvent(
            cartId = UUID.randomUUID(),
            inventoryUpdatedAt = LocalDateTime.now()
        )

        event.name shouldBe "InventoryChangeEvent"
    }

    it("cart ID를 페이로드로 가진다") {
        val cartId = UUID.randomUUID()
        val event = InventoryChangeEvent(
            cartId = cartId,
            inventoryUpdatedAt = LocalDateTime.now()
        )

        event.payload shouldBe mapOf("cartId" to cartId)
    }

    it("인벤토리 업데이트 시간을 +09:00를 오프셋으로 가지는 타임스탬프로 가진다") {
        val inventoryUpdatedAt = LocalDateTime.now()
        val event = InventoryChangeEvent(
            cartId = UUID.randomUUID(),
            inventoryUpdatedAt = inventoryUpdatedAt
        )

        event.timestamp shouldBe inventoryUpdatedAt.toInstant(ZoneOffset.of("+09:00"))
    }
})
