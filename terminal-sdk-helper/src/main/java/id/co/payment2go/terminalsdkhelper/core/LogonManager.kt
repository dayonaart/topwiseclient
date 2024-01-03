package id.co.payment2go.terminalsdkhelper.core

import android.content.SharedPreferences
import android.util.Log
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.BniDebitService
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.perform_logon.LogonRequestDto

class LogonManager(
    private val pinpadUtility: PinpadUtility,
    private val bniDebitService: BniDebitService,
    private val sharedPrefs: SharedPreferences,
    private val deviceManagerUtility: DeviceManagerUtility
) {

    private val TAG = "LogonManager"
    suspend fun performLogon(): Resource<Unit> {
        // perform Logon Request
        val injectMasterKeyResult = pinpadUtility
            .injectMasterKey(BytesUtil.hexString2ByteArray("484455B474A6C6115FF62236D8A09C74"))
        if (injectMasterKeyResult is Resource.Error) {
            Log.d(
                TAG,
                "onCreate: Inject master key failed: ${injectMasterKeyResult.message}"
            )
            return Resource.Error(injectMasterKeyResult.message ?: "Unknown error occurred")
        }
        Log.d(TAG, "onCreate: Inject master key success")
        // request logon for session key for the first time
        val logonResult = bniDebitService.postLogon(
            LogonRequestDto(
                mMID = sharedPrefs.getString(Constant.MERCHANT_ID, "") ?: "",
                mTID = sharedPrefs.getString(Constant.TERMINAL_ID, "") ?: "",
                serialNo = deviceManagerUtility.getSerialNumberDevice()
            )
        )
        if (logonResult.secData != null) {
            var dataKey = logonResult.secData.dataKey
            // dataKey hapus x di depan
            dataKey = dataKey.substring(1, dataKey.length)
            val injectDataKey =
                pinpadUtility.injectDataKey(BytesUtil.hexString2ByteArray(dataKey))
            if (injectDataKey is Resource.Success) {
                Log.d(TAG, "onCreate: Inject data key success")
            } else {
                Log.d(TAG, "onCreate: Inject data key failed: ${injectDataKey.message}")
                return Resource.Error(injectDataKey.message ?: "Unknown error occurred")
            }

            var pinKey = logonResult.secData.pinKey
            // dataKey hapus x di depan
            pinKey = pinKey.substring(1, pinKey.length)
            val injectPinKey =
                pinpadUtility.injectPinKey(BytesUtil.hexString2ByteArray(pinKey))
            if (injectPinKey is Resource.Success) {
                Log.d(TAG, "onCreate: Inject pin key success")
            } else {
                Log.d(TAG, "onCreate: Inject pin key failed: ${injectDataKey.message}")
                return Resource.Error(injectPinKey.message ?: "Unknown error occurred")
            }
        } else {
            Log.d("GSTPaymentService", "Gagal mendapatkan session key")
            return Resource.Error("Gagal mendapatkan session key")
        }
        sharedPrefs.edit().putBoolean(Constant.IS_KEY_INJECTED, true).apply()
        return Resource.Success(Unit)
    }
}