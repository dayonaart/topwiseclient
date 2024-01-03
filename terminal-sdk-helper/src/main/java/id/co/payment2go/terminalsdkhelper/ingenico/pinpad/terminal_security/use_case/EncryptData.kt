package id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security.use_case

import android.os.RemoteException
import android.util.Log
import com.usdk.apiservice.aidl.pinpad.DESMode
import com.usdk.apiservice.aidl.pinpad.UPinpad
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId

class EncryptData(
    private val pinpad: UPinpad
) {
    operator fun invoke(data: String): Resource<String> {
        val hexString = BytesUtil.stringToHex(data)
        Log.d("EncryptData", "data: $data")
        return try {
            pinpad.close()
            pinpad.open()
            val des = DESMode(DESMode.DM_ENC, DESMode.DM_OM_TECB)
            val result = pinpad.calculateDes(
                KeyId.dekKey,
                des,
                null,
                BytesUtil.hexString2ByteArray(hexString)
            )
            Log.d("EncryptData", "result: $result")
            pinpad.close()
            Resource.Success(BytesUtil.byteArray2HexString(result))
        } catch (e: RemoteException) {
            e.printStackTrace()
            Resource.Error(e.message.toString())
        }
    }
}