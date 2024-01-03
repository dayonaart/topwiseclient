package id.co.payment2go.terminalsdkhelper.program_pemerintah.ui

import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CheckedBin

data class ProgramPemerintahUiState(
    val transactionId: String = "",
    val fld3: String = "",
    val fld4: String = "",
    val fld48: String = "",
    val refNumber: String = "",
    val description: String = "Bansos",
    val cardNumber: String = "",
    val maskedCardNumber: String = "",
    val isCardInserted: Boolean = false,
    var isErrorPopUpShowing: Boolean = false,
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
)
