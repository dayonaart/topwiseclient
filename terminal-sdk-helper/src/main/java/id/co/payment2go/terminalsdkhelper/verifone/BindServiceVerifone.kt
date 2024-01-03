package id.co.payment2go.terminalsdkhelper.verifone

import android.content.Context
import android.content.Intent
import android.util.Log
import com.vfi.smartpos.deviceservice.aidl.IBeeper
import com.vfi.smartpos.deviceservice.aidl.IDeviceService
import com.vfi.smartpos.deviceservice.aidl.IEMV
import com.vfi.smartpos.deviceservice.aidl.IPinpad
import com.vfi.smartpos.deviceservice.aidl.IPrinter
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.core.util.BoundService
import id.co.payment2go.terminalsdkhelper.core.util.bindServiceAndWait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class BindServiceVerifone(private val context: Context) : BindService {
    lateinit var idevice: IDeviceService
    lateinit var iPinpad: IPinpad
    lateinit var printer: IPrinter
    lateinit var emv: IEMV
    lateinit var iBeeper: IBeeper
    @Suppress("PrivatePropertyName") private val TAG = "VERIFONE SDK"
    private var boundService: BoundService? = null

    override suspend fun bindServiceSDK() {
        coroutineScope {
            async(Dispatchers.IO) {
                val intent = Intent()
                intent.action = "com.vfi.smartpos.device_service"
                intent.setPackage("com.vfi.smartpos.deviceservice")
                boundService = context.bindServiceAndWait(intent, Context.BIND_AUTO_CREATE)
                if (boundService?.name != null) {
                    idevice = IDeviceService.Stub.asInterface(boundService?.service)
                    printer = idevice.printer
                    iPinpad = idevice.getPinpad(1)
                    emv = idevice.emv
                    iBeeper = idevice.beeper
                } else {
                    Log.d(TAG, "can't bind VERIFONE device")
                }
            }.join()
        }
    }

    override fun disconnectSDK() {
        TODO("Not yet implemented")
    }
}