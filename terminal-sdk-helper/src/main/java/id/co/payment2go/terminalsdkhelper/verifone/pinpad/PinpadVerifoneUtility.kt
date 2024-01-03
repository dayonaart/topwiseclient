package id.co.payment2go.terminalsdkhelper.verifone.pinpad

import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vfi.smartpos.deviceservice.aidl.PinInputListener
import com.vfi.smartpos.deviceservice.constdefine.ConstIPinpad
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.pinpad.OnPinPadResult
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId
import id.co.payment2go.terminalsdkhelper.verifone.BindServiceVerifone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PinpadVerifoneUtility(bindService: BindServiceVerifone) : PinpadUtility {

    private val pinpad = bindService.iPinpad
    private var savedPinBlock by mutableStateOf<ByteArray?>(null)
    private val ipboc = bindService.emv
    private val workKeyId = 1

    override fun showPinPad(
        disorder: Boolean,
        cardNumber: String,
        onPinpadResult: (OnPinPadResult) -> Unit
    ) {

        val param = Bundle()
        val globleparam = Bundle()
        val panBlock: String = cardNumber
        val pinLimit = byteArrayOf(6)
        param.putByteArray(ConstIPinpad.startPinInput.param.KEY_pinLimit_ByteArray, pinLimit)
        param.putInt(ConstIPinpad.startPinInput.param.KEY_timeout_int, 20)
        param.putBoolean(ConstIPinpad.startPinInput.param.KEY_isOnline_boolean, true)
        param.putString(ConstIPinpad.startPinInput.param.KEY_pan_String, panBlock)
        param.putInt(
            ConstIPinpad.startPinInput.param.KEY_desType_int,
            ConstIPinpad.startPinInput.param.Value_desType_3DES
        )
        try {
            val pinPadListener = object : PinInputListener.Stub() {
                /**
                 * Listener Pinpad I/O Verifone
                 */
                override fun onInput(len: Int, key: Int) {
                    onPinpadResult(OnPinPadResult.OnInput(len, key))
                    Log.d("onInput", "PinPad onInput, len:$len, key:$key")
                }

                override fun onConfirm(data: ByteArray?, isNonePin: Boolean) {
                    Log.d("onConfirm", "PinPad onConfirm")
                    ipboc.importPin(1, data)
                    savedPinBlock = data
                    onPinpadResult(OnPinPadResult.OnConfirm(data, isNonePin))
                }

                override fun onCancel() {
                    onPinpadResult(OnPinPadResult.OnCancel)
                    Log.d("onCancel", "onCancel: Pinpad canceled")
                }

                override fun onError(errorCode: Int) {
                    onPinpadResult(OnPinPadResult.OnError(errorCode))
                    Log.d("onError", "onError: Pinpad error -- $errorCode")
                }
            }
            pinpad.startPinInput(workKeyId, param, globleparam, pinPadListener)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override suspend fun injectMasterKey(masterKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            val result =
                pinpad.loadMainKey(
                    KeyId.mainKey,
                    masterKey,
                    null
                )
            if (result) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error injecting master key")
            }
        }
    }

    override suspend fun injectPinKey(pinKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            val result =
                pinpad.loadWorkKeyEX(
                    2,
                    KeyId.mainKey,
                    KeyId.pinKey,
                    0x00,
                    pinKey,
                    null,
                    null
                )
            if (result) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error injecting pin key")
            }
        }
    }

    override suspend fun injectDataKey(dataKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            val result =
                pinpad.loadWorkKeyEX(
                    3,
                    KeyId.mainKey,
                    KeyId.tdkKey,
                    0x00,
                    dataKey,
                    null,
                    null
                )
            if (result) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error injecting data key")
            }
        }
    }

    override suspend fun encryptData(message: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val padded16message = if (message.length % 16 == 0) {
                    message
                } else {
                    message.padEnd(message.length + (16 - message.length % 16), '0')
                }
                val hexed = BytesUtil.stringToHex(padded16message)
                val result =
                    pinpad.calculateByDataKey(
                        KeyId.tdkKey,
                        0x01,
                        0x01,
                        0x00,
                        BytesUtil.hexString2ByteArray(hexed),
                        null
                    )
                Log.d("TAG", "encryptData: $result")
                return@withContext Resource.Success(BytesUtil.byteArray2HexString(result))
            } catch (e: Exception) {
                Log.d("TAG", "encryptData: ${e.message}")
                return@withContext Resource.Error("Error encrypting data")
            }
        }
    }

    override suspend fun decryptData(hexedMessage: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val result =
                    pinpad.calculateByDataKey(
                        KeyId.tdkKey,
                        0x01,
                        0x01,
                        0x01,
                        BytesUtil.hexStr2Bytes(hexedMessage),
                        null
                    )
                Log.d("TAG", "encryptData: $result")
                return@withContext Resource.Success(String(result))
            } catch (e: Exception) {
                Log.d("TAG", "encryptData: ${e.message}")
                return@withContext Resource.Error("Error encrypting data")
            }
        }
    }
}