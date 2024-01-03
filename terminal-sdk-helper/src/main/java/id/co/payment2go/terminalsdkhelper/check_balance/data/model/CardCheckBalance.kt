package id.co.payment2go.terminalsdkhelper.check_balance.data.model

data class CardCheckBalance(
    val cardMasked: String,
    val transactionId: String,
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
    val toAccount: String,
    val dataJson: String,
    val secData: String,
    val fld3: String
)