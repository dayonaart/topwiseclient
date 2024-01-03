package id.co.payment2go.terminalsdkhelper.verifone.emv

import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.util.SparseArray
import com.vfi.smartpos.deviceservice.aidl.OnlineResultHandler
import com.vfi.smartpos.deviceservice.constdefine.ConstCheckCardListener
import com.vfi.smartpos.deviceservice.constdefine.ConstIPBOC
import com.vfi.smartpos.deviceservice.constdefine.ConstOnlineResultHandler
import com.vfi.smartpos.deviceservice.constdefine.ConstPBOCHandler
import id.co.payment2go.terminalsdkhelper.common.emv.EMVUtility
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.szzt.utils.SzztEmVTag
import id.co.payment2go.terminalsdkhelper.verifone.BindServiceVerifone
import id.co.payment2go.terminalsdkhelper.verifone.emv.ktx.EMVStartResult
import id.co.payment2go.terminalsdkhelper.verifone.emv.ktx.EmvVerifoneResult
import id.co.payment2go.terminalsdkhelper.verifone.emv.ktx.emvStart
import id.co.payment2go.terminalsdkhelper.verifone.emv.ktx.searchCard
import id.co.payment2go.terminalsdkhelper.verifone.emv.set_param.EmvParamInit
import id.co.payment2go.terminalsdkhelper.verifone.emv.tlv.EMVTLVParam
import id.co.payment2go.terminalsdkhelper.verifone.emv.tlv.Utility
import id.co.payment2go.terminalsdkhelper.verifone.emv.tlv.caseA.ISO8583u
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Locale

class EMVUtilityVerifone(bindService: BindServiceVerifone) : EMVUtility {

    private val emv = bindService.emv
    private val beeper = bindService.iBeeper
    private var isoResponse: ISO8583u? = ISO8583u()
    private var authAmount: Long = 0
    private var cardReadOutput = CardReadOutput()
    private var tagOfF55 = SparseArray<String>()
    private var iDevice = bindService.idevice


    override fun searchCard(stan: Long, amount: Long): Flow<Resource<CardReadOutput>> {
        return flow {
            val cardOption = Bundle()
            cardOption.putBoolean(
                ConstIPBOC.checkCard.cardOption.KEY_Contactless_boolean,
                ConstIPBOC.checkCard.cardOption.VALUE_supported
            )
            cardOption.putBoolean(
                ConstIPBOC.checkCard.cardOption.KEY_SmartCard_boolean,
                ConstIPBOC.checkCard.cardOption.VALUE_supported
            )
            cardOption.putBoolean(
                ConstIPBOC.checkCard.cardOption.KEY_MagneticCard_boolean,
                ConstIPBOC.checkCard.cardOption.VALUE_supported
            )
            val timeoutEmv = 60
            when (val cardData = emv.searchCard(cardOption, timeoutEmv)) {

                /** IC Card Section */
                EmvVerifoneResult.CardInsert -> {
                    emit(Resource.Loading())

                    // Initialize AID and CAPK data
                    EmvParamInit.installEmvParam(emv)

                    authAmount = amount
                    emv.stopCheckCard()
                    emv.abortEMV()
                    beeper.startBeep(200)

                    var resultCardOutput: CardReadOutput? = null
                    startEMV(stan) { cardReadOutput ->
                        if (cardReadOutput == null) return@startEMV
                        resultCardOutput = cardReadOutput
                        resultCardOutput.let {
                            it?.posEntryMode = Constant.POS_ENTRY_MODE_DIP
                        }
                    }

                    while (resultCardOutput == null) {
                        delay(100L)
                    }
                    if (Util.isCardExpired(resultCardOutput!!.cardExpiry)) {
                        emit(Resource.Error("Kartu debit Anda sudah melewati masa aktif kartu (Kadaluwarsa). Silahkan melakukan penggantian kartu debit di kantor cabang terdekat."))
                        return@flow
                    }

                    Log.i("Success", "searchCard: $resultCardOutput")
                    emit(Resource.Success(resultCardOutput!!))
                }

                /** Magnetic Card Section */
                is EmvVerifoneResult.CardSwiped -> {
                    emit(Resource.Loading())
                    emv.stopCheckCard()
                    beeper.startBeep(200)

                    val track = cardData.bundle
                    Log.i("CardSwiped", "all bean: $track")

                    val pan: String =
                        track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_PAN_String)
                            ?: ""
                    Log.i("CardSwiped", "pan: $pan")

                    var track2: String =
                        track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_TRACK2_String)
                            ?: ""
                    Log.i("CardSwiped", "track2: $track2")

                    val track1: String =
                        track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_TRACK1_String)
                            ?: ""
                    Log.i("CardSwiped", "track1: $track1")

                    val track3: String =
                        track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_TRACK3_String)
                            ?: ""
                    Log.i("CardSwiped", "track3: $track3")

