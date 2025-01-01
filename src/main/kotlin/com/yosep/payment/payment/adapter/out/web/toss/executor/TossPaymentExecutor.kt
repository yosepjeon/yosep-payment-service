package com.yosep.payment.payment.adapter.out.web.toss.executor

import com.yosep.payment.common.objectMapper
import com.yosep.payment.payment.adapter.out.web.exception.PSPConfirmationException
import com.yosep.payment.payment.adapter.out.web.exception.TossPaymentError
import com.yosep.payment.payment.adapter.out.web.response.TossFailureResponse
import com.yosep.payment.payment.adapter.out.web.response.TossPaymentConfirmationResponse
import com.yosep.payment.payment.application.port.`in`.PaymentConfirmCommand
import com.yosep.payment.payment.domain.*
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class TossPaymentExecutor(
    private val tossPaymentWebClient: WebClient,
) : PaymentExecutor {

    private val uri: String = "v1/payments/confirm"

    override fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult> {
        return tossPaymentWebClient.post()
            .uri(uri)
            .header("Idempotency-Key", command.orderId)
            .bodyValue(
                """
        {
          "paymentKey": "${command.paymentKey}",
          "orderId": "${command.orderId}", 
          "amount": ${command.amount}
        }
      """.trimIndent()
            )
            .retrieve()
            .onStatus({ statusCode: HttpStatusCode -> statusCode.is4xxClientError || statusCode.is5xxServerError }) { response ->
                response.bodyToMono(TossFailureResponse::class.java)
                    .flatMap {
                        val error = TossPaymentError.get(it.code)
                        Mono.error<PSPConfirmationException>(
                            PSPConfirmationException(
                                errorCode = error.name,
                                errorMessage = error.description,
                                isSuccess = error.isSuccess(),
                                isFailure = error.isFailure(),
                                isUnknown = error.isUnknown(),
                                isRetryableError = error.isRetryableError()
                            )
                        )
                    }
            }
            .bodyToMono(TossPaymentConfirmationResponse::class.java)
            .map {
                PaymentExecutionResult(
                    paymentKey = command.paymentKey,
                    orderId = command.orderId,
                    extraDetails = PaymentExtraDetails(
                        type = PaymentType.get(it.type),
                        method = PaymentMethod.get(it.method),
                        approvedAt = LocalDateTime.parse(
                            it.approvedAt,
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        ),
                        pspRawData = objectMapper.writeValueAsString(it),
                        orderName = it.orderName,
                        pspConfirmationStatus = PSPConfirmationStatus.get(it.status),
                        totalAmount = it.totalAmount.toLong()
                    ),
                    isSuccess = true,
                    isFailure = false,
                    isUnknown = false,
                    isRetryable = false
                )
            }
    }
}