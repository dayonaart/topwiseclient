package id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security.use_case

import android.os.RemoteException
import com.usdk.apiservice.aidl.pinpad.KeyType
import com.usdk.apiservice.aidl.pinpad.UPinpad
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId

class LoadSessionKey(
    private val pinpad: UPinpad?
) {

    operator fun invoke(pinKey: ByteArray): Resource<Unit> {
        return try {
            pinpad?.open()
            pinpad?.loadEncKey(KeyType.PIN_KEY, KeyId.mainKey, KeyId.pinKey, pinKey, null)
            pinpad?.close()
            Resource.Success(Unit)
        } catch (e: RemoteException) {
            e.printStackTrace()
            Resource.Error("Inside PK Error : " + e.localizedMessage)
        }
    }
}