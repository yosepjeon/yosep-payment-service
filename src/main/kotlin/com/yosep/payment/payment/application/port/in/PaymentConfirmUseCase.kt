package com.yosep.payment.payment.application.port.`in`

import com.yosep.payment.payment.domain.PaymentConfirmationResult
import reactor.core.publisher.Mono

interface PaymentConfirmUseCase {

    fun confirm(command: PaymentConfirmCommand): Mono<PaymentConfirmationResult>
}