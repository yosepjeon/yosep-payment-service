package com.yosep.payment.payment.domain

/**
 * @property isLedgerUpdated : 장부에 기입을 했는지 여부
 * @property isWalletUpdated : 정산 처리를 했는지 여부
 */
data class PaymentOrder(
    val id: Long? = null,
    val paymentEventId: Long? = null,
    val sellerId: Long,
    val productId: Long,
    val orderId: String,
    val amount: Long,
    val paymentStatus: PaymentStatus,
    private var isLedgerUpdated: Boolean = false,
    private var isWalletUpdated: Boolean = false,
) {

    fun isLedgerUpdated(): Boolean = isLedgerUpdated

    fun isWalletUpdated(): Boolean = isWalletUpdated

    fun confirmWalletUpdate() {
        isWalletUpdated = true
    }

    fun confirmLedgerUpdate() {
        isLedgerUpdated = true
    }
}
