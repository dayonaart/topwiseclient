package id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security.use_case

import android.os.RemoteException
import com.usdk.apiservice.aidl.pinpad.KeyType
import com.usdk.apiservice.aidl.pinpad.UPinpad
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId

class LoadMACEncryptionKey(
    private val pinpad: UPinpad?
) {
    operator fun invoke(macKey: ByteArray): Resource<Unit> {
        return try {
            pinpad?.open()
            pinpad?.loadEncKey(KeyType.DEK_KEY, KeyId.mainKey, KeyId.macKey, macKey, null)
            pinpad?.close()
            Resource.Success(Unit)
        } catch (e: RemoteException) {
            e.printStackTrace()
            Resource.Error("Inside DK Error: " + e.localizedMessage)
        }
    }
}