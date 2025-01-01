package com.yosep.payment.payment.adapter.out.web.toss

import com.yosep.payment.common.WebAdapter
import com.yosep.payment.payment.adapter.out.web.toss.executor.PaymentExecutor
import com.yosep.payment.payment.application.port.`in`.PaymentConfirmCommand
import com.yosep.payment.payment.application.port.out.PaymentExecutorPort
import com.yosep.payment.payment.domain.PaymentExecutionResult
import reactor.core.publisher.Mono

@WebAdapter
class PaymentExecutorWebAdapter (
  private val paymentExecutor: PaymentExecutor
) : PaymentExecutorPort {

  override fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult> {
    return paymentExecutor.execute(command)
      .map {
        println("!!!!!")
        println(it)
        return@map it
      }
  }
}