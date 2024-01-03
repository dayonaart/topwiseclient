@file:Suppress("SpellCheckingInspection")

package id.co.payment2go.terminalsdkhelper.topwise.emv.aid

import com.topwise.cloudpos.aidl.emv.level2.AidlEmvL2
import com.topwise.cloudpos.struct.BytesUtil
import com.topwise.toptool.api.convert.IConvert
import com.topwise.toptool.api.packer.IPacker
import com.topwise.toptool.api.packer.ITlv.ITlvDataObj
import com.topwise.toptool.api.packer.ITlv.ITlvDataObjList
import com.topwise.toptool.api.packer.TlvException
import com.topwise.toptool.api.utils.AppLog
import okhttp3.internal.and

fun installAidTerminal(iPacker: IPacker, iConvert: IConvert, emvManager: AidlEmvL2) {
    val aidHarcode = listOf(
        /** MIR */
        "9F0607A0000006581010DF0101009F08020002DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        /** RUPAY */
        "9F0607A0000005241010DF0101009F08020002DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        /** PURE */
        "9F0605A000000736DF810C0112DF0101009F08020002DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        /** VISA */
        "9F0607A0000000031010DF0101009F08020096DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000000000DF2006000999999999DF2106000000500000",
        "9F0607A0000000032010DF0101009F08020096DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        "9F0607A0000000033010DF0101009F08020096DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        /** VISA MASTERCARD */
        "9F0607A0000000041010DF0101009F08020002DF1105FC5080A000DF1205F85080F800DF130504000000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        "9F0607A0000000043060DF0101009F08020002DF1105FC5058A000DF1205F85058F800DF130504000000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180101DF1906000000030000DF2006000999999999DF2106000000500000",
        /** JCB MASTERCARD */
        "9F0607A0000000651010DF0101009F08020200DF1105FC6024A800DF1205FC60ACF800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        /** JCB CUPI */
        "9F0608A000000333010101DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000500000",
        "9F0608A000000333010102DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000500000",
        "9F0608A000000333010103DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000500000",
        "9F0608A000000333010106DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000500000",
        "9F0607A0000003330101DF0101019F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000500000",
        /** CUPI */
        "9F0606A00000002501DF0101009F08020002DF1105FC5080A000DF1205F85080F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        /** DINERS CLUB */
        "9F0607A0000001523010DF010100DF11050000000000DF12050000000000DF13050000000000DF14039F3704DF150400000000DF160100DF1701009F1B04000050009F08020001DF180100DF1906000000015000DF2006000000020000DF2106000000050000",
        "9F0607A0000005241010DF0101009F08020140DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        "9F0607A0000000031010DF0101009F08020140DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        "9F0607A0000000032010DF0101009F08020140DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        "9F0607A0000000033010DF0101009F08020140DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        "9F0607A0000000041010DF0101009F08020002DF1105FC5080A000DF1205F85080F800DF130504000000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        "9F0607A0000000043060DF0101009F08020002DF1105FC5058A000DF1205F85058F800DF130504000000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180101DF1906000000030000DF2006000999999999DF2106000000500000",
        "9F0607A0000000651010DF0101009F08020200DF1105FC6024A800DF1205FC60ACF800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        "9F0608A000000333010101DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000500000",
        "9F0608A000000333010102DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000500000",
        "9F0608A000000333010103DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000500000",
        "9F0608A000000333010106DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000500000",
        "9F0607A0000003330101DF0101019F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000500000",
        "9F0606A00000002501DF0101009F08020002DF1105FC5080A000DF1205F85080F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100DF1906000000030000DF2006000999999999DF2106000000500000",
        /** DISCOVER CARD 2 */
        "9F0607A0000001523010DF010100DF11050000000000DF12050000000000DF13050000000000DF14039F3704DF150400000000DF160100DF1701009F1B04000050009F08020001DF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        /** VISA 4 */
        "9F0607A0000000038010DF010100DF1105DC4000A800DF1205DC4004F800DF13050010000000DF14039F3704DF150400000000DF160100DF1701009F1B04000000009F0802008CDF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        /** MASTER 3 */
        "9F0607A0000000046000DF010100DF1105FC50ACA000DF1205F850ACF800DF13050400000000DF14039F3704DF150400000000DF160100DF1701009F1B04000000009F08020002DF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        /** MASTER 4 */
        "9F0607A0000000046010DF010100DF1105FC50ACA000DF1205F850ACF800DF13050400000000DF14039F3704DF150400000000DF160100DF1701009F1B04000000009F08020002DF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        /** MASTER 5 */
        "9F0607A0000000042203DF010100DF1105FC50ACA000DF1205F850ACF800DF13050400000000DF14039F3704DF150400000000DF160100DF1701009F1B04000027109F08020002DF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        "9F0607A0000000101030DF010100DF1105FC50ACA000DF1205F850ACF800DF13050400000000DF14039F3704DF150400000000DF160100DF1701009F1B04000027109F08020002DF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        /** DISCOVER CARD 1 */
        "9F0607A0000003241010DF010100DF11050000000000DF12050000000000DF13050000000000DF14039F3704DF150400000000DF160100DF1701009F1B04000050009F08020001DF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        /** DISCOVER CARD 3 */
        "9F0607A0000001524010DF010100DF11050000000000DF12050000000000DF13050000000000DF14039F3704DF150400000000DF160100DF1701009F1B04000050009F08020001DF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        /** MCCS */
        "9F0607A0000006151010DF010100DF11050000000000DF12050000000000DF13050000000000DF14039F3704DF150400000000DF160100DF1701009F1B04000050009F08020001DF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        /** INTERAC */
        "9F0607A0000002771010DF010100DF1105DC4000A800DF1205DC4004F800DF13050010000000DF14039F3704DF150400000000DF160100DF1701009F1B04000050009F08020001DF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        /** EMVTEST */
        "9F0607A0000000999090DF010100DF1105D84000A800DF1205D84004F800DF13050010000000DF14039F3704DF150400000000DF160100DF1701009F1B04000050009F0802008CDF180100DF1906000000015000DF2006000000020000DF2106000000010000",
        /** NSICC */
        "9F0607A0000006021010DF010100DF1105FC6024A800DF1205FC60ACF800DF13050000000000DF14039F3704DF150400000000DF160100DF1701009F1B04000050009F08020001DF180100DF1906000000015000DF2006000000020000DF2106000000010000"
    )
    val aidList: MutableList<EmvAidParam?> = ArrayList()
    for (aid in aidHarcode) {
        AppLog.d("TAG", "Aid==$aid")
        aidList.add(saveAid(aid, iPacker, iConvert))
    }

    emvManager.EMV_DelAllAIDList()
    for (aid in aidList) {
        AppLog.d("TAG", "init AddAIDList EmvAidParam: " + aid?.aid)
        val aucAid: ByteArray = BytesUtil.hexString2Bytes(aid?.aid)
        emvManager.EMV_AddAIDList(aucAid, aucAid.size.toByte(), 1.toByte())
    }
}

