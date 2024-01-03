package id.example.terminalsdkhelper

import com.usdk.apiservice.aidl.emv.EMVTag
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.model.TLV
import id.co.payment2go.terminalsdkhelper.core.util.Util
import org.junit.Test

class EMVTest {

    @Test
    fun replaceEmv() {
        val amount: Long = 10_000
        println(BytesUtil.byteArray2HexString(BytesUtil.toBCDAmountBytes(amount)))
    }

//    fun replaceTLVValue(emvData: String, tagToReplace: String, newValue: String): String {
//        val updatedTLVs = mutableListOf<TLV>()
//
//        for (tlv in tlvs) {
//            if (tlv.tag == tagToReplace) {
//                val updatedTLV = TLV(tlv.tag, tlv.length, newValue)
//                updatedTLVs.add(updatedTLV)
//            } else {
//                updatedTLVs.add(tlv)
//            }
//        }
//
//        return updatedTLVs.joinToString("") { constructTLV(it.tag, it.value) }
//    }

    data class TLV(val tag: String, val length: Int, val value: String)

    fun parseTLV(data: String): List<TLV> {
        val tlvs = mutableListOf<TLV>()
        var index = 0

        while (index < data.length) {
            val tag = data.substring(index, index + 2)
            index += 2

            val lengthHex = data.substring(index, index + 2)
            val length = lengthHex.toInt(16)
            index += 2

            val value = data.substring(index, index + length * 2)
            index += length * 2

            tlvs.add(TLV(tag, length, value))
        }

        return tlvs
    }

    fun constructTLV(tag: String, value: String): String {
        val length = value.length / 2
        return "$tag${String.format("%02X", length)}$value"
    }
}