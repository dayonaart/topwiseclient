package id.co.payment2go.terminalsdkhelper.verifone.util

import java.util.Locale


object VerifoneUtility {
    fun byte2HexStr(var0: ByteArray?, offset: Int, length: Int): String {
        return if (var0 == null) {
            ""
        } else {
            var var1 = ""
            val var2 = StringBuilder("")
            for (var3 in offset until offset + length) {
                var1 = Integer.toHexString(var0[var3].toInt() and 255)
                var2.append(if (var1.length == 1) "0$var1" else var1)
            }
            var2.toString().uppercase(Locale.getDefault()).trim { it <= ' ' }
        }
    }

    fun byte2HexStr(var0: ByteArray?): String {
        return if (var0 == null) {
            ""
        } else {
            var var1 = ""
            val var2 = StringBuilder("")
            for (var3 in var0.indices) {
                var1 = Integer.toHexString(var0[var3].toInt() and 255)
                var2.append(if (var1.length == 1) "0$var1" else var1)
            }
            var2.toString().uppercase(Locale.getDefault()).trim { it <= ' ' }
        }
    }

    fun hexStr2Byte(hexString: String?): ByteArray {
//        Log.d(TAG, "hexStr2Byte:" + hexString);
//        if (hexString == null || hexString.length() == 0 ) {
//            return new byte[] {0};
//        } else {
//            String hexStr = hexString;
//            byte result [] = new byte[hexString.length()/2];
//            if( (hexStr.length() % 2 ) == 1 ){
//                hexStr = hexString + "0";
//            }
//            String s;
//            for( int i=0; i< hexStr.length(); i++ ) {
//                s = hexStr.substring(i,i+2);
//                int v = Integer.parseInt(s, 16);
//
//                result[i/2] = (byte) v;
//                i++;
//            }
//            return  result;
        if (hexString == null || hexString.length == 0) {
            return byteArrayOf(0)
        }
        val hexStrTrimed = hexString.replace(" ", "")
        //        Log.d(TAG, "hexStr2Byte:" + hexStrTrimed);
        run {
            var hexStr = hexStrTrimed
            var len = hexStrTrimed.length
            if (len % 2 == 1) {
                hexStr = hexStrTrimed + "0"
                ++len
            }
            val result = ByteArray(len / 2)
            var s: String
            var i = 0
            while (i < hexStr.length) {
                s = hexStr.substring(i, i + 2)
                val v = s.toInt(16)
                result[i / 2] = v.toByte()
                i++
                i++
            }
            return result
        }
    }

    fun hexStr2Byte(hexString: String?, beginIndex: Int, length: Int): ByteArray {
        var length = length
        if (hexString == null || hexString.length == 0) {
            return byteArrayOf(0)
        }
        run {
            if (length > hexString.length) {
                length = hexString.length
            }
            var hexStr: String = hexString
            var len = length
            if (len % 2 == 1) {
                hexStr = hexString + "0"
                ++len
            }
            val result = ByteArray(len / 2)
            var s: String
            var i = beginIndex
            while (i < len) {
                s = hexStr.substring(i, i + 2)
                val v = s.toInt(16)
                result[i / 2] = v.toByte()
                i++
                i++
            }
            return result
        }
    }

    fun HEX2DEC(hex: Int): Byte {
        return (hex / 10 * 16 + hex % 10).toByte()
    }

    fun DEC2INT(dec: Byte): Int {
        var high = 0x007F and dec.toInt() shr 4
        if (0 != 0x0080 and dec.toInt()) {
            high += 8
        }
        return high * 10 + (dec.toInt() and 0x0F)
    }
}