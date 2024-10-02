package com.shoppin_and_go.inventory_server.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.setExp
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartInventoryRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.dao.ProductRepository
import com.shoppin_and_go.inventory_server.domain.*
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
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
    @Autowired private val cartConnectionRepository: CartConnectionRepository,
    @Autowired private val cartInventoryRepository: CartInventoryRepository,
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
                beforeEach { cartRepository.delete(cart) }

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

        this.describe("GET /devices/{deviceId}/carts/{cartCode}/inventories") {
            lateinit var cart: Cart
            lateinit var cartConnection: CartConnection
            lateinit var cartInventories: List<CartInventory>
            val deviceId = DeviceId("device-test_${UUID.randomUUID()}")
            val cartCode = CartCode("cart-test_${UUID.randomUUID()}")

            beforeEach {
                cart = fixtureMonkey
                    .giveMeBuilder<Cart>()
                    .setExp(Cart::code, cartCode)
                    .sample()
                    .let(cartRepository::save)

                cartConnection = cartConnectionRepository.save(CartConnection(cart, deviceId))

                val products = fixtureMonkey
                    .giveMeBuilder<Product>()
                    .sampleList(2)
                    .let(productRepository::saveAll)

                cartInventories = products.map {
                    CartInventory(cart, it).apply { changeQuantity(Random().nextInt(1, 10)) }
                }.let(cartInventoryRepository::saveAll)
            }

            it("200을 반환한다") {
                mockMvc.perform(
                    get("/devices/${deviceId}/carts/${cartCode}/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result.inventory.cartCode").value(cartCode.toString()))
                    .andExpect(jsonPath("$.result.inventory.items[0].name").value(cartInventories[0].product.name))
                    .andExpect(jsonPath("$.result.inventory.items[0].price").value(cartInventories[0].product.price))
                    .andExpect(jsonPath("$.result.inventory.items[0].quantity").value(cartInventories[0].quantity))
                    .andExpect(jsonPath("$.result.inventory.items[1].name").value(cartInventories[1].product.name))
                    .andExpect(jsonPath("$.result.inventory.items[1].price").value(cartInventories[1].product.price))
                    .andExpect(jsonPath("$.result.inventory.items[1].quantity").value(cartInventories[1].quantity))
            }

            context("코드에 해당하는 카트가 없을 때") {
                val invalidCartCode = CartCode("invalid-cart-code")

                it("400을 응답한다") {
                    mockMvc.perform(
                        get("/devices/${deviceId}/carts/${invalidCartCode}/inventories")
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isBadRequest)
                        .andExpect(jsonPath("$.code").value("ERROR"))
                        .andExpect(jsonPath("$.message").value("Cart not found: $invalidCartCode"))
                }
            }

            context("기기에 연결되지 않은 카트인 경우") {
                beforeEach { cartConnectionRepository.delete(cartConnection) }

                it("401을 응답한다") {
                    mockMvc.perform(
                        get("/devices/${deviceId}/carts/${cartCode}/inventories")
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isUnauthorized)
                        .andExpect(jsonPath("$.code").value("ERROR"))
                        .andExpect(jsonPath("$.message").value("This cart is not connected to this device"))
                }
            }
        }
    }
}
