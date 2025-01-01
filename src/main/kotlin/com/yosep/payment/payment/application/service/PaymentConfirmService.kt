package com.yosep.payment.payment.application.service

import com.yosep.payment.common.UseCase
import com.yosep.payment.payment.application.port.`in`.PaymentConfirmCommand
import com.yosep.payment.payment.application.port.`in`.PaymentConfirmUseCase
import com.yosep.payment.payment.application.port.out.PaymentExecutorPort
import com.yosep.payment.payment.application.port.out.PaymentStatusUpdateCommand
import com.yosep.payment.payment.application.port.out.PaymentStatusUpdatePort
import com.yosep.payment.payment.application.port.out.PaymentValidationPort
import com.yosep.payment.payment.domain.PaymentConfirmationResult
import reactor.core.publisher.Mono

/**
 * NOT_STARTED에서 EXECUTING으로 변경하는 작업 구현
 */
@UseCase
class PaymentConfirmService(
    private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    private val paymentValidationPort: PaymentValidationPort,
    private val paymentExecutorPort: PaymentExecutorPort,
    private val paymentErrorHandler: PaymentErrorHandler,
): PaymentConfirmUseCase {

    override fun confirm(command: PaymentConfirmCommand): Mono<PaymentConfirmationResult> {
        return paymentStatusUpdatePort.updatePaymentStatusToExecuting(
            command.orderId,
            command.paymentKey)
            .filterWhen { paymentValidationPort.isValid(command.orderId, command.amount) }
            .flatMap { paymentExecutorPort.execute(command) }
            .flatMap {
                paymentStatusUpdatePort.updatePaymentStatus(
                    command = PaymentStatusUpdateCommand(
                        paymentKey = it.paymentKey,
                        orderId = it.orderId,
                        status = it.paymentStatus(),
                        extraDetails = it.extraDetails,
                        failure = it.failure
                    )
                ).thenReturn(it)
            }
            .map { PaymentConfirmationResult(status = it.paymentStatus(), failure = it.failure) }
            .onErrorResume { paymentErrorHandler.handlePaymentConfirmationError(it,command) }

    }
}