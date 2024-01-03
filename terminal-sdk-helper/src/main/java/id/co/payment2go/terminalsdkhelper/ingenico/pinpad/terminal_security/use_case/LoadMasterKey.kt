package id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security.use_case

import android.content.Context
import android.os.RemoteException
import com.usdk.apiservice.aidl.device.UDeviceManager
import com.usdk.apiservice.aidl.pinpad.DeviceName
import com.usdk.apiservice.aidl.pinpad.EncKeyFmt
import com.usdk.apiservice.aidl.pinpad.KAPId
import com.usdk.apiservice.aidl.pinpad.KeyAlgorithm
import com.usdk.apiservice.aidl.pinpad.KeySystem
import com.usdk.apiservice.aidl.pinpad.KeyType
import com.usdk.apiservice.aidl.pinpad.UPinpad
import com.usdk.apiservice.limited.pinpad.PinpadLimited
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadMasterKey(
    private val context: Context,
    private val kapId: KAPId,
    private val pinpad: UPinpad,
    private val deviceManager: UDeviceManager?
) {

    suspend operator fun invoke(masterKey: ByteArray): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val pinpadLimited = PinpadLimited(
                    context,
                    kapId,
                    KeySystem.KS_MKSK,
                    if (deviceManager?.deviceInfo?.model == "AECR C10") DeviceName.COM_EPP else DeviceName.IPP
                )
                pinpad.open()
                pinpad.setKeyAlgorithm(KeyAlgorithm.KA_AES)
                pinpad.setEncKeyFormat(EncKeyFmt.ENC_KEY_FMT_NORMAL)
                pinpadLimited.format()
                pinpadLimited.switchToWorkMode()
                pinpadLimited.loadPlainTextKey(KeyType.MAIN_KEY, KeyId.mainKey, masterKey)
                pinpad.close()
                Resource.Success(Unit)
            } catch (e: RemoteException) {
                e.printStackTrace()
                Resource.Error("Can't inject master key!")
            }
        }

    }
}