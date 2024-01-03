package id.co.payment2go.terminalsdkhelper.sunmi

import android.content.Context
import android.util.Log
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2
import com.sunmi.pay.hardware.aidlv2.print.PrinterOptV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.core.util.BoundService
import sunmi.paylib.SunmiPayKernel

class BindServiceSunmi(
    private val context: Context
) : BindService {
    private var boundService: BoundService? = null
    lateinit var basic: BasicOptV2
    lateinit var printer: PrinterOptV2
    lateinit var readCardOptV2: ReadCardOptV2
    lateinit var emvOptV2: EMVOptV2
    lateinit var sunmiPrinterService: SunmiPrinterService
    lateinit var securityOptV2: SecurityOptV2
    lateinit var pinpadOptV2: PinPadOptV2


    override suspend fun bindServiceSDK() {
        val payKernel = SunmiPayKernel.getInstance()
        payKernel.initPaySDKAwait(context)
        basic = payKernel.mBasicOptV2
        printer = payKernel.mPrinterOptV2
        readCardOptV2 = payKernel.mReadCardOptV2
        emvOptV2 = payKernel.mEMVOptV2
        securityOptV2 = payKernel.mSecurityOptV2
        pinpadOptV2 = payKernel.mPinPadOptV2
        Log.d("GSTPaymentService", "onConnectPaySDK: Sunmi SDK Connected")
        bindPrintService()
    }

    override fun disconnectSDK() {
        boundService?.unbind()
    }

    private fun bindPrintService() {
        try {
            InnerPrinterManager.getInstance().bindService(context, object : InnerPrinterCallback() {
                override fun onConnected(service: SunmiPrinterService) {
                    sunmiPrinterService = service
                    Log.d("GSTPaymentService", "onConnectPaySDK: Sunmi Printer SDK Connected")
                }

                override fun onDisconnected() {
                }

            })
        } catch (e: InnerPrinterException) {
            e.printStackTrace()
        }

    }
}