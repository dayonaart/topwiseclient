package id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.model


data class BansosInquiryRequest(
    val amount: String,
    val idTransaction: String,
    val description: String,
    val cardMasked: String,
    val emvData: String,
    val kodeAgen: String,
    val mid: String,
    val tid: String,
    val nii: String,
    val pinBlock: String,
    val poscon: String,
    val posent: String,
    val stan: Long,
    val track2Data: String,
    val fld3: String,
    val fld48: String,
    val iid: String,
    val txnType: String,
    val toAccount: String,
    val dataJson: String,
    val secData: String,
    val refNum: String,
)