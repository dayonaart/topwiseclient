package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.perform_logon


import com.google.gson.annotations.SerializedName

data class SECDATA(
    @SerializedName("DATAKEY")
    val dataKey: String,
    @SerializedName("MACKEY")
    val macKey: String?,
    @SerializedName("PINKEY")
    val pinKey: String
)