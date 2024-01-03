package id.co.payment2go.terminalsdkhelper.program_pemerintah.data.model

import com.google.gson.annotations.SerializedName

data class BansosPaymentRequestDto(
//    @SerializedName("AMT")
//    val amount: String,
    @SerializedName("CardMasked")
    val cardMasked: String,
    @SerializedName("IdTransaction")
    val idTransaction: String,
    @SerializedName("REFNO")
    val refNum: String,
//    @SerializedName("EMVD")
//    val emvData: String,
    @SerializedName("kodeAgen")
    val kodeAgen: String,
    @SerializedName("MMID")
    val mid: String,
    @SerializedName("MTID")
    val tid: String,
    @SerializedName("NII")
    val nii: String,
    @SerializedName("PINB")
    val pinBlock: String,
    @SerializedName("POSCON")
    val poscon: String,
    @SerializedName("POSENT")
    val posent: String,
    @SerializedName("STAN")
    val stan: Long,
//    @SerializedName("T2D")
//    val track2Data: String,
    @SerializedName("FLD3")
    val fld3: String,
    @SerializedName("FLD48")
    val fld48: String,
    @SerializedName("IID")
    val iid: String,
    @SerializedName("TXNTYPE")
    val txnType: String,
    @SerializedName("SEC")
    val secData: String,
    @SerializedName("Narasi")
    val narasi: String,
)