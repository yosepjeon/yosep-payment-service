package com.yosep.payment.payment.adapter.out.web.toss.executor

import com.yosep.payment.payment.application.port.`in`.PaymentConfirmCommand
import com.yosep.payment.payment.domain.PaymentExecutionResult
import reactor.core.publisher.Mono

interface PaymentExecutor {

  fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult>
}