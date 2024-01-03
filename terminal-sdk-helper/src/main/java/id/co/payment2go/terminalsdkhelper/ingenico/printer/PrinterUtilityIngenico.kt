package id.co.payment2go.terminalsdkhelper.ingenico.printer

import android.content.Context
import android.content.Intent
import com.google.gson.JsonParser
import com.usdk.apiservice.aidl.printer.ASCScale
import com.usdk.apiservice.aidl.printer.ASCSize
import com.usdk.apiservice.aidl.printer.ECLevel
import com.usdk.apiservice.aidl.printer.OnPrintListener
import com.usdk.apiservice.aidl.printer.PrintFormat
import com.usdk.apiservice.aidl.printer.PrinterError
import id.co.payment2go.terminalsdkhelper.common.printer.AlignMode
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterErrorDesc
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterException
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterUtility
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico

class PrinterUtilityIngenico(
    private val context: Context,
    bindService: BindServiceIngenico,
    private val printerErrorDesc: PrinterErrorDesc
) : PrinterUtility {

    private val printer = bindService.printer

    override fun isPrinterReady(): Boolean {
        if (printer.status != PrinterError.SUCCESS) {
            throw PrinterException(printerErrorDesc.getDescription(printer.status))
        }
        return printer.status == PrinterError.SUCCESS
    }

    override fun setAutoNextLine() {
        val format = PrintFormat.FORMAT_MOREDATAPROC
        val value = PrintFormat.VALUE_MOREDATAPROC_PRNTOEND
        printer.setPrintFormat(format, value)
    }

    override fun addImage(image: ByteArray) {
        printer.addImage(AlignMode.CENTER, image)
    }

    override fun addQR(qr: String) {
        printer.addQrCode(AlignMode.CENTER, 240, ECLevel.ECLEVEL_Q, qr)
    }

    override fun addHeader(value: String) {
        // 16 characters per line
        printer.setAscScale(ASCScale.SC1x2)
        printer.setAscSize(ASCSize.DOT24x12)
        printer.addText(AlignMode.CENTER, value)
    }

    override fun addFooter(value: String) {
        printer.setAscScale(ASCScale.SC1x1)
        printer.setAscSize(ASCSize.DOT24x12)
        printer.addText(AlignMode.CENTER, value)
    }

    override fun addSmallText(value: String, align: Int) {
        // 48 characters per line
        printer.setAscScale(ASCScale.SC1x1)
        printer.setAscSize(ASCSize.DOT24x8)
        printer.addText(align, value)
    }

    override fun addMediumText(value: String, align: Int) {
        // 32 characters per line
        printer.setAscScale(ASCScale.SC1x1)
        printer.setAscSize(ASCSize.DOT24x12)
        printer.addText(align, value)
    }

    override fun addLargeText(value: String, align: Int) {
        // 24 characters per line
        printer.setAscScale(ASCScale.SC2x3)
        printer.setAscSize(ASCSize.DOT16x8)
        printer.addText(align, value)
    }

    override fun addBlankLine(times: Int) {
        printer.setAscScale(ASCScale.SC1x1)
        printer.setAscSize(ASCSize.DOT24x12)
        for (i in 0 until times) {
            printer.addText(AlignMode.CENTER, " ")
        }
    }

    override fun buildPrintTemplate(jsonString: String): String {
        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        return try {
            val qrCodeNull = jsonObject["qrCode"].isJsonNull
            val fiturName = jsonObject["namaFitur"].asString

            if (isPrinterReady()) {
                setAutoNextLine()
                // untuk header
                Util.readAssetsFile(context = context, fileName = "bni_keagenan.png")?.let {
                    addImage(it)
                }
                addBlankLine(1)

                if (fiturName == "Cek Saldo Kartu") {
                    addHeader("BERHASIL")
                } else {
                    addHeader("Transaksi Berhasil")
                }


                if (jsonObject["isCetakUlang"].asBoolean) {
                    addMediumText("Cetak Ulang", AlignMode.CENTER)
                    addBlankLine(1)
                }
                addMediumText(jsonObject["namaFitur"].asString, AlignMode.CENTER)

                addBlankLine(1)

                if (!qrCodeNull) {
                    addQR(jsonObject["qrCode"].asString)
                    addBlankLine(1)
                    addMediumText("Kode Booking", AlignMode.CENTER)
                    addLargeText(jsonObject["qrCode"].asString, AlignMode.CENTER)
                    addBlankLine(1)
                }

                // untuk body
                jsonObject["dataList"].asJsonArray.forEach { element ->
                    val lineString = element.asJsonObject.get("lineString").asString
                    val isLarge = element.asJsonObject.get("isLarge").asBoolean
                    if (isLarge) {
                        addLargeText(lineString, AlignMode.LEFT)
                    } else {
                        addMediumText(lineString, AlignMode.LEFT)
                    }
                }
                addBlankLine(1)
                // untuk footer
                val footerMessageNull = jsonObject["footerMessage"].isJsonNull
                if (!footerMessageNull) {
                    addFooter(jsonObject["footerMessage"].asString)
                    addBlankLine(1)
                }
                addLargeText("TERIMA KASIH", AlignMode.CENTER)
                addBlankLine(1)
                addMediumText("Simpan resi ini sebagai\ntanda bukti yang sah", AlignMode.CENTER)
                addBlankLine(1)
                Util.readAssetsFile(context = context, fileName = "footer_ttd.png")
                    ?.let { addImage(it) }
                addBlankLine(1)
                addMediumText(jsonObject["namaAgen"].asString, AlignMode.CENTER)
                printer.feedLine(5)
                startPrint()
            }
            "Success"
        } catch (e: PrinterException) {
            e.message ?: "Unknown error occurred"
        }
    }

    override fun startPrint() {
        printer.startPrint(object : OnPrintListener.Stub() {
            override fun onFinish() {
                val intent = Intent("com.example.paymentservice.RESPONSE_BROADCAST")
                intent.putExtra("PRINT_RESPONSE", "Print finished")
                context.sendBroadcast(intent)
            }

            override fun onError(error: Int) {
                val intent = Intent("com.example.paymentservice.RESPONSE_BROADCAST")
                intent.putExtra("PRINT_RESPONSE", printerErrorDesc.getDescription(error))
                context.sendBroadcast(intent)
            }

        })

    }
}
