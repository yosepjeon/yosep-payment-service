package com.yosep.payment.payment.adapter.out.persistent

import com.yosep.payment.common.PersistentAdapter
import com.yosep.payment.payment.adapter.out.persistent.repository.PaymentOutboxRepository
import com.yosep.payment.payment.adapter.out.persistent.repository.PaymentRepository
import com.yosep.payment.payment.adapter.out.persistent.repository.PaymentStatusUpdateRepository
import com.yosep.payment.payment.adapter.out.persistent.repository.PaymentValidationRepository
import com.yosep.payment.payment.application.port.out.*
import com.yosep.payment.payment.domain.PaymentEvent
import com.yosep.payment.payment.domain.PaymentEventMessage
import com.yosep.payment.payment.domain.PendingPaymentEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@PersistentAdapter
class PaymentPersistentAdapter(
    private val paymentRepository: PaymentRepository,
    private val paymentStatusUpdateRepository: PaymentStatusUpdateRepository,
    private val paymentValidationRepository: PaymentValidationRepository,
    private val paymentOutboxRepository: PaymentOutboxRepository,
) : SavePaymentPort, PaymentStatusUpdatePort, PaymentValidationPort, LoadPendingPaymentPort, LoadPendingPaymentEventMessagePort {

    override fun save(paymentEvent: PaymentEvent): Mono<Void> {
        return paymentRepository.save(paymentEvent)
    }

    override fun updatePaymentStatusToExecuting(
        orderId: String,
        paymentKey: String
    ): Mono<Boolean> {
        return paymentStatusUpdateRepository.updatePaymentStatusToExecuting(orderId, paymentKey)
    }

    override fun isValid(orderId: String, amount: Long): Mono<Boolean> {
        return paymentValidationRepository.isValid(orderId, amount)
    }

    override fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Mono<Boolean> {
        return paymentStatusUpdateRepository.updatePaymentStatus(command)
    }

    override fun getPendingPayments(): Flux<PendingPaymentEvent> {
        return paymentRepository.getPendingPayments()
    }

    override fun getPendingPaymentEventMessage(): Flux<PaymentEventMessage> {
        return paymentOutboxRepository.getPendingPaymentOutboxes()
    }
}