                    val serviceCode: String =
                        track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_SERVICE_CODE_String)
                            ?: ""
                    Log.i("CardSwiped", "serviceCode: $serviceCode")

                    val validDate: String =
                        track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_EXPIRED_DATE_String)
                            ?: ""
                    Log.i("CardSwiped", "validDate: $validDate")

                    val track2IsOdd = track2.length % 2 != 0
                    if (track2IsOdd) {
                        track2 += "0"
                    }

                    track2 = track2.replace("=", "D")
                    if (Util.isIcCard(track2)) {
                        emit(Resource.Error("Silahkan masukkan kartu debit ber-chip Nasabah Anda."))
                        return@flow
                    }

                    val expiryCard = Util.getExpiryFromTrack2Data(track2)
                    if (Util.isCardExpired(expiryCard)) {
                        emit(Resource.Error("Kartu debit Anda sudah melewati masa aktif kartu (Kadaluwarsa). Silahkan melakukan penggantian kartu debit di kantor cabang terdekat."))
                        return@flow
                    }

                    Log.i("CardSwiped", "improved track2: $track2")

                    emit(
                        Resource.Success(
                            CardReadOutput(
                                cardNo = pan,
                                track2Data = track2,
                                posEntryMode = Constant.POS_ENTRY_MODE_SWIPE
                            )
                        )
                    )
                }

                /** Error section */
                is EmvVerifoneResult.Error -> {
                    emit(Resource.Error(message = cardData.errMsg))
                }

                /** RF/Contactless Card Section */
                EmvVerifoneResult.CardPass -> {
                    searchCard(stan, amount).collect {
                        emit(it)
                    }
                }

                /** Timeout Section */
                EmvVerifoneResult.Timeout -> {
                    searchCard(stan, amount).collect {
                        emit(it)
                    }
                }
            }
        }.flowOn(Dispatchers.IO)

    }

    override suspend fun startEMV(stan: Long, onFinish: (CardReadOutput?) -> Unit) {
        Log.i("Init EMV", "Initialize EMV for Configuration Data Process...")

        try {
            emv.emvStart(
                ConstIPBOC.startEMV.processType.full_process,
                emvSetIntent()
            ) { cardResult ->
                when (cardResult) {

                    /** process to requesting amount */
                    EMVStartResult.OnRequestAmount -> {
                        Log.i("OnRequestAmount", "requesting amount...")
                    }

                    /** Process to select candidate as index */
                    is EMVStartResult.OnSelectApplication -> {

                        if (cardResult.appList != null) {
                            Log.i("OnSelectApplication", "Scanning Card...")

                            val applicationListResult = cardResult.appList
                            Log.i(
                                "OnSelectApplication",
                                "Application Result : $applicationListResult"
                            )

                            if (cardResult.appList.size >= 1) {
                                Log.i("OnSelectApplication", "Multiple AID Detected..")
                            }


                            /** Selecting Aid as Priority */
                            var priority = 0
                            for (i in applicationListResult.indices) {
                                if (applicationListResult[i].getString("aidName")
                                        .equals("NSICCS")
                                ) {
                                    Log.i(
                                        "OnSelectApplication",
                                        "Select NSICCS -- priority : ${
                                            applicationListResult[i].getInt("aidPriority")
                                        }, aidName : ${applicationListResult[i].getString("aidName")}, aidNo : ${
                                            applicationListResult[i].getString(
                                                "aid"
                                            )
                                        }"
                                    )
                                    priority = applicationListResult[i].getInt("aidPriority")
                                    cardReadOutput.cardAID =
                                        applicationListResult[i].getString("aid").toString()
                                    cardReadOutput.cardAppName =
                                        applicationListResult[i].getString("aidName").toString()
                                    break
                                }
                            }
                            Log.i("OnSelectApplication", "check priority card : $priority")
                            emv.importAppSelection(priority)
                        }
                    }

                    /** Process to get basic data from card */
                    is EMVStartResult.OnConfirmCardInfo -> {
                        Log.i("OnConfirmCardInfo", "Getting Card Information...")

                        val panNumberSaved =
                            cardResult.info?.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_PAN_String)
                                ?: ""
                        Log.i("OnConfirmCardInfo", "Pan Number : $panNumberSaved")

                        val t2Data =
                            cardResult.info?.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_TRACK2_String)
                                ?: ""
                        Log.i("OnConfirmCardInfo", "Track 2 Data : $t2Data")

                        val cardSN =
                            cardResult.info?.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_CARD_SN_String)
                                ?: ""
                        Log.i("OnConfirmCardInfo", "Card Serial Number : $cardSN")

                        val cardServiceCode =
                            cardResult.info?.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_SERVICE_CODE_String)
                                ?: ""
                        Log.i("OnConfirmCardInfo", "Card Service Code : $cardServiceCode")

                        val cardExpDate =
                            cardResult.info?.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_EXPIRED_DATE_String)
                                ?: ""
                        Log.i("OnConfirmCardInfo", "Card Expired Date : $cardExpDate")

                        cardReadOutput.let {
                            it.cardNo = panNumberSaved
                            it.track2Data = t2Data
                            it.cardExpiry = cardExpDate
                        }
                        emv.importCardConfirmResult(ConstIPBOC.importCardConfirmResult.pass.allowed)

                    }

                    /** Process getting result from pin to store in emv */
                    is EMVStartResult.OnRequestInputPIN -> {
                        // Just Log, Pin result will be requested after emv process
                        Log.i("OnRequestInputPIN", "Request Input Pin...")
                        Log.i("OnRequestInputPIN", "is online : ${cardResult.isOnlinePin}")
                        Log.i("OnRequestInputPIN", "retry times : ${cardResult.retryTimes}")
                        emv.importPin(1, Utility.hexStr2ByteV2("FB262473DC976A42"))

                    }

                    /** Process to collect information card */
                    is EMVStartResult.OnConfirmCertInfo -> {
                        Log.i("OnConfirmCertInfo", "Requesting Card Certification Data...")
                        Log.i("OnConfirmCertInfo", "Certification Type : ${cardResult.certType}")
                        Log.i(
                            "OnConfirmCertInfo",
                            "Certification Information : ${cardResult.certInfo}"
                        )
                        emv.importCertConfirmResult(ConstIPBOC.importCertConfirmResult.option.CONFIRM)
                    }

                    /** Process to Getting TLV */
                    is EMVStartResult.OnRequestOnlineProcess -> {
                        // Process getting TLV in State OnRequestOnlineProcess
                        Log.i("onRequestOnlineProcess", "onRequestOnlineProcess...")

                        val result =
                            cardResult.aaResult!!.getInt(ConstPBOCHandler.onRequestOnlineProcess.aaResult.KEY_RESULT_int)
                        Log.i("onRequestOnlineProcess", "onRequestOnlineProcess result = $result")

                        when (result) {
                            ConstPBOCHandler.onRequestOnlineProcess.aaResult.VALUE_RESULT_AARESULT_ARQC -> {
                                Log.i(
                                    "onRequestOnlineProcess", "VALUE_RESULT_AARESULT_ARQC : ${
                                        cardResult.aaResult.getString(
                                            ConstPBOCHandler.onRequestOnlineProcess.aaResult.KEY_ARQC_DATA_String
                                        )
                                    }"
                                )
                            }

                            ConstPBOCHandler.onRequestOnlineProcess.aaResult.VALUE_RESULT_QPBOC_ARQC -> {
                                Log.i(
                                    "onRequestOnlineProcess", "VALUE_RESULT_QPBOC_ARQC : ${
                                        cardResult.aaResult.getString(
                                            ConstPBOCHandler.onRequestOnlineProcess.aaResult.KEY_ARQC_DATA_String
                                        )
                                    }"
                                )
                            }

                            ConstPBOCHandler.onRequestOnlineProcess.aaResult.VALUE_RESULT_PAYPASS_EMV_ARQC -> {
                                Log.i(
                                    "onRequestOnlineProcess", "VALUE_RESULT_PAYPASS_EMV_ARQC : ${
                                        cardResult.aaResult.getString(
                                            ConstPBOCHandler.onRequestOnlineProcess.aaResult.KEY_ARQC_DATA_String
                                        )
                                    }"
                                )
                            }
                        }

                        var tlv: ByteArray?

                        val tagList = intArrayOf(
                            SzztEmVTag.cryptogramInformationData,
                            SzztEmVTag.amountAuthorisedNumeric,
                            SzztEmVTag.amountOtherNumeric,
                            SzztEmVTag.applicationCryptogram,
                            SzztEmVTag.applicationInterchangeProfile,
                            SzztEmVTag.applicationTransactionCounter,
                            SzztEmVTag.issuerApplicationData,
                            SzztEmVTag.terminalCapabilities,
                            SzztEmVTag.transactionCurrencyCode,
                            SzztEmVTag.terminalCountryCode,
                            SzztEmVTag.terminalVerificationResults,
                            SzztEmVTag.transactionDate,
                            SzztEmVTag.transactionType,
                            SzztEmVTag.unpredictableNumber,
                            SzztEmVTag.cardholderVerificationMethodResults,
                            SzztEmVTag.applicationPrimaryAccountNumberSequenceNumber,
                            SzztEmVTag.dedicatedFileName,
                            SzztEmVTag.applicationVersionNumber,
                            SzztEmVTag.interfaceDeviceSerialNumber,
                            SzztEmVTag.terminalType,
                            SzztEmVTag.unknown,
                            SzztEmVTag.cardholderName,
                            SzztEmVTag.t2Data,
                            SzztEmVTag.applicationPrimaryAccountNumber,
                        )

                        for (tag in tagList) {
                            tlv = emv.getCardData(Integer.toHexString(tag).uppercase(Locale.ROOT))
                            if (null != tlv && tlv.isNotEmpty()) {
                                Log.i(
                                    "onRequestOnlineProcess",
                                    "TLV $tag = ${Utility.byte2HexStr(tlv)}"
                                )
                                tagOfF55.put(tag, Utility.byte2HexStrV2(tlv))
                                println("test data tagOfF55 item $tagOfF55")
                            } else {
                                Log.e(
                                    "onRequestOnlineProcess",
                                    "getCardData:" + Integer.toHexString(tag) + ", fails"
                                )
                            }
                        }

                        println("test data tagOfF55 $tagOfF55")

                        val buffer = StringBuffer()
                        for (i in 0 until tagOfF55.size()) {
                            val tag: Int = tagOfF55.keyAt(i)
                            val value: String? = tagOfF55.valueAt(i)
                            val tagBufferValue = StringBuffer()
                            println("test data tag $tag, value $value")
                            println(
                                "test data tagV2 ${
                                    Integer.toHexString(tag).uppercase(Locale.ROOT)
                                }, value $value"
                            )
                            val convertTag = Integer.toHexString(tag).uppercase(Locale.ROOT)
                            if (value != null) {
                                /**
                                 * Changing Value TLV Condition from Tag
                                 * can actually change it from the AID param (only for a constant value),
                                 * but because there are certain conditions
                                 * so that the value can change every device use this condition for the solution
                                 */
                                when (convertTag) {
                                    "9F1E" -> {
                                        // Adding in 9F1E Interface Device (IFD) Serial Number with version number (V9 or etc..)
                                        val getVersionDevice = BytesUtil.toHex(
                                            iDevice.deviceInfo.serialNo.substring(
                                                0,
                                                2
                                            )
                                        )
                                        println("test data getVersionDevice $getVersionDevice")
                                        tagBufferValue.append(getVersionDevice)
                                        tagBufferValue.append(value)
                                        println("test data tagBufferValue $tagBufferValue")
                                        val tmp: ByteArray? =
                                            appendF55(tag, tagBufferValue.toString())
                                        buffer.append(Utility.byte2HexStrV2(tmp))
                                    }

                                    else -> {
                                        val tmp: ByteArray? = appendF55(tag, value)
                                        println("value is not null , tmp is $tmp")
                                        buffer.append(Utility.byte2HexStrV2(tmp))
                                        println("value is not null , buffer is $buffer")
                                    }
                                }
                            }
                        }
                        tagOfF55 = SparseArray()

                        println("buffer is $buffer")

                        setConfigTLV(buffer)

                        val f55 = buffer.toString()
                        Log.i("OnRequestOnlineProcess", "Bit55 has been generated : $f55")

                        // import the online result
                        val onlineResult = Bundle()
                        onlineResult.putBoolean(
                            ConstIPBOC.inputOnlineResult.onlineResult.KEY_isOnline_boolean,
                            true
                        )
                        if (isoResponse!!.unpackValidField[ISO8583u.F_ResponseCode_39]) {
                            onlineResult.putString(
                                ConstIPBOC.inputOnlineResult.onlineResult.KEY_field55_String,
                                isoResponse!!.getUnpack(ISO8583u.F_ResponseCode_39)
                            )
                        } else {
                            onlineResult.putString(
                                ConstIPBOC.inputOnlineResult.onlineResult.KEY_respCode_String,
                                "00"
                            )
                        }

                        if (isoResponse!!.unpackValidField[ISO8583u.F_AuthorizationIdentificationResponseCode_38]) {
                            onlineResult.putString(
                                ConstIPBOC.inputOnlineResult.onlineResult.KEY_authCode_String,
                                isoResponse!!.getUnpack(ISO8583u.F_AuthorizationIdentificationResponseCode_38)
                            )
                        } else {
                            onlineResult.putString(
                                ConstIPBOC.inputOnlineResult.onlineResult.KEY_authCode_String,
                                "123456"
                            )
                        }

                        if (isoResponse!!.unpackValidField[55]) {
                            onlineResult.putString(
                                ConstIPBOC.inputOnlineResult.onlineResult.KEY_field55_String,
                                isoResponse!!.getUnpack(55)
                            )
                        } else {
                            onlineResult.putString(
                                ConstIPBOC.inputOnlineResult.onlineResult.KEY_field55_String,
                                "5F3401019F3303E0F9C8950500000000009F1A0201569A039707039F3704F965E43082027C009F3602041C9F260805142531F709C8669C01009F02060000000000125F2A0201569F101307010103A02000010A01000000000063213EC29F2701809F1E0831323334353637389F0306000000000000"
                            )
                        }

                        emv.inputOnlineResult(
                            onlineResult,
                            object : OnlineResultHandler.Stub() {

                                @Throws(RemoteException::class)
                                override fun onProccessResult(
                                    result: Int,
                                    data: Bundle
                                ) {
                                    Log.i(
                                        "onProccessResult",
                                        "onProccessResult: getResult : $result"
                                    )
                                    Log.i(
                                        "onProccessResult",
                                        "onProccessResult: TC Data : ${
                                            data.getString(
                                                ConstOnlineResultHandler.onProccessResult.data.KEY_TC_DATA_String,
                                                "not defined"
                                            )
                                        }"
                                    )
                                    Log.i(
                                        "onProccessResult",
                                        "onProccessResult: Script Data : ${
                                            data.getString(
                                                ConstOnlineResultHandler.onProccessResult.data.KEY_SCRIPT_DATA_String,
                                                "not defined"
                                            )
                                        }"
                                    )
                                    Log.i(
                                        "onProccessResult",
                                        "onProccessResult: Reversal Data : ${
                                            data.getString(
                                                ConstOnlineResultHandler.onProccessResult.data.KEY_REVERSAL_DATA_String,
                                                "not defined"
                                            )
                                        }"
                                    )
                                    when (result) {
                                        ConstOnlineResultHandler.onProccessResult.result.TC -> Log.i(
                                            "onProccessResult",
                                            "ConstOnlineResultHandler.onProccessResult.result.TC is TC"
                                        )

                                        ConstOnlineResultHandler.onProccessResult.result.Online_AAC -> Log.i(
                                            "onProccessResult",
                                            "ConstOnlineResultHandler.onProccessResult.result.Online_AAC is Online AAC"
                                        )

                                        else -> Log.i(
                                            "onProccessResult",
                                            "Error code : $result"
                                        )
                                    }
                                }
                            })
                    }

                    /** Completion Section / Final result emv */
                    is EMVStartResult.OnTransactionResult -> {
                        Log.i("OnTransactionResult", "requesting transaction result...")
                        val msg: String? = cardResult.bundle!!.getString("ERROR")
                        Log.i(
                            "OnTransactionResult",
                            "onTransactionResult result = ${cardResult.result}, msg = $msg"
                        )

                        when (cardResult.result) {
                            ConstPBOCHandler.onTransactionResult.result.AARESULT_AAC -> {
                                onFinish(cardReadOutput)
                                Log.i("OnTransactionResult", "AARESULT_AAC : $cardReadOutput")
                            }

                            ConstPBOCHandler.onTransactionResult.result.EMV_CARD_BIN_CHECK_FAIL -> {
                                // read card fail
                                Log.i("OnTransactionResult", "read card fail")
                                return@emvStart
                            }

                            ConstPBOCHandler.onTransactionResult.result.EMV_MULTI_CARD_ERROR -> {
                                // multi-cards found
                                Log.i(
                                    "OnTransactionResult",
                                    cardResult.bundle.getString(ConstPBOCHandler.onTransactionResult.data.KEY_ERROR_String)
                                        ?: ""
                                )
                                return@emvStart
                            }
                        }
                    }
                }
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun appendF55(tag: Int, value: String?): ByteArray? {
        val emvtlvF55 = EMVTLVParam()
        val tlv: String = emvtlvF55.append(tag, value)
        return Utility.hexStr2ByteV2(tlv)
    }

    private fun setConfigTLV(buffer: StringBuffer) {
        try {

            Log.i("OnRequestOnlineProcess", "Requesting Card Tag Length Value (TLV)...")

            val issuerApplicationData = Utility.hexString(emv.getCardData("9F10"))
            val acquirerRequestCryptogram = Utility.hexString(emv.getCardData("9F26"))
            val applicationTransactionCounter = Utility.hexString(emv.getCardData("9F36"))
            val terminalVerificationResult = Utility.hexString(emv.getCardData("95"))
            val txnStatusInformation = Utility.hexString(emv.getCardData("9B"))
            val cardHolderVerificationMethod = Utility.hexString(emv.getCardData("9F34"))
            val panSeqNo = Utility.hexString(emv.getCardData("5F34"))
            val terminalCapabilities = Utility.hexString(emv.getCardData("9F33"))
            val txnDate = Utility.hexString(emv.getCardData("9A"))
            val authAmount = Utility.hexString(emv.getCardData("9F02"))
            val otherAmount = Utility.hexString(emv.getCardData("9F03"))
            val applicationInterchangeProfile = Utility.hexString(emv.getCardData("82"))
            val unpredictableNo = Utility.hexString(emv.getCardData("9F37"))
            val iacOnline = Utility.hexString(emv.getCardData("9F0F"))
            val iacDefault = Utility.hexString(emv.getCardData("9F0D"))
            val iacDenial = Utility.hexString(emv.getCardData("9F0E"))
            val customerName = Utility.hexString(emv.getCardData("5F20"))

            if (buffer.toString().isNotEmpty()) {
                cardReadOutput.EMVData = buffer.toString()
            }
            if (issuerApplicationData.isNotEmpty()) {
                cardReadOutput.issuerApplicationData = issuerApplicationData
            }
            if (acquirerRequestCryptogram.isNotEmpty()) {
                cardReadOutput.transactionCertificate = acquirerRequestCryptogram
            }
            if (applicationTransactionCounter.isNotEmpty()) {
                cardReadOutput.cardTransactionCount = applicationTransactionCounter
            }
            if (terminalVerificationResult.isNotEmpty()) {
                cardReadOutput.TVRData = terminalVerificationResult
            }
            if (txnStatusInformation.isNotEmpty()) {
                cardReadOutput.TSIData = txnStatusInformation
            }
            if (cardHolderVerificationMethod.isNotEmpty()) {
                cardReadOutput.cardHolderVerificationMethod = cardHolderVerificationMethod
            }
            if (panSeqNo.isNotEmpty()) {
                cardReadOutput.PANSEQ = panSeqNo
            }
            if (terminalCapabilities.isNotEmpty()) {
                cardReadOutput.terminalCapability = terminalCapabilities
            }
            if (txnDate.isNotEmpty()) {
                cardReadOutput.txnDate = txnDate
            }
            if (authAmount.isNotEmpty()) {
                cardReadOutput.txnAmount = authAmount
            }
            if (otherAmount.isNotEmpty()) {
                cardReadOutput.otherAmount = otherAmount
            }
            if (applicationInterchangeProfile.isNotEmpty()) {
                cardReadOutput.applicationInterchangeProfile = applicationInterchangeProfile
            }
            if (unpredictableNo.isNotEmpty()) {
                cardReadOutput.unpredictableNumber = unpredictableNo
            }
            if (iacDefault.isNotEmpty()) {
                cardReadOutput.IACDefault = iacDefault
            }
            if (iacDenial.isNotEmpty()) {
                cardReadOutput.IACDenial = iacDenial
            }
            if (iacOnline.isNotEmpty()) {
                cardReadOutput.IACOnline = iacOnline
            }
            if (customerName.isNotEmpty()) {
                if (customerName != "2020202020202020202020202020202020202020202020202020") {
                    cardReadOutput.customerName = customerName
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun emvSetIntent(): Bundle {
        val emvIntent = Bundle()

        /** Introducing card types to EMV */
        emvIntent.putInt(
            ConstIPBOC.startEMV.intent.KEY_cardType_int,
            ConstIPBOC.startEMV.intent.VALUE_cardType_smart_card
        )

        /** Configure the Amount of the transaction */
        emvIntent.putLong(
            ConstIPBOC.startEMV.intent.KEY_authAmount_long,
            authAmount
        )

        /** Configure the Merchant Name of the transaction */
        emvIntent.putString(
            ConstIPBOC.startEMV.intent.KEY_merchantName_String,
            "BNIAG22071998"
        ) // BNIAG22071998 value just for assessment and test emv process

        /** Configure the Merchant ID of the transaction */
        emvIntent.putString(
            ConstIPBOC.startEMV.intent.KEY_merchantId_String,
            "010001020270123"
        ) // 010001020270123 value just for assessment and test emv process

        /** Configure the Terminal ID of the transaction */
        emvIntent.putString(
            ConstIPBOC.startEMV.intent.KEY_terminalId_String,
            "00000001"
        ) // 00000001 value just for assessment and test emv process

        /** Configure the -- of the transaction */
        emvIntent.putBoolean(
            ConstIPBOC.startEMV.intent.KEY_isSupportQ_boolean,
            ConstIPBOC.startEMV.intent.VALUE_supported
        )

        /** Configure the -- of the transaction */
        emvIntent.putBoolean(
            ConstIPBOC.startEMV.intent.KEY_isSupportSM_boolean,
            ConstIPBOC.startEMV.intent.VALUE_supported
        )

        /** Configure the -- of the transaction */
        emvIntent.putBoolean(
            ConstIPBOC.startEMV.intent.KEY_isQPBOCForceOnline_boolean,
            ConstIPBOC.startEMV.intent.VALUE_forced
        )

        /** Configure the -- of the transaction */
        emvIntent.putBoolean("isSupportPBOCFirst", false)
        return emvIntent
    }


    override fun stopEMVSearch() {
        emv.stopCheckCard()
        emv.abortEMV()
    }
}