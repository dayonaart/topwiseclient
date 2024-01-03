package id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.model

data class BansosPaymentResponse(
    val emvData: String?,
    val fld48: String?,
    val mmid: String,
    val mtid: String,
    val responseCode: String,
    val responseMessage: String,
    val secData: String?,
    val narasi: String?
)
