package id.co.payment2go.terminalsdkhelper.sunmi.system.device

import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.sunmi.BindServiceSunmi

class DeviceManagerUtilitySunmi(
    val bindService: BindServiceSunmi
) : DeviceManagerUtility {

    private val basicOptV2 = bindService.basic

    override fun getSerialNumberDevice(): String {
        return basicOptV2.getSysParam("SN")
    }

    override fun getImeiDevice(): String {
        return "not yet implemented"
    }

    override fun setActiveButtonNavigation(active: Boolean) {
        if (active) {
            basicOptV2.setHideNavigationBarItems(0)
        } else {
            basicOptV2.setHideNavigationBarItems(0x00200000 or 0x00400000 or 0x01000000)
        }
    }
}