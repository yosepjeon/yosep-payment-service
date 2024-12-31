package com.yosep.payment.payment.application.port.`in`

data class PaymentConfirmCommand (
  val paymentKey: String,
  val orderId: String,
  val amount: Long
)