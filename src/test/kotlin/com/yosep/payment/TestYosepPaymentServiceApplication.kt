package com.yosep.payment

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<YosepPaymentServiceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
