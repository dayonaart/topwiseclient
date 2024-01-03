package id.co.payment2go.terminalsdkhelper.testing.printer

import android.content.Context
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterUtility
import id.co.payment2go.terminalsdkhelper.testing.BindServiceTesting

class PrinterUtilityTesting(
    private val context: Context,
    bindService: BindServiceTesting
) : PrinterUtility {
    override fun isPrinterReady(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setAutoNextLine() {
        TODO("Not yet implemented")
    }

    override fun addImage(image: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun addQR(qr: String) {
        TODO("Not yet implemented")
    }

    override fun addHeader(value: String) {
        TODO("Not yet implemented")
    }

    override fun addFooter(value: String) {
        TODO("Not yet implemented")
    }

    override fun addSmallText(value: String, align: Int) {
        TODO("Not yet implemented")
    }

    override fun addMediumText(value: String, align: Int) {
        TODO("Not yet implemented")
    }

    override fun addLargeText(value: String, align: Int) {
        TODO("Not yet implemented")
    }

    override fun addBlankLine(times: Int) {
        TODO("Not yet implemented")
    }

    override fun buildPrintTemplate(jsonString: String): String {
        TODO("Not yet implemented")
    }

    override fun startPrint() {
        TODO("Not yet implemented")
    }
}