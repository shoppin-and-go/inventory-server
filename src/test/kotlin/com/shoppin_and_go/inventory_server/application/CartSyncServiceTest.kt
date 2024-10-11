package com.shoppin_and_go.inventory_server.application

import com.navercorp.fixturemonkey.kotlin.setExp
import com.ninjasquad.springmockk.MockkBean
import com.shoppin_and_go.inventory_server.dao.CartConnectionRepository
import com.shoppin_and_go.inventory_server.dao.CartRepository
import com.shoppin_and_go.inventory_server.domain.Cart
import com.shoppin_and_go.inventory_server.domain.CartConnection
import com.shoppin_and_go.inventory_server.dto.CartConnectionStatus
import com.shoppin_and_go.inventory_server.exception.CartAlreadyConnectedException
import com.shoppin_and_go.inventory_server.exception.CartNotFoundException
import com.shoppin_and_go.inventory_server.exception.DeviceAlreadyConnectedException
import com.shoppin_and_go.inventory_server.utils.FixtureBuilders
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
    @MockkBean private val inventoryCommandService: InventoryCommandService,
) : DescribeSpec({
    extensions(SpringExtension)

    val cartSyncService = CartSyncService(cartRepository, cartConnectionRepository, inventoryCommandService)

    describe("CartSyncService#connectToCart") {

        val cartCode = FixtureBuilders.cartCode()
        val deviceId = FixtureBuilders.deviceId()
        val cart = FixtureBuilders.get<Cart>().setExp(Cart::code, cartCode).sample()

        beforeEach {
            every { cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId) } returns false
            every { cartConnectionRepository.existsByCartAndDisconnectedAtIsNull(cart) } returns false
            every { cartRepository.findByCode(cartCode) } returns cart
        }

        it("카트에 성공적으로 연결해야 한다") {
            val savedConnection = CartConnection(cart, deviceId)
            every { cartConnectionRepository.save(any()) } returns savedConnection

            val result = cartSyncService.connectToCart(cartCode, deviceId)

            result shouldBe CartConnectionStatus(savedConnection.cart.code, savedConnection.createdAt, savedConnection.connected)

            verify(exactly = 1) { cartRepository.findByCode(cartCode) }
            verify(exactly = 1) { cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId) }
            verify(exactly = 1) { cartConnectionRepository.save(any()) }
        }

        context("카트를 찾을 수 없을 때") {
            beforeEach {
                every { cartRepository.findByCode(cartCode) } returns null
            }

            it("CartNotFoundException을 던져야 한다") {
                shouldThrow<CartNotFoundException> {
                    cartSyncService.connectToCart(cartCode, deviceId)
                }

                verify(exactly = 1) { cartRepository.findByCode(cartCode) }
                verify(exactly = 0) { cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId) }
                verify(exactly = 0) { cartConnectionRepository.save(any()) }
            }
        }

        context("기기의 연결이 이미 존재할 때") {
            beforeEach {
                every { cartConnectionRepository.existsByDeviceIdAndDisconnectedAtIsNull(deviceId) } returns true
            }

            it("DuplicateCartConnectionException을 던져야 한다") {
                shouldThrow<DeviceAlreadyConnectedException> {
                    cartSyncService.connectToCart(cartCode, deviceId)
                }

                verify(exactly = 0) { cartConnectionRepository.save(any()) }
            }
        }

        context("카트의 연결이 이미 존재할 때") {
            beforeEach {
                every { cartConnectionRepository.existsByCartAndDisconnectedAtIsNull(cart) } returns true
            }

            it("AlreadyConnectedCartException을 던져야 한다") {
                shouldThrow<CartAlreadyConnectedException> {
                    cartSyncService.connectToCart(cartCode, deviceId)
                }

                verify(exactly = 0) { cartConnectionRepository.save(any()) }
            }
        }
    }

    describe("CartSyncService#disconnectFromAllCarts") {
        val deviceId = FixtureBuilders.deviceId()
        val cart1 = mockk<Cart>(relaxed = true)
        val cart2 = mockk<Cart>(relaxed = true)

        lateinit var connection1: CartConnection
        lateinit var connection2: CartConnection

        beforeEach {
            connection1 = mockk<CartConnection>(relaxed = true)
            connection2 = mockk<CartConnection>(relaxed = true)

            every { connection1.disconnect() } returns Unit
            every { connection2.disconnect() } returns Unit

            every { connection1.cart } returns cart1
            every { connection2.cart } returns cart2

            every { cartConnectionRepository.findByDeviceIdAndDisconnectedAtIsNull(deviceId) } returns listOf(connection1, connection2)
            every { cartConnectionRepository.save(connection1) } returns connection1
            every { cartConnectionRepository.save(connection2) } returns connection2
            every { inventoryCommandService.flushCartInventory(any()) } returns Unit

        }

        it("연결이 존재하는지 확인한다") {
            cartSyncService.disconnectFromAllCarts(deviceId)

            verify(exactly = 1) { cartConnectionRepository.findByDeviceIdAndDisconnectedAtIsNull(deviceId) }
        }

        it("모든 연결을 해제 한다") {
            val result = cartSyncService.disconnectFromAllCarts(deviceId)

            verify(exactly = 1) { connection1.disconnect() }
            verify(exactly = 1) { connection2.disconnect() }
            verify(exactly = 1) { cartConnectionRepository.save(connection1) }
            verify(exactly = 1) { cartConnectionRepository.save(connection2) }

            result shouldBe listOf(
                CartConnectionStatus.of(connection1),
                CartConnectionStatus.of(connection2)
            )
        }

        it("모든 연결된 카트의 인벤토리를 비운다") {
            cartSyncService.disconnectFromAllCarts(deviceId)

            verify(exactly = 1) { inventoryCommandService.flushCartInventory(cart1) }
            verify(exactly = 1) { inventoryCommandService.flushCartInventory(cart2) }
        }

        context("연결이 존재하지 않을 때") {
            beforeEach {
                every { cartConnectionRepository.findByDeviceIdAndDisconnectedAtIsNull(deviceId) } returns emptyList()
            }

            it("아무 작업도 수행하지 않아야 한다") {
                val result = cartSyncService.disconnectFromAllCarts(deviceId)

                verify(exactly = 0) { cartConnectionRepository.save(any()) }

                result shouldBe emptyList()
            }
        }
    }

    describe("CartSyncService#listCartConnections") {
        val deviceId = FixtureBuilders.deviceId()

        val connection1 = mockk<CartConnection>(relaxed = true)
        val connection2 = mockk<CartConnection>(relaxed = true)

        beforeEach {
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
            beforeEach {
                every { cartConnectionRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId) } returns emptyList()
            }

            it("빈 목록을 반환한다") {
                val result = cartSyncService.listCartConnections(deviceId)

                result shouldBe emptyList()
            }
        }
    }
})