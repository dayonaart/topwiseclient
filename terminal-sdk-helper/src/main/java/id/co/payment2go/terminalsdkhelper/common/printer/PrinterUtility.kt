package id.co.payment2go.terminalsdkhelper.common.printer

interface PrinterUtility {

    fun isPrinterReady(): Boolean
    fun setAutoNextLine()
    fun addImage(image: ByteArray)
    fun addQR(qr: String)
    fun addHeader(value: String)
    fun addFooter(value: String)
    fun addSmallText(value: String, align: Int)
    fun addMediumText(value: String, align: Int)
    fun addLargeText(value: String, align: Int)
    fun addBlankLine(times: Int)
    fun buildPrintTemplate(jsonString: String): String
    fun startPrint()
}