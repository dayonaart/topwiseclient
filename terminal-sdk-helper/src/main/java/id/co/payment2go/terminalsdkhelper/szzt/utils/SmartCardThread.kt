package id.co.payment2go.terminalsdkhelper.szzt.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.szzt.sdk.device.emv.EmvInterface

class SmartCardThread(
    looper: Looper,
    private val onMessage: (status: Int, retCode: Int) -> Unit
) :
    Handler(looper) {
    override fun handleMessage(msg: Message) {
        if (msg.what == EmvInterface.EMV_PROCESS_MSG) {
            val status: Int = msg.arg1
            val retCode: Int = msg.arg2
            onMessage(status, retCode)
        }
    }
}