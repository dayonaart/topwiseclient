package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto


import com.google.gson.annotations.SerializedName

data class InitResponseDto(
    @SerializedName("KEY1")
    val key1: String?,
    @SerializedName("KEY2")
    val key2: String?,
    @SerializedName("KEYS1")
    val keys1: String?,
    @SerializedName("KEYS2")
    val keys2: String?,
    @SerializedName("KODEAGEN")
    val kodeAgen: String?,
    @SerializedName("MMID")
    val mmid: String?,
    @SerializedName("MTID")
    val mtid: String?,
    @SerializedName("BMID")
    val bmid: String?,
    @SerializedName("BTID")
    val btid: String?,
    @SerializedName("RSPC")
    val responseCode: String,
    @SerializedName("RSPM")
    val responseMessage: String,
    @SerializedName("HSMFilter")
    val hsmFilter: String
)