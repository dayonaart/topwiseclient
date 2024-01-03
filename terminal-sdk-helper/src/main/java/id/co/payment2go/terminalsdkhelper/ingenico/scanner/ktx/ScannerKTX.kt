package id.co.payment2go.terminalsdkhelper.ingenico.scanner.ktx

import android.os.Bundle
import com.usdk.apiservice.aidl.scanner.OnScanListener
import com.usdk.apiservice.aidl.scanner.UScanner
import id.co.payment2go.terminalsdkhelper.ingenico.scanner.ScannerErrorDesc
import id.co.payment2go.terminalsdkhelper.ingenico.scanner.ScannerException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun UScanner.startScanAwait(param: Bundle, scannerErrorDesc: ScannerErrorDesc): String {
    return suspendCoroutine {
        this.startScan(param, object : OnScanListener.Stub() {
            override fun onSuccess(barcode: String) {
                it.resume(barcode)
            }

            override fun onError(error: Int) {
                it.resumeWithException(ScannerException(scannerErrorDesc.getDescription(error)))
            }

            override fun onTimeout() {
                stopScan()
                it.resumeWithException(ScannerException("Scanner timeout"))
            }

            override fun onCancel() {
                stopScan()
                it.resumeWithException(ScannerException("Scanner canceled"))
            }
        })
    }
}