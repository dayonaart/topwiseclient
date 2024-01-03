package id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.util

import android.content.SharedPreferences
import id.co.payment2go.terminalsdkhelper.core.Constant
import java.util.Calendar
import javax.inject.Inject

class StanManager @Inject constructor(
    private val sharedPrefs: SharedPreferences
) {

    fun getCurrentStan(): Long {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val lastResetDay = sharedPrefs.getInt(Constant.LAST_RESET_DAY, -1)
        if (currentDay != lastResetDay) {
            resetStan()
            saveLastResetDay(currentDay)
        }
        return sharedPrefs.getLong(Constant.STAN_KEY, 0L)
    }

    fun increaseStan() {
        saveStan(sharedPrefs.getLong(Constant.STAN_KEY, 0L) + 1)
    }

    fun setStanManual(value: Long) {
        saveStan(value)
    }

    private fun resetStan() {
        saveStan(1L)
    }

    private fun saveStan(value: Long) {
        sharedPrefs.edit().putLong(Constant.STAN_KEY, value).apply()
    }

    private fun saveLastResetDay(value: Int) {
        sharedPrefs.edit().putInt(Constant.LAST_RESET_DAY, value).apply()
    }
}