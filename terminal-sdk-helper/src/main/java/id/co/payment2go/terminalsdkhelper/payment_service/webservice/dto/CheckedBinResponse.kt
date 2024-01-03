package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto


import com.google.gson.annotations.SerializedName

data class CheckedBinResponse(
    @SerializedName("BINNAME")
    val binName: String?,
    @SerializedName("CARDTYPE")
    val cardType: String?,
    @SerializedName("NII")
    val NII: String?,
    @SerializedName("RSPC")
    val responseCode: String,
    @SerializedName("RSPM")
    val responseMessage: String,
    @SerializedName("isOnUs")
    val isOnUs: Boolean
) {
    fun toCheckedBin(): CheckedBin {
        return CheckedBin(
            binName = binName.toString(),
            cardType = cardType.toString(),
            NII = NII.toString(),
            isOnUs = isOnUs
        )
    }
}