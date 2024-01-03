package id.co.payment2go.terminalsdkhelper.verifone.printer

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Message
import android.util.Log
import com.google.gson.JsonParser
import com.vfi.smartpos.deviceservice.aidl.PrinterListener
import id.co.payment2go.terminalsdkhelper.common.printer.AlignMode
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterException
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterUtility
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.verifone.printer.utils.PrinterFonts
import id.co.payment2go.terminalsdkhelper.verifone.BindServiceVerifone
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class PrinterUtilityVerifone(
    private val context: Context,
    bindService: BindServiceVerifone
) : PrinterUtility {
    private val printer = bindService.printer

    // bundle format for addText
    var formatImage = Bundle()
    var formatQR = Bundle()
    var formatText = Bundle()
    var formatHeader = Bundle()

    init {
        InitializeFontFiles()
    }

    override fun isPrinterReady(): Boolean {
        Log.d("isPrinterReady: ", printer.status.toString())
        return printer.status == 0
    }

    override fun setAutoNextLine() {
        TODO("Not yet implemented")
    }

    override fun addImage(image: ByteArray) {
        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        formatImage.putInt("offset", (384 - bitmap.width) / 2)
        formatImage.putInt("width", 128)
        formatImage.putInt("height", 128)

        printer.addBmpImage(formatImage, bitmap)
    }

    override fun addQR(qr: String) {
        formatQR.putInt("offset",  (384 - 200) / 2)
        formatQR.putInt("expectedHeight", 200)

        printer.addQrCode(formatQR, qr)
    }

    override fun addHeader(value: String) {
        formatHeader.putInt("font", 7)
        formatHeader.putInt("align", AlignMode.CENTER)

        printer.addText(formatHeader, value)
        addBlankLine(1)
    }

    override fun addFooter(value: String) {
        addMediumText(value, AlignMode.CENTER)
    }

    override fun addSmallText(value: String, align: Int) {
        formatText.putInt("font", 0)

        when(align){
            AlignMode.LEFT -> {
                //left
               formatText.putInt("align", AlignMode.LEFT)
            }
            AlignMode.CENTER -> {
                //mid
                formatText.putInt("align", AlignMode.CENTER)
            }
            AlignMode.RIGHT -> {
                //right
                formatText.putInt("align", AlignMode.RIGHT)
            }
        }

        printer.addText(formatText, value)
    }

    override fun addMediumText(value: String, align: Int) {

        formatText.putInt("font", 1)

        when(align){
            AlignMode.LEFT -> {
                //left
                formatText.putInt("align", AlignMode.LEFT)
            }
            AlignMode.CENTER -> {
                //mid
                formatText.putInt("align", AlignMode.CENTER)
            }
            AlignMode.RIGHT -> {
                //right
                formatText.putInt("align", AlignMode.RIGHT)
            }
        }


        printer.addText(formatText, value)
    }

    override fun addLargeText(value: String, align: Int) {
        formatText.putInt("font", 4)

        when(align){
            AlignMode.LEFT -> {
                //left
                formatText.putInt("align", AlignMode.LEFT)
            }
            AlignMode.CENTER -> {
                //mid
                formatText.putInt("align", AlignMode.CENTER)
            }
            AlignMode.RIGHT -> {
                //right
                formatText.putInt("align", AlignMode.RIGHT)
            }
        }

        printer.addText(formatText, value)
    }

    override fun addBlankLine(times: Int) {

        for (i in 0 until times) {

            printer.feedLine(times)
        }

    }

    override fun buildPrintTemplate(jsonString: String): String {
        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        return try {
            val qrCodeNull = jsonObject["qrCode"].isJsonNull
            val fiturName = jsonObject["namaFitur"].asString
            Log.d("IsReady", "buildPrintTemplate: ${isPrinterReady()}")

            if (isPrinterReady()){
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

                addBlankLine(2)

                if (!qrCodeNull) {
                    addQR(jsonObject["qrCode"].asString)
                    addBlankLine(1)
                    addMediumText("Kode Booking", AlignMode.CENTER)
                    addLargeText(jsonObject["qrCode"].asString, AlignMode.CENTER)
                    addBlankLine(2)
                }

//                // untuk body
                jsonObject["dataList"].asJsonArray.forEach { element ->
                    val lineString = element.asJsonObject.get("lineString").asString
                    val isLarge = element.asJsonObject.get("isLarge").asBoolean
                    if (isLarge) {
                        addLargeText(lineString, AlignMode.LEFT)
                    } else {
                        addMediumText(lineString, AlignMode.LEFT)
                    }
                }
                addBlankLine(2)
                // untuk footer
                val footerMessageNull = jsonObject["footerMessage"].isJsonNull
                if (!footerMessageNull) {
                    addFooter(jsonObject["footerMessage"].asString)
                    addBlankLine(1)
                }
                addLargeText("TERIMA KASIH", AlignMode.CENTER)
                addBlankLine(2)
                addMediumText("Simpan resi ini sebagai \n tanda bukti yang sah", AlignMode.CENTER)
                addBlankLine(2)

                Util.readAssetsFile(context = context, fileName = "footer_ttd.png")
                    ?.let { addImage(it) }

                addBlankLine(1)

                addMediumText(jsonObject["namaAgen"].asString, AlignMode.CENTER)
                addBlankLine(3)

                startPrint()
            }

            "Success"
        }catch (e: PrinterException){
            e.message ?: "Unknown error occurred"
        }
    }

    override fun startPrint() {
        printer.startPrint(object : PrinterListener.Stub(){
            override fun onFinish() {
                val msg = Message()
                msg.data.putString("msg", "print finished")
            }

            override fun onError(error: Int) {
                val msg = Message()
                msg.data.putString("msg", "print error,errno:$error")
            }

        })
    }


    // JNI call back -- end
    private fun InitializeFontFiles() {
        PrinterFonts.initialize(this.context.assets)
    }

    fun image2byte(path: String?): ByteArray? {
        var data: ByteArray? = null
        var input: FileInputStream? = null
        try {
            input = FileInputStream(File(path))
            val output = ByteArrayOutputStream()
            val buf = ByteArray(1024)
            var numBytesRead = 0
            while (input.read(buf).also { numBytesRead = it } != -1) {
                output.write(buf, 0, numBytesRead)
            }
            data = output.toByteArray()
            output.close()
            input.close()
        } catch (ex1: FileNotFoundException) {
            ex1.printStackTrace()
        } catch (ex1: IOException) {
            ex1.printStackTrace()
        }
        return data
    }
}