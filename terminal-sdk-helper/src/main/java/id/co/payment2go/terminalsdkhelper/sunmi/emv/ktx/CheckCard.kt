package id.co.payment2go.terminalsdkhelper.sunmi.emv.ktx


import android.os.Bundle
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import id.co.payment2go.terminalsdkhelper.common.emv.SearchCardException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun ReadCardOptV2.checkCardAwait(): SearchCardResultSunmi {
    return suspendCoroutine { continuation ->
        this.checkCard(
            AidlConstants.CardType.IC.value or AidlConstants.CardType.MAGNETIC.value,
            object : CheckCardCallbackV2.Stub() {
                override fun findMagCard(bundle: Bundle) {
                    continuation.resume(SearchCardResultSunmi.FindMagCard(bundle))
                }

                override fun findICCard(atr: String) {
                    continuation.resume(SearchCardResultSunmi.FindICCard(atr))
                }

                override fun findRFCard(atr: String) {
                    continuation.resume(SearchCardResultSunmi.FindRFCard(atr))
                }

                override fun onError(errorCode: Int, message: String?) {
                    continuation.resumeWithException(SearchCardException("$errorCode: $message"))
                }

                override fun findICCardEx(p0: Bundle?) {
//                    TODO("Not yet implemented")
                }

                override fun findRFCardEx(p0: Bundle?) {
//                    TODO("Not yet implemented")
                }

                override fun onErrorEx(p0: Bundle?) {
//                    TODO("Not yet implemented")
                }
            }, 60
        )
    }
}