package id.co.payment2go.terminalsdkhelper.sunmi.pinpad

import android.util.Log
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.pinpad.OnPinPadResult
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId
import id.co.payment2go.terminalsdkhelper.sunmi.BindServiceSunmi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

class PinpadSunmiUtility(
    bindService: BindServiceSunmi
) : PinpadUtility {

    private val TAG = "PinpadSunmiUtility"
    private val security = bindService.securityOptV2
    private val pinpad = bindService.pinpadOptV2

    private val pinpadConfig = PinPadConfigV2()

    override fun showPinPad(
        disorder: Boolean,
        cardNumber: String,
        onPinpadResult: (OnPinPadResult) -> Unit
    ) {

        val panBytes =
            cardNumber.substring(cardNumber.length - 13, cardNumber.length - 1).toByteArray(
                StandardCharsets.US_ASCII
            )

        pinpadConfig.pinPadType = 0
        pinpadConfig.pinType = 0
        pinpadConfig.isOrderNumKey = !disorder
        pinpadConfig.pinblockFormat = AidlConstants.PinBlockFormat.SEC_PIN_BLK_ISO_FMT0
        pinpadConfig.algorithmType = 0
        pinpadConfig.keySystem = 0
        pinpadConfig.timeout = 60000
        pinpadConfig.pinKeyIndex = KeyId.pinKey
        pinpadConfig.minInput = 0
        pinpadConfig.maxInput = 6
        pinpadConfig.pan = panBytes

        pinpad.initPinPad(pinpadConfig, object : PinPadListenerV2.Stub() {
            override fun onPinLength(p0: Int) {
                Log.d(TAG, "onPinLength: $p0")
            }

            override fun onConfirm(pinType: Int, byteArray: ByteArray?) {
                Log.d(TAG, "onConfirm: ${BytesUtil.byteArray2HexString(byteArray)}")
                onPinpadResult(OnPinPadResult.OnConfirm(byteArray, false))
            }

            override fun onCancel() {
                onPinpadResult(OnPinPadResult.OnCancel)
            }

            override fun onError(error: Int) {
                Log.d(TAG, "onError: $error")
                onPinpadResult(OnPinPadResult.OnError(error))
            }

        })
    }

    override suspend fun injectMasterKey(masterKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            val result = security.savePlaintextKey(
                AidlConstants.Security.KEY_TYPE_TMK,
                masterKey,
                null,
                AidlConstants.Security.KEY_ALG_TYPE_3DES,
                KeyId.mainKey
            )
            if (result == 0) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error injecting master key: $result")
            }
        }
    }

    override suspend fun injectPinKey(pinKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            val result = security.saveCiphertextKey(
                AidlConstants.Security.KEY_TYPE_PIK,
                pinKey,
                null,
                AidlConstants.Security.KEY_ALG_TYPE_3DES,
                KeyId.mainKey,
                KeyId.pinKey
            )
            if (result == 0) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error injecting master key: $result")
            }
        }
    }

    override suspend fun injectDataKey(dataKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            val result = security.saveCiphertextKey(
                AidlConstants.Security.KEY_TYPE_TDK,
                dataKey,
                null,
                AidlConstants.Security.KEY_ALG_TYPE_3DES,
                KeyId.mainKey,
                KeyId.tdkKey
            )
            if (result == 0) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error injecting master key: $result")
            }
        }
    }

    override suspend fun encryptData(message: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            val padded16message = if (message.length % 16 == 0) {
                message
            } else {
                message.padEnd(message.length + (16 - message.length % 16), '0')
            }
            val byteOut = ByteArray(padded16message.length)
            val hexed = BytesUtil.stringToHex(padded16message)
            val result = security.dataEncrypt(
                KeyId.tdkKey,
                BytesUtil.hexString2ByteArray(hexed),
                AidlConstants.Security.DATA_MODE_ECB,
                null,
                byteOut
            )
            Log.d(TAG, "byte size: ${byteOut.size}")
            Log.d(TAG, "output: ${BytesUtil.byteArray2HexString(byteOut)}")
            if (result == 0) {
                Resource.Success(BytesUtil.byteArray2HexString(byteOut))
            } else {
                Resource.Error("Error injecting master key: $result")
            }
        }
    }

    override suspend fun decryptData(hexedMessage: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            val byteOut = ByteArray(hexedMessage.length)
            val result = security.dataDecrypt(
                KeyId.tdkKey,
                BytesUtil.hexString2ByteArray(hexedMessage),
                AidlConstants.Security.DATA_MODE_ECB,
                null,
                byteOut
            )
            Log.d(TAG, "byte size: ${byteOut.size}")
            Log.d(TAG, "output: ${BytesUtil.byteArray2HexString(byteOut)}")
            val formattedResult = Util.removeTextAfterLastCurlyBrace(String(byteOut))
            if (result == 0) {
                Resource.Success(formattedResult)
            } else {
                Resource.Error("Error injecting master key: $result")
            }
        }
    }
}
