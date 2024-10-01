package com.shoppin_and_go.inventory_server.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.setExp
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.dao.ProductRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.Product
import com.shoppin_and_go.inventory_server.domain.ProductCode
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import com.shoppin_and_go.inventory_server.event.InventoryChangeEvent
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RecordApplicationEvents
class InventoryControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired private val cartRepository: CartRepository,
    @Autowired private val productRepository: ProductRepository,
) : DescribeSpec() {
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") // Intellij false positive bug
    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    override fun extensions() = listOf(SpringExtension)

    val fixtureMonkey: FixtureMonkey = FixtureMonkey.builder().plugin(KotlinPlugin()).build()

    init {
        this.describe("PATCH /carts/{cartCode}/inventories") {
            lateinit var cart: Cart
            val cartCode = CartCode("cart-test_${UUID.randomUUID()}")
            val productCode = ProductCode("product-test_${UUID.randomUUID()}")

            beforeEach {
                cart = fixtureMonkey
                    .giveMeBuilder<Cart>()
                    .setExp(Cart::code, cartCode)
                    .sample()
                    .let(cartRepository::save)

                fixtureMonkey
                    .giveMeBuilder<Product>()
                    .setExp(Product::code, productCode)
                    .sample()
                    .let(productRepository::save)
            }

            it("200을 반환한다") {
                val request = InventoryUpdateRequest(productCode, 1)

                mockMvc.perform(
                    MockMvcRequestBuilders.patch("/carts/${cartCode}/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ).andExpect(status().isOk)
            }

            it("인벤토리 변경 이벤트를 발행한다") {
                val request = InventoryUpdateRequest(productCode, 1)

                mockMvc.perform(
                    MockMvcRequestBuilders.patch("/carts/${cartCode}/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                val publishedEvents = applicationEvents.stream(InventoryChangeEvent::class.java).toList()

                publishedEvents.size shouldBe 1
                publishedEvents[0].cartId shouldBe cart.id
            }

            context("코드에 해당하는 카트가 없을 때") {
                beforeTest { cartRepository.deleteAllByCode(cartCode) }

                it("400을 응답한다") {
                    val request = InventoryUpdateRequest(productCode, 1)

                    mockMvc.perform(
                        MockMvcRequestBuilders.patch("/carts/${cartCode}/inventories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    ).andExpect(status().isBadRequest)
                }
            }
        }
    }
}
