package id.co.payment2go.terminalsdkhelper.check_balance.data.model

data class CardCheckBalanceTest(
//    val amount: String,
    val cardMasked: String,
//    val emvData: String,
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
//    val track2Data: String,
    val iid: String,
    val toAccount: String,
    val dataJson: String,
    val secData: String
)