package com.yosep.payment.payment.domain

data class PaymentFailure (
  val errorCode: String,
  val message: String
)