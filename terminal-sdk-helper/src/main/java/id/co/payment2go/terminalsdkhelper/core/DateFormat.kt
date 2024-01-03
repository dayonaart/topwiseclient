package id.co.payment2go.terminalsdkhelper.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.toCurrentFormat(expectedPattern: String): String {
    val targetFormat = SimpleDateFormat(expectedPattern, Locale.ROOT)
    return targetFormat.format(this)
}