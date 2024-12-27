package com.yosep.payment.payment.adapter.out.web.product.client

import com.yosep.payment.payment.domain.Product
import reactor.core.publisher.Flux

interface ProductClient {

    fun getProduct(cartId: Long, productIds: List<Long>): Flux<Product>
}