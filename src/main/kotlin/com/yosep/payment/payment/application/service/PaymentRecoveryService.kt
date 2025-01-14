package com.yosep.payment.payment.application.service

import com.yosep.payment.common.UseCase
import com.yosep.payment.payment.application.port.`in`.PaymentConfirmCommand
import com.yosep.payment.payment.application.port.`in`.PaymentRecoveryUseCase
import com.yosep.payment.payment.application.port.out.*
import org.springframework.scheduling.annotation.Scheduled
import reactor.core.scheduler.Schedulers
import java.util.concurrent.TimeUnit

@UseCase
class PaymentRecoveryService(
    private val loadPendingPaymentPort: LoadPendingPaymentPort,
    private val paymentValidationPort: PaymentValidationPort,
    private val paymentExecutorPort: PaymentExecutorPort,
    private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    private val paymentErrorHandler: PaymentErrorHandler,
) : PaymentRecoveryUseCase {

    private val scheduler = Schedulers.newSingle("recovery")

    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.SECONDS)
    override fun recovery() {
        // pending payment를 조회하는 코드
        loadPendingPaymentPort.getPendingPayments()
            .map {
                // 조회된 결제를 바탕으로 결제 승인 요청을 보내기 위한 command
                PaymentConfirmCommand(
                    paymentKey = it.paymentKey,
                    orderId = it.orderId,
                    amount = it.totalAmount()
                )
            }
            // Payment Confirm Command를 사용하여 결제 유효성 검사를하고
            // PSP에 전달해서 결제 승인 요청을 전달한 후 결과에 따라서 DB 결제 상태 업데이트
            // bulkhead pattern: 중요한 처리 작업을 시스템의 다른 부분에 영향을 주지 않도록 격리하는 방법
            .parallel(2)
            .runOn(Schedulers.parallel())
            .flatMap { command ->
                paymentValidationPort.isValid(command.orderId, command.amount).thenReturn(command)
                    .flatMap { paymentExecutorPort.execute(it) }
                    .flatMap { paymentStatusUpdatePort.updatePaymentStatus(PaymentStatusUpdateCommand(it)) }
                    .onErrorResume { paymentErrorHandler.handlePaymentConfirmationError(it, command).thenReturn(true) }
            }
            .sequential()
            .subscribeOn(scheduler)
            .subscribe()

    }
}