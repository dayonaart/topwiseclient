package id.co.payment2go.terminalsdkhelper.verifone.system.device

import android.util.Log
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.verifone.BindServiceVerifone

class DeviceManagerUtilityVerifone(bindService: BindServiceVerifone) :
    DeviceManagerUtility {
    private val system = bindService.idevice
    override fun getSerialNumberDevice(): String {
        Log.d("THE SN", "getSerialNumberDevice: ${system.deviceInfo.serialNo}")
        return system.deviceInfo.serialNo
    }

    override fun getImeiDevice(): String {
        return system.deviceInfo.imei
    }

    override fun setActiveButtonNavigation(active: Boolean) {
//        keyboard.setHomeKeyEnabled(false)
//        keyboard.setNavigationBarEnabled(false)
        println("")
    }
}