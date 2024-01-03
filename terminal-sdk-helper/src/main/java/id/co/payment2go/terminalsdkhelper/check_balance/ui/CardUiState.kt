package id.co.payment2go.terminalsdkhelper.check_balance.ui

import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CheckedBin

data class CardUiState(
    val transactionId: String = "",
    val totalAmount: Long = 0,
    val description: String = "",
    val refNumber: String = "",
    val cardNumber: String = "",
    val maskedCardNumber: String = "",
    val isCardInserted: Boolean = false,
    var isPopUpShowing: Boolean = false,
    val popUpMessage: String = "",
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val checkedBin: CheckedBin = CheckedBin(),
    val pins: List<Char> = listOf(),
    val stan: Long = 1,
    val posent: String = "",
    val poscon: String = "",
    val track2Data: String = "",
    val pinBlock: String = "",
    val emvData: String = "",
    val originEmv: String = "",
    val onlyOnUs: Boolean = true,
    val fld3: String = "",
    val mustOffUs: Boolean = false,
)
