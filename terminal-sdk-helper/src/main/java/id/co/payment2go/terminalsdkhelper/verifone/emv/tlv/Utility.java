package id.co.payment2go.terminalsdkhelper.verifone.emv.tlv;

/**
 * Created by Simon on 2018/8/31.
 */

public class Utility {


    public static String byte2HexStr(byte[] var0, int offset, int length ) {
        if (var0 == null) {
            return "";
        } else {
            String var1 = "";
            StringBuilder var2 = new StringBuilder("");

            for (int var3 = offset; var3 < (offset+length); ++var3) {
                var1 = Integer.toHexString(var0[var3] & 255);
                var2.append(var1.length() == 1 ? "0" + var1 : var1);
            }

            return var2.toString().toUpperCase().trim();
        }
    }
    public static String byte2HexStr(byte[] var0) {
        if (var0 == null) {
            return "";
        } else {
            String var1 = "";
            StringBuilder var2 = new StringBuilder("");

            for (int var3 = 0; var3 < var0.length; ++var3) {
                var1 = Integer.toHexString(var0[var3] & 255);
                var2.append(var1.length() == 1 ? "0" + var1 : var1);
            }

            return var2.toString().toUpperCase().trim();
        }
    }

    public static String byte2HexStrV2(byte[] var0) {
        if (var0 == null) {
            return "";
        } else {
            String var1 = "";
            StringBuilder var2 = new StringBuilder("");

            for (int var3 = 0; var3 < var0.length; ++var3) {
                var1 = Integer.toHexString(var0[var3] & 255);
                var2.append(var1.length() == 1 ? "0" + var1 : var1);
            }

            return var2.toString().toUpperCase().trim();
        }
    }


    public static String hexString(byte[] b) {
        StringBuffer d = new StringBuffer(b.length * 2);

        for(int i = 0; i < b.length; ++i) {
            char hi = Character.forDigit(b[i] >> 4 & 15, 16);
            char lo = Character.forDigit(b[i] & 15, 16);
            d.append(Character.toUpperCase(hi));
            d.append(Character.toUpperCase(lo));
        }

        return d.toString();
    }

    public static byte[] hexStr2ByteV2(String hexString) {
        if (hexString == null || hexString.length() == 0 ) {
            return new byte[] {0};
        }
        String hexStrTrimed = hexString.replace(" ", "");
        {
            String hexStr = hexStrTrimed;
            int len = hexStrTrimed.length();
            if( (len % 2 ) == 1 ){
                hexStr = hexStrTrimed + "0";
                ++len;
            }
            byte[] result = new byte[len/2];
            String s;
            for( int i=0; i< hexStr.length(); i++ ) {
                s = hexStr.substring(i,i+2);
                int v = Integer.parseInt(s, 16);

                result[i/2] = (byte) v;
                i++;
            }
            return  result;

        }
    }

    public static byte[] hexStr2Byte(String hexString) {
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
        if (hexString == null || hexString.length() == 0 ) {
            return new byte[] {0};
        }
        String hexStrTrimed = hexString.replace(" ", "");
//        Log.d(TAG, "hexStr2Byte:" + hexStrTrimed);
        {
            String hexStr = hexStrTrimed;
            int len = hexStrTrimed.length();
            if( (len % 2 ) == 1 ){
                hexStr = hexStrTrimed + "0";
                ++len;
            }
            byte result [] = new byte[len/2];
            String s;
            for( int i=0; i< hexStr.length(); i++ ) {
                s = hexStr.substring(i,i+2);
                int v = Integer.parseInt(s, 16);

                result[i/2] = (byte) v;
                i++;
            }
            return  result;

        }
    }
    public static byte[] hexStr2Byte(String hexString, int beginIndex, int length ) {
        if (hexString == null || hexString.length() == 0 ) {
            return new byte[] {0};
        }
        {
            if( length > hexString.length() ){
                length = hexString.length();
            }
            String hexStr = hexString;
            int len = length;
            if( (len % 2 ) == 1 ){
                hexStr = hexString + "0";
                ++len;
            }
            byte result [] = new byte[len/2];
            String s;
            for( int i=beginIndex; i< len; i++ ) {
                s = hexStr.substring(i,i+2);
                int v = Integer.parseInt(s, 16);

                result[i/2] = (byte) v;
                i++;
            }
            return  result;

        }
    }

    public static byte HEX2DEC( int hex ){
        return (byte)((hex/10)*16+hex%10);
    }

    public static int DEC2INT( byte dec ){
        int high = ((0x007F & dec) >> 4);
        if( 0!= (0x0080&dec) ) {
            high += 8;
        }
        return (high)*10+(dec&0x0F) ;
    }


}
