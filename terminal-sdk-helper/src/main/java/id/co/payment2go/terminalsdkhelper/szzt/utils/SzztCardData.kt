package id.co.payment2go.terminalsdkhelper.szzt.utils

data class SzztCardData(
    val mode: String = "",
    var cardNumber: String = "",
    val trackData: String = "",
    val message: String = "",
    val emvData: String = "",
    val expired: Boolean = false
)