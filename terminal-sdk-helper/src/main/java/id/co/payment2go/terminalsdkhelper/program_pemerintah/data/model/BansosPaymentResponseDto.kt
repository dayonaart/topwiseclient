package id.co.payment2go.terminalsdkhelper.program_pemerintah.data.model


import com.google.gson.annotations.SerializedName
import id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.model.BansosPaymentResponse

data class BansosPaymentResponseDto(
    @SerializedName("EMVD")
    val emvData: String?,
    @SerializedName("FLD48")
    val fld48: String?,
    @SerializedName("MMID")
    val mmid: String,
    @SerializedName("MTID")
    val mtid: String,
    @SerializedName("RSPC")
    val responseCode: String,
    @SerializedName("RSPM")
    val responseMessage: String,
    @SerializedName("SEC")
    val secData: String?,
    @SerializedName("Narasi")
    val narasi: String?,
) {
    fun toBansosPaymentResponse(): BansosPaymentResponse {
        return BansosPaymentResponse(
            emvData = emvData,
            fld48 = fld48,
            mmid = mmid,
            mtid = mtid,
            responseCode = responseCode,
            responseMessage = responseMessage,
            secData = secData,
            narasi = narasi
        )
    }
}