package com.yosep.payment.payment.adapter.out.exception

import com.yosep.payment.payment.domain.PaymentStatus

class PaymentAlreadyProcessedException(
  val status: PaymentStatus,
  message: String
) : RuntimeException(message) {
}