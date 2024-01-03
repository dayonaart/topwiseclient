package id.co.payment2go.terminalsdkhelper.common.pinpad

sealed class OnPinPadResult {
    data class OnInput(val p0: Int,val p1: Int): OnPinPadResult()
    data class OnConfirm(val data: ByteArray?, val isNonPin: Boolean): OnPinPadResult()
    object OnCancel: OnPinPadResult()
    data class OnError(val error: Int): OnPinPadResult()
}
