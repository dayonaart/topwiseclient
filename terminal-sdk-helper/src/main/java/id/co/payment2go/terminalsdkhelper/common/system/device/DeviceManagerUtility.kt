package id.co.payment2go.terminalsdkhelper.common.system.device

interface DeviceManagerUtility {
    fun getSerialNumberDevice(): String
    fun getImeiDevice(): String
    fun setActiveButtonNavigation(active: Boolean)
}