package id.co.payment2go.terminalsdkhelper.ingenico.system.device

import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico

class DeviceManagerUtilityIngenico(
    val bindService: BindServiceIngenico
) : DeviceManagerUtility {

    private val deviceManager = bindService.deviceManager
    private val keyboard = bindService.keyboard

    override fun getSerialNumberDevice(): String {
        keyboard.setHomeKeyEnabled(false)
        keyboard.setNavigationBarEnabled(false)
        return deviceManager.deviceInfo.serialNo
    }

    override fun getImeiDevice(): String {
        return deviceManager.deviceInfo.imei
    }

    override fun setActiveButtonNavigation(active: Boolean) {
        keyboard.setHomeKeyEnabled(false)
        keyboard.setNavigationBarEnabled(false)
    }
}