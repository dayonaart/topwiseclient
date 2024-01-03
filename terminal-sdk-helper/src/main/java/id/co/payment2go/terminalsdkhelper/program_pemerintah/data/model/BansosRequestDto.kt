package id.co.payment2go.terminalsdkhelper.program_pemerintah.data.model

import com.google.gson.annotations.SerializedName
import id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.model.BansosRequest

data class BansosRequestDto(
    @SerializedName("AMT")
    val amount: String,
    @SerializedName("CardMasked")
    val cardMasked: String,
    @SerializedName("EMVD")
    val emvData: String,
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
    @SerializedName("T2D")
    val track2Data: String,
    @SerializedName("FLD3")
    val fld3: String,
    @SerializedName("FLD48")
    val fld48: String,
    @SerializedName("IID")
    val iid: String,
    @SerializedName("REFNO")
    val refNum: String
)

fun BansosRequest.toBansosRequestDto(): BansosRequestDto {
    return BansosRequestDto(
        amount = amount,
        cardMasked = cardMasked,
        emvData = emvData,
        kodeAgen = kodeAgen,
        mid = mid,
        tid = tid,
        nii = nii,
        pinBlock = pinBlock,
        poscon = poscon,
        posent = posent,
        stan = stan,
        track2Data = track2Data,
        fld3 = fld3,
        fld48 = fld48,
        iid = iid,
        refNum = refNum,
    )
}