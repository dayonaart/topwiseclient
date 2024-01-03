package id.co.payment2go.terminalsdkhelper.topwise.printer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Build
import android.os.RemoteException
import android.util.Log
import com.google.gson.JsonParser
import com.topwise.cloudpos.aidl.printer.AidlPrinterListener
import com.topwise.cloudpos.aidl.printer.Align
import com.topwise.cloudpos.aidl.printer.ImageUnit
import com.topwise.cloudpos.aidl.printer.PrintTemplate
import com.topwise.cloudpos.aidl.printer.TextUnit
import com.topwise.cloudpos.aidl.printer.TextUnit.TextSize
import id.co.payment2go.terminalsdkhelper.common.printer.AlignMode
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterUtility
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.topwise.BindServiceTopWise
import id.co.payment2go.terminalsdkhelper.topwise.utils.QRCodeUtil

class PrinterUtilityTopWise(
    private val context: Context,
    bindService: BindServiceTopWise
) : PrinterUtility {

    private val template = PrintTemplate.getInstance()
    private var printRunning: Boolean? = null
    private val printer = bindService.printerDev

    init {
        val typeface = Typeface.createFromAsset(context.assets, "fonts/montserrat_medium.ttf")
        PrintTemplate.getInstance().init(context, typeface)
    }



    override fun isPrinterReady(): Boolean {
        Log.d("print status: ", "${printer.printerState}")
        return when(printer.printerState){
            0 -> { true }
            else -> { false }
        }
    }

    override fun setAutoNextLine() {
        TODO("Not yet implemented")
    }

    override fun addImage(image: ByteArray) {
        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        template.add(ImageUnit(bitmap, bitmap.width, bitmap.height))
        printer.addRuiImage(bitmap, 0)
        template.add(TextUnit("\n\n"))
    }

    override fun addQR(qr: String) {
        val bitmap: Bitmap = QRCodeUtil.createQRImage(qr, 190, 190, null)
        template.add(ImageUnit(Align.CENTER, bitmap, 190, 190))
    }

    override fun addHeader(value: String) {
        template.add(TextUnit(value ,TextSize.LARGE, Align.CENTER).setBold(true))
    }

    override fun addFooter(value: String) {
        addMediumText(value, AlignMode.CENTER)
    }

    override fun addSmallText(value: String, align: Int) {
        Log.d("Align", "addSmallText: Align $align")
        when (align) {
            0 -> {
                template.add(TextUnit(value ,12, Align.LEFT))
            }
            1 -> {
                template.add(TextUnit(value ,12, Align.CENTER))
            }
            2 -> {
                template.add(TextUnit(value ,12, Align.RIGHT))
            }
        }
    }

    override fun addMediumText(value: String, align: Int) {
        Log.d("Align", "addMediumText: Align $align")
        when (align) {
            0 -> {
                template.add(TextUnit(value ,TextSize.NORMAL, Align.LEFT))
            }
            1 -> {
                template.add(TextUnit(value ,TextSize.NORMAL, Align.CENTER))
            }
            2 -> {
                template.add(TextUnit(value ,TextSize.NORMAL, Align.RIGHT))
            }
        }

    }

    override fun addLargeText(value: String, align: Int) {
        Log.d("Align", "addLargeText: Align $align")
        when (align) {
            0 -> {
                template.add(TextUnit(value ,TextSize.LARGE, Align.LEFT))
            }
            1 -> {
                template.add(TextUnit(value ,TextSize.LARGE, Align.CENTER))
            }
            2 -> {
                template.add(TextUnit(value ,TextSize.LARGE, Align.RIGHT))
            }
        }
    }

    override fun addBlankLine(times: Int) {
        template.add(
            TextUnit(
               "",
               12,
                Align.LEFT
            ).setWordWrap(true).setLineSpacing(times)
        )
    }

    override fun buildPrintTemplate(jsonString: String): String {
        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        return try {
            template.clear()

            val qrCodeNull = jsonObject["qrCode"].isJsonNull
            val fiturName = jsonObject["namaFitur"].asString
            Log.d("IsReady", "buildPrintTemplate: ${isPrinterReady()}")

            if (isPrinterReady()){
                Util.readAssetsFile(context = context, fileName = "bni_keagenan.png")?.let {
                    addImage(it)
                }
                addBlankLine(15)

                if (fiturName == "Cek Saldo Kartu") {
                    addHeader("BERHASIL")
                } else {
                    addHeader("Transaksi Berhasil")
                }

                if (jsonObject["isCetakUlang"].asBoolean) {
                    addMediumText("Cetak Ulang", AlignMode.CENTER)
                    addBlankLine(15)
                }
                addMediumText(jsonObject["namaFitur"].asString, AlignMode.CENTER)

                addBlankLine(2)

                if (!qrCodeNull) {
                    addQR(jsonObject["qrCode"].asString)
                    addBlankLine(1)
                    addMediumText("Kode Booking", AlignMode.CENTER)
                    addLargeText(jsonObject["qrCode"].asString, AlignMode.CENTER)
                    addBlankLine(2)
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
                addBlankLine(15)
                // untuk footer
                val footerMessageNull = jsonObject["footerMessage"].isJsonNull
                if (!footerMessageNull) {
                    addFooter(jsonObject["footerMessage"].asString)
                    addBlankLine(15)
                }
                addLargeText("TERIMA KASIH", AlignMode.CENTER)
                addBlankLine(2)
                addMediumText("Simpan resi ini sebagai \n tanda bukti yang sah", AlignMode.CENTER)
                addBlankLine(2)

                Util.readAssetsFile(context = context, fileName = "footer_ttd.png")
                    ?.let {
                        addImage(it)
                    }

                addBlankLine(8)

                addMediumText(jsonObject["namaAgen"].asString, AlignMode.CENTER)
                addBlankLine(40)

                printAddLineFree(template)
                printer.addRuiImage(template.printBitmap, 0)
                startPrint()
                printRunning = true
            }

            "Success"
        }catch (e: Exception){
            e.message ?: "Unknown error occurred"
        }
    }

    override fun startPrint() {
       printer.printRuiQueue(mListen)
    }

    private var mListen: AidlPrinterListener = object : AidlPrinterListener.Stub() {
        @Throws(RemoteException::class)
        override fun onError(p0: Int) {
            Log.d("Wise print state: ", "Error: $p0")
            printRunning = false
        }

        override fun onPrintFinish() {
            Log.d("Wise print state: ", "Success")
            printRunning = false
        }
    }

    private fun printAddLineFree(template: PrintTemplate) {
        Log.d("Wise Printer: ", "Build Display: ${Build.DISPLAY}")
        if (Build.DISPLAY.contains("Z3909")) {
            template.add(TextUnit(""))
        }
    }
}