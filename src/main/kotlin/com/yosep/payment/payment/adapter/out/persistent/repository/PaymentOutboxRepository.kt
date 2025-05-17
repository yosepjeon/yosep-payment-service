package com.yosep.payment.payment.adapter.out.persistent.repository

import com.yosep.payment.payment.application.port.out.PaymentStatusUpdateCommand
import com.yosep.payment.payment.domain.PaymentEventMessage
import com.yosep.payment.payment.domain.PaymentEventMessageType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PaymentOutboxRepository {

    fun insertOutbox(command: PaymentStatusUpdateCommand): Mono<PaymentEventMessage>

    fun markMessageAsSend(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean>

    fun markMessageAsFailure(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean>

    fun getPendingPaymentOutboxes(): Flux<PaymentEventMessage>
}