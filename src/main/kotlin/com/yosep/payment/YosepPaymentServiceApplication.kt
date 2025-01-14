package com.yosep.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class YosepPaymentServiceApplication

fun main(args: Array<String>) {
	runApplication<YosepPaymentServiceApplication>(*args)
}
