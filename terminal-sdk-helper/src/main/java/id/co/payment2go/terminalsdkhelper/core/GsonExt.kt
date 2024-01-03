package id.co.payment2go.terminalsdkhelper.core

import com.google.gson.JsonElement

/**
 * Returns the string value if the element is not null, otherwise returns null.
 * @author Samuel Mareno
 */
fun JsonElement.asStringOrNull(): String? {
    return if (this.isJsonNull) {
        null
    } else {
        this.asString
    }
}

/**
 * Returns the boolean value if the element is not null, otherwise returns false.
 * @author Samuel Mareno
 */
fun JsonElement.asBooleanOrNull(): Boolean {
    return if (this.isJsonNull) {
        false
    } else {
        this.asBoolean
    }
}