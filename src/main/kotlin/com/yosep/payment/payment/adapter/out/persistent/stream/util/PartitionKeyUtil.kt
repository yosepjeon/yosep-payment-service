package com.yosep.payment.payment.adapter.out.persistent.stream.util

import org.springframework.stereotype.Component
import kotlin.math.abs

@Component
class PartitionKeyUtil {
    val PARTITION_KEY_COUNT = 3

    fun createPartitionKey(number: Int): Int {
        return abs(number) % PARTITION_KEY_COUNT
    }
}