package id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.util

import android.content.SharedPreferences
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.Constant
import java.util.Calendar
import javax.inject.Inject

class SessionKeyManager @Inject constructor(
    private val sharedPrefs: SharedPreferences,
    private val pinpadUtility: PinpadUtility
) {

    fun getCurrentStan(): Long {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val lastResetDay = sharedPrefs.getInt(Constant.LAST_RESET_DAY, -1)
        if (currentDay != lastResetDay) {
            saveLastResetDay(currentDay)
        }
        return sharedPrefs.getLong(Constant.STAN_KEY, 0L)
    }


    suspend fun injectSessionKey(pinKey: String, dataKey: String) {
        pinpadUtility.injectPinKey(BytesUtil.hexString2ByteArray(pinKey))
        pinpadUtility.injectDataKey(BytesUtil.hexString2ByteArray(dataKey))
    }

    private fun saveLastResetDay(value: Int) {
        sharedPrefs.edit().putInt(Constant.LAST_RESET_DAY, value).apply()
    }
}