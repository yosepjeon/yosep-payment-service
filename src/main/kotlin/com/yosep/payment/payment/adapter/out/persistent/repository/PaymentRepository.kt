package com.yosep.payment.payment.adapter.out.persistent.repository

import com.yosep.payment.payment.domain.PaymentEvent
import com.yosep.payment.payment.domain.PendingPaymentEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PaymentRepository {

    fun save(paymentEvent: PaymentEvent): Mono<Void>

    fun getPendingPayments(): Flux<PendingPaymentEvent>

    fun getPayment(orderId: String): Mono<PaymentEvent>

    fun complete(paymentEvent: PaymentEvent): Mono<Void>
}