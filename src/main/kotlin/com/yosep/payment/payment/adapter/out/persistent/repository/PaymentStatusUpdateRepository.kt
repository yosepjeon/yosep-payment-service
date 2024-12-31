package com.yosep.payment.payment.adapter.out.persistent.repository

import com.yosep.payment.payment.application.port.out.PaymentStatusUpdateCommand
import reactor.core.publisher.Mono

interface PaymentStatusUpdateRepository {

  fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Mono<Boolean>

  fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Mono<Boolean>
}