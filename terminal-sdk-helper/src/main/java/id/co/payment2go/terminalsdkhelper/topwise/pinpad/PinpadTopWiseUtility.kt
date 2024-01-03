package id.co.payment2go.terminalsdkhelper.topwise.pinpad

import android.os.Bundle
import android.util.Log
import androidx.core.text.isDigitsOnly
import com.topwise.cloudpos.aidl.pinpad.GetPinListener
import com.topwise.cloudpos.data.PinpadConstant
import com.topwise.cloudpos.data.PinpadConstant.KeyType
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.pinpad.OnPinPadResult
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId
import id.co.payment2go.terminalsdkhelper.topwise.BindServiceTopWise
import id.co.payment2go.terminalsdkhelper.topwise.utils.HexUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class PinpadTopWiseUtility(bindService: BindServiceTopWise) : PinpadUtility {

    private val pinpad = bindService.pinpad

    override fun showPinPad(
        disorder: Boolean,
        cardNumber: String,
        onPinpadResult: (OnPinPadResult) -> Unit
    ) {
        /** Config Bundle PinPad */
        val bundle = Bundle()
        bundle.putInt("wkeyid", 0) // Pin Index
        bundle.putInt("keytype", 0x00) // pin type = online pin
        bundle.putByteArray("random", null)
        bundle.putInt("inputtimes", 1)
        bundle.putString("input_pin_mode", "0,4,5,6")
        bundle.putString("pan", cardNumber)
        bundle.putBoolean("is_lkl", disorder)

        /** Show Pinpad and Listener Action Pinpad */
        pinpad.setPinKeyboardMode(0)
        pinpad.buttonNum
        pinpad.getPin(bundle, object : GetPinListener.Stub() {
            override fun onInputKey(p0: Int, p1: String?) {
                onPinpadResult(OnPinPadResult.OnInput(p0, if (p1?.isDigitsOnly()!!) p1.toInt() else p0))
            }

            override fun onError(p0: Int) {
                onPinpadResult(OnPinPadResult.OnError(p0))
            }

            override fun onConfirmInput(p0: ByteArray?) {
                onPinpadResult(OnPinPadResult.OnConfirm(p0,false))
            }

            override fun onCancelKeyPress() {
                onPinpadResult(OnPinPadResult.OnCancel)
            }

            override fun onStopGetPin() {
                pinpad.stopGetPin()
                onPinpadResult(OnPinPadResult.OnError(99))
            }

            override fun onTimeout() {
                onPinpadResult(OnPinPadResult.OnError(99))
            }

        })
    }

    override suspend fun injectMasterKey(masterKey: ByteArray): Resource<Unit> {
        val ret = pinpad.loadMainkey(
            KeyId.mainKey,
            masterKey,
            null
        )

        return if (ret) {
            Resource.Success(Unit)
        } else {
            Resource.Error(message = "Error Injecting Master Key $ret")
        }

    }

    override suspend fun injectPinKey(pinKey: ByteArray): Resource<Unit> {
        val ret = pinpad.loadWorkKey(
            KeyType.KEYTYPE_PEK,
            KeyId.mainKey,
            KeyId.pinKey,
            pinKey,
            null
        )
        return if (ret) {
            Resource.Success(Unit)
        } else {
            Resource.Error(message = "Error Injecting Master Key $ret")
        }
    }

    override suspend fun injectDataKey(dataKey: ByteArray): Resource<Unit> {
        val ret = pinpad.loadWorkKey(
            KeyType.KEYTYPE_TDK,
            KeyId.mainKey,
            KeyId.tdkKey,
            dataKey,
            null
        )
        return if (ret) {
            Resource.Success(Unit)
        } else {
            Resource.Error(message = "Error Injecting Master Key $ret")
        }
    }

    override suspend fun encryptData(message: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("Wise Encrypt message", message)
                val padded16message = if (message.length % 16 == 0) {
                    message
                } else {
                    message.padEnd(message.length + (16 - message.length % 16), '0')
                }
                val hexed = BytesUtil.stringToHex(padded16message)
                val entryData = ByteArray(hexed.length / 2)

                val ret = pinpad.cryptByTdk(
                    KeyId.tdkKey,
                    PinpadConstant.BasicAlg.ALG_ENCRYPT_DES_ECB.toByte(),
                    HexUtil.hexStringToByte(hexed),
                    null,
                    entryData
                )

                return@withContext when (ret == 0) {
                    true -> {
                        Log.d("Wise Encrypt result", HexUtil.bcd2str(entryData))
                        Resource.Success(data = HexUtil.bcd2str(entryData))
                    }

                    else -> {
                        Log.d("Wise Encrypt: ", "Error")
                        Resource.Error(message = "Error while encrypting data")
                    }
                }
            } catch (e: Exception) {
                Log.d("TAG", "encryptData: ${e.message}")
                return@withContext Resource.Error("Error encrypting data")
            }
        }
    }

    override suspend fun decryptData(hexedMessage: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("Wise Decrypt message", hexedMessage)
                val entryData = ByteArray(hexedMessage.length / 2)
                val ret = pinpad.cryptByTdk(
                    KeyId.tdkKey,
                    PinpadConstant.BasicAlg.ALG_DECRYPT_DES_ECB.toByte(),
                    HexUtil.hexStringToByte(hexedMessage),
                    null,
                    entryData
                )
                val formattedResult = Util.removeTextAfterLastCurlyBrace(String(entryData))

                return@withContext when (ret == 0) {
                    true -> {
                        Log.d("Wise Decrypt result", formattedResult)
                        Resource.Success(data = formattedResult)
                    }

                    else -> {
                        Log.d("Wise Decrypt: ", "Error")
                        Resource.Error(message = "Error while decrypting data")
                    }
                }
            } catch (e: Exception) {
                Log.d("TAG", "encryptData: ${e.message}")
                return@withContext Resource.Error("Error encrypting data")
            }
        }
    }

}