package com.yosep.payment.payment.application.port.out

import com.yosep.payment.payment.domain.PaymentEventMessage
import reactor.core.publisher.Flux

interface LoadPendingPaymentEventMessagePort {

    fun getPendingPaymentEventMessage(): Flux<PaymentEventMessage>
}