package com.yosep.payment.payment.application.port.out

import com.yosep.payment.payment.domain.PaymentEvent
import reactor.core.publisher.Mono

interface SavePaymentPort {

  fun save(paymentEvent: PaymentEvent): Mono<Void>
}