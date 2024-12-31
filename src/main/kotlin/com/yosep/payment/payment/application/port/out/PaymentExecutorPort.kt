package com.yosep.payment.payment.application.port.out

import com.yosep.payment.payment.application.port.`in`.PaymentConfirmCommand
import com.yosep.payment.payment.domain.PaymentExecutionResult
import reactor.core.publisher.Mono

interface PaymentExecutorPort {

  fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult>
}