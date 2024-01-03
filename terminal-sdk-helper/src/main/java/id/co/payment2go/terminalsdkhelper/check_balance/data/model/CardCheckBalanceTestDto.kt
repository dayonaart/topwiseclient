package id.co.payment2go.terminalsdkhelper.check_balance.data.model

import com.google.gson.annotations.SerializedName

data class CardCheckBalanceTestDto(
    @SerializedName("CardMasked")
    val cardMasked: String,
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
    @SerializedName("IID")
    val iid: String,
    @SerializedName("SEC")
    val secData: String,
    @SerializedName("DJson")
    val dataJson: String,
    val toAccount: String,
) {
    fun toCardCheckBalanceTest(): CardCheckBalanceTest {
        return CardCheckBalanceTest(
            cardMasked = cardMasked,
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
            iid = iid,
            secData = secData,
            toAccount = toAccount,
            dataJson = dataJson
        )
    }
}

fun CardCheckBalanceTest.toCardCheckBalanceTestDto(): CardCheckBalanceTestDto {
    return CardCheckBalanceTestDto(
        cardMasked = cardMasked,
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
        iid = iid,
        secData = secData,
        toAccount = toAccount,
        dataJson = dataJson
    )
}