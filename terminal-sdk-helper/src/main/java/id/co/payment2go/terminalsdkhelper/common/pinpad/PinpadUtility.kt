package id.co.payment2go.terminalsdkhelper.common.pinpad

import id.co.payment2go.terminalsdkhelper.core.util.Resource

interface PinpadUtility {
    fun showPinPad(disorder: Boolean, cardNumber: String, onPinpadResult: (OnPinPadResult) -> Unit,)
    suspend fun injectMasterKey(masterKey: ByteArray): Resource<Unit>
    suspend fun injectPinKey(pinKey: ByteArray): Resource<Unit>
    suspend fun injectDataKey(dataKey: ByteArray): Resource<Unit>
    suspend fun encryptData(message: String): Resource<String>
    suspend fun decryptData(hexedMessage: String): Resource<String>
}
