package id.co.payment2go.terminalsdkhelper.szzt.pinpad

import com.szzt.sdk.device.Constants.*

object PinPadErrorSzzt {

    fun getErrorMessageEncDecrypt(error: Int): String {
        return when(error) {
            Error.FAILD -> "encryption or decryption is failed"
            Error. PINPAD_KEYUSEAGE_ERR -> "key id not exist"
            Error.PINPAD_KEYDESOUTSIZE_ERR -> "encrypt value length error"
            Error.PINPAD_KEYMODE_ERR -> "key mode error"
            Error.PINPAD_UNKNOWN_ERR -> "pinPad unknown error"
            Error.DEVICE_API_PARAM_ERROR -> "function parameter error"
            Error.ERROR_UNKNOWN -> "unknown error"
            Error.PINPAD_INDEX_OCCUPIED -> "The key index is already in use"
            else -> "unknown error"
        }
    }

}