package com.yosep.payment.payment.application.port.out

import reactor.core.publisher.Mono

interface PaymentStatusUpdatePort {

    // 이렇게 초기단계에서 executiong으로 변환하기 때문에 처리중인 문제가 발생해도 작업을 복구할수있는 기반을 마련할 수 있다.
    fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Mono<Boolean>

    fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Mono<Boolean>
}