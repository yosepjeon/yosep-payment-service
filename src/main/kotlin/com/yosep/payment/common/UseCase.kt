package com.yosep.payment.common

import org.springframework.stereotype.Component

/**
 * usecase: application이 제공하는 핵심 기능들의 작업흐름
 * usecase에서 도메인 패키지들의 클래스들이 상호작용하면서 비즈니스 로직들이 처리됨
 */
@Component
@Target(AnnotationTarget.CLASS)
annotation class UseCase()
