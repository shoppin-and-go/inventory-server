package com.shoppin_and_go.inventory_server.api

import com.epages.restdocs.apispec.Schema
import com.epages.restdocs.apispec.SimpleType
import com.fasterxml.jackson.databind.ObjectMapper
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.dao.ProductRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.domain.Product
import com.shoppin_and_go.inventory_server.dto.InventoryUpdateRequest
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.UnconnectedCartException
import com.shoppin_and_go.inventory_server.utils.FixtureBuilders
import com.shoppin_and_go.inventory_server.utils.restdoc.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class InventoryControllerDocumentTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired private val cartRepository: CartRepository,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val cartConnectionRepository: CartConnectionRepository,
) : DescribeSpec({
    extensions(SpringExtension)

    val cartBuilder = FixtureBuilders.builder<Cart>()
    val productFixtureBuilder = FixtureBuilders.builder<Product>()

    describe("PATCH /carts/{cartCode}/inventories") {
        val apiSpecIdentifier = "ChangeCartInventory"

        lateinit var cart: Cart
        lateinit var product: Product

        beforeEach {
            cart = cartRepository.save(cartBuilder.sample())
            product = productRepository.save(productFixtureBuilder.sample())
        }

        fun mockRequest(cart: Cart, request: InventoryUpdateRequest) = mockMvc.perform(
            patch("/carts/{cartCode}/inventories", cart.code)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )

        it("200 OK") {
            val request = InventoryUpdateRequest(product.code, FixtureBuilders.quantity())

            mockRequest(cart, request)
                .andExpect(status().isOk)
                .andApiSpec(apiSpecIdentifier) {
                    description("카트의 재고를 변경합니다.")
                    tags("CartInventory")
                    pathParameters(
                        "cartCode" pathMeans "변경할 카트의 코드" type SimpleType.STRING
                    )
                }
        }

        context("코드에 해당하는 카트가 없을 때") {
            beforeEach { cartRepository.delete(cart) }

            it("400 BadRequest") {
                val request = InventoryUpdateRequest(product.code, FixtureBuilders.quantity())

                mockRequest(cart, request)
                    .andExpect(status().isBadRequest)
                    .andErrorApiSpec<CartNotFoundException>(apiSpecIdentifier)

            }
        }
    }

    describe("GET /devices/{deviceId}/carts/{cartCode}/inventories") {
        val apiSpecIdentifier = "ListCartInventory"

        lateinit var cart: Cart
        lateinit var cartConnection: CartConnection
        var deviceId: DeviceId = FixtureBuilders.deviceId()

        beforeEach {
            cart = cartBuilder.sample()
            deviceId = FixtureBuilders.deviceId()

            val products = productFixtureBuilder.sampleList(2)
            products.forEach {
                cart.changeProductQuantity(it, FixtureBuilders.quantity())
            }

            productRepository.saveAll(products)
            cartRepository.save(cart)
            cartConnection = cartConnectionRepository.save(CartConnection(cart, deviceId))
        }

        fun mockRequest(deviceId: DeviceId, cart: Cart) = mockMvc.perform(
            get("/devices/{deviceId}/carts/{cartCode}/inventories", deviceId, cart.code)
        )

        it("200 OK") {
            mockRequest(deviceId, cart)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.inventory.cartCode").value(cart.code.toString()))
                .andExpect(jsonPath("$.result.inventory.items[0].name").value(cart.inventories[0].product.name))
                .andExpect(jsonPath("$.result.inventory.items[0].price").value(cart.inventories[0].product.price))
                .andExpect(jsonPath("$.result.inventory.items[0].quantity").value(cart.inventories[0].quantity))
                .andExpect(jsonPath("$.result.inventory.items[1].name").value(cart.inventories[1].product.name))
                .andExpect(jsonPath("$.result.inventory.items[1].price").value(cart.inventories[1].product.price))
                .andExpect(jsonPath("$.result.inventory.items[1].quantity").value(cart.inventories[1].quantity))
                .andApiSpec(apiSpecIdentifier) {
                    description("카트의 재고를 조회합니다.")
                    tags("CartInventory")
                    pathParameters(
                        "deviceId" pathMeans "조회할 기기의 ID" type SimpleType.STRING,
                        "cartCode" pathMeans "조회할 카트의 코드" type SimpleType.STRING
                    )
                    responseSchema(Schema("CartInventoryStatus"))
                    responseFields(
                        "code" type STRING means "응답 코드",
                        "message" type STRING means "응답 메시지",
                        "result.inventory.cartCode" type STRING means "카트코드",
                        "result.inventory.items[].name" type STRING means "상품명",
                        "result.inventory.items[].price" type NUMBER means "상품 가격",
                        "result.inventory.items[].quantity" type NUMBER means "상품 수량",
                    )
                }
        }

        context("코드에 해당하는 카트가 없을 때") {
            beforeEach {
                cartConnectionRepository.delete(cartConnection)
                cartRepository.delete(cart)
            }

            it("400 BadRequest") {
                mockRequest(deviceId, cart)
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.code").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("Cart not found: ${cart.code}"))
                    .andErrorApiSpec<CartNotFoundException>(apiSpecIdentifier)
            }
        }

        context("기기에 연결되지 않은 카트일 때") {
            beforeEach { cartConnectionRepository.delete(cartConnection) }

            it("401 Unauthorized") {
                mockRequest(deviceId, cart)
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.code").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("This cart is not connected to this device"))
                    .andErrorApiSpec<UnconnectedCartException>(apiSpecIdentifier)
            }
        }
    }
})
