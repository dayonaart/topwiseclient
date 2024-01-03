package id.co.payment2go.terminalsdkhelper.ingenico.scanner

interface ScannerErrorDesc {
    fun getDescription(error: Int): String
}