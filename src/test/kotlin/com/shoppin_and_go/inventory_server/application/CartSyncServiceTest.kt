package com.shoppin_and_go.inventory_server.application

import com.ninjasquad.springmockk.MockkBean
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartCode
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.domain.DeviceId
import com.shoppin_and_go.inventory_server.dto.CartConnectionStatus
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.DuplicateCartConnectionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class CartSyncServiceTest(
    @MockkBean private val cartRepository: CartRepository,
    @MockkBean private val cartConnectionRepository: CartConnectionRepository,
) : DescribeSpec({
    extensions(SpringExtension)

    val cartSyncService = CartSyncService(cartRepository, cartConnectionRepository)

    describe("CartSyncService#connectToCart") {

        val cartCode = CartCode("ABC123")
        val deviceId = DeviceId("device-xyz")

        context("연결이 존재하지 않을 때") {
            beforeTest {
                every { cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId) } returns false
            }

            context("카트를 찾을 수 없을 때") {
                beforeTest {
                    every { cartRepository.findByCode(cartCode) } returns null
                }

                it("CartNotFoundException을 던져야 한다") {
                    shouldThrow<CartNotFoundException> {
                        cartSyncService.connectToCart(cartCode, deviceId)
                    }

                    verify(exactly = 1) { cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId) }
                    verify(exactly = 1) { cartRepository.findByCode(cartCode) }
                    verify(exactly = 0) { cartConnectionRepository.save(any()) }
                }
            }

            context("카트를 찾을 수 있을 때") {
                val cart = Cart(cartCode)

                beforeTest {
                    every { cartRepository.findByCode(cartCode) } returns cart
                }

                it("카트에 성공적으로 연결해야 한다") {
                    val savedConnection = CartConnection(cart, deviceId)
                    every { cartConnectionRepository.save(any()) } returns savedConnection

                    val result = cartSyncService.connectToCart(cartCode, deviceId)

                    result shouldBe CartConnectionStatus(savedConnection.cart.code, savedConnection.createdAt, savedConnection.connected)

                    verify(exactly = 1) { cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId) }
                    verify(exactly = 1) { cartRepository.findByCode(cartCode) }
                    verify(exactly = 1) { cartConnectionRepository.save(any()) }
                }
            }
        }

        context("연결이 이미 존재할 때") {
            beforeTest {
                every { cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId) } returns true
            }

            it("DuplicateCartConnectionException을 던져야 한다") {
                shouldThrow<DuplicateCartConnectionException> {
                    cartSyncService.connectToCart(cartCode, deviceId)
                }

                verify(exactly = 1) { cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId) }
                verify(exactly = 0) { cartRepository.findByCode(any()) }
                verify(exactly = 0) { cartConnectionRepository.save(any()) }
            }
        }
    }

    describe("CartSyncService#disconnectAll") {
        val deviceId = DeviceId("device-xyz")

        beforeTest {
            every { cartConnectionRepository.findByDeviceIdAndDisconnectedAtIsNull(deviceId) } returns emptyList()
        }

        it("연결이 존재하는지 확인한다") {
            cartSyncService.disconnectAll(deviceId)

            verify(exactly = 1) { cartConnectionRepository.findByDeviceIdAndDisconnectedAtIsNull(deviceId) }
        }

        context("연결이 존재할 때") {
            val connection1 = mockk<CartConnection>(relaxed = true)
            val connection2 = mockk<CartConnection>(relaxed = true)

            beforeTest {
                every { cartConnectionRepository.findByDeviceIdAndDisconnectedAtIsNull(deviceId) } returns listOf(connection1, connection2)
                every { cartConnectionRepository.saveAll(any()) } returns listOf(connection1, connection2)
            }

            it("모든 연결을 해제해야 한다") {
                val result = cartSyncService.disconnectAll(deviceId)

                verify(exactly = 1) { connection1.disconnect() }
                verify(exactly = 1) { connection2.disconnect() }
                verify(exactly = 1) { cartConnectionRepository.saveAll(any()) }

                result shouldBe listOf(
                    CartConnectionStatus(connection1.cart.code, connection1.createdAt, connection1.connected),
                    CartConnectionStatus(connection2.cart.code, connection2.createdAt, connection2.connected)
                )
            }
        }

        context("연결이 존재하지 않을 때") {
            beforeTest {
                every { cartConnectionRepository.findByDeviceIdAndDisconnectedAtIsNull(deviceId) } returns emptyList()
            }

            it("아무 작업도 수행하지 않아야 한다") {
                val result = cartSyncService.disconnectAll(deviceId)

                verify(exactly = 0) { cartConnectionRepository.saveAll(any()) }

                result shouldBe emptyList()
            }
        }
    }

    describe("CartSyncService#listCartConnections") {
        val deviceId = DeviceId("device-xyz")

        val connection1 = mockk<CartConnection>(relaxed = true)
        val connection2 = mockk<CartConnection>(relaxed = true)

        beforeTest {
            every { cartConnectionRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId) } returns listOf(connection1, connection2)
        }

        it("device ID에 해당하는 연결 목록을 생성시간의 역순으로 조회한다") {
            cartSyncService.listCartConnections(deviceId)

            verify(exactly = 1) { cartConnectionRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId) }
        }

        it("연결 목록을 반환한다") {
            val result = cartSyncService.listCartConnections(deviceId)

            result shouldBe listOf(
                CartConnectionStatus(connection1.cart.code, connection1.createdAt, connection1.connected),
                CartConnectionStatus(connection2.cart.code, connection2.createdAt, connection2.connected)
            )
        }

        context("연결이 존재하지 않을 때") {
            beforeTest {
                every { cartConnectionRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId) } returns emptyList()
            }

            it("빈 목록을 반환한다") {
                val result = cartSyncService.listCartConnections(deviceId)

                result shouldBe emptyList()
            }
        }
    }
})