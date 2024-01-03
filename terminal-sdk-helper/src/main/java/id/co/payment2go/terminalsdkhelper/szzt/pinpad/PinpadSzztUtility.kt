package id.co.payment2go.terminalsdkhelper.szzt.pinpad

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.szzt.sdk.device.Constants
import com.szzt.sdk.device.Device
import com.szzt.sdk.device.pinpad.PinPad
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.pinpad.OnPinPadResult
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId
import id.co.payment2go.terminalsdkhelper.szzt.BindServiceSzzt
import id.co.payment2go.terminalsdkhelper.szzt.utils.StringUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PinpadSzztUtility(bindService: BindServiceSzzt) : PinpadUtility {
    private val deviceManager = bindService.mDeviceManager
    private val pinPad = deviceManager.getDeviceByType(Device.TYPE_PINPAD).toList()[0] as PinPad
    private var mode by mutableStateOf(PinPad.Mode.MODE_3DES)

    override fun showPinPad(
        disorder: Boolean,
        cardNumber: String,
        onPinpadResult: (OnPinPadResult) -> Unit
    ) {
        pinPad.open {
            onPinpadResult(OnPinPadResult.OnInput(it, it))
            Log.i("open","input key $it")
        }
        pinPad.showText(0, "Masukkan Pin Kartu".toByteArray(), false)
        pinPad.setPinLimit(byteArrayOf(0, 6))
        val bundle = Bundle()
        bundle.putBoolean(PinPad.PinStyle.DELETE_MODE, false)
        bundle.putInt(PinPad.PinStyle.BTN_NUM_TEXT_SIZE, 10)
        bundle.putInt(PinPad.PinStyle.BTN_FUN_TEXT_SIZE, 10)
        bundle.putInt(PinPad.PinStyle.CUSTOM_VIEW_TYPE, 5)
        bundle.putBoolean("showTop", false)
        bundle.putBoolean("isNotSortKey", true)
        bundle.putBoolean("showTopText", false)
        bundle.putInt("btnStyle", 1)
        bundle.putString(PinPad.PinStyle.BTN_TEXT_COLOR, "#414141")
        pinPad.setPinViewStyle(bundle)
        pinPad.calculatePinBlock(
            2,
            cardNumber.toByteArray(),   //arrayASCIICardNumber:card number
            60000,                      //nTimeout_MS:Timeout, in milliseconds
            0,                          // nFlagSound for Keyboard sound 0 = active | 1 = inActive
            PinPad.PIN_TYPE_ANSI98,     // pinAlgMode：Encryption Algorithm
            mode                        // defaultMode PinPad (MODE_3DES)
        ) { retCode, data ->
            if (retCode > 0) {
                /** Confirm Section / Submit Section */
                onPinpadResult(OnPinPadResult.OnConfirm(data, false))
                Log.i("RET $retCode", "User Submit Data ${StringUtility.ByteArrayToString(data, retCode)}")
            } else if (retCode == 0) {
                /** Confirm Section with Empty Condition */
                onPinpadResult(OnPinPadResult.OnError(retCode))
                Log.i("RET 0", "User ByPass Submit Data ${StringUtility.ByteArrayToString(data, retCode)}")
            } else if (retCode == Constants.Error.PINPAD_USER_TIMEOUT) {
                /** PinPad get timeout */
                onPinpadResult(OnPinPadResult.OnError(retCode))
                Log.i("RET $retCode", "PINPAD_USER_TIMEOUT: $retCode has timeout")
            } else if (retCode == Constants.Error.PINPAD_USER_CANCEL) {
                /** PinPad canceled by user */
                onPinpadResult(OnPinPadResult.OnCancel)
                Log.i("RET $retCode", "PINPAD_USER_CANCEL: $retCode has canceled")
            } else {
                /** PinPad unknown Error */
                onPinpadResult(OnPinPadResult.OnError(retCode))
                Log.i("RET $retCode", "Unknown Status Pinpad : $retCode")
            }
            pinPad.close()
        }

    }


    override suspend fun injectMasterKey(masterKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            val ret =
                pinPad.updateMasterKey(
                    KeyId.mainKey,
                    masterKey,
                    null,
                )
            if (ret >= 0) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error injecting master key")
            }

        }
    }

    override suspend fun injectPinKey(pinKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            val ret: Int =
                pinPad.updateUserKey(
                    KeyId.mainKey,
                    PinPad.KeyType.PIN,
                    2,
                    pinKey,
                )
            if (ret >= 0) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error injecting pin key")
            }
        }
    }

    override suspend fun injectDataKey(dataKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            val ret: Int =
                pinPad.updateUserKey(
                    KeyId.mainKey,
                    PinPad.KeyType.TD,
                    3,
                    dataKey,
                )
            if (ret >= 0) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error injecting data key")
            }
        }
    }

    override suspend fun encryptData(message: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {

                Log.d("isi Message: ", message)

                val padded16message = if (message.length % 16 == 0) {
                    message
                } else {
                    message.padEnd(message.length + (16 - message.length % 16), '0')
                }

                val hexed = BytesUtil.stringToHex(padded16message)
                val data = ByteArray(512)
                val encryptResult = BytesUtil.hexString2ByteArray(hexed)
                val result = ArrayList<String>()

                if (encryptResult.size > data.size) {
                    val splitArray = splitByteArray(encryptResult, data.size)

                    splitArray.forEach {
                        Log.d("Isi Split: ", it.toString(Charsets.UTF_8))
                        result.add(isEncrypt(it).replace("\\s".toRegex(), ""))
                    }
                    Log.d("Join array encrypt: ", result.joinToString())
                    Resource.Success(result.joinToString(""))
                } else {
                    Log.d("hasil short encrypt: ", isEncrypt(encryptResult))

                    if (isEncrypt(encryptResult) != "Error Encrypt"){

                        Resource.Success(isEncrypt(encryptResult).replace("\\s".toRegex(), ""))

                    }else{
                        Log.d("State encrypt: ", isEncrypt(encryptResult))
                        Resource.Error("Error Encrypt")
                    }

                }

            } catch (e: Exception) {
                Log.d("TAG", "encryptData Crash: ${e.message}")
                Resource.Error(e.message.toString())
            }
        }
    }

    override suspend fun decryptData(hexedMessage: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val decryptResult = StringUtility.StringToByteArray(hexedMessage)
                val data = ByteArray(512)
                val result = ArrayList<String>()

                if (decryptResult.size > data.size) {
                    val splitArray = splitByteArray(decryptResult, data.size)

                    splitArray.forEach {
                        Log.d("Isi Split Decrypt: ", it.toString(Charsets.UTF_8))
                        result.add(isDecrypt(it))
                    }
                    Log.d("Join Array Decrypt: ", result.joinToString())
                    Resource.Success(result.joinToString(""))

                } else {
                    Log.d("hasil short decrypt: ", isDecrypt(decryptResult))
                    if (isDecrypt(decryptResult) != "Error Decrypt"){
                        Resource.Success(isDecrypt(decryptResult))
                    }else{
                        Resource.Error("Error Decrypt")
                    }

                }

            } catch (e: Exception) {
                Resource.Error("Error decrypting data")
            }
        }
    }

    private fun isEncrypt(encryptResult: ByteArray): String {
        return try {
            val data = ByteArray(512)

            val ret: Int = pinPad.encryptData(
                3,
                PinPad.ALGORITHM.ECB_ENCTYPT,
                null,
                encryptResult,
                data,
                mode
            )

            when (ret >= 0) {
                true -> {
                    Log.d("encryptData Sukses ", StringUtility.ByteArrayToString(data, ret))
                    Log.d("encryptData Sukses: ", encryptResult.size.toString())
                    StringUtility.ByteArrayToString(data, ret)
                }

                else -> {
                    Log.d("encryptData Error: ", encryptResult.size.toString())
                    Log.d("encrypt error: ", ret.toString())
                    "Error Encrypt"
                }
            }
        }catch (e: Exception){
            e.message.toString()
        }
    }

    private fun isDecrypt(decryptResult: ByteArray): String {

        return try {
            val data = ByteArray(512)

            val ret: Int = pinPad.encryptData(
                3,
                PinPad.ALGORITHM.ECB_DECRYPT,
                null,
                decryptResult,
                data,
                mode
            )

            when (ret >= 0) {
                true -> {
                    Log.d("decryptData Sukses ", Util.removeTextAfterLastCurlyBrace(String(data)))
                    Log.d("decryptData Sukses: ", decryptResult.size.toString())

                    val formattedResult = Util.removeTextAfterLastCurlyBrace(String(data))
                    formattedResult
                }

                else -> {
                    Log.d("decryptData Error: ", decryptResult.size.toString())
                    Log.d("decrypt error: ", ret.toString())

                    "Error Decrypt"
                }
            }
        }catch (e: Exception){
            e.message.toString()
        }

    }

    private fun splitByteArray(inputArray: ByteArray, chunkSize: Int): List<ByteArray> {
        val result = ArrayList<ByteArray>()
        var offset = 0

        while (offset < inputArray.size) {
            val size = chunkSize.coerceAtMost(inputArray.size - offset)
            val chunk = ByteArray(size)
            System.arraycopy(inputArray, offset, chunk, 0, size)
            result.add(chunk)
            offset += size
        }

        return result
    }


}