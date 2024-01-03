package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto

/**
 * Represents the result of a Bank Identification Number (BIN) check.
 *
 * @property binName The name or description associated with the Bank Identification Number (BIN).
 *                   It is an empty string by default if no name is available.
 * @property cardType The type or category of the card associated with the BIN.
 *                    It is an empty string by default if the card type is not specified.
 * @property NII The Network Identification Number (NII) associated with the BIN.
 *              It is an empty string by default if the NII is not specified.
 * @property isOnUs A Boolean value indicating whether the transaction is on-us or not.
 *                 It is false by default if not specified.
 */
data class CheckedBin(
    val binName: String = "",
    val cardType: String = "",
    val NII: String = "",
    val isOnUs: Boolean = false
)