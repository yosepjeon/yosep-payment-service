package com.yosep.payment.payment.adapter.`in`.web.api

import com.yosep.payment.common.WebAdapter
import com.yosep.payment.payment.adapter.`in`.web.request.TossPaymentConfirmRequest
import com.yosep.payment.payment.adapter.`in`.web.response.ApiResponse
import com.yosep.payment.payment.application.port.`in`.PaymentConfirmCommand
import com.yosep.payment.payment.application.port.`in`.PaymentConfirmUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono

@WebAdapter
@Controller
@RequestMapping("/v1/toss")
class TossPaymentController (
    private val paymentConfirmUseCase: PaymentConfirmUseCase,
) {

    /**
     * TossPaymentController에서 UseCase와 연결하도록 변경
     * 기존 코드는 연동목적의 임시 코드로 제거후 재작성
     */
    @PostMapping("/confirm")
    fun confirm(@RequestBody request: TossPaymentConfirmRequest): Mono<ResponseEntity<ApiResponse<PaymentConfirmUseCase>>> {
        val command = PaymentConfirmCommand(
            paymentKey = request.paymentKey,
            orderId = request.orderId,
            amount = request.amount
        )

        return paymentConfirmUseCase.confirm(command)
            .map {
                ResponseEntity.ok()
                    .body(ApiResponse.with(
                        httpStatus = HttpStatus.OK,
                        message = "",
                        data = it))
            }
    }
}