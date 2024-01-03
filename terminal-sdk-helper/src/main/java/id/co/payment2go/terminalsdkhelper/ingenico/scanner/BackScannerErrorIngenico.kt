package id.co.payment2go.terminalsdkhelper.ingenico.scanner

import com.usdk.apiservice.aidl.scanner.BackError

class BackScannerErrorIngenico : ScannerErrorDesc, BackError() {
    override fun getDescription(error: Int): String {
        return when (error) {
            ERROR_INIT_FAIL -> "Initialize bottom decode library failed"
            ERROR_ALREADY_INIT -> "Already initialized"
            ERROR_AUTH_LICENSE -> "License authentication failed"
            ERROR_OPEN_CAMERA -> "Open camera failed"
            ERROR_NOT_FIND_DECODE_LIB -> "Can not find the correct underlying decoding library"
            REQEUST_HAND_INPUT -> "Request to use hand transmission bar code"
            else -> "Unknown error : $error"
        }
    }
}