package id.co.payment2go.terminalsdkhelper.core.util

import java.util.regex.Pattern

object StringUtil {

    private fun isNumber(value: String): Boolean {
        val pattern = Pattern.compile("[0-9]*")
        return pattern.matcher(value).matches()
    }

    fun getDigits(data: String): String {
        val sb = StringBuilder()
        if (isNumber(data)) {
            return data
        }
        for (element in data) {
            if (element in '0'..'9') {
                sb.append(element)
            }
        }
        return sb.toString()
    }
}