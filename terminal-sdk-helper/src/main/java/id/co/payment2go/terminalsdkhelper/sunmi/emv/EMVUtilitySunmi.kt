package id.co.payment2go.terminalsdkhelper.sunmi.emv

import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2
import id.co.payment2go.terminalsdkhelper.common.emv.EMVUtility
import id.co.payment2go.terminalsdkhelper.common.emv.SearchCardException
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.sunmi.BindServiceSunmi
import id.co.payment2go.terminalsdkhelper.sunmi.emv.ktx.SearchCardResultSunmi
import id.co.payment2go.terminalsdkhelper.sunmi.emv.ktx.checkCardAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.TreeMap

class EMVUtilitySunmi(
    bindService: BindServiceSunmi,
) : EMVUtility {

    @Suppress("PrivatePropertyName")
    private val TAG = "EMVUtilitySunmi"

    private var mCardType = 0
    private var mAppSelect = 0
    private var authAmount: Long = 0

    private var cardReadOutput = CardReadOutput()

    private val emvOptV2 = bindService.emvOptV2
    private val readCardOptV2 = bindService.readCardOptV2

    override fun searchCard(stan: Long, amount: Long): Flow<Resource<CardReadOutput>> = flow {
        cardReadOutput = CardReadOutput()
        authAmount = amount
        cardReadOutput =
            cardReadOutput.copy(STAN = Util.addZerosToNumber(stan.toString(), 8))
        try {
            when (val result = readCardOptV2.checkCardAwait()) {
                is SearchCardResultSunmi.FindMagCard -> {
                    var track2 = result.bundle.getString("TRACK2") ?: ""
                    val track2IsOdd = track2.length % 2 != 0
                    if (track2IsOdd) {
                        track2 += "0"
                    }
                    track2 = track2.replace("=", "D")
//                    if (Util.isIcCard(track2)) {
//                        emit(Resource.Error("Silahkan masukkan kartu debit ber-chip Nasabah Anda."))
//                        return@flow
//                    }
                    val cardNumber = track2.substringBefore('D')

                    val expiryCard = Util.getExpiryFromTrack2Data(track2)
                    if (Util.isCardExpired(expiryCard)) {
                        emit(Resource.Error("Kartu debit Anda sudah melewati masa aktif kartu (Kadaluwarsa). Silahkan melakukan penggantian kartu debit di kantor cabang terdekat."))
                        return@flow
                    }
                    emit(
                        Resource.Success(
                            CardReadOutput(
                                cardNo = cardNumber,
                                track2Data = track2,
                                posEntryMode = Constant.POS_ENTRY_MODE_SWIPE
                            )
                        )
                    )
                }

                is SearchCardResultSunmi.FindICCard -> {
                    emit(Resource.Loading())
                    cardReadOutput = cardReadOutput.copy(
                        posEntryMode = Constant.POS_ENTRY_MODE_DIP
                    )
                    initEmv()
                    mCardType = AidlConstants.CardType.IC.value
                    transactProcess()
                    while (cardReadOutput.EMVData.isEmpty()) {
                        delay(100L)
                    }

                    val expiryCard = Util.getExpiryFromTrack2Data(cardReadOutput.track2Data)
                    if (Util.isCardExpired(expiryCard)) {
                        emit(Resource.Error("Kartu debit Anda sudah melewati masa aktif kartu (Kadaluwarsa). Silahkan melakukan penggantian kartu debit di kantor cabang terdekat."))
                        return@flow
                    }
                    emit(Resource.Success(cardReadOutput))
                }

                is SearchCardResultSunmi.FindRFCard -> {
                    searchCard(stan, amount).collect {
                        emit(it)
                    }
                }

                else -> {}
            }

        } catch (e: SearchCardException) {
            emit(Resource.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun startEMV(stan: Long, onFinish: (CardReadOutput?) -> Unit) {

    }

    private fun initEmv() {
        val configMap = EmvUtil.getConfig(EmvUtil.COUNTRY_INDONESIA)
        EmvUtil.initAidAndRid(emvOptV2)
        EmvUtil.setTerminalParam(
            configMap,
            emvOptV2
        )
        emvOptV2.initEmvProcess()
        initEmvTlvData()
    }

    private fun initEmvTlvData() {
        try {
            // set PayPass(MasterCard) tlv data
            val tagsPayPass = arrayOf(
                "DF8117", "DF8118", "DF8119", "DF811F", "DF811E", "DF812C",
                "DF8123", "DF8124", "DF8125", "DF8126",
                "DF811B", "DF811D", "DF8122", "DF8120", "DF8121"
            )
            val valuesPayPass = arrayOf(
                "E0", "F8", "F8", "E8", "00", "00",
                "000000000000", "000000100000", "999999999999", "000000100000",
                "30", "02", "0000000000", "000000000000", "000000000000"
            )
            emvOptV2.setTlvList(
                AidlConstants.EMV.TLVOpCode.OP_PAYPASS,
                tagsPayPass,
                valuesPayPass
            )
            emvOptV2.setTlvList(
                AidlConstants.EMV.TLVOpCode.OP_NORMAL,
                arrayOf("9F02"),
                arrayOf(Util.addZerosToNumber(authAmount.toString(), 12))
            )


            // set AMEX(AmericanExpress) tlv data
            val tagsAE =
                arrayOf("9F6D", "9F6E", "9F33", "9F35", "DF8168", "DF8167", "DF8169", "DF8170")
            val valuesAE = arrayOf("C0", "D8E00000", "E0E888", "22", "00", "00", "00", "60")
            emvOptV2.setTlvList(AidlConstants.EMV.TLVOpCode.OP_AE, tagsAE, valuesAE)
            val tagsJCB = arrayOf("9F53", "DF8161")
            val valuesJCB = arrayOf("708000", "7F00")
            emvOptV2.setTlvList(AidlConstants.EMV.TLVOpCode.OP_JCB, tagsJCB, valuesJCB)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun getTlvData() {
        try {
            val tagList = arrayOf(
                "DF02", "5F34", "9F06", "FF30", "FF31", "95", "9B", "9F36", "9F26",
                "9F27", "DF31", "9F1A", "9F33", "9F40",
                "9F03", "9F10", "9F37", "9C", "9A", "9F02", "5F2A", "82", "9F34", "9F1E",
                "84", "9F09", "5F20", "9F12", "5F1A", "5F24", "57", "5A"
            )
            val outData = ByteArray(2048)
            val map: MutableMap<String, TLV> =
                TreeMap()
            val tlvOpCode: Int = if (AidlConstants.CardType.NFC.value == mCardType) {
                when (mAppSelect) {
                    1 -> {
                        AidlConstants.EMV.TLVOpCode.OP_PAYWAVE
                    }

                    2 -> {
                        AidlConstants.EMV.TLVOpCode.OP_PAYPASS
                    }

                    else -> {
                        AidlConstants.EMV.TLVOpCode.OP_NORMAL
                    }
                }
            } else {
                AidlConstants.EMV.TLVOpCode.OP_NORMAL
            }
            val len: Int = emvOptV2.getTlvList(tlvOpCode, tagList, outData)
            if (len > 0) {
                val bytes = outData.copyOf(len)
                val hexStr: String = BytesUtil.byteArray2HexString(bytes)
                val tlvMap =
                    TLVUtil.buildTLVMap(
                        hexStr
                    )
                map.putAll(tlvMap)
            }

            // payPassTags
//            val payPassTags = arrayOf(
//                "DF811E", "DF812C", "DF8118", "DF8119", "DF811F", "DF8117", "DF8124",
//                "DF8125", "9F6D", "DF811B", "9F53", "DF810C", "9F1D", "DF8130", "DF812D",
//                "DF811C", "DF811D", "9F7C"
//            )
            //            len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, payPassTags, outData);
//            if (len > 0) {
//                byte[] bytes = Arrays.copyOf(outData, len);
//                String hexStr = ByteUtil.bytes2HexStr(bytes);
//                Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);
//                map.putAll(tlvMap);
//            }
            val emvData = StringBuilder()
            val keySet: Set<String> = map.keys
            for (key in keySet) {
                val tlv = map[key]
                if (tlv != null) {
                    val myTLV: id.co.payment2go.terminalsdkhelper.common.model.TLV =
                        id.co.payment2go.terminalsdkhelper.common.model.TLV.fromData(
                            key, BytesUtil.hexString2Bytes(tlv.value)
                        )
                    emvData.append(myTLV.toString())
                    if (key == "57") { // TRACK2DATA
                        cardReadOutput = cardReadOutput.copy(track2Data = tlv.value)
                    }
                    // We need maintain own Transaction Sequence Counter
                }
            }
            println("test data emv $emvData")
            cardReadOutput = cardReadOutput.copy(EMVData = emvData.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun transactProcess() {
        try {
            val bundle = Bundle()
            bundle.putString("amount", "0")
            bundle.putString("transType", "00")
            //flowType:0x01-emv standard, 0x04：NFC-Speedup
            //Note:(1) flowType=0x04 only valid for QPBOC,PayPass,PayWave contactless transaction
            //     (2) set fowType=0x04, only EMVListenerV2.onRequestShowPinPad(),
            //         EMVListenerV2.onCardDataExchangeComplete() and EMVListenerV2.onTransResult() may will be called.
            if (mCardType == AidlConstants.CardType.NFC.value) {
                bundle.putInt("flowType", AidlConstants.EMV.FlowType.TYPE_NFC_SPEEDUP)
            } else {
                bundle.putInt("flowType", AidlConstants.EMV.FlowType.TYPE_EMV_STANDARD)
            }
            bundle.putInt("cardType", mCardType)
            //            bundle.putBoolean("preProcessCompleted", false);
//            bundle.putInt("emvAuthLevel", 0);
            emvOptV2.transactProcessEx(bundle, object : EMVListenerV2.Stub() {
                /**
                 * Notify client to do multi App selection, this method may called when card have more than one Application
                 * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
                 * method will not be called
                 *
                 * @param appIdList   The App list for selection
                 * @param isFirstSelect is first time selection
                 */
                override fun onWaitAppSelect(
                    appIdList: MutableList<EMVCandidateV2>,
                    isFirstSelect: Boolean
                ) {
                    Log.d(
                        TAG, "onWaitAppSelect: ${
                            appIdList.map {
                                "----------------- " +
                                        "application ID: ${it.aid} " +
                                        "application name: ${it.appName} " +
                                        "application label: ${it.appLabel} " +
                                        "-----------------"
                            }
                        }"
                    )
                    emvOptV2.importAppSelect(0) // NSICCS
                }

                /**
                 * Notify client the final selected Application
                 * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
                 * method will not be called
                 *
                 * @param tag9F06Value The final selected Application id
                 */
                override fun onAppFinalSelect(tag9F06Value: String) {
                    Log.d(TAG, "onAppFinalSelect: $tag9F06Value")
                    emvOptV2.importAppFinalSelectStatus(0)
                }

                /**
                 * Notify client to confirm card number
                 * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
                 * method will not be called
                 *
                 * @param cardNo The card number
                 */
                override fun onConfirmCardNo(cardNo: String) {
                    Log.d(TAG, "onConfirmCardNo: $cardNo")
                    cardReadOutput = cardReadOutput.copy(cardNo = cardNo)
                    emvOptV2.importCardNoStatus(0)
                }

                override fun onRequestShowPinPad(pinType: Int, remainTime: Int) {
                    Log.d(TAG, "onRequestShowPinPad: pinType: $pinType remainTime: $remainTime")
                    emvOptV2.importPinInputStatus(pinType, 0)
                }

                override fun onRequestSignature() {
                    emvOptV2.importSignatureStatus(0)
                }

                override fun onCertVerify(certType: Int, certInfo: String) {
                    Log.d(TAG, "onCertVerify: certType: $certType certInfo: $certInfo")
                    emvOptV2.importCertStatus(0)
                }

                override fun onOnlineProc() {
                    getTlvData()
                }

                override fun onCardDataExchangeComplete() {
//                    TODO("Not yet implemented")
                }

                override fun onTransResult(p0: Int, p1: String?) {
//                    TODO("Not yet implemented")
                }

                override fun onConfirmationCodeVerified() {
//                    TODO("Not yet implemented")
                }

                override fun onRequestDataExchange(p0: String?) {
//                    TODO("Not yet implemented")
                }

                override fun onTermRiskManagement() {
//                    TODO("Not yet implemented")
                }

                override fun onPreFirstGenAC() {
//                    TODO("Not yet implemented")
                }

                override fun onDataStorageProc(
                    p0: Array<out String>?,
                    p1: Array<out String>?
                ) {
//                    TODO("Not yet implemented")
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "transactProcess: error = $e")
        }
    }

    override fun stopEMVSearch() {
        emvOptV2.clearData(0)
        readCardOptV2.cancelCheckCard()
    }
}