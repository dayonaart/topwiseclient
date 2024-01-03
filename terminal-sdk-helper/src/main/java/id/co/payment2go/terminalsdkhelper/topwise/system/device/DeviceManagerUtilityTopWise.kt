package id.co.payment2go.terminalsdkhelper.topwise.system.device

import android.util.Log
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.topwise.BindServiceTopWise

class DeviceManagerUtilityTopWise(private val bindService: BindServiceTopWise) :
    DeviceManagerUtility {
    private val TAG = "DeviceManagerUtilityTop"
    override fun getSerialNumberDevice(): String {
        Log.d(TAG, "getSerialNumberDevice WISE: ${bindService.deviceSystem.serialNo}")
        return bindService.deviceSystem.serialNo
    }

    override fun getImeiDevice(): String = bindService.deviceSystem.imei

    override fun setActiveButtonNavigation(active: Boolean) {
        bindService.deviceSystem.enableBackButton(active)
        bindService.deviceSystem.enableRecentAppButton(active)
        bindService.deviceSystem.enablePowerButton(active)
    }
}