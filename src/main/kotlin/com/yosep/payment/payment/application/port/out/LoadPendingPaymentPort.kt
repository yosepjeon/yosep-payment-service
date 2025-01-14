package com.yosep.payment.payment.application.port.out

import com.yosep.payment.payment.domain.PendingPaymentEvent
import reactor.core.publisher.Flux

interface LoadPendingPaymentPort {

    fun getPendingPayments(): Flux<PendingPaymentEvent>
}