package id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.model


data class BansosRequest(
    val amount: String,
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
    val refNum: String,
    val description: String
)