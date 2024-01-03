package id.co.payment2go.terminalsdkhelper.ingenico

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.util.Log
import com.usdk.apiservice.aidl.UDeviceService
import com.usdk.apiservice.aidl.constants.LogLevel
import com.usdk.apiservice.aidl.device.UDeviceManager
import com.usdk.apiservice.aidl.emv.UEMV
import com.usdk.apiservice.aidl.pinpad.DeviceName
import com.usdk.apiservice.aidl.pinpad.KAPId
import com.usdk.apiservice.aidl.pinpad.KeySystem
import com.usdk.apiservice.aidl.pinpad.UPinpad
import com.usdk.apiservice.aidl.printer.UPrinter
import com.usdk.apiservice.aidl.scanner.CameraId
import com.usdk.apiservice.aidl.scanner.UScanner
import com.usdk.apiservice.aidl.system.USystem
import com.usdk.apiservice.aidl.system.application.UApplication
import com.usdk.apiservice.aidl.system.keyboard.UKeyboard
import com.usdk.apiservice.limited.DeviceServiceLimited
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.core.util.BoundService
import id.co.payment2go.terminalsdkhelper.core.util.bindServiceAndWait

class BindServiceIngenico(
    private val context: Context
) : BindService {
    lateinit var deviceService: UDeviceService
        private set
    lateinit var emv: UEMV
        private set
    lateinit var printer: UPrinter
        private set
    lateinit var frontScanner: UScanner
        private set
    lateinit var backScanner: UScanner
        private set
    lateinit var deviceManager: UDeviceManager
        private set
    lateinit var system: USystem
        private set
    lateinit var application: UApplication
        private set
    lateinit var keyboard: UKeyboard
        private set
    lateinit var pinpad: UPinpad
        private set

    private var boundService: BoundService? = null


    override suspend fun bindServiceSDK() {
        val serviceIntent = Intent("com.usdk.apiservice")
        serviceIntent.setPackage("com.usdk.apiservice")
        boundService = context.bindServiceAndWait(serviceIntent, Context.BIND_AUTO_CREATE)
        deviceService = UDeviceService.Stub.asInterface(boundService?.service)
        deviceService.register(null, Binder())
        deviceService.setLogLevel(LogLevel.EMVLOG_REALTIME, LogLevel.USDKLOG_VERBOSE)
        printer = UPrinter.Stub.asInterface(deviceService.printer)
        emv = UEMV.Stub.asInterface(deviceService.emv)
        frontScanner = UScanner.Stub.asInterface(deviceService.getScanner(CameraId.FRONT))
        backScanner = UScanner.Stub.asInterface(deviceService.getScanner(CameraId.BACK))
        deviceManager = UDeviceManager.Stub.asInterface(deviceService.deviceManager)
        system = USystem.Stub.asInterface(deviceService.system)
        application = UApplication.Stub.asInterface(system.application)
        keyboard = UKeyboard.Stub.asInterface(system.keyboard)
        pinpad = UPinpad.Stub.asInterface(
            deviceService.getPinpad(
                KAPId(0, 0),
                KeySystem.KS_MKSK,
                DeviceName.IPP
            )
        )
        bindDeviceServiceLimited()
    }

    override fun disconnectSDK() {
        deviceService.unregister(null)
        boundService?.unbind()
    }

    private fun bindDeviceServiceLimited() {
        DeviceServiceLimited.bind(
            context,
            deviceService,
            object : DeviceServiceLimited.ServiceBindListener {
                override fun onSuccess() {
                    Log.d("GSTPaymentService", "Bind device Ingenico service limited success")
                }

                override fun onFail() {
                    Log.e("GSTPaymentService", "Bind device Ingenico service limited is failed")
                }
            })
    }
}