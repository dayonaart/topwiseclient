package id.co.payment2go.terminalsdkhelper.ingenico.scanner

import com.usdk.apiservice.aidl.scanner.FrontError

class FrontScannerErrorIngenico : ScannerErrorDesc, FrontError() {
    override fun getDescription(error: Int): String {
        return when (error) {
            ERROR_INIT_FAIL -> "Initialize bottom decode library failed"
            ERROR_ALREADY_INIT -> "Already initialized"
            ERROR_INIT_ENGINE -> "Initialize scan module failed"
            ERROR_AUTH_LICENSE -> "License authentication failed"
            ERROR_NOT_FIND_DECODE_LIB -> "Can not find the correct underlying decoding library"
            ERROR_NOT_INIT -> "Not initialize"
            ERROR_START_SCANNER -> "Start scanning module failed"
            ERROR_NOT_SUPPORT -> "Device not support"
            else -> "Unknown error: $error"
        }
    }
}