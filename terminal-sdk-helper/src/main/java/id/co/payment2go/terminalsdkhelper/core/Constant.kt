package id.co.payment2go.terminalsdkhelper.core

import android.os.Build
import id.co.payment2go.terminalsdkhelper.common.DeviceType
import id.co.payment2go.terminalsdkhelper.common.UnknownDevice

object Constant {

    /**
     * Retrieves the device type based on the manufacturer.
     *
     * @return The corresponding [DeviceType] for the device.
     * @deprecated Use [DeviceTypeManager.getDeviceType] instead.
     * @author Samuel Mareno
     */
    @Deprecated(
        "This function is deprecated. Use DeviceTypeManager.getDeviceType instead.",
        ReplaceWith("DeviceTypeManager.getDeviceType()"),
        DeprecationLevel.WARNING
    )
    fun getDeviceType(): DeviceType {
        val ingenicoDevices = listOf("LANDI", "INGENICO", "APOS")
        val sunmiDevices = listOf("SUNMI")
        val type = Build.MANUFACTURER.uppercase()
        if (ingenicoDevices.contains(type)) return DeviceType.INGENICO
        if (sunmiDevices.contains(type)) return DeviceType.SUNMI
        throw UnknownDevice("$type device is not recognized")
    }

    const val NOTIFICATION_CHANNEL_ID = "service"
    const val NOTIFICATION_ID = 1

    const val baseCurrencyCode = "0360"
    const val baseCountryCode = "0360" //Indonesia

    const val POS_ENTRY_MODE_SWIPE = "22"
    const val POS_ENTRY_MODE_DIP = "51"
    const val POS_ENTRY_MODE_CONTACTLESS = "07"

    const val REVERSAL_TRANSACTION_ID_KEY = "REVERSAL_TRANSACTION_ID_KEY"
    const val REVERSAL_AMOUNT_KEY = "REVERSAL_AMOUNT_KEY"
    const val STAN_KEY = "INCREASE_STAN_KEY"
    const val LAST_RESET_DAY = "LAST_RESET_DAY"
    const val AGENT_CODE = "AGENT_CODE"
    const val MERCHANT_ID = "MMID"
    const val TERMINAL_ID = "MTID"
    const val BANK_MERCHANT_ID = "BMID"
    const val BANK_TERMINAL_ID = "BTID"
    const val KEY_1 = "KEY_1"
    const val KEY_2 = "KEY_2"
    const val IP_ADDRESS_KEY = "IP_ADDRESS_KEY"
    const val SESSION_KEY = "SESSION_KEY"
    const val ACCOUNT_NUM_KEY = "ACCOUNT_NUM_KEY"
    const val KODE_LOKET_KEY = "KODE_LOKET_KEY"
    const val KODE_MITRA_KEY = "KODE_MITRA_KEY"
    const val KODE_CABANG_KEY = "KODE_CABANG_KEY"
    const val ID_API_KEY = "ID_API_KEY"
    const val IP_SERVER_KEY = "IP_SERVER_KEY"
    const val REQ_ID_KEY = "REQ_ID_KEY"
    const val BROWSER_AGENT_KEY = "BROWSER_AGENT_KEY"
    const val LAST_LOGON_DAY: String = "LAST_LOGON_DAY"
    const val IS_KEY_INJECTED: String = "IS_KEY_INJECTED"
    const val HSM_FILTER_KEY: String = "HSM_FILTER_KEY"
}