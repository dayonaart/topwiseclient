package id.co.payment2go.terminalsdkhelper.common.model;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class BytesUtil {

    public static byte[] hexStr2Bytes(String hexStr) {
        if (hexStr.length() <= 0) {
            return new byte[0];
        }
        int length = hexStr.length() / 2;
        char[] chars = hexStr.toCharArray();
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = (byte) (char2Byte(chars[i * 2]) << 4 | char2Byte(chars[i * 2 + 1]));
        }
        return b;
    }

    private static int char2Byte(char c) {
        if (c >= 'a') {
            return (c - 'a' + 10) & 0x0f;
        }
        if (c >= 'A') {
            return (c - 'A' + 10) & 0x0f;
        }
        return (c - '0') & 0x0f;
    }

    public static byte hexStr2Byte(String hexStr) {
        return (byte) Integer.parseInt(hexStr, 16);
    }

    /**
     * Change byte to hex string data.
     */
    public static String byte2HexString(byte data) {
        StringBuilder buffer = new StringBuilder();
        String hex = Integer.toHexString(data & 0xFF);
        if (hex.length() == 1) {
            buffer.append('0');
        }
        buffer.append(hex);
        return buffer.toString().toUpperCase();
    }

    /**
     * Change hex string data to bytes.
     */
    public static byte[] hexString2Bytes(String data) {
        byte[] result = new byte[(data.length() + 1) / 2];
        if ((data.length() & 0x1) == 1) {
            data = data + "0";
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = ((byte) (hex2byte(data.charAt(i * 2 + 1)) | hex2byte(data.charAt(i * 2)) << 4));
        }
        return result;
    }

    /**
     * Change hex char to byte.
     */
    public static byte hex2byte(char hex) {
        if ((hex <= 'f') && (hex >= 'a')) {
            return (byte) (hex - 'a' + 10);
        }

        if ((hex <= 'F') && (hex >= 'A')) {
            return (byte) (hex - 'A' + 10);
        }

        if ((hex <= '9') && (hex >= '0')) {
            return (byte) (hex - '0');
        }

        return 0;
    }

    /**
     * Sub bytes.
     */
    public static byte[] subBytes(byte[] data, int offset, int len) {
        if ((offset < 0) || (data.length <= offset)) {
            return null;
        }

        if ((len < 0) || (data.length < offset + len)) {
            len = data.length - offset;
        }

        byte[] ret = new byte[len];

        System.arraycopy(data, offset, ret, 0, len);
        return ret;
    }

    /**
     * Change bytes to sring data.
     */
    public static String fromBytes(byte[] data, String charsetName) throws UnsupportedEncodingException {
        return new String(data, charsetName);
    }

    /**
     * Change byte array to GBK string data.
     */
    public static String fromGBK(byte[] data) throws UnsupportedEncodingException {
        return fromBytes(data, "GBK");
    }

    /**
     * Merge bytes.
     */
    public static byte[] mergeBytes(byte[] bytesA, byte[] bytesB) {
        if ((bytesA == null) || (bytesA.length == 0))
            return bytesB;
        if ((bytesB == null) || (bytesB.length == 0)) {
            return bytesA;
        }

        byte[] bytes = new byte[bytesA.length + bytesB.length];

        System.arraycopy(bytesA, 0, bytes, 0, bytesA.length);
        System.arraycopy(bytesB, 0, bytes, bytesA.length, bytesB.length);

        return bytes;
    }

    /**
     * Merge byte array.
     */
    public static byte[] merge(byte[]... data) {
        if (data == null) {
            return null;
        }

        byte[] bytes = null;
        for (byte[] aData : data) {
            bytes = mergeBytes(bytes, aData);
        }

        return bytes;
    }

    /**
     * Compare bytes.
     */
    public static int bytecmp(byte[] hex1, byte[] hex2, int len) {
        for (int i = 0; i < len; i++) {
            if (hex1[i] != hex2[i]) {
                return 1;
            }
        }

        return 0;
    }

    public static String ConvertNumericWithLeadingZeros(long Numeric, int TotalDigits) {
        return String.format("%" + TotalDigits + "s", Numeric)
                .replace(' ', '0');
    }

    /**
     * Change hex string data to byte array.
     */
    public static byte[] hexString2ByteArray(String hexStr) {
        if (hexStr == null) return null;
        if (hexStr.length() % 2 != 0) {
            return null;
        }
        byte[] data = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            char hc = hexStr.charAt(2 * i);
            char lc = hexStr.charAt(2 * i + 1);
            byte hb = hexChar2Byte(hc);
            byte lb = hexChar2Byte(lc);
            if ((hb < 0) || (lb < 0)) {
                return null;
            }
            int n = hb << 4;
            data[i] = ((byte) (n + lb));
        }
        return data;
    }

    /**
     * Change hex char to byte data.
     */
    public static byte hexChar2Byte(char c) {
        if ((c >= '0') && (c <= '9')) return (byte) (c - '0');
        if ((c >= 'a') && (c <= 'f')) return (byte) (c - 'a' + 10);
        if ((c >= 'A') && (c <= 'F')) return (byte) (c - 'A' + 10);
        return -1;
    }

    /**
     * Change byte array to hex string data.
     */
    public static String byteArray2HexString(byte[] arr) {
        if (arr != null) {
            StringBuilder sb = new StringBuilder();
            for (byte anArr : arr) {
                sb.append(String.format("%02x", anArr).toUpperCase());
            }
            return sb.toString();
        } else
            return "";
    }

    /**
     * Change int type data to four places byte array.
     */
    public static byte[] toFourByteArray(int i) {
        byte[] array = new byte[4];
        array[0] = (byte) (i >> 24 & 0x7F);
        array[1] = (byte) (i >> 16);
        array[2] = (byte) (i >> 8);
        array[3] = (byte) i;
        return array;
    }

    /**
     * Change long type data to BCD bytes.
     */
    public static byte[] toBCDAmountBytes(long data) {
        byte[] bcd = {0, 0, 0, 0, 0, 0};
        byte[] bcdDou = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        if (data <= 0) {
            return bcd;
        }

        int i = bcdDou.length - 1;

        while (data != 0) {
            bcdDou[i] = (byte) (data % 10);
            data /= 10;
            i--;
        }

        for (i = bcd.length - 1; i >= 0; i--) {
            bcd[i] = (byte) (((bcdDou[i * 2 + 1] & 0x0f)) | ((bcdDou[i * 2] << 4) & 0xf0));
        }

        return bcd;
    }

    public static byte[] reverse(byte[] array) {
        if (array == null) {
            return null;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
        return array;
    }

    public static byte[] intToBytes(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    public static int hexToDecimal(String hexnum) {
        String hstring = "0123456789ABCDEF";
        hexnum = hexnum.toUpperCase();
        int num = 0;
        for (int i = 0; i < hexnum.length(); i++) {
            char ch = hexnum.charAt(i);
            int n = hstring.indexOf(ch);
            num = 16 * num + n;
        }
        return num;
    }

    public static long hexToLongDecimal(String hexnum) {
        String hstring = "0123456789ABCDEF";
        hexnum = hexnum.toUpperCase();
        int num = 0;
        for (int i = 0; i < hexnum.length(); i++) {
            char ch = hexnum.charAt(i);
            int n = hstring.indexOf(ch);
            num = 16 * num + n;
        }
        return num;
    }

    public static String toSpecificSizeString(String original, int Size) {
        return toSpecificSizeString(original, Size, "0", "right");
    }

    public static String toSpecificSizeString(String original, int Size, String Filler, String txtAlignment) {
        String retval = original;
        if (original != null && original.length() > Size) {
            retval = retval.substring(0, Size);
        } else if (original != null && original.length() < Size) {
            retval = "";
            int PaddingSize = Size - original.length();
            switch (txtAlignment.toLowerCase()) {
                case "center": {
                    int leftSide = PaddingSize / 2;
                    int rightSide = PaddingSize - leftSide;
                    for (int i = 0; i < leftSide; i++) {
                        retval += Filler;
                    }

                    retval += original;

                    for (int i = 0; i < rightSide; i++) {
                        retval += Filler;
                    }
                    break;
                }
                case "right": {

                    for (int i = 0; i < PaddingSize; i++) {
                        retval += Filler;
                    }

                    retval += original;

                    break;
                }
                case "left": {
                    retval += original;
                    for (int i = 0; i < PaddingSize; i++) {
                        retval += Filler;
                    }
                    break;
                }
            }

        }
        return retval;
    }

    public static String toHex(String ba) {
        StringBuilder str = new StringBuilder();
        for (char ch : ba.toCharArray()) {
            str.append(String.format("%02x", (int) ch));

        }
        return str.toString();
    }

    public static byte[] ConvertFromByteBuffer(byte[] originalValue) {

        byte[] bytes = originalValue;

// Wrap a byte array into a buffer
        ByteBuffer buf = ByteBuffer.wrap(bytes);

// Retrieve bytes between the position and limit
// (see Putting Bytes into a ByteBuffer)
        bytes = new byte[buf.remaining()];

// transfer bytes from this buffer into the given destination array
        buf.get(bytes, 0, bytes.length);

// Retrieve all bytes in the buffer
        buf.clear();
        bytes = new byte[buf.capacity()];

// transfer bytes from this buffer into the given destination array
        buf.get(bytes, 0, bytes.length);

        return bytes;
    }

    public static int ReverseHexToInt(String Hex) {
        byte[] AmountRecievedBytes = BytesUtil.hexString2ByteArray(Hex);
        AmountRecievedBytes = BytesUtil.reverse(AmountRecievedBytes);
        Hex = BytesUtil.byteArray2HexString(AmountRecievedBytes);
        return BytesUtil.hexToDecimal(Hex);
    }

    public static String IntToReverseHex(int Num) {
        String HexValue = BytesUtil.byteArray2HexString(
                BytesUtil.reverse(BytesUtil.intToBytes(Num)));
        return HexValue;
    }

    /*
     * XOR String
     */
    public static String XOR(String s1, String s2, int iLen) {
        byte[] hexs1 = BytesUtil.hexString2ByteArray(s1);
        byte[] hexs2 = BytesUtil.hexString2ByteArray(s2);

        return BytesUtil.byteArray2HexString(XORHEX(hexs1, hexs2, iLen));
    }

    public static byte[] XORHEX(byte[] s1, byte[] s2, int iLen) {
        byte[] result = new byte[iLen];
        for (int i = 0; i < iLen; i++) {
            result[i] = (byte) (s1[i] ^ s2[i]);
        }

        return result;
    }

    /*
     * Common use in Tapcash
     */
    public static long toJulianDay(int y, int m, int d) {
        long L_retVal = 0, iYear = 0, iMonth = 0, iDate = 0, lngTemp = 0;
        L_retVal = 2449719;

        iYear = y;
        iMonth = m;
        iDate = d;
        iYear += 8000;
        if (iMonth < 3) {
            iYear--;
            iMonth += 12;
        }
        lngTemp = ((iYear * 365) + (iYear / 4) - (iYear / 100) + (iYear / 400) - 1200820 + ((iMonth * 153 + 3) / 5) - 92 + iDate - 1);
        return lngTemp - L_retVal;
    }

    public static String toJulianSecond(long lngFirstJulian, String t) {
        String strTemp;
        long L_retVal = 0, L_second = 0, iHour = 0, iMinute = 0, iSecond = 0;

        iHour = Integer.parseInt(t.substring(0, 2));
        iMinute = Integer.parseInt(t.substring(2, 4));
        iSecond = Integer.parseInt(t.substring(4, 6));
        L_second = (iHour * 3600) + (iMinute * 60) + iSecond;
        L_retVal = L_second + (lngFirstJulian * 86400);

        return Long.toHexString(L_retVal);
    }

    public static String toAmountComplement(int amount) {
        byte[] amtHex = intToBytes(amount);
        byte[] firstC = XORHEX(Arrays.copyOfRange(amtHex, 1, 4), hexString2Bytes("FFFFFF"), 3);
        firstC[2]++;
        if (firstC[2] == 0x00) {
            firstC[1]++;
            if (firstC[1] == 0x00) {
                firstC[0]++;
            }
        }
        return byteArray2HexString(firstC);
    }

    public static String toDate(String data) throws ParseException {
//        String input = data;
//        DateFormat fmt1 = new SimpleDateFormat("yyDDD");
//        Date date = fmt1.parse(input);
//        DateFormat fmt2 = new SimpleDateFormat("dd/MM/yyyy");
//        String output = fmt2.format(date);

        String output = "";
        int days = Integer.valueOf(data);
        SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date startDate = dateParser.parse("01/01/1995");
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + days);
            Date newDate = c.getTime();
            output = dateParser.format(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static String stringToHex(String input) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte element : input.getBytes()) {
            String hex = Integer.toHexString(element & 0xFF);
            if (hex.length() == 1) {
                stringBuilder.append('0');
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }


}
