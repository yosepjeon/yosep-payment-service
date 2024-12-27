package com.yosep.payment.payment.adapter.out.web.product

import com.yosep.payment.common.WebAdapter
import com.yosep.payment.payment.adapter.out.web.product.client.ProductClient
import com.yosep.payment.payment.application.port.out.LoadProductPort
import com.yosep.payment.payment.domain.Product
import reactor.core.publisher.Flux

@WebAdapter
class ProductWebAdapter(
    private val productClient: ProductClient
) : LoadProductPort {
    override fun getProducts(cartId: Long, productIds: List<Long>): Flux<Product> {
        return productClient.getProduct(cartId, productIds)
    }
}