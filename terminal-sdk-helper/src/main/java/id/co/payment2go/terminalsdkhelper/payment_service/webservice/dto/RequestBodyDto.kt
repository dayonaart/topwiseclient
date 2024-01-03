package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto

import com.google.gson.annotations.SerializedName

data class RequestBodyDto(
    @SerializedName("KodeAgen")
    val agentCode: String,
    @SerializedName("Body")
    val body: String,
    @SerializedName("MID")
    val mid: String,
    @SerializedName("Param")
    val param: String,
    @SerializedName("IDTransaction")
    val idTransaction: String,
    @SerializedName("RefNumber")
    val refNumber: String,
    @SerializedName("TID")
    val tid: String,

    @SerializedName("SEC")
    val secData: String,

    @SerializedName("SofType")
    val sofType: String,
//    @SerializedName("TotalAmount")
//    val totalAmount: String,
    @SerializedName("SECH")
    val secHeader: String,
    @SerializedName("TXNDate")
    val txnDate: String,
    @SerializedName("Narasi")
    val description: String,

    @SerializedName("TrxType")
    val trxType: String,

    @SerializedName("ServerKey")
    val serverKey: String,
)