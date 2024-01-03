package id.co.payment2go.terminalsdkhelper.verifone.emv.ktx

import android.os.Bundle

sealed class EmvVerifoneResult {

    data class CardSwiped(val bundle: Bundle) : EmvVerifoneResult()
    object CardInsert : EmvVerifoneResult()
    object CardPass : EmvVerifoneResult()
    object Timeout : EmvVerifoneResult()
    data class Error(val errCode: Int, val errMsg: String) : EmvVerifoneResult()

}
