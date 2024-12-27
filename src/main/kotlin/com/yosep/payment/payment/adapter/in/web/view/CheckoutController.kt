package com.yosep.payment.payment.adapter.`in`.web.view

import com.yosep.payment.common.IdempotencyCreator
import com.yosep.payment.common.WebAdapter
import com.yosep.payment.payment.adapter.`in`.web.request.CheckoutRequest
import com.yosep.payment.payment.application.port.`in`.CheckoutCommand
import com.yosep.payment.payment.application.port.`in`.CheckoutUseCase
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@WebAdapter
@Controller
class CheckoutController(
    private val checkoutUseCase: CheckoutUseCase
) {

    @GetMapping("/")
    fun checkoutPage(request: CheckoutRequest, model: Model): Mono<String> {
        val command = CheckoutCommand(
            cartId = request.cartId,
            buyerId = request.buyerId,
            productIds = request.productIds,
            idempotencyKey = IdempotencyCreator.create(request.seed) // 원래는 request seed가 아닌 request 자체가 들어가야함
        )

        return checkoutUseCase.checkout(command = command)
            .map {
                model.addAttribute("orderId", it.orderId)
                model.addAttribute("orderName", it.orderName)
                model.addAttribute("amount", it.amount)
                "checkout"
            }
    }
}