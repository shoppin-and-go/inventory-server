package com.shoppin_and_go.inventory_server.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.dto.CartConnectRequest
import com.shoppin_and_go.inventory_server.utils.FixtureBuilders
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeviceConnectionControllerIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val cartRepository: CartRepository,
    @Autowired val cartConnectionRepository: CartConnectionRepository,
) : DescribeSpec({
    extensions(SpringExtension)

    val cartBuilder = FixtureBuilders.builder<Cart>()

    describe("POST /cart-connections") {
        val deviceId = FixtureBuilders.deviceId()
        val cartCode = FixtureBuilders.cartCode()
        lateinit var cart: Cart

        beforeEach {
            cart = cartRepository.save(Cart(cartCode))
        }

        it("연결정보를 응답한다") {
            val request = CartConnectRequest(deviceId, cartCode)

            mockMvc.perform(
                post("/cart-connections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result.connection.cartCode").value(cartCode.toString()))
                .andExpect(jsonPath("$.result.connection.connected").value(true))
        }

        context("코드에 해당하는 카트가 없는 경우") {
            beforeEach {
                cartRepository.delete(cart)
            }

            it("400 오류를 반환한다") {
                val request = CartConnectRequest(deviceId, cartCode)

                mockMvc.perform(
                    post("/cart-connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.code").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("Cart not found: $cartCode"))
            }
        }

        context("기기가 다른 카트와 연결되어 있는 경우") {
            beforeEach {
                val anotherCart = cartRepository.save(cartBuilder.sample())
                val connection = CartConnection(anotherCart, deviceId)
                cartConnectionRepository.save(connection)
            }

            it("409 오류를 반환한다") {
                val request = CartConnectRequest(deviceId, cartCode)

                mockMvc.perform(
                    post("/cart-connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isConflict)
                    .andExpect(jsonPath("$.code").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("This device is already connected with another cart"))
            }
        }

        context("연결하려는 카트가 다른 기기와 연결되어 있는 경우") {
            beforeEach {
                val anotherDeviceId = FixtureBuilders.deviceId()
                val connection = CartConnection(cart, anotherDeviceId)
                cartConnectionRepository.save(connection)
            }

            it("409 오류를 반환한다") {
                val request = CartConnectRequest(deviceId, cartCode)

                mockMvc.perform(
                    post("/cart-connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isConflict)
                    .andExpect(jsonPath("$.code").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("This cart is already connected with another device"))
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

            mockMvc.perform(
                get("/devices/$deviceId/cart-connections")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result.connections[0].cartCode").value(secondConnection.cart.code.toString()))
                .andExpect(jsonPath("$.result.connections[0].connected").value(false))
                .andExpect(jsonPath("$.result.connections[0].connectedAt").value(secondConnection.createdAt.format(dateTimeFormatter)))
                .andExpect(jsonPath("$.result.connections[1].cartCode").value(firstConnection.cart.code.toString()))
                .andExpect(jsonPath("$.result.connections[1].connected").value(true))
                .andExpect(jsonPath("$.result.connections[1].connectedAt").value(firstConnection.createdAt.format(dateTimeFormatter)))
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
            mockMvc.perform(
                get("/devices/$deviceId/cart-connections")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result.connections[0].connected").value(true))
                .andExpect(jsonPath("$.result.connections[1].connected").value(true))

            mockMvc.perform(
                delete("/devices/$deviceId/cart-connections")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result.connections[0].connected").value(false))
                .andExpect(jsonPath("$.result.connections[1].connected").value(false))
        }

        it("다른 연결은 유지한다") {
            mockMvc.perform(
                delete("/devices/$deviceId/cart-connections")
            )

            mockMvc.perform(
                get("/devices/$otherDeviceId/cart-connections")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result.connections[0].connected").value(true))
                .andExpect(jsonPath("$.result.connections[1].connected").value(true))
        }
    }
})
