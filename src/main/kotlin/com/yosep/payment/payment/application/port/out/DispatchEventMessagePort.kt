package com.yosep.payment.payment.application.port.out

import com.yosep.payment.payment.domain.PaymentEventMessage

interface DispatchEventMessagePort {

    fun dispatch(paymentEventMessage: PaymentEventMessage)
}