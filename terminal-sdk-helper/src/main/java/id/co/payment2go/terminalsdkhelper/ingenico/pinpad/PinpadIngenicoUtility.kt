package id.co.payment2go.terminalsdkhelper.ingenico.pinpad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import com.usdk.apiservice.aidl.pinpad.DESMode
import com.usdk.apiservice.aidl.pinpad.DeviceName
import com.usdk.apiservice.aidl.pinpad.EncKeyFmt
import com.usdk.apiservice.aidl.pinpad.KAPId
import com.usdk.apiservice.aidl.pinpad.KeyAlgorithm
import com.usdk.apiservice.aidl.pinpad.KeySystem
import com.usdk.apiservice.aidl.pinpad.KeyType
import com.usdk.apiservice.aidl.pinpad.OnPinEntryListener
import com.usdk.apiservice.aidl.pinpad.PinpadData
import com.usdk.apiservice.limited.pinpad.PinpadLimited
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.pinpad.OnPinPadResult
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PinpadIngenicoUtility(
    private val context: Context,
    bindService: BindServiceIngenico
) : PinpadUtility {

    private val TAG = "PinpadIngenicoUtility"
    private val pinpad = bindService.pinpad
    private val deviceManager = bindService.deviceManager

    override fun showPinPad(
        disorder: Boolean,
        cardNumber: String,
        onPinpadResult: (OnPinPadResult) -> Unit
    ) {
        val intent = Intent("com.landicorp.pinpad.pinentry.server.SET_SKIN")
        intent.putExtra("disorder", disorder)
        context.sendBroadcast(intent)
        val param = Bundle()
        param.putInt(PinpadData.TIMEOUT, 60)
        param.putInt(PinpadData.BETWEEN_PINKEY_TIMEOUT, 60)
        param.putByteArray("panBlock", BytesUtil.hexString2ByteArray(cardNumber))
        param.putByteArray(PinpadData.PIN_LIMIT, byteArrayOf(0, 6))
        pinpad.open()
        pinpad.startPinEntry(KeyId.pinKey, param, object : OnPinEntryListener.Stub() {
            override fun onInput(p0: Int, p1: Int) {
                onPinpadResult(OnPinPadResult.OnInput(p0, p1))
            }

            override fun onConfirm(data: ByteArray?, isNonPin: Boolean) {
                Log.d(
                    TAG,
                    "onConfirm: ${
                        BytesUtil.byteArray2HexString(
                            data
                        )
                    }"
                )
                onPinpadResult(OnPinPadResult.OnConfirm(data, isNonPin))
                pinpad.close()
            }

            override fun onCancel() {
                onPinpadResult(OnPinPadResult.OnCancel)
                pinpad.close()
            }

            override fun onError(error: Int) {
                Log.d(TAG, "onError: $error")
                onPinpadResult(OnPinPadResult.OnError(error))
                pinpad.close()
            }
        })
    }

    override suspend fun injectMasterKey(masterKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val kapId = KAPId(0, 0)
                val pinpadLimited = PinpadLimited(
                    context,
                    kapId,
                    KeySystem.KS_MKSK,
                    if (deviceManager.deviceInfo?.model == "AECR C10") DeviceName.COM_EPP else DeviceName.IPP
                )
                pinpad.open()
                pinpadLimited.format()
                pinpad.setKeyAlgorithm(KeyAlgorithm.KA_TDEA)
                pinpad.setEncKeyFormat(EncKeyFmt.ENC_KEY_FMT_NORMAL)
                pinpadLimited.loadPlainTextKey(KeyType.MAIN_KEY, KeyId.mainKey, masterKey)
                pinpadLimited.switchToWorkMode()
                pinpad.close()
                Resource.Success(Unit)
            } catch (e: RemoteException) {
                e.printStackTrace()
                Resource.Error("Can't inject master key!")
            }
        }
    }

    override suspend fun injectPinKey(pinKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                pinpad.open()
                pinpad.loadEncKey(KeyType.PIN_KEY, KeyId.mainKey, KeyId.pinKey, pinKey, null)
                pinpad.close()
                Resource.Success(Unit)
            } catch (e: RemoteException) {
                e.printStackTrace()
                Resource.Error("Inside PK Error : " + e.localizedMessage)
            }
        }
    }

    override suspend fun injectDataKey(dataKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                pinpad.open()
                pinpad.loadEncKey(KeyType.DEK_KEY, KeyId.mainKey, KeyId.dekKey, dataKey, null)
                pinpad.close()
                Resource.Success(Unit)
            } catch (e: RemoteException) {
                e.printStackTrace()
                Resource.Error("Inside PK Error : " + e.localizedMessage)
            }
        }
    }

    override suspend fun encryptData(message: String): Resource<String> {
        val hexString = BytesUtil.stringToHex(message)
        Log.d("EncryptData", "hexed data: $hexString")
        return try {
            pinpad.close()
            pinpad.open()
            val des = DESMode()
            val result = pinpad.calculateDes(
                KeyId.dekKey,
                des,
                null,
                BytesUtil.hexString2ByteArray(hexString)
            )
            Log.d("EncryptData", "result: $result")
            pinpad.close()
            Resource.Success(BytesUtil.byteArray2HexString(result))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun decryptData(hexedMessage: String): Resource<String> {
        return try {
            pinpad.open()
            val des = DESMode(DESMode.DM_DEC, DESMode.DM_OM_TECB)
            val result = pinpad.calculateDes(
                KeyId.dekKey,
                des,
                null,
                BytesUtil.hexString2ByteArray(hexedMessage)
            )
            pinpad.close()
            Log.d(
                "DecryptData",
                "result: ${BytesUtil.byteArray2HexString(result)}"
            )
            val formattedResult = Util.removeTextAfterLastCurlyBrace(String(result))
            Resource.Success(formattedResult)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message.toString())
        }
    }
}
