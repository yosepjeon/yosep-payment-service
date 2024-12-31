package com.yosep.payment.payment.application.port.out

import reactor.core.publisher.Mono

// 클라이언트에서 조작된 결제 승인요청이 올수 있기때문에 결제 요청이 올바른지 검사하는 기능이 중요하다.
interface PaymentValidationPort {

  fun isValid(orderId: String, amount: Long): Mono<Boolean>
}