package id.co.payment2go.terminalsdkhelper.common.model

import java.util.Date

data class TransactionConfig(
    var ecashCardOnlineEnabled: Boolean = false,
    var manuallyInputSupported: Boolean = false,
    var magCardSupported: Boolean = true,
    var contactIcCardSupported: Boolean = true,
    var rfCardSupported: Boolean = true,
    var eCashOnlineForbidden: Boolean = true,
    var onlineNeeded: Boolean = true,
    var pinInputNeeded: Boolean = true,
    var minPinLength: Long = 4,
    var maxPinLength: Long = 12,
    var pinRule: ByteArray = byteArrayOf(0, 6, 7, 8, 9, 10, 11, 12),
    var pinTimeout: Int = 60,
    var amount: Long = 0,
    var transactionDate: Date? = null,
    var transactionCode: String? = null,
    var certificate: String? = null,
    var rfQPbocSupported: Boolean? = null,
    var rfPayPassSupported: Boolean = false,
    var rfDebitCreditSupported: Boolean? = null,
    var gacFlag: Int = GAC_NONE,
    var transactionType: Int = TRANSACTION_TYPE_FULL,
    var rfTransactionAmountLimitCheckNeeded: Boolean = true,
    var eCashSupported: Boolean = false,
    var transactionResult: String? = null,
    var scriptResultCheckNeeded: Boolean = false,
    var rfOnlineForced: Boolean = false,
    var isQPSSupported: Boolean = false,
    var isRfwouldStopEvent: Boolean = false,
    var balanceNeeded: Boolean = false
) {
    companion object {
        const val GAC_NONE = 0
        const val GAC_FORCE_ONLINE = 1
        const val GAC_FORCE_OFFLINE = 2
        const val GAC_FORCE_DENIAL = 3
        const val TRANSACTION_TYPE_FULL = 0
        const val TRANSACTION_TYPE_SIMPLE = 1
        const val TRANSACTION_TYPE_ECLOG = 2
        const val TRANSACTION_TYPE_ICLOG = 3
        const val TRANSACTION_TYPE_RECOVERY = 4
        const val TRANSACTION_TYPE_BALANCE = 5
    }
}
