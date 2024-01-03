package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto


import com.google.gson.annotations.SerializedName

data class CardPaymentResponseDto(
    @SerializedName("AMT")
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