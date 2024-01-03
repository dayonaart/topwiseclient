package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.perform_logon


import com.google.gson.annotations.SerializedName

data class LogonResponseDto(
    @SerializedName("SECDATA")
    val secData: SECDATA?
)