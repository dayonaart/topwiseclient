package id.co.payment2go.terminalsdkhelper.testing.system.device

import android.util.Log
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.testing.BindServiceTesting

class DeviceManagerUtilityTesting(
    val bindService: BindServiceTesting
) : DeviceManagerUtility {
    override fun getSerialNumberDevice(): String {
        return "74328749284"
    }

    override fun getImeiDevice(): String {
        TODO("Not yet implemented")
    }

    override fun setActiveButtonNavigation(active: Boolean) {
        Log.d("TAG", "setActiveButtonNavigation:SetNavbutton")
    }
}