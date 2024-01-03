package id.co.payment2go.terminalsdkhelper.szzt.utils

import android.util.Log
import com.szzt.sdk.device.emv.EmvInterface

object TagUtils {

    fun getTag(tagInt: Int, emvInterface: EmvInterface): ByteArray? {
        val temp = ByteArray(256)
        val result: ByteArray?
        val ret = emvInterface.getTagData(tagInt, temp)
        if (ret <= 0) {
            return null
        }
        result = ByteArray(ret)
        System.arraycopy(temp, 0, result, 0, ret)
        return result
    }


    fun getTagDataStr(tag: Int, emvInterface: EmvInterface): String? {
        val retData: ByteArray? = getTagData(tag, emvInterface)
        return try {
            String(retData!!).trim { it <= ' ' }
        } catch (e: Exception) {
            null
        }
    }

    private fun getTagData(tag: Int, emvInterface: EmvInterface): ByteArray? {
        val retData = ByteArray(256)
        val ret = emvInterface.getTagData(tag, retData)
        if (ret > 0) {
            val data = ByteArray(ret)
            System.arraycopy(retData, 0, data, 0, ret)
            return data
        }
        return null
    }

    fun trackTrim(data: ByteArray?): ByteArray? {
        return if (data == null) {
            null
        } else {
            var end = 0
            for (i in data.indices.reversed()) {
                if (data[i].toInt() != 0) {
                    end = i
                    break
                }
            }
            val d = ByteArray(end + 1)
            System.arraycopy(data, 0, d, 0, end + 1)
            d
        }
    }

    fun getTagV2(tag: Int, emvInterface: EmvInterface): String {
        val tags = intArrayOf(tag)
        val bytes = byteArrayOf((tag and 0xff).toByte())
        val tagString: String = bytesToHexString(bytes)
        val recvFiled55 = ByteArray(256)
        val data = ByteArray(256)
        var index = 0
        for (i in tags.indices) {
            val len = emvInterface.getTagData(tags[i], data)
            if (len > 0) {
                recvFiled55[index++] = (tags[i] and 0xff).toByte()
                recvFiled55[index++] = len.toByte()
                System.arraycopy(data, 0, recvFiled55, index, len)
                index += len
            }
        }
        val filed55 = ByteArray(index)
        System.arraycopy(recvFiled55, 0, filed55, 0, index)
        return try {
            bytesToHexString(filed55).substring(tagString.length + 2)
        } catch (e: StringIndexOutOfBoundsException) {
            e.printStackTrace()
            ""
        }
    }

    private fun bytesToHexString(src: ByteArray): String {
        val stringBuilder = StringBuilder("0x")
        for (it in src) {
            val tempStr = String.format("%02X", it)
            Log.d("bytesToHexString", "byte to hex: $tempStr")
            stringBuilder.append(tempStr)
        }
        return stringBuilder.toString()
    }

}