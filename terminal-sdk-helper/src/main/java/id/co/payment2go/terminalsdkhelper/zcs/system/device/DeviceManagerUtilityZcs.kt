package id.co.payment2go.terminalsdkhelper.zcs.system.device

import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.zcs.BindServiceZcs
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsUtility
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsUtility.setDisableNavigation

class DeviceManagerUtilityZcs(private val bindService: BindServiceZcs) : DeviceManagerUtility {
    private val TAG = "DeviceManagerUtilityZcs"
    private val deviceSerialNumber = bindService.getSerialNumberDevice()
    override fun getSerialNumberDevice(): String {
        return deviceSerialNumber
    }

    override fun getImeiDevice(): String {
        TODO("Not yet implemented")
    }

    override fun setActiveButtonNavigation(active: Boolean) {
        bindService.ctx.setDisableNavigation(active)
    }
}