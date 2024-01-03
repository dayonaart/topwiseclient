package id.co.payment2go.terminalsdkhelper.ingenico.scanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import com.usdk.apiservice.aidl.scanner.CameraId
import com.usdk.apiservice.aidl.scanner.ScannerData
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico
import id.co.payment2go.terminalsdkhelper.ingenico.scanner.ktx.startScanAwait

class ScannerIngenicoUtility(
    bindService: BindServiceIngenico,
    private val context: Context
) {
    var cameraType = 0
    private val frontScanner = bindService.frontScanner
    private val backScanner = bindService.backScanner

    private val frontScannerError = FrontScannerErrorIngenico()
    private val backScannerError = BackScannerErrorIngenico()

    suspend fun startFrontScan(): String {
        return try {
            val param = Bundle()
            param.putInt(ScannerData.TIMEOUT, 15)
            param.putString(ScannerData.TITLE, "QR Scanner")
            param.putBoolean(ScannerData.IS_SHOW_HAND_INPUT_BUTTON, false)
            param.putBoolean(ScannerData.ENABLE_FIX_FOCUS, true)

            cameraType = CameraId.FRONT
            val result = frontScanner.startScanAwait(param, frontScannerError)

            context.sendBroadcast(
                Intent("com.example.paymentservice.RESPONSE_BROADCAST").putExtra(
                    "QR_RESPONSE",
                    result
                )
            )

            result
        } catch (e: ScannerException) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun startBackScan(): String {
        return try {
            val param = Bundle()
            param.putInt(ScannerData.TIMEOUT, 15)
            param.putString(ScannerData.TITLE, "QR Scanner")
            param.putBoolean(ScannerData.IS_SHOW_HAND_INPUT_BUTTON, false)
            param.putBoolean(ScannerData.ENABLE_FIX_FOCUS, true)

            cameraType = CameraId.BACK
            val result = backScanner.startScanAwait(param, backScannerError)

            context.sendBroadcast(
                Intent("com.example.paymentservice.RESPONSE_BROADCAST").putExtra(
                    "QR_RESPONSE",
                    result
                )
            )

            result
        } catch (e: ScannerException) {
            e.printStackTrace()
            ""
        }
    }

    fun stopScan() {
        try {
            if (cameraType == CameraId.FRONT) {
                frontScanner.stopScan()
            }
            if (cameraType == CameraId.BACK) {
                backScanner.stopScan()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
}