private fun saveAid(aid: String, iPacker: IPacker, iConvert: IConvert): EmvAidParam? {
    val tlv = iPacker.tlv
    val aidTlvList: ITlvDataObjList
    var tlvDataObj: ITlvDataObj?
    val aidParam: EmvAidParam

    var value: ByteArray?
    val bytes = iConvert.strToBcd(aid, IConvert.EPaddingPosition.PADDING_LEFT)
    return try {
        aidTlvList = tlv.unpack(bytes)
        aidParam = EmvAidParam()
        // 9f06 AID
        tlvDataObj = aidTlvList.getByTag(0x9f06)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.aid = iConvert.bcdToStr(value)
            }
        }
        // DF810C Emv RF kernel ID
        tlvDataObj = aidTlvList.getByTag(0xDF810C)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.kernelID = iConvert.bcdToStr(value)
            }
        }
        // DF01
        tlvDataObj = aidTlvList.getByTag(0xDF01)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.selFlag = value[0].toInt()
            }
        }
        // 9F08
        tlvDataObj = aidTlvList.getByTag(0x9f08)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.version = iConvert.bcdToStr(value)
            }
        }

        // DF11
        tlvDataObj = aidTlvList.getByTag(0xDF11)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.tbcDefualt = iConvert.bcdToStr(value)
            }
        }

        // DF12
        tlvDataObj = aidTlvList.getByTag(0xDF12)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.tacOnline = iConvert.bcdToStr(value)
            }
        }

        // DF13
        tlvDataObj = aidTlvList.getByTag(0xDF13)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.tacDenial = iConvert.bcdToStr(value)
            }
        }

        // 9F1B
        tlvDataObj = aidTlvList.getByTag(0x9F1B)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.floorLimit = iConvert.bcdToStr(value).toLong()
                aidParam.floorlimitCheck = 1
            }
        }

        // DF15
        tlvDataObj = aidTlvList.getByTag(0xDF15)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.threshold = iConvert.bcdToStr(value).toLong()
            }
        }

        // DF16
        tlvDataObj = aidTlvList.getByTag(0xDF16)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.maxTargetPer = iConvert.bcdToStr(value).toInt()
            }
        }

        // DF17
        tlvDataObj = aidTlvList.getByTag(0xDF17)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.targetPer = iConvert.bcdToStr(value).toInt()
            }
        }

        // DF14
        tlvDataObj = aidTlvList.getByTag(0xDF14)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.setdDOL(iConvert.bcdToStr(value))
            }
        }

        // DF18
        tlvDataObj = aidTlvList.getByTag(0xDF18)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.onlinePin = value[0] and 0x01
            }
        }

        // 9F7B
        tlvDataObj = aidTlvList.getByTag(0x9F7B)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.ecTTLVal = iConvert.bcdToStr(value).toLong()
                aidParam.ecTTLFlg = 1
            }
        }

        // DF19
        tlvDataObj = aidTlvList.getByTag(0xDF19)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.rdClssFLmt = iConvert.bcdToStr(value).toLong()
                aidParam.rdClssFLmtFlg = 1
            }
        }

        // DF20
        tlvDataObj = aidTlvList.getByTag(0xDF20)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.rdClssTxnLmt = iConvert.bcdToStr(value).toLong()
                aidParam.rdClssTxnLmtFlg = 1
            }
        }

        // DF21
        tlvDataObj = aidTlvList.getByTag(0xDF21)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.rdCVMLmt = iConvert.bcdToStr(value).toLong()
                aidParam.rdCVMLmtFlg = 1
            }
        }

        //DF8102 tDol
        tlvDataObj = aidTlvList.getByTag(0xDF8102)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.settDOL(iConvert.bcdToStr(value))
            }
        }
        //9F1D riskManData
        tlvDataObj = aidTlvList.getByTag(0x9F1D)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.riskmanData = iConvert.bcdToStr(value)
            }
        }
        //9F01 s acquierId
        tlvDataObj = aidTlvList.getByTag(0x9F01)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.acquierId = iConvert.bcdToStr(value)
            }
        }
        //9F4E s merchName
        tlvDataObj = aidTlvList.getByTag(0x9F4E)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.merchName = iConvert.bcdToStr(value)
            }
        }
        //9F15 s merchCateCode
        tlvDataObj = aidTlvList.getByTag(0x9F15)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.merchCateCode = iConvert.bcdToStr(value)
            }
        }
        //9F16 s merchId
        tlvDataObj = aidTlvList.getByTag(0x9F16)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.merchId = iConvert.bcdToStr(value)
            }
        }
        //9F1C s termId
        tlvDataObj = aidTlvList.getByTag(0x9F1C)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.termId = iConvert.bcdToStr(value)
            }
        }
        //5F2A s transCurrCode
        tlvDataObj = aidTlvList.getByTag(0x5F2A)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.transCurrCode = iConvert.bcdToStr(value)
            }
        }
        //5F36 i transCurrExp
        tlvDataObj = aidTlvList.getByTag(0xDF8101)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.transCurrExp = Integer.valueOf(iConvert.bcdToStr(value))
            }
        }
        //9F3C s referCurrCode
        tlvDataObj = aidTlvList.getByTag(0x9F3C)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.referCurrCode = iConvert.bcdToStr(value)
            }
        }
        //9F3D byte referCurrExp
        tlvDataObj = aidTlvList.getByTag(0x9F3D)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.referCurrExp = Integer.valueOf(iConvert.bcdToStr(value))
            }
        }
        //DF8101 int referCurrCon
        tlvDataObj = aidTlvList.getByTag(0xDF8101)
        if (tlvDataObj != null) {
            value = tlvDataObj.value
            if (value != null && value.isNotEmpty()) {
                aidParam.referCurrCon = Integer.valueOf(iConvert.bcdToStr(value))
            }
        }
        AppLog.e("TAG", "mUAidDaoUtils uAid  ==$aidParam")
        aidParam
    } catch (e: TlvException) {
        e.printStackTrace()
        AppLog.e("TAG", "mUAidDaoUtils TlvException ==")
        return null
    }
}

