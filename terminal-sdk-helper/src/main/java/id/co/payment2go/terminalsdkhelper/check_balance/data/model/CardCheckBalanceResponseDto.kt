package id.co.payment2go.terminalsdkhelper.check_balance.data.model


import com.google.gson.annotations.SerializedName

data class CardCheckBalanceResponseDto(
    @SerializedName("TAMT")
    val amount: Long,
    @SerializedName("RSPC")
    val responseCode: String,
    @SerializedName("RSPM")
    val responseMessage: String,
    @SerializedName("COREJOURNAL")
    val coreJournal: String?,
    @SerializedName("TXNID")
    val transactionId: String?,
    @SerializedName("FROMACCOUNT")
    val fromAccount: String?
)