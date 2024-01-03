package id.co.payment2go.terminalsdkhelper.core

import android.os.Build
import id.co.payment2go.terminalsdkhelper.common.DeviceType
import id.co.payment2go.terminalsdkhelper.common.UnknownDevice

class DeviceTypeManager {

    /**
     * Retrieves the type of the device based on its manufacturer.
     *
     * @return The type of the device as a [DeviceType] enum value.
     * @throws UnknownDevice if the device manufacturer is not recognized.
     * @author Samuel Mareno
     */
    fun getDeviceType(): DeviceType {
        val ingenicoDevices = listOf("LANDI", "INGENICO", "APOS", "AXIUM")
        val sunmiDevices = listOf("SUNMI")
        val zcsDevices = listOf("BLU")
        val verifoneDevices = listOf("VERIFONE")
        val szztDevices = listOf("KS8223")
        val topWiseDevices = listOf("TOPWISE")
        val testingDevices = listOf("SONY")
        val type = Build.MANUFACTURER.uppercase()
        val model = Build.MODEL.uppercase()
        if (ingenicoDevices.contains(type)) return DeviceType.INGENICO
        if (sunmiDevices.contains(type)) return DeviceType.SUNMI
        if (zcsDevices.contains(type)) return DeviceType.ZCS
        if (verifoneDevices.contains(type)) return DeviceType.VERIFONE
        if (szztDevices.contains(model)) return DeviceType.SZZT
        if (topWiseDevices.contains(type)) return DeviceType.TOPWISE
        if (testingDevices.contains(type)) return DeviceType.TESTING
        throw UnknownDevice("$type device is not recognized")
    }
}