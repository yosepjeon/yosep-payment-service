package com.yosep.payment.payment.application.service

import com.yosep.payment.common.Logger
import com.yosep.payment.common.UseCase
import com.yosep.payment.payment.application.port.`in`.PaymentEventMessageRelayUseCase
import com.yosep.payment.payment.application.port.out.DispatchEventMessagePort
import com.yosep.payment.payment.application.port.out.LoadPendingPaymentEventMessagePort
import org.springframework.scheduling.annotation.Scheduled
import reactor.core.scheduler.Schedulers
import java.util.concurrent.TimeUnit

@UseCase
class PaymentEventMessageRelayService(
    private val loadPendingPaymentEventMessagePort: LoadPendingPaymentEventMessagePort,
    private val dispatchEventMessagePort: DispatchEventMessagePort,
): PaymentEventMessageRelayUseCase {

    private val scheduler = Schedulers.newSingle("message-relay")

    @Scheduled(fixedDelay = 1, initialDelay = 1, timeUnit = TimeUnit.SECONDS)
    override fun relay() {
        loadPendingPaymentEventMessagePort.getPendingPaymentEventMessage()
            .map { dispatchEventMessagePort.dispatch(it) }
            .onErrorContinue{ err, _ -> Logger.error("messageRelay", err.message ?: "failed to relay message.", err) }
            .subscribeOn(scheduler)
            .subscribe()
    }

}