package id.co.payment2go.terminalsdkhelper.szzt.system.device

import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.szzt.BindServiceSzzt

class DeviceManagerUtilitySzzt(bindService: BindServiceSzzt) : DeviceManagerUtility {

    private val system = bindService.mSystemManager

    override fun getSerialNumberDevice(): String {
        return system.deviceInfo?.sn ?: ""
    }

    override fun getImeiDevice(): String {
        TODO("Not yet implemented")
    }

    override fun setActiveButtonNavigation(active: Boolean) {
        println("test")
    }
}