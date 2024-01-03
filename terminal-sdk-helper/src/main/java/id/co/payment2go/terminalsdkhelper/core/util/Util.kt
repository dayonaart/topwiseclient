package id.co.payment2go.terminalsdkhelper.core.util

import android.content.Context
import android.os.Build
import com.google.gson.JsonObject
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.szzt.utils.StringUtility
import java.io.IOException
import java.io.InputStream
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

/**
 * Utility object providing various helper functions.
 *
 * The [Util] object contains static methods that offer utility functionalities for general use in the application.
 * These methods include masking card numbers, reading asset files, converting
 * hexadecimal strings to ASCII and vice versa.
 * @author Samuel Mareno
 */
object Util {

    /**
     * Masks the input card number by replacing all but the last four digits with asterisks.
     *
     * @param input The original card number.
     * @return The masked card number with asterisks.
     * @author Samuel Mareno
     */
    fun maskCardNumber(input: String): String {
        var maskedString = ""

        if (input.length > 4) {
            val maskedLength = input.length - 4
            maskedString = "*".repeat(maskedLength) + input.takeLast(4)
        }

        return maskedString
    }

    /**
     * Reads a file from the assets folder of the application.
     *
     * @param context The application context.
     * @param fileName The name of the file to be read.
     * @return The content of the file as a byte array, or null if the file could not be read.
     * @author Samuel Mareno
     */
    fun readAssetsFile(context: Context, fileName: String): ByteArray? {
        var input: InputStream? = null
        return try {
            input = context.assets.open(fileName)
            val buffer = ByteArray(input.available())
            input.read(buffer)
            buffer
        } catch (e: IOException) {
            null
        } finally {
            input?.close()
        }
    }

    /**
     * Converts a hexadecimal string to its corresponding ASCII representation.
     *
     * @param hexStr The hexadecimal string to be converted.
     * @return The ASCII representation of the hexadecimal string.
     * @author Samuel Mareno
     */
    fun hexToAscii(hexStr: String): String {
        val output = StringBuilder("")
        var i = 0
        while (i < hexStr.length) {
            val str = hexStr.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        return output.toString()
    }

    /**
     * Converts a string to its hexadecimal representation.
     *
     * @param ba The string to be converted.
     * @return The hexadecimal representation of the input string.
     * @author Samuel Mareno
     */
    fun toHex(ba: String): String {
        val str = StringBuilder()
        for (ch in ba.toCharArray()) {
            str.append(String.format("%02x", ch.code))
        }
        return str.toString()
    }

    /**
     * Checks if the given track2 data represents an IC card.
     *
     * @param track2 The track2 data of the card.
     * @return `true` if the track2 data represents an IC card, `false` otherwise.
     * @author Samuel Mareno
     */
    fun isIcCard(track2: String): Boolean {
        val index = track2.indexOf('D')
        if (index < 0) {
            return false
        }
        if (index + 6 > track2.length) {
            return false
        }
        return "2" == track2.substring(index + 5, index + 6) ||
                "6" == track2.substring(index + 5, index + 6)
    }

    /**
     * Checks whether a card is expired based on its expiry date.
     *
     * @param expiryDate The expiry date of the card in the format "yyMM".
     * @return true if the card is expired, false otherwise.
     * @author Samuel Mareno
     */
    fun isCardExpired(expiryDate: String?): Boolean {

        if (expiryDate == null) return true

        val currentDate = YearMonth.now()

        val formatter = DateTimeFormatter.ofPattern("yyMM")
        val expiryDateFormatted = YearMonth.parse(expiryDate, formatter)

        return expiryDateFormatted.isBefore(currentDate) || expiryDateFormatted == currentDate
    }

    fun getExpiryFromTrack2Data(input: String): String? {
        val index = input.indexOf('D')

        if (index != -1 && index + 4 < input.length) {
            return input.substring(index + 1, index + 5)
        }

        return null
    }

    /**
     * Merges the fields of an extension JSON object into a base JSON object recursively.
     *
     * @param baseObject The base JSON object to which the fields will be merged.
     * @param extensionObject The extension JSON object containing fields to merge.
     * @return A new JSON object containing the merged fields.
     * @author Samuel Mareno
     */
    fun mergeJsonObjects(baseObject: JsonObject, extensionObject: JsonObject): JsonObject {
        val newJsonObject = baseObject
        for ((key, value) in extensionObject.entrySet()) {
            if (value.isJsonObject && newJsonObject.has(key) && newJsonObject.get(key).isJsonObject) {
                mergeJsonObjects(newJsonObject.get(key).asJsonObject, value.asJsonObject)
            } else {
                newJsonObject.add(key, value)
            }
        }
        return newJsonObject
    }

    /**
     * Adds leading zeros to a given [number] string to achieve the desired number of [desiredDigits].
     *
     * If the length of the provided [number] is less than [desiredDigits], leading zeros are added to
     * make the final length equal to [desiredDigits].
     *
     * If the length of the provided [number] is already greater than or equal to [desiredDigits],
     * the original [number] is returned without any modifications.
     *
     * @param number The input [String] representing the number to which leading zeros are to be added.
     * @param desiredDigits The desired number of digits the output should have after adding leading zeros.
     * @return A [String] containing the [number] with added leading zeros to achieve [desiredDigits] length.
     * @author Samuel Mareno
     */
    fun addZerosToNumber(number: String, desiredDigits: Int): String {
        val currentDigits = number.length
        val zerosToAdd = desiredDigits - currentDigits

        return if (zerosToAdd > 0) {
            "0".repeat(zerosToAdd) + number
        } else {
            number
        }
    }

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

    /**
     * Removes the text after the last curly brace ('}') character from the given input string.
     *
     * If the input string contains at least one curly brace character ('}'), this function returns
     * a substring that includes all characters from the beginning of the input up to and including
     * the last occurrence of the curly brace. If no curly brace is found in the input, the function
     * returns the input string itself.
     *
     * @param input The input string from which the text after the last curly brace will be removed.
     * @return The modified string with the text after the last curly brace removed, or the original
     * input string if no curly brace is present.
     * @author Samuel Mareno
     */
    fun removeTextAfterLastCurlyBrace(input: String): String {
        val lastCurlyBraceIndex = input.lastIndexOf('}')

        return if (lastCurlyBraceIndex >= 0) {
            input.substring(0, lastCurlyBraceIndex + 1)
        } else {
            input
        }
    }

    fun onConfirmPinpadCase(data: ByteArray?): String {
        val type = Build.MANUFACTURER.uppercase()
        return when (Build.MODEL.uppercase()) {
            "KS8223" -> {
                //SZZT
                toHex(
                    StringUtility.ByteArrayToString(data, data!!.size).replace("\\s".toRegex(), "")
                )
            }

            else -> {
                if (type.contains("SUNMI") || type.contains("BLU")) {
                    toHex(BytesUtil.byteArray2HexString(data)).replace("\\s".toRegex(), "")
                } else {
                    BytesUtil.byteArray2HexString(data).replace("\\s".toRegex(), "")
                }
            }
        }
    }
}