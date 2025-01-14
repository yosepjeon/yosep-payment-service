package com.yosep.payment.payment.application.service

import com.yosep.payment.payment.adapter.out.exception.PaymentValidationException
import com.yosep.payment.payment.adapter.out.persistent.repository.PaymentRepository
import com.yosep.payment.payment.adapter.out.web.exception.PSPConfirmationException
import com.yosep.payment.payment.adapter.out.web.exception.TossPaymentError
import com.yosep.payment.payment.application.port.`in`.CheckoutCommand
import com.yosep.payment.payment.application.port.`in`.CheckoutUseCase
import com.yosep.payment.payment.application.port.`in`.PaymentConfirmCommand
import com.yosep.payment.payment.application.port.out.PaymentExecutorPort
import com.yosep.payment.payment.application.port.out.PaymentStatusUpdatePort
import com.yosep.payment.payment.application.port.out.PaymentValidationPort
import com.yosep.payment.payment.domain.*
import com.yosep.payment.payment.test.PaymentDatabaseHelper
import com.yosep.payment.payment.test.PaymentTestConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest
@Import(PaymentTestConfiguration::class)
class PaymentConfirmServiceTest(
    @Autowired private val checkoutUseCase: CheckoutUseCase,
    @Autowired private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    @Autowired private val paymentValidationPort: PaymentValidationPort,
    @Autowired private val paymentDatabaseHelper: PaymentDatabaseHelper,
    @Autowired private val paymentRepository: PaymentRepository,
    @Autowired private val paymentErrorHandler: PaymentErrorHandler,
) {

    private val mockPaymentExecutorPort = mockk<PaymentExecutorPort>()

    @BeforeEach
    fun setup() {
        paymentDatabaseHelper.clean().block()
    }

    @Test
    fun `should be marked as SUCCESS if Payment Confirmation success in PSP`() {
        Hooks.onOperatorDebug()

        val orderId = UUID.randomUUID().toString()

        val checkoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = listOf(1, 2, 3),
            idempotencyKey = orderId
        )

        val checkoutResult = checkoutUseCase.checkout(checkoutCommand).block()!!

        val paymentConfirmCommand = PaymentConfirmCommand(
            paymentKey = UUID.randomUUID().toString(),
            orderId = orderId,
            amount = checkoutResult.amount
        )

        val paymentConfirmService = PaymentConfirmService(
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentValidationPort = paymentValidationPort,
            paymentExecutorPort = mockPaymentExecutorPort,
            paymentErrorHandler = paymentErrorHandler
        )

        val paymentExecutionResult = PaymentExecutionResult(
            paymentKey = paymentConfirmCommand.paymentKey,
            orderId = paymentConfirmCommand.orderId,
            extraDetails = PaymentExtraDetails(
                type = PaymentType.NORMAL,
                method = PaymentMethod.EASY_PAY,
                totalAmount = paymentConfirmCommand.amount,
                orderName = "test_order_name",
                pspConfirmationStatus = PSPConfirmationStatus.DONE,
                approvedAt = LocalDateTime.now(),
                pspRawData = "{}"
            ),
            isSuccess = true,
            isRetryable = false,
            isUnknown = false,
            isFailure = false
        )

        every { mockPaymentExecutorPort.execute(paymentConfirmCommand) } returns Mono.just(
            paymentExecutionResult
        )

        val paymentConfirmationResult =
            paymentConfirmService.confirm(paymentConfirmCommand).block()!!

        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertEquals(paymentConfirmationResult.status, PaymentStatus.SUCCESS)
        assertTrue(paymentEvent.isSuccess())
        assertEquals(paymentEvent.paymentType, paymentExecutionResult.extraDetails!!.type)
        assertEquals(paymentEvent.paymentMethod, paymentExecutionResult.extraDetails!!.method)
        assertEquals(paymentEvent.orderName, paymentExecutionResult.extraDetails!!.orderName)
        assertEquals(
            paymentEvent.approvedAt?.truncatedTo(ChronoUnit.MINUTES),
            paymentExecutionResult.extraDetails!!.approvedAt.truncatedTo(ChronoUnit.MINUTES)
        )
    }

    @Test
    fun `should be marked as FAILURE if Payment Confirmation fails on PSP`() {
        Hooks.onOperatorDebug()

        val orderId = UUID.randomUUID().toString()

        val checkoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = listOf(1, 2, 3),
            idempotencyKey = orderId
        )

        val checkoutResult = checkoutUseCase.checkout(checkoutCommand).block()!!

        val paymentConfirmCommand = PaymentConfirmCommand(
            paymentKey = UUID.randomUUID().toString(),
            orderId = orderId,
            amount = checkoutResult.amount
        )

        val paymentConfirmService = PaymentConfirmService(
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentValidationPort = paymentValidationPort,
            paymentExecutorPort = mockPaymentExecutorPort,
            paymentErrorHandler = paymentErrorHandler
        )

        val paymentExecutionResult = PaymentExecutionResult(
            paymentKey = paymentConfirmCommand.paymentKey,
            orderId = paymentConfirmCommand.orderId,
            extraDetails = PaymentExtraDetails(
                type = PaymentType.NORMAL,
                method = PaymentMethod.EASY_PAY,
                totalAmount = paymentConfirmCommand.amount,
                orderName = "test_order_name",
                pspConfirmationStatus = PSPConfirmationStatus.DONE,
                approvedAt = LocalDateTime.now(),
                pspRawData = "{}"
            ),
            failure = PaymentFailure("ERROR", "Test Error"),
            isSuccess = false,
            isRetryable = false,
            isUnknown = false,
            isFailure = true
        )

        every { mockPaymentExecutorPort.execute(paymentConfirmCommand) } returns Mono.just(paymentExecutionResult)

        val paymentConfirmationResult = paymentConfirmService.confirm(paymentConfirmCommand).block()!!

        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertEquals(paymentConfirmationResult.status, PaymentStatus.FAILURE)
        assertTrue(paymentEvent.isFailure())
    }

    @Test
    fun `should handle PSPConfirmationException`() {
        val orderId = UUID.randomUUID().toString()

        val checkoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = listOf(1, 2, 3),
            idempotencyKey = orderId
        )

        val checkoutResult = checkoutUseCase.checkout(checkoutCommand).block()!!

        val paymentConfirmCommand = PaymentConfirmCommand(
            paymentKey = UUID.randomUUID().toString(),
            orderId = orderId,
            amount = checkoutResult.amount
        )

        val paymentConfirmService = PaymentConfirmService(
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentValidationPort = paymentValidationPort,
            paymentExecutorPort = mockPaymentExecutorPort,
            paymentErrorHandler = paymentErrorHandler
        )

        val pspConfirmationException = PSPConfirmationException(
            errorCode = TossPaymentError.REJECT_ACCOUNT_PAYMENT.name,
            errorMessage = TossPaymentError.REJECT_ACCOUNT_PAYMENT.description,
            isSuccess = false,
            isFailure = true,
            isUnknown = false,
            isRetryableError = false
        )

        every { mockPaymentExecutorPort.execute(paymentConfirmCommand) } returns Mono.error(pspConfirmationException)

        val paymentConfirmationResult = paymentConfirmService.confirm(paymentConfirmCommand).block()!!
        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertEquals(paymentConfirmationResult.status, PaymentStatus.FAILURE)
        assertTrue(paymentEvent.isFailure())
    }

    @Test
    fun `should handle PaymentValidationException`() {
        val orderId = UUID.randomUUID().toString()

        val checkoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = listOf(1, 2, 3),
            idempotencyKey = orderId
        )

        val checkoutResult = checkoutUseCase.checkout(checkoutCommand).block()!!

        val paymentConfirmCommand = PaymentConfirmCommand(
            paymentKey = UUID.randomUUID().toString(),
            orderId = orderId,
            amount = checkoutResult.amount
        )

        val mockPaymentValidationPort = mockk<PaymentValidationPort>()

        val paymentConfirmService = PaymentConfirmService(
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentExecutorPort = mockPaymentExecutorPort,
            paymentValidationPort = mockPaymentValidationPort,
            paymentErrorHandler = paymentErrorHandler
        )

        val paymentValidationException = PaymentValidationException("결제 유효성 검증에서 실패하였습니다.")

        every { mockPaymentValidationPort.isValid(orderId, paymentConfirmCommand.amount) } returns Mono.error(paymentValidationException)

        val paymentConfirmationResult = paymentConfirmService.confirm(paymentConfirmCommand).block()!!
        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertEquals(paymentConfirmationResult.status, PaymentStatus.FAILURE)
        assertTrue(paymentEvent.isFailure())
    }

    @Test
    fun `should handle PaymentAlreadyProcessedException`() {
        val orderId = UUID.randomUUID().toString()

        val checkoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = listOf(1, 2, 3),
            idempotencyKey = orderId
        )

        val checkoutResult = checkoutUseCase.checkout(checkoutCommand).block()!!

        val paymentConfirmCommand = PaymentConfirmCommand(
            paymentKey = UUID.randomUUID().toString(),
            orderId = orderId,
            amount = checkoutResult.amount
        )


        val paymentConfirmService = PaymentConfirmService(
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentValidationPort = paymentValidationPort,
            paymentExecutorPort = mockPaymentExecutorPort,
            paymentErrorHandler = paymentErrorHandler
        )

        val paymentExecutionResult = PaymentExecutionResult(
            paymentKey = paymentConfirmCommand.paymentKey,
            orderId = paymentConfirmCommand.orderId,
            extraDetails = PaymentExtraDetails(
                type = PaymentType.NORMAL,
                method = PaymentMethod.EASY_PAY,
                totalAmount = paymentConfirmCommand.amount,
                orderName = "test_order_name",
                pspConfirmationStatus = PSPConfirmationStatus.DONE,
                approvedAt = LocalDateTime.now(),
                pspRawData = "{}"
            ),
            isSuccess = true,
            isRetryable = false,
            isUnknown = false,
            isFailure = false
        )

        every { mockPaymentExecutorPort.execute(paymentConfirmCommand) } returns Mono.just(paymentExecutionResult)

        paymentConfirmService.confirm(paymentConfirmCommand).block()!!
        val paymentConfirmationResult = paymentConfirmService.confirm(paymentConfirmCommand).block()!!

        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertEquals(paymentConfirmationResult.status, PaymentStatus.SUCCESS)
        assertTrue(paymentEvent.paymentOrders.all { it.paymentStatus == PaymentStatus.SUCCESS })
    }
}