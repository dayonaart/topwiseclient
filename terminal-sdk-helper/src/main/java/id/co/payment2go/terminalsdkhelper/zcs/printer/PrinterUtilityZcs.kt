package id.co.payment2go.terminalsdkhelper.zcs.printer

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import android.text.Layout
import com.google.gson.JsonParser
import com.zcs.sdk.SdkResult
import com.zcs.sdk.print.PrnStrFormat
import com.zcs.sdk.print.PrnTextFont
import com.zcs.sdk.print.PrnTextStyle
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterUtility
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.zcs.BindServiceZcs
import java.io.ByteArrayInputStream

class PrinterUtilityZcs(
    private val context: Context,
    bindService: BindServiceZcs
) : PrinterUtility {
    private val printer = bindService.printer
    private var printerStatus = bindService.printer.printerStatus
    private val format: PrnStrFormat = PrnStrFormat()

    override fun isPrinterReady(): Boolean {
        return printerStatus == 0
    }

    override fun setAutoNextLine() {
    }

    override fun addImage(image: ByteArray) {
        val imageInputStream = ByteArrayInputStream(image)
        val bitmap = BitmapFactory.decodeStream(imageInputStream)
        printer.setPrintAppendBitmap(bitmap, Layout.Alignment.ALIGN_CENTER)
    }

    override fun addQR(qr: String) {
        printer.setPrintAppendQRCode(
            qr,
            200,
            200,
            Layout.Alignment.ALIGN_CENTER
        )
    }

    override fun addHeader(value: String) {
        format.textSize = 30
        format.font = PrnTextFont.MONOSPACE
        format.ali = Layout.Alignment.ALIGN_CENTER
        format.style = PrnTextStyle.BOLD
        printer.setPrintAppendString(value, format)
    }

    override fun addFooter(value: String) {
        format.textSize = 30
        format.font = PrnTextFont.MONOSPACE
        format.ali = Layout.Alignment.ALIGN_CENTER
        format.style = PrnTextStyle.BOLD
        printer.setPrintAppendString(value, format)
    }

    override fun addSmallText(value: String, align: Int) {
        format.textSize = 13
        format.font = PrnTextFont.MONOSPACE
        format.ali = align.printerAlignment()
        printer.setPrintAppendString(value, format)
    }

    override fun addMediumText(value: String, align: Int) {
        format.textSize = 37  / 2
        format.font = PrnTextFont.MONOSPACE
        format.ali = align.printerAlignment()
        format.letterSpacing = 0.1f
        printer.setPrintAppendString(value, format)
    }

    override fun addLargeText(value: String, align: Int) {
        format.textSize = 30
        format.font = PrnTextFont.MONOSPACE
        format.ali = align.printerAlignment()
        format.style = PrnTextStyle.BOLD
        printer.setPrintAppendString(value, format)
    }

    override fun addBlankLine(times: Int) {
        val blank = "\n".repeat(times)
        format.textSize = 20
        format.font = PrnTextFont.MONOSPACE
        format.ali = 0.printerAlignment()
        printer.setPrintAppendString(blank, format)
    }

    override fun buildPrintTemplate(jsonString: String): String {
        format.font = PrnTextFont.CUSTOM
        format.path = "${Environment.getExternalStorageDirectory()}" + "/fonts/montserrat_black.ttf"
        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        val isReprint = jsonObject["isCetakUlang"].asBoolean
        val footerMessageNull = jsonObject["footerMessage"].isJsonNull
        if (!isPrinterReady()) return printerStatusMessage()
        val qrCodeNull = jsonObject["qrCode"].isJsonNull
        setAutoNextLine()
        // untuk header
        Util.readAssetsFile(context = context, fileName = "bni_keagenan.png")?.let {
            addImage(it)
        }
        addBlankLine(1)
        addHeader("Transaksi Berhasil")

        if (isReprint) {
            addMediumText("Cetak Ulang", 0)
            addBlankLine(1)
        }
        addHeader(jsonObject["namaFitur"].asString)
        addBlankLine(1)

        if (!qrCodeNull) {
            addQR(jsonObject["qrCode"].asString)
            addBlankLine(1)
            addMediumText("Kode Booking", 0)
            addLargeText(jsonObject["qrCode"].asString, 0)
            addBlankLine(1)
        }
        // untuk body
        jsonObject["dataList"].asJsonArray.forEach { element ->
            val lineString = element.asJsonObject.get("lineString").asString
            val isLarge = element.asJsonObject.get("isLarge").asBoolean
            if (isLarge) {
                addLargeText(lineString, 1)
            } else {
                addMediumText(lineString, 1)
            }
        }
        addBlankLine(1)

        // untuk footer
        if (!footerMessageNull) {
            addFooter(jsonObject["footerMessage"].asString)
            addBlankLine(1)
        }

        addLargeText("TERIMA KASIH", 0)
        addBlankLine(1)
        addMediumText("Simpan resi ini sebagai\ntanda bukti yang sah", 0)
        addBlankLine(1)
        Util.readAssetsFile(context = context, fileName = "footer_ttd.png")
            ?.let { addImage(it) }
        addBlankLine(1)
        addMediumText(jsonObject["namaAgen"].asString, 0)
        addBlankLine(3)
        startPrint()
        return printerStatusMessage()
    }

    override fun startPrint() {
        printerStatus = printer.setPrintStart()
    }


    private fun printerStatusMessage(): String {
        when (printerStatus) {
            SdkResult.SDK_PRN_STATUS_PRINTING -> {
                return "PRINTING IN PROGRESS"
            }

            SdkResult.SDK_PRN_STATUS_PAPEROUT -> {
                return "PRINTER PAPER OUT"
            }

            SdkResult.SDK_PRN_ERROR -> {
                return "PRINTER ERROR CODE ${SdkResult.SDK_PRN_ERROR}"
            }

            SdkResult.SDK_PRN_STATUS_TOOHEAT -> {
                return "PRINTER TOO HEAT"
            }

            SdkResult.SDK_PRN_STATUS_FAULT -> {
                return "PRINTER FAULT"
            }

            SdkResult.SDK_PRN_PARAM_ERROR -> {
                return "PRINTER ERROR PARAMS"
            }

            SdkResult.SDK_PRN_BASE_ERR -> {
                return "PRINTER BASE ERROR CODE ${SdkResult.SDK_PRN_BASE_ERR}"
            }

            0 -> {
                return "PRINTER READY"
            }

            else -> {
                return "UNKNOWN ERROR PRINTER"
            }
        }
    }

    /**
     *
     * @sample printerAlignment
     */
    private fun Int.printerAlignment(): Layout.Alignment {
        return when (this) {
            0 -> Layout.Alignment.ALIGN_CENTER
            1 -> Layout.Alignment.ALIGN_NORMAL
            2 -> Layout.Alignment.ALIGN_OPPOSITE
            else -> {
                Layout.Alignment.ALIGN_CENTER
            }
        }
    }
}