package com.yosep.payment.payment.adapter.`in`.web.api

import com.yosep.payment.common.WebAdapter
import com.yosep.payment.payment.adapter.`in`.web.request.TossPaymentConfirmRequest
import com.yosep.payment.payment.adapter.`in`.web.response.ApiResponse
import com.yosep.payment.payment.adapter.out.web.toss.executor.TossPaymentExecutor
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
    private val tossPaymentExecutor: TossPaymentExecutor,
) {

    @PostMapping("/confirm")
    fun confirm(@RequestBody request: TossPaymentConfirmRequest): Mono<ResponseEntity<ApiResponse<String>>> {
        println(request)

        println("###########")
        println(request)
        return tossPaymentExecutor.execute(
            paymentKey = request.paymentKey,
            orderId = request.orderId,
            amount = request.amount.toString()
        ).map {
            ResponseEntity.ok().body(
                ApiResponse.with(HttpStatus.OK, "Ok", it)
            )
        }
    }
}