package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto


import com.google.gson.annotations.SerializedName

data class ResponseBodyDto(
    @SerializedName("Body")
    val body: String,
    @SerializedName("Error")
    val error: String,
    @SerializedName("MID")
    val mid: String,
    @SerializedName("Message")
    val message: String,
    @SerializedName("Param")
    val param: String,
    @SerializedName("TID")
    val tid: String,
    @SerializedName("SecData")
    val secData: String?
)