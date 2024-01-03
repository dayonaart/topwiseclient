package id.co.payment2go.terminalsdkhelper.ingenico.emv.ktx

import android.os.Bundle
import com.usdk.apiservice.aidl.emv.SearchCardListener
import com.usdk.apiservice.aidl.emv.UEMV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun UEMV.searchCardAndAwait(param: Bundle, timeout: Int): EMVResult {
    return withContext(Dispatchers.IO) {
        suspendCoroutine {
            this@searchCardAndAwait.searchCard(param, timeout, object : SearchCardListener.Stub() {
                override fun onCardSwiped(bundle: Bundle) {
                    it.resume(EMVResult.CardSwiped(bundle))
                }

                override fun onCardInsert() {
                    it.resume(EMVResult.CardInsert)
                }

                override fun onCardPass(type: Int) {
                    it.resume(EMVResult.CardPass(type))
                }

                override fun onTimeout() {
                    it.resume(EMVResult.Timeout)
                }

                override fun onError(error: Int, message: String) {
                    it.resume(EMVResult.Error(error, message))
                }
            })
        }
    }
}