package id.co.payment2go.terminalsdkhelper.sunmi.emv.ktx

import android.os.Bundle

sealed class SearchCardResultSunmi {
    data class FindMagCard(val bundle: Bundle) : SearchCardResultSunmi()
    data class FindICCard(val atr: String) : SearchCardResultSunmi()

    data class FindRFCard(val atr: String) : SearchCardResultSunmi()

}
