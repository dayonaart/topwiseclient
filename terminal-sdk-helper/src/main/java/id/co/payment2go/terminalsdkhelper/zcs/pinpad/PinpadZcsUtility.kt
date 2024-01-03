package id.co.payment2go.terminalsdkhelper.zcs.pinpad

import com.zcs.sdk.pin.PinAlgorithmMode
import com.zcs.sdk.pin.PinAlgorithmModeEnum
import com.zcs.sdk.pin.PinWorkKeyTypeEnum
import com.zcs.sdk.util.StringUtils
import id.co.payment2go.terminalsdkhelper.common.pinpad.OnPinPadResult
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.TermLog
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId
import id.co.payment2go.terminalsdkhelper.zcs.BindServiceZcs


class PinpadZcsUtility(private val bindServiceZcs: BindServiceZcs) : PinpadUtility {
    private val TAG = "PinpadZcsUtility"
    private val pinpad = bindServiceZcs.pinpad

    init {
        pinpad.pinPadSetAlgorithmMode(PinAlgorithmModeEnum.DES)
    }

    override fun showPinPad(
        disorder: Boolean,
        cardNumber: String,
        onPinpadResult: (OnPinPadResult) -> Unit
    ) {
        pinpad.inputOnlinePin(
            bindServiceZcs.ctx,
            6,
            6,
            60,
            true,
            cardNumber,
            KeyId.mainKey.toByte(),
            PinAlgorithmMode.ANSI_X_9_8,
            ZcsPinpadListener(onPinpadResult),
        )
    }


    override suspend fun injectMasterKey(masterKey: ByteArray): Resource<Unit> {
        val ret: Int = pinpad.pinPadUpMastKey(
            KeyId.mainKey,
            masterKey,
            masterKey.size.toByte()
        )
        return if (ret == 0) {
            Resource.Success(Unit)
        } else {
            Resource.Error(message = "Error Injecting Master Key $ret")
        }
    }

    override suspend fun injectPinKey(pinKey: ByteArray): Resource<Unit> {
        val ret: Int =
            pinpad.pinPadUpWorkKey(
                KeyId.mainKey,
                pinKey,
                pinKey.size.toByte(),
                null,
                0,
                null,
                0
            )
        return if (ret == 0) {
            Resource.Success(Unit)
        } else {
            Resource.Error(message = "Error Injecting Pin Key $ret")
        }
    }

    override suspend fun injectDataKey(dataKey: ByteArray): Resource<Unit> {
        val ret: Int =
            pinpad.pinPadUpWorkKey(
                KeyId.mainKey,
                null,
                0,
                null,
                0,
                dataKey,
                dataKey.size.toByte()
            )
        return if (ret == 0) {
            Resource.Success(Unit)
        } else {
            Resource.Error(message = "Error Injecting Data Key $ret")
        }
    }

    override suspend fun encryptData(message: String): Resource<String> {
        val padded16message = if (message.length % 16 == 0) {
            message
        } else {
            message.padEnd(message.length + (16 - message.length % 16), '0')
        }
        val hexed = StringUtils.convertStringToHex(padded16message)
        val output = ByteArray(hexed.length / 2)
        val ret: Int = pinpad.pinPadEncryptData(
            KeyId.mainKey,
            PinWorkKeyTypeEnum.TDKEY,
            StringUtils.convertHexToBytes(hexed),
            hexed.length / 2,
            output
        )
        return if (ret == 0) {
            TermLog.d(TAG, "encryptData -> ${StringUtils.convertBytesToHex(output).uppercase()}")
            Resource.Success(data = StringUtils.convertBytesToHex(output).uppercase())
        } else {
            Resource.Error(message = "Error while encrypting data")
        }
    }

    override suspend fun decryptData(hexedMessage: String): Resource<String> {
        val output = ByteArray(hexedMessage.length / 2)
        val ret = pinpad.pinPadDecryptData(
            KeyId.mainKey,
            PinWorkKeyTypeEnum.TDKEY,
            StringUtils.convertHexToBytes(hexedMessage),
            hexedMessage.length / 2,
            output
        )
        val formattedResult = Util.removeTextAfterLastCurlyBrace(String(output))
        return if (ret == 0) {
            TermLog.d(TAG, "decryptData -> ${String(output)}")
            Resource.Success(data = formattedResult)
        } else {
            Resource.Error(message = "Error while decrypting data")
        }
    }

}