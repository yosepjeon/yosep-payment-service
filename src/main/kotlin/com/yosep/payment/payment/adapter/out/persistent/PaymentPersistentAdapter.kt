package com.yosep.payment.payment.adapter.out.persistent

import com.yosep.payment.common.PersistentAdapter
import com.yosep.payment.payment.adapter.out.persistent.repository.PaymentRepository
import com.yosep.payment.payment.application.port.out.SavePaymentPort
import com.yosep.payment.payment.domain.PaymentEvent
import reactor.core.publisher.Mono

@PersistentAdapter
class PaymentPersistentAdapter(
    private val paymentRepository: PaymentRepository,
) : SavePaymentPort {
    override fun save(paymentEvent: PaymentEvent): Mono<Void> {
        return paymentRepository.save(paymentEvent)
    }
}