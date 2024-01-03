package id.co.payment2go.terminalsdkhelper.topwise

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Typeface
import android.os.IBinder
import com.topwise.cloudpos.aidl.AidlDeviceService
import com.topwise.cloudpos.aidl.card.AidlCheckCard
import com.topwise.cloudpos.aidl.emv.level2.AidlEmvL2
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad
import com.topwise.cloudpos.aidl.printer.AidlPrinter
import com.topwise.cloudpos.aidl.printer.PrintTemplate
import com.topwise.cloudpos.aidl.system.AidlSystem
import com.topwise.toptool.api.ITool
import com.topwise.toptool.api.convert.IConvert
import com.topwise.toptool.api.packer.IPacker
import com.topwise.toptool.impl.TopTool
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.core.TermLog
import id.co.payment2go.terminalsdkhelper.core.util.BoundService
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class BindServiceTopWise(private val context: Context) : BindService {
    private val TAG = "BindServiceTopWise"
    private val DEVICE_SERVICE_PACKAGE_NAME = "com.android.topwise.topusdkservice"
    private val DEVICE_SERVICE_CLASS_NAME =
        "com.android.topwise.topusdkservice.service.DeviceService"
    private val ACTION_DEVICE_SERVICE = "topwise_cloudpos_device_service"
    private lateinit var deviceService: AidlDeviceService
    lateinit var deviceSystem: AidlSystem
    lateinit var pinpad: AidlPinpad
    lateinit var printerDev: AidlPrinter
    lateinit var cardManager: AidlCheckCard
    lateinit var emvManager: AidlEmvL2
    lateinit var topTool: ITool
    lateinit var iPacker: IPacker
    lateinit var iConvert: IConvert
    override suspend fun bindServiceSDK() {
        initSdk()
    }

    override fun disconnectSDK() {
        TODO("Not yet implemented")
    }

    private suspend fun initSdk() {
        val intent = Intent()
        intent.setAction(ACTION_DEVICE_SERVICE)
        intent.setClassName(
            DEVICE_SERVICE_PACKAGE_NAME,
            DEVICE_SERVICE_CLASS_NAME
        )
        context.bindServiceAndWait(intent, Context.BIND_AUTO_CREATE)
    }

    private suspend fun Context.bindServiceAndWait(intent: Intent, flags: Int) {
        suspendCoroutine { c ->
            val con = object : ServiceConnection {
                override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                    try {
                        deviceService = AidlDeviceService.Stub.asInterface(p1)
                        deviceSystem = AidlSystem.Stub.asInterface(deviceService.systemService)
                        pinpad = AidlPinpad.Stub.asInterface(deviceService.getPinPad(0))
                        printerDev = AidlPrinter.Stub.asInterface(deviceService.printer)
                        cardManager = AidlCheckCard.Stub.asInterface(deviceService.checkCard)
                        emvManager = AidlEmvL2.Stub.asInterface(deviceService.l2Emv)
                        topTool = TopTool.getInstance()
                        iPacker = topTool.packer
                        iConvert = topTool.convert
                        c.resume(BoundService(this@bindServiceAndWait, p0, p1, this))
                    } catch (e: Exception) {
                        TermLog.d(TAG, "onServiceConnected -> ${e.message}")
                    }
                }

                override fun onServiceDisconnected(p0: ComponentName?) {
                    TermLog.d(TAG, "onServiceDisconnected -> ")
                }
            }
            this.bindService(intent, con, flags)
        }
    }
}