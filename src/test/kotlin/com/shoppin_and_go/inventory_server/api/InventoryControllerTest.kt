package com.shoppin_and_go.inventory_server.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.fixturemonkey.kotlin.setExp
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.dao.ProductRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.domain.Product
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import com.shoppin_and_go.inventory_server.event.InventoryChangeEvent
import com.shoppin_and_go.inventory_server.utils.FixtureBuilders
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
) : DescribeSpec() {
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") // Intellij false positive bug
    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    override fun extensions() = listOf(SpringExtension)

    init {
        this.describe("PATCH /carts/{cartCode}/inventories") {
            lateinit var cart: Cart
            val cartCode = FixtureBuilders.cartCode()
            val productCode = FixtureBuilders.productCode()

            val cartFixtureBuilder = FixtureBuilders.builder<Cart>().setExp(Cart::code, cartCode)
            val productFixtureBuilder = FixtureBuilders.builder<Product>().setExp(Product::code, productCode)

            beforeEach {
                val product = productFixtureBuilder.sample()
                cart = cartFixtureBuilder.sample()

                productRepository.save(product)
                cartRepository.save(cart)
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
            val deviceId = FixtureBuilders.deviceId()
            val cartCode = FixtureBuilders.cartCode()

            val cartFixtureBuilder = FixtureBuilders.builder<Cart>().setExp(Cart::code, cartCode)
            val productFixtureBuilder = FixtureBuilders.builder<Product>()

            beforeEach {
                val products = productFixtureBuilder.sampleList(2)
                cart = cartFixtureBuilder.sample()

                products.forEach {
                    cart.changeProductQuantity(it, Random().nextInt(1, 10))
                }

                productRepository.saveAll(products)
                cartRepository.save(cart)
                cartConnection = cartConnectionRepository.save(CartConnection(cart, deviceId))
            }

            it("200을 반환한다") {
                mockMvc.perform(
                    get("/devices/${deviceId}/carts/${cartCode}/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result.inventory.cartCode").value(cartCode.toString()))
                    .andExpect(jsonPath("$.result.inventory.items[0].name").value(cart.inventories[0].product.name))
                    .andExpect(jsonPath("$.result.inventory.items[0].price").value(cart.inventories[0].product.price))
                    .andExpect(jsonPath("$.result.inventory.items[0].quantity").value(cart.inventories[0].quantity))
                    .andExpect(jsonPath("$.result.inventory.items[1].name").value(cart.inventories[1].product.name))
                    .andExpect(jsonPath("$.result.inventory.items[1].price").value(cart.inventories[1].product.price))
                    .andExpect(jsonPath("$.result.inventory.items[1].quantity").value(cart.inventories[1].quantity))
            }

            context("코드에 해당하는 카트가 없을 때") {
                val invalidCartCode = FixtureBuilders.cartCode()

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
