package id.co.payment2go.terminalsdkhelper.verifone.emv.ktx

import android.os.Bundle

sealed class EMVStartResult {
    object OnRequestAmount: EMVStartResult()
    data class OnSelectApplication(val appList: MutableList<Bundle>?): EMVStartResult()
    data class OnConfirmCardInfo(val info: Bundle?): EMVStartResult()
    data class OnRequestInputPIN(val isOnlinePin: Boolean, val retryTimes: Int): EMVStartResult()
    data class OnConfirmCertInfo(val certType: String?, val certInfo: String?): EMVStartResult()
    data class OnRequestOnlineProcess(val aaResult: Bundle?) : EMVStartResult()
    data class OnTransactionResult(val result: Int, val bundle: Bundle?): EMVStartResult()
}