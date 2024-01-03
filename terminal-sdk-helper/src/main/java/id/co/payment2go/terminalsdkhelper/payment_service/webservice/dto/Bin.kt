package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto

/**
 * Represents a Bank Identification Number (BIN).
 *
 * @property number The numerical value of the Bank Identification Number (BIN).
 *                 The value should be a 10-digit number, which is derived from the first 10 characters
 *                 of the original number represented as a string.
 *                 If the original number is less than 10 characters or cannot be parsed into a Long,
 *                 the default value 0 will be used.
 */
data class Bin(
    val number: Long
)
