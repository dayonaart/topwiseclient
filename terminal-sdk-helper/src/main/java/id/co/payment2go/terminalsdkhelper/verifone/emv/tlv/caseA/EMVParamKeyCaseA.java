package id.co.payment2go.terminalsdkhelper.verifone.emv.tlv.caseA;

import static id.co.payment2go.terminalsdkhelper.verifone.emv.tlv.EMVParamKey.TAG_Index_9F22;
import android.util.Log;
import id.co.payment2go.terminalsdkhelper.verifone.emv.tlv.EMVTLVParam;

/**
 * Created by Simon on 2018/9/6.
 * This a Fixed EMVParamKey DEMO
 * You can change the TAG or VALUE here refer YOUR Specification
 */

public class EMVParamKeyCaseA extends EMVTLVParam {
    private static final String TAG = "EMVParamKeyCaseA";
    @Override
    public String append(int tag, String value) {
        int fixedTag = tag;
        String fixedValue = value;
        // THIS IS THE DEMO CODE !!!!, PLEASE FIX IT Refer YOUR Specification.
        if( tag == 0x9F23 ) {
            // fix the tag 0x9F23 to 0x9F22
            fixedTag = TAG_Index_9F22;
            Log.d(TAG, "reset tag " + Integer.toHexString(tag) + " -> " + Integer.toHexString(fixedTag));
        }
        return super.append(fixedTag, fixedValue);
    }
}
