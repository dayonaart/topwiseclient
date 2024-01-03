package id.co.payment2go.terminalsdkhelper.core.util

object InputUtil {

    fun addZerosToNumber(number: String, desiredDigits: Int): String {
        val currentDigits = number.length
        val zerosToAdd = desiredDigits - currentDigits

        return if (zerosToAdd > 0) {
            "0".repeat(zerosToAdd) + number
        } else {
            number
        }
    }
}