package id.co.payment2go.terminalsdkhelper.zcs.pinpad

import com.zcs.sdk.SdkResult
import com.zcs.sdk.pin.pinpad.PinPadManager
import id.co.payment2go.terminalsdkhelper.common.pinpad.OnPinPadResult
import id.co.payment2go.terminalsdkhelper.core.TermLog


class ZcsPinpadListener(val result: (OnPinPadResult) -> Unit) :
    PinPadManager.OnPinPadInputListener {
    private val TAG = "ZcsPinpadListener"

    override fun onError(p0: Int) {
        when (p0) {
            SdkResult.SDK_PAD_ERR_NOPIN -> {
                TermLog.d(TAG, "onError -> SdkResult.SDK_PAD_ERR_NOPIN")
                result(OnPinPadResult.OnError(p0))
            }

            SdkResult.SDK_PAD_ERR_TIMEOUT -> {
                TermLog.d(TAG, "onError -> SdkResult.SDK_PAD_ERR_TIMEOUT")
                result(OnPinPadResult.OnError(p0))
            }

            SdkResult.SDK_PAD_ERR_CANCEL -> {
                TermLog.d(TAG, "onError -> SdkResult.SDK_PAD_ERR_CANCEL")
                result(OnPinPadResult.OnCancel)
            }

            SdkResult.SDK_PAD_ERR_INVALID_INDEX -> {
                TermLog.d(TAG, "onError -> SDK_PAD_ERR_INVALID_INDEX")
                result(OnPinPadResult.OnError(p0))
            }

            SdkResult.SDK_PAD_ERR_NOTSET_KEY -> {
                TermLog.d(TAG, "onError -> SDK_PAD_ERR_NOTSET_KEY")
                result(OnPinPadResult.OnError(p0))
            }

            SdkResult.SDK_PAD_ERR_NEED_WAIT -> {
                TermLog.d(TAG, "onError -> SdkResult.SDK_PAD_ERR_NEED_WAIT")
                result(OnPinPadResult.OnError(p0))
            }

            SdkResult.SDK_PAD_ERR_DUPLI_KEY -> {
                TermLog.d(TAG, "onError -> SdkResult.SDK_PAD_ERR_DUPLI_KEY")
                result(OnPinPadResult.OnError(p0))
            }

            SdkResult.SDK_PAD_BASE_ERR -> {
                TermLog.d(TAG, "onError -> SdkResult.SDK_PAD_BASE_ERR")
                result(OnPinPadResult.OnError(p0))
            }

            SdkResult.SDK_PAD_ERR_EXCEPTION -> {
                TermLog.d(TAG, "onError -> SdkResult.SDK_PAD_ERR_EXCEPTION")
                result(OnPinPadResult.OnError(p0))
            }

            else -> {
                result(OnPinPadResult.OnError(p0))
            }
        }
    }

    override fun onSuccess(p0: ByteArray?) {
        result(OnPinPadResult.OnConfirm(p0, false))
    }
}