package id.co.payment2go.terminalsdkhelper.common.printer

interface PrinterErrorDesc {
    fun getDescription(error: Int): String
}