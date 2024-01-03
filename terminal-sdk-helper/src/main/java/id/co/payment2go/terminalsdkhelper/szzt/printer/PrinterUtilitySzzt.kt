package id.co.payment2go.terminalsdkhelper.szzt.printer

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import com.google.gson.JsonParser
import com.szzt.sdk.device.aidl.IPrinterListener
import com.szzt.sdk.device.printer.Printer
import com.szzt.sdk.device.printer.Printer.Unit.LINE
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterException
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterUtility
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.szzt.BindServiceSzzt
import id.co.payment2go.terminalsdkhelper.szzt.printer.PrinterErrorSzzt.getDescription


class PrinterUtilitySzzt(
    private val context: Context,
    private val bindService: BindServiceSzzt
) : PrinterUtility, IPrinterListener.Stub() {
    private val TAG = "PrinterUtilitySzzt"

    private val printer = bindService.mPrint
    override fun isPrinterReady(): Boolean {
        return printer?.status != 0
    }

    override fun setAutoNextLine() {}

    override fun addImage(image: ByteArray) {
        val format = Bundle()
        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        format.putInt(Printer.Format.FORMAT_OFFSET_TAG, (384 - bitmap.width) / 2)
        printer?.setConfig(format)
        printer?.addImg(image)
    }

    override fun addQR(qr: String) {
        printer?.addQrCode(qr)
    }

    override fun addHeader(value: String) {
        printer?.addStr(value, Printer.Font.FONT_3, false, Printer.Align.CENTER)
        printer?.addFeedPaper(1, LINE)

    }

    override fun addFooter(value: String) {
        printer?.addStr(value, Printer.Font.FONT_2, false, Printer.Align.CENTER)
    }

    override fun addSmallText(value: String, align: Int) {
        printer?.addStr(value, Printer.Font.SMALL, false, align)
    }

    override fun addMediumText(value: String, align: Int) {
        printer?.addStr(value, Printer.Font.FONT_2, false, align)
    }

    override fun addLargeText(value: String, align: Int) {
        printer?.addStr(value, Printer.Font.LARGE, false, align)
    }

    override fun addBlankLine(times: Int) {
        printer?.addFeedPaper(times, LINE)
    }

    override fun buildPrintTemplate(jsonString: String): String {
        printer?.open()
        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        return try {
            val isPrinterConnected = bindService.isDeviceManagerConnected()
            if (isPrinterConnected) {
                val qrCodeNull = jsonObject["qrCode"].isJsonNull

                setAutoNextLine()
                // untuk header
                Util.readAssetsFile(context = context, fileName = "bni_keagenan.png")?.let {
                    addImage(it)
                }
                addBlankLine(1)
                addHeader("Transaksi Berhasil")

                if (jsonObject["isCetakUlang"].asBoolean) {
                    addMediumText("Cetak Ulang", Printer.Align.CENTER)
                    addBlankLine(1)
                }
                addHeader(jsonObject["namaFitur"].asString)
                addBlankLine(1)

                if (!qrCodeNull) {
                    addQR(jsonObject["qrCode"].asString)
                    addBlankLine(1)
                    addMediumText("Kode Booking", Printer.Align.CENTER)
                    addLargeText(jsonObject["qrCode"].asString, Printer.Align.CENTER)
                    addBlankLine(1)
                }

                // untuk body
                jsonObject["dataList"].asJsonArray.forEach { element ->
                    val lineString = element.asJsonObject.get("lineString").asString
                    val isLarge = element.asJsonObject.get("isLarge").asBoolean
                    if (isLarge) {
                        addLargeText(lineString, Printer.Align.LEFT)
                    } else {
                        addMediumText(lineString, Printer.Align.LEFT)
                    }
                }
                addBlankLine(1)

                // untuk footer
                val footerMessageNull = jsonObject["footerMessage"].isJsonNull
                if (!footerMessageNull) {
                    addFooter(jsonObject["footerMessage"].asString)
                    addBlankLine(1)
                }

                addLargeText("TERIMA KASIH", Printer.Align.CENTER)
                addBlankLine(1)
                addMediumText("Simpan resi ini sebagai\ntanda bukti yang sah", Printer.Align.CENTER)
                addBlankLine(1)
                Util.readAssetsFile(context = context, fileName = "footer_ttd.png")
                    ?.let { addImage(it) }
                addBlankLine(1)
                addMediumText(jsonObject["namaAgen"].asString, Printer.Align.CENTER)
                printer?.addFeedPaper(5, LINE)
                printer?.start(this)

            }
            "Success"
        } catch (e: PrinterException) {
            e.message ?: "Unknown error occurred"
        }
    }

    override fun startPrint() {
        Log.d(TAG, "startPrint: ")
    }

    override fun PrinterNotify(p0: Int) {
        if (p0 == 0) {
//            // Success Section send intent for show toast
//            val intent = Intent("com.example.paymentservice.RESPONSE_BROADCAST")
//            intent.putExtra("PRINT_RESPONSE", "Printer sukses")
//            context.sendBroadcast(intent)

        } else {
            // Exception Section send intent for show toast
            val intent = Intent("com.example.paymentservice.RESPONSE_BROADCAST")
            intent.putExtra("PRINT_RESPONSE", getDescription(printer?.status ?: -999))
            context.sendBroadcast(intent)

        }
    }
}