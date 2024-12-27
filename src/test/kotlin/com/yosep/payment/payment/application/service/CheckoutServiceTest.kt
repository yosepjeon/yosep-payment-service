package com.yosep.payment.payment.application.service

import com.yosep.payment.payment.application.port.`in`.CheckoutCommand
import com.yosep.payment.payment.application.port.`in`.CheckoutUseCase
import com.yosep.payment.payment.test.PaymentDatabaseHelper
import com.yosep.payment.payment.test.PaymentTestConfiguration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import reactor.test.StepVerifier
import java.util.UUID

@SpringBootTest
@Import(PaymentTestConfiguration::class)
class CheckoutServiceTest (
    @Autowired private val checkoutUseCase: CheckoutUseCase,
    @Autowired private val paymentDatabaseHelper: PaymentDatabaseHelper,
) {

    @BeforeEach
    fun setUp() {
        paymentDatabaseHelper.clean().block()
    }

    @Test
    fun `should save Payment Event and PaymentOrder successfully`() {
        val orderId = UUID.randomUUID().toString()
        val checkoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = listOf(1, 2, 3),
            idempotencyKey = orderId
        )

        StepVerifier.create(checkoutUseCase.checkout(checkoutCommand))
            .expectNextMatches {
                it.amount.toInt() == 60000 && it.orderId == orderId
            }
            .verifyComplete()

        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertEquals(paymentEvent.orderId, orderId)
        assertEquals(paymentEvent.paymentOrders.size, checkoutCommand.productIds.size)
        assertFalse(paymentEvent.isPaymentDone())
        assertFalse(paymentEvent.paymentOrders.all { it.isLedgerUpdated() })
        assertFalse(paymentEvent.paymentOrders.all { it.isWalletUpdated() })

    }
}