package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto

import com.google.gson.annotations.SerializedName

data class CardPaymentDto(
    @SerializedName("AMT")
    val amount: String,
    @SerializedName("CardMasked")
    val cardMasked: String,
    @SerializedName("EMVD")
    val emvData: String,
    @SerializedName("IdTransaction")
    val idTransaction: String,
    @SerializedName("kodeAgen")
    val kodeAgen: String,
    @SerializedName("MMID")
    val mid: String,
    @SerializedName("MTID")
    val tid: String,
    @SerializedName("NII")
    val nii: String,
    @SerializedName("Narasi")
    val description: String,
    @SerializedName("PINB")
    val pinBlock: String,
    @SerializedName("POSCON")
    val poscon: String,
    @SerializedName("POSENT")
    val posent: String,
    @SerializedName("REFNO")
    val refNum: String,
    @SerializedName("STAN")
    val stan: Long,
    @SerializedName("T2D")
    val track2Data: String,
    val toAccount: String,
    @SerializedName("DJson")
    val dataJson: String,
    @SerializedName("SEC")
    val secData: String,
    @SerializedName("IID")
    val iid: String,
    @SerializedName("FLD3")
    val fld3: String
)

fun CardPayment.toCardPaymentDto(): CardPaymentDto {
    return CardPaymentDto(
        amount = amount,
        cardMasked = cardMasked,
        emvData = emvData,
        idTransaction = idTransaction,
        kodeAgen = kodeAgen,
        mid = mid,
        tid = tid,
        nii = nii,
        description = description,
        pinBlock = pinBlock,
        poscon = poscon,
        posent = posent,
        refNum = refNum,
        stan = stan,
        track2Data = track2Data,
        toAccount = toAccount,
        dataJson = dataJson,
        secData = secData,
        iid = iid,
        fld3 = fld3
    )
}