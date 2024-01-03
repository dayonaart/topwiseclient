package id.co.payment2go.terminalsdkhelper.check_balance.data.model

import com.google.gson.annotations.SerializedName

data class CardCheckBalanceDto(
    @SerializedName("CardMasked")
    val cardMasked: String,
    @SerializedName("IdTransaction")
    val transactionId: String,
    @SerializedName("ReffNum")
    val refNumber: String,
    @SerializedName("kodeAgen")
    val kodeAgen: String,
    @SerializedName("MMID")
    val mid: String,
    @SerializedName("MTID")
    val tid: String,
    @SerializedName("NII")
    val nii: String,
    @SerializedName("NARASI")
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
    @SerializedName("IID")
    val iid: String,
    @SerializedName("SEC")
    val secData: String,
    @SerializedName("DJson")
    val dataJson: String,
    val toAccount: String,
    @SerializedName("FLD3")
    val fld3: String
)

fun CardCheckBalance.toCardCheckBalanceDto(): CardCheckBalanceDto {
    return CardCheckBalanceDto(
        cardMasked = cardMasked,
        transactionId = transactionId,
        refNumber = refNum,
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
        iid = iid,
        secData = secData,
        toAccount = toAccount,
        dataJson = dataJson,
        fld3 = fld3
    )
}