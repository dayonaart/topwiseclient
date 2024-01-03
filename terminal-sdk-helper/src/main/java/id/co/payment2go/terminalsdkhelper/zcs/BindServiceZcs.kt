package id.co.payment2go.terminalsdkhelper.zcs

import android.content.Context
import com.zcs.sdk.DriverManager
import com.zcs.sdk.Printer
import com.zcs.sdk.SdkResult
import com.zcs.sdk.Sys
import com.zcs.sdk.card.CardReaderManager
import com.zcs.sdk.emv.EmvHandler
import com.zcs.sdk.emv.EmvTermParam
import com.zcs.sdk.pin.pinpad.PinPadManager
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.core.TermLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class BindServiceZcs(private val context: Context) : BindService {
    private val TAG = "BindServiceZcs"
    private val driverManager = DriverManager.getInstance()
    lateinit var cardReader: CardReaderManager
    private val sys: Sys = driverManager.baseSysDevice
    lateinit var pinpad: PinPadManager
    lateinit var emvHandler: EmvHandler
    lateinit var printer: Printer
    val ctx = context
    override suspend fun bindServiceSDK() {
        try {
            val init = sys.sdkInit()
            val success = init == SdkResult.SDK_OK
            if (success) {
                val powerOn = sys.sysPowerOn()
                TermLog.d(TAG, "SYSTEM POWER ON -> $powerOn")
                sys.showLog(false)
                sys.showDetailLog(false)
                withContext(Dispatchers.IO) {
                    Thread.sleep(1500)
                }
                pinpad = driverManager.padManager
                cardReader = driverManager.cardReadManager
                printer = driverManager.printer
                setTerminalParam()
            }
        } catch (e: Exception) {
            TermLog.d(TAG, "bindServiceSDK -> ${e.message}")
        }
    }

    override fun disconnectSDK() {
        sys.sysPowerOff()
    }

    fun getSerialNumberDevice(): String {
        val pid = arrayOfNulls<String>(1)
        val ret = sys.getPid(pid)
        if (ret == SdkResult.SDK_OK) {
            val sn = pid[0]?.takeLast(16)
            TermLog.d(TAG, "getSerialNumberDevice -> $sn")
            return "$sn"
        }
        TermLog.d(TAG, "ERROR getSerialNumberDevice -> $ret")
        return "000000000"
    }

    private fun setTerminalParam() {
        emvHandler = EmvHandler.getInstance()
        EmvTermParam.ifd = getSerialNumberDevice()
        EmvTermParam.emvParamFilePath = context.filesDir.path + "/emv/"
        EmvTermParam.termCapa = "E0F8C8"
        EmvTermParam.addTermCapa = "F000F0A001"
        EmvTermParam.terminalCountry = "0360"
        EmvTermParam.tranCurrCode = "0360"
        EmvTermParam.termType = 0x22
    }
}