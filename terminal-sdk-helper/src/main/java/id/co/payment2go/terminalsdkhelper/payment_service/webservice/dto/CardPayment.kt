package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto

data class CardPayment(
    val amount: String,
    val cardMasked: String,
    val emvData: String,
    val idTransaction: String,
    val kodeAgen: String,
    val mid: String,
    val tid: String,
    val nii: String,
    val description: String,
    val pinBlock: String,
    val poscon: String,
    val posent: String,
    val refNum: String,
    val stan: Long,
    val iid: String,
    val track2Data: String,
    val toAccount: String,
    val dataJson: String,
    val secData: String,
    val fld3: String
)