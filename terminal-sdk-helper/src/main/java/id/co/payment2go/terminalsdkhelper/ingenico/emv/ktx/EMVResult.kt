package id.co.payment2go.terminalsdkhelper.ingenico.emv.ktx

import android.os.Bundle

sealed class EMVResult {
    data class CardSwiped(val bundle: Bundle) : EMVResult()
    object CardInsert : EMVResult()
    data class CardPass(val type: Int) : EMVResult()
    object Timeout : EMVResult()
    data class Error(val errorCode: Int, val message: String) : EMVResult()
}
