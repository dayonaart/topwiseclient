package id.co.payment2go.terminalsdkhelper.zcs.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object ZcsFileUtil {
    @Throws(IOException::class)
    fun doCopy(context: Context, assetsPath: String, desPath: String) {
        var assets = assetsPath
        var destinationPath = desPath
        val srcFiles = context.assets.list(assets) //for directory
        for (srcFileName in srcFiles!!) {
            if (!destinationPath.endsWith(File.separator)) destinationPath += File.separator
            if (!assets.endsWith(File.separator)) assets += File.separator
            val desDir = File(destinationPath)
            if (!desDir.exists()) desDir.mkdir()
            val outFileName = destinationPath + srcFileName
            var inFileName = assets + srcFileName
            if (assets == "") { // for first time
                inFileName = srcFileName
            }
            Log.e(
                "tag",
                "========= assets: $assets  filename: $srcFileName infile: $inFileName outFile: $outFileName"
            )
            try {
                val inputStream = context.assets.open(inFileName)
                copyAndClose(inputStream, FileOutputStream(outFileName))
            } catch (e: IOException) { //if directory fails exception
                e.printStackTrace()
                File(outFileName).mkdir()
                doCopy(context, inFileName, outFileName)
            }
        }
    }

    private fun closeQuietly(out: OutputStream?) {
        try {
            out?.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun closeQuietly(`is`: InputStream?) {
        try {
            `is`?.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun copyAndClose(`is`: InputStream, out: OutputStream) {
        copy(`is`, out)
        closeQuietly(`is`)
        closeQuietly(out)
    }

    @Throws(IOException::class)
    private fun copy(`is`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var n = 0
        while (-1 != `is`.read(buffer).also { n = it }) {
            out.write(buffer, 0, n)
        }
    }
}