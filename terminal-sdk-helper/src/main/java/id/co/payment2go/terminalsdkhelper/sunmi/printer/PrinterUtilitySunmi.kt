package id.co.payment2go.terminalsdkhelper.sunmi.printer

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.JsonParser
import com.sunmi.peripheral.printer.InnerResultCallback
import com.sunmi.peripheral.printer.WoyouConsts
import id.co.payment2go.terminalsdkhelper.common.printer.AlignMode
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterUtility
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.sunmi.BindServiceSunmi

class PrinterUtilitySunmi(
    private val context: Context,
    bindService: BindServiceSunmi
) : PrinterUtility {
    private val TAG = "PrinterUtility"
    private val printer = bindService.sunmiPrinterService

    private val callback: InnerResultCallback = object : InnerResultCallback() {
        override fun onRunResult(isSuccess: Boolean) {
            Log.d(TAG, "onRunResult: isSuccess = $isSuccess")
        }

        override fun onReturnString(result: String?) {
            Log.d(TAG, "onReturnString: $result")
        }

        override fun onRaiseException(code: Int, msg: String?) {
            Log.d(TAG, "onRaiseException: code = $code, msg = $msg")
        }

        override fun onPrintResult(code: Int, msg: String?) {
            Log.d(TAG, "onPrintResult: code = $code, msg = $msg")
        }

    }

    override fun isPrinterReady(): Boolean {
        return printer.updatePrinterState() != 0
    }

    override fun setAutoNextLine() {

    }

    override fun addImage(image: ByteArray) {
        printer.setAlignment(AlignMode.CENTER, callback)
        printer.printBitmap(BitmapFactory.decodeByteArray(image, 0, image.size), null)
    }

    override fun addQR(qr: String) {
        printer.setAlignment(AlignMode.CENTER, null)
        printer.printQRCode(qr, 8, 3, null)
    }

    override fun addHeader(value: String) {
        printer.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
        printer.setPrinterStyle(WoyouConsts.SET_LINE_SPACING, 5)
        printer.setAlignment(AlignMode.CENTER, null)
        printer.printTextWithFont(value + "\n", null, (36).toFloat(), null)
    }

    override fun addFooter(value: String) {
        printer.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
        printer.setPrinterStyle(WoyouConsts.SET_LINE_SPACING, 5)
        printer.setAlignment(AlignMode.CENTER, null)
        printer.printTextWithFont(value + "\n", null, (30).toFloat(), null)
    }

    override fun addSmallText(value: String, align: Int) {
        printer.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
        printer.setPrinterStyle(WoyouConsts.SET_LINE_SPACING, 0)
        printer.setAlignment(align, null)
        printer.printTextWithFont(value + "\n", null, (15).toFloat(), null)
    }

    override fun addMediumText(value: String, align: Int) {
        printer.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
        printer.setPrinterStyle(WoyouConsts.SET_LINE_SPACING, 0)
        printer.setAlignment(align, null)
        printer.printTextWithFont(value + "\n", null, (26).toFloat(), null)
    }

    override fun addLargeText(value: String, align: Int) {
        printer.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
        printer.setPrinterStyle(WoyouConsts.SET_LINE_SPACING, 5)
        printer.setAlignment(align, null)
        printer.printTextWithFont(value + "\n", null, (36).toFloat(), null)
    }

    override fun addBlankLine(times: Int) {
        for (i in 0 until times) {
            printer.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            printer.setPrinterStyle(WoyouConsts.SET_LINE_SPACING, 0)
            printer.setAlignment(AlignMode.CENTER, null)
            printer.printTextWithFont(" " + "\n", null, (30).toFloat(), null)
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
                printer.autoOutPaper(null)
                startPrint()
            }
            "Success"
        } catch (e: Exception) {
            e.message ?: "Unknown error occurred"
        }
    }

    override fun startPrint() {

    }

    private fun buildPrinterTemplateQR(qr: String) {
        addMediumText(
            value = "Silahkan scan qr berikut untuk dapat melihat kode booking Anda",
            align = AlignMode.LEFT
        )
        addQR(qr)
        addBlankLine(1)
        addMediumText(
            value = """
                        Informasi Penting
            
                        1. Wajib menunjukkan struk ini saat Check-In di Pelabuhan
                        2. Wajib menunjukkan kartu identitas dan STNK yang sesuai
                        3. Tiket akan hangus (expired) bila penumpang datang terlambat
                    """.trimIndent(),
            align = AlignMode.LEFT
        )
        addBlankLine(1)
        addMediumText(value = "Call Center ASDP:\n021-191/08111-021191", AlignMode.CENTER)

    }

}
