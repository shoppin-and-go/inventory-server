package com.shoppin_and_go.inventory_server.api

import com.epages.restdocs.apispec.Schema
import com.fasterxml.jackson.databind.ObjectMapper
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.dto.CartConnectRequest
import com.shoppin_and_go.inventory_server.exception.CartAlreadyConnectedException
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.DeviceAlreadyConnectedException
import com.shoppin_and_go.inventory_server.utils.FixtureBuilders
import com.shoppin_and_go.inventory_server.utils.restdoc.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.format.DateTimeFormatter

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class DeviceConnectionControllerIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val cartRepository: CartRepository,
    @Autowired val cartConnectionRepository: CartConnectionRepository,
) : DescribeSpec({
    extensions(SpringExtension)

    val cartBuilder = FixtureBuilders.builder<Cart>()

    describe("POST /cart-connections") {
        lateinit var cart: Cart

        beforeEach {
            cart = cartBuilder.sample()
            cartRepository.save(cart)
        }

        it("200 OK") {
            val deviceId = FixtureBuilders.deviceId()
            val request = CartConnectRequest(deviceId, cart.code)

            mockMvc.perform(
                post("/cart-connections")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result.connection.cartCode").value(cart.code.toString()))
                .andExpect(jsonPath("$.result.connection.connected").value(true))
                .andApiSpec("ConnectToCart") {
                    description("기기와 카트를 연결합니다.")
                    tags("DeviceConnection")
                    requestFields(
                        "deviceId" type STRING means "디바이스 ID",
                        "cartCode" type STRING means "카트 코드",
                    )
                    responseSchema(Schema("CartConnectionStatus"))
                    responseFields(
                        "code" type STRING means "응답 코드",
                        "message" type STRING means "응답 메시지",
                        "result.connection.cartCode" type STRING means "연결된 카트 코드",
                        "result.connection.connected" type BOOLEAN means "연결 여부",
                        "result.connection.connectedAt" type STRING means "연결 시간",
                    )
                }
        }

        context("존재하지 않는 카트에 연결하는 경우") {
            val deviceId = FixtureBuilders.deviceId()

            beforeEach {
                cartRepository.delete(cart)
            }

            it("400 Bad Request") {
                val request = CartConnectRequest(deviceId, cart.code)

                mockMvc.perform(
                    post("/cart-connections")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.code").value("ERROR"))
                    .andErrorApiSpec<CartNotFoundException>("ConnectToCart")
            }
        }

        context("기기가 다른 카트와 연결되어 있는 경우") {
            val deviceId = FixtureBuilders.deviceId()

            beforeEach {
                val anotherCart = FixtureBuilders.sample<Cart>()
                cartRepository.save(anotherCart)
                cartConnectionRepository.save(CartConnection(anotherCart, deviceId))
            }

            it("409 Conflict") {
                val request = CartConnectRequest(deviceId, cart.code)

                mockMvc.perform(
                    post("/cart-connections")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isConflict)
                    .andExpect(jsonPath("$.code").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("This device is already connected with another cart"))
                    .andErrorApiSpec<DeviceAlreadyConnectedException>("ConnectToCart")
            }
        }

        context("연결하려는 카트가 다른 기기와 연결되어 있는 경우") {
            val deviceId = FixtureBuilders.deviceId()

            beforeEach {
                val anotherDeviceId = FixtureBuilders.deviceId()
                cartConnectionRepository.save(CartConnection(cart, anotherDeviceId))
            }

            it("409 Conflict") {
                val request = CartConnectRequest(deviceId, cart.code)

                mockMvc.perform(
                    post("/cart-connections")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isConflict)
                    .andExpect(jsonPath("$.code").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("This cart is already connected with another device"))
                    .andErrorApiSpec<CartAlreadyConnectedException>("ConnectToCart")
            }
        }
    }

    describe("GET /devices/{deviceId}/cart-connections") {
        val deviceId = FixtureBuilders.deviceId()
        val otherDeviceId = FixtureBuilders.deviceId()

        lateinit var firstConnection: CartConnection
        lateinit var secondConnection: CartConnection

        beforeEach {
            val cartOne = cartRepository.save(cartBuilder.sample())
            val cartTwo = cartRepository.save(cartBuilder.sample())

            firstConnection = cartConnectionRepository.save(CartConnection(cartOne, deviceId))
            cartConnectionRepository.save(CartConnection(cartOne, otherDeviceId))

            secondConnection = cartConnectionRepository.save(CartConnection(cartTwo, deviceId).apply(CartConnection::disconnect))
            cartConnectionRepository.save(CartConnection(cartTwo, otherDeviceId))
        }

        it("연결 히스토리를 연결 시간의 역순으로 응답한다") {
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

            mockMvc.perform(get("/devices/{deviceId}/cart-connections", deviceId))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result.connections[0].cartCode").value(secondConnection.cart.code.toString()))
                .andExpect(jsonPath("$.result.connections[0].connected").value(false))
                .andExpect(jsonPath("$.result.connections[0].connectedAt").value(secondConnection.createdAt.format(dateTimeFormatter)))
                .andExpect(jsonPath("$.result.connections[1].cartCode").value(firstConnection.cart.code.toString()))
                .andExpect(jsonPath("$.result.connections[1].connected").value(true))
                .andExpect(jsonPath("$.result.connections[1].connectedAt").value(firstConnection.createdAt.format(dateTimeFormatter)))
                .andApiSpec("ListCartConnections") {
                    description("기기의 카트 연결 히스토리를 조회합니다.")
                    tags("DeviceConnection")
                    pathParameters(
                        "deviceId" pathMeans "디바이스 ID",
                    )
                    responseSchema(Schema("CartConnectionStatusList"))
                    responseFields(
                        "code" type STRING means "응답 코드",
                        "message" type STRING means "응답 메시지",
                        "result.connections" type ARRAY means "카트 연결 히스토리",
                        "result.connections[].cartCode" type STRING means "카트 코드",
                        "result.connections[].connected" type BOOLEAN means "연결 여부",
                        "result.connections[].connectedAt" type DATETIME means "연결 시간",
                    )
                }
        }
    }

    describe("DELETE /devices/{deviceId}/cart-connections") {
        val deviceId = FixtureBuilders.deviceId()
        val otherDeviceId = FixtureBuilders.deviceId()

        beforeEach {
            val cartOne = cartRepository.save(cartBuilder.sample())
            val cartTwo = cartRepository.save(cartBuilder.sample())

            cartConnectionRepository.save(CartConnection(cartOne, deviceId))
            cartConnectionRepository.save(CartConnection(cartOne, otherDeviceId))

            cartConnectionRepository.save(CartConnection(cartTwo, deviceId))
            cartConnectionRepository.save(CartConnection(cartTwo, otherDeviceId))
        }

        it("연결을 해제한다") {
            mockMvc.perform(get("/devices/{deviceId}/cart-connections", deviceId))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result.connections[0].connected").value(true))
                .andExpect(jsonPath("$.result.connections[1].connected").value(true))

            mockMvc.perform(delete("/devices/{deviceId}/cart-connections", deviceId))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result.connections[0].connected").value(false))
                .andExpect(jsonPath("$.result.connections[1].connected").value(false))
                .andApiSpec("DisconnectFromAllCarts") {
                    description("기기의 카트 연결을 모두 해제합니다.")
                    tags("DeviceConnection")
                    pathParameters(
                        "deviceId" pathMeans "디바이스 ID",
                    )
                    responseSchema(Schema("CartConnectionStatusList"))
                    responseFields(
                        "code" type STRING means "응답 코드",
                        "message" type STRING means "응답 메시지",
                        "result.connections" type ARRAY means "카트 연결 히스토리",
                        "result.connections[].cartCode" type STRING means "카트 코드",
                        "result.connections[].connected" type BOOLEAN means "연결 여부",
                        "result.connections[].connectedAt" type DATETIME means "연결 시간",
                    )
                }
        }

        it("다른 연결은 유지한다") {
            mockMvc.perform(delete("/devices/{deviceId}/cart-connections", deviceId))

            mockMvc.perform(get("/devices/{deviceId}/cart-connections", otherDeviceId))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result.connections[0].connected").value(true))
                .andExpect(jsonPath("$.result.connections[1].connected").value(true))
        }
    }
})


