package com.yosep.payment.payment.domain

import java.math.BigDecimal

data class Product(
    val id: Long,
    val amount: Long,
    val quantity: Int,
    val name: String,
    val sellerId: Long
)
