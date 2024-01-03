package id.co.payment2go.terminalsdkhelper.verifone.emv.ktx

import android.os.Bundle
import com.vfi.smartpos.deviceservice.aidl.EMVHandler
import com.vfi.smartpos.deviceservice.aidl.IEMV
import com.vfi.smartpos.deviceservice.aidl.IPBOC
import com.vfi.smartpos.deviceservice.aidl.PBOCHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun IEMV.emvStart(processType: Int, intent: Bundle, onResult: (EMVStartResult) -> Unit) {
    return withContext(Dispatchers.IO) {
            this@emvStart.startEMV(processType, intent, object : EMVHandler.Stub() {
                @Suppress("OVERRIDE_DEPRECATION")
                override fun onRequestAmount() {
                    onResult(EMVStartResult.OnRequestAmount)
                }

                override fun onSelectApplication(appList: MutableList<Bundle>?) {
                    onResult(EMVStartResult.OnSelectApplication(appList))
                }

                override fun onConfirmCardInfo(info: Bundle?) {
                    onResult(EMVStartResult.OnConfirmCardInfo(info))
                }

                override fun onRequestInputPIN(isOnlinePin: Boolean, retryTimes: Int) {
                    onResult(EMVStartResult.OnRequestInputPIN(isOnlinePin, retryTimes))
                }

                override fun onConfirmCertInfo(certType: String?, certInfo: String?) {
                    onResult(EMVStartResult.OnConfirmCertInfo(certType, certInfo))
                }

                override fun onRequestOnlineProcess(aaResult: Bundle?) {
                    onResult(EMVStartResult.OnRequestOnlineProcess(aaResult))
                }

                override fun onTransactionResult(result: Int, data: Bundle?) {
                    onResult(EMVStartResult.OnTransactionResult(result, data))
                }
            })
    }
}

