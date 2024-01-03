package id.co.payment2go.terminalsdkhelper.verifone.emv.ktx

import android.os.Bundle
import com.vfi.smartpos.deviceservice.aidl.CheckCardListener
import com.vfi.smartpos.deviceservice.aidl.IEMV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Utility for search card
 * @param bundle = configuration emv verifone
 * @param timeout = timeout emv process
 */
suspend fun IEMV.searchCard(bundle: Bundle, timeout: Int): EmvVerifoneResult {
    return withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            this@searchCard.checkCard(bundle, timeout, object : CheckCardListener.Stub() {

                /** Magnetic card section.. */
                override fun onCardSwiped(track: Bundle?) {
                    if (track != null) {
                        continuation.resume(EmvVerifoneResult.CardSwiped(track))
                    } else {
                        continuation.resume(EmvVerifoneResult.Error(220798,"track is null"))
                    }
                }

                /** IC Card section.. */
                override fun onCardPowerUp() {
                    continuation.resume(EmvVerifoneResult.CardInsert)
                }

                /** Contactless Card / RF Card section..*/
                override fun onCardActivate() {
                    continuation.resume(EmvVerifoneResult.CardPass)
                }

                /** Condition Emv Timeout */
                override fun onTimeout() {
                    continuation.resume(EmvVerifoneResult.Timeout)
                }

                /** Error section about exception process emv */
                override fun onError(error: Int, message: String?) {
                    continuation.resume(EmvVerifoneResult.Error(error, message ?: ""))
                }

            })
        }

    }
}