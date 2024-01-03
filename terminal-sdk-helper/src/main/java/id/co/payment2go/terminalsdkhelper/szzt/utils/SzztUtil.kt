package id.co.payment2go.terminalsdkhelper.szzt.utils

import android.os.HandlerThread
import android.util.Base64
import android.util.Log
import com.szzt.sdk.device.Constants
import com.szzt.sdk.device.card.MagneticStripeCardListener
import com.szzt.sdk.device.card.MagneticStripeCardReader
import com.szzt.sdk.device.card.SmartCardReader
import com.szzt.sdk.device.card.SmartCardReader.SmartCardReaderForCardListener
import com.szzt.sdk.device.emv.EMV_CONSTANTS
import com.szzt.sdk.device.emv.EMV_STATUS
import com.szzt.sdk.device.emv.EmvInterface
import com.szzt.sdk.device.pinpad.PinPad
import com.szzt.sdk.system.SystemManager
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.szzt.emv.EMVErrorMessage
import id.co.payment2go.terminalsdkhelper.szzt.emv.aid.EMVSzztParam
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


interface SzztUtil {
    val emvInterface: EmvInterface
    val smCardReader: SmartCardReader
    val magCardReader: MagneticStripeCardReader
    var smartCardHandlerThread: HandlerThread?
    var smartCardThread: SmartCardThread
    var smartTransType: Byte
    var searchCardResultSzzt: MutableStateFlow<SearchCardResultSzzt>
    var systemManager: SystemManager?
    var pinPad: PinPad

    val tag: IntArray
        get() = intArrayOf(
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

    suspend fun searchCard(): Boolean {
        return withContext(Dispatchers.IO) {
            suspendCoroutine {
                setEMVParam()
                magCardReader.open()
                smCardReader.open(0, smartOpenCardListener)
                magCardReader.listenForCard(Constants.WAIT_INFINITE, magCardListener)
                it.resume(true)
            }
        }
    }

    /*
      MAGNETIC CARD STATE
    */

    private val magCardListener: MagneticStripeCardListener
        get() = MagneticStripeCardListener { p0 ->
            CoroutineScope(Dispatchers.IO).launch {
                if (p0 >= 0) {
                    try {
                        searchCardResultSzzt.emit(SearchCardResultSzzt.Loading)
                        Log.d("SSZTMAG Listener ", p0.toString())
                        val output =
                            TagUtils.trackTrim(magCardReader.getTrackData(MagneticStripeCardReader.TRACK_INDEX_2))
                                ?.let { o ->
                                    String(o)
                                }?.replace("=", "D")

                        var t2Data = output?.filter { f -> f.isLetter() || f.isDigit() }
                        val track2IsOdd = t2Data?.length!! % 2 != 0

                        if (track2IsOdd) {
                            t2Data += "0"
                        }

                        if (Util.isIcCard(t2Data)) {
                            Log.d("Is IC Card", Util.isIcCard(t2Data).toString())
                            searchCardResultSzzt.emit(SearchCardResultSzzt.OnError("Silahkan masukkan kartu debit ber-chip Nasabah Anda."))
                        } else {
                            val result = magCardOutput(output).szztCardData
                            when {
                                result.expired -> {
                                    searchCardResultSzzt.emit(SearchCardResultSzzt.Expired)
                                }

                                result.cardNumber == "" -> {
                                    searchCardResultSzzt.emit(SearchCardResultSzzt.OnError(message = result.message))
                                }


                                else -> {
                                    searchCardResultSzzt.emit(SearchCardResultSzzt.FindICCard(result))
                                }
                            }
                        }
                        stopEmv()
                    } catch (e: Exception) {
                        searchCardResultSzzt.emit(SearchCardResultSzzt.OnError(e.message.toString()))
                        stopEmv()
                    }
                }
            }
        }

    private fun magCardOutput(output: String?): SearchCardResultSzzt.FindMagCard {
        try {
            val cardNumber =
                output?.substringBefore("D")?.filter { f -> f.isDigit() }

            val expired = output?.substringAfter("D")?.take(4)
            var t2Data = output?.filter { f -> f.isLetter() || f.isDigit() }

            val track2IsOdd = t2Data?.length!! % 2 != 0

            if (track2IsOdd) {
                t2Data += "0"
            }
            val data = SzztCardData(
                cardNumber = cardNumber ?: "",
                trackData = t2Data,
                message = "Success",
                expired = Util.isCardExpired(expired),
                mode = Constant.POS_ENTRY_MODE_SWIPE
            )
            return SearchCardResultSzzt.FindMagCard(data)
        } catch (e: Exception) {
            val data = SzztCardData(message = "Mohon Ulangi Lagi")
            return SearchCardResultSzzt.FindMagCard(data)
        }
    }

    /*
      IC CARD STATE
    */

    private val smartOpenCardListener: SmartCardReader.SCReaderListener
        get() = SmartCardReader.SCReaderListener { p0, p1 ->
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(TAG, "smartOpenCardListener: $p0 : $p1")
                if (p1 == SmartCardReader.EVENT_SMARTCARD_READY) {
                    searchCardResultSzzt.emit(SearchCardResultSzzt.Loading)
                    smCardReader.listenerForCard(0, Constants.WAIT_INFINITE, smartCardListener)
                }
            }
        }

    private val smartCardListener: SmartCardReaderForCardListener
        get() = SmartCardReaderForCardListener { p0, p1, p2 ->
            Log.d(TAG, "smartCardListener: $p0 : $p1 $p2")
            smartCardEmv(smCardReader)
        }

    fun smartCardEmv(smartCardReader: SmartCardReader) {
        val unixHandlerName = Calendar.getInstance().time
        smartCardHandlerThread = HandlerThread("SmartCardHandler$unixHandlerName")
        val result: Int = smartCardReader.waitForCard(0, Constants.WAIT_INFINITE)
        while (result >= 0) {
            val data = ByteArray(256)
            val powerRet = smartCardReader.powerOn(0, data)
            while (powerRet >= 0) {
                smartCardEmvInit(smartCardReader.getCardType(0))
                break
            }
            break
        }
    }

    private fun smartCardEmvInit(result: Int) {
        smartCardHandlerThread?.start()
        smartCardThread =
            SmartCardThread(smartCardHandlerThread?.looper!!, onMessage = { status, retCode ->
                runningSmartCardEmv(status, retCode)
            })
        emvInterface.initialize(smartCardThread)
        emvInterface.preprocess(0)
        emvInterface.setCardType(result)
        emvInterface.setTransType(smartTransType)
        //first processing
        emvInterface.process()
    }

    fun runningSmartCardEmv(status: Int, retCode: Int) {
        Log.e(TAG, "EMV Processing... , result :  retcode $retCode , status $status")
        when (status) {
            EmvInterface.STATUS_CONTINUE -> {
                when (retCode) {
                    EMV_STATUS.EMV_STA_CANDIDATES_BUILT -> {
                        Log.i("EMV_STATUS_2", "EMV_STA_CANDIDATES_BUILD")
                        val a1 = ByteArray(64)
                        emvInterface.getCardCandidateList(a1)
                        var resultZero = true
                        for (a1comp in a1) {
                            if (a1comp.toInt() != 0) {
                                resultZero = false
                                break
                            }
                        }
                        if (resultZero) {
                            Log.d(
                                "EMV_STATUS_2",
                                "performEMVTest:EMV_STA_CANDIDATES_BUILT:A1 zeroed"
                            )
                            Log.e("EMV_STATUS_2", "EMV ERROR ON EMV_STA_CANDIDATES_BUILT")
                        } else {
                            val a1s = String(
                                Base64.encode(a1, Base64.DEFAULT),
                                StandardCharsets.US_ASCII
                            )
                            Log.d("EMV_STATUS_2", "performEMVTest:EMV_STA_CANDIDATES_BUILT:$a1s")
                            //The One and Only Candidate
                            emvInterface.process()
                        }
                    }

                    EMV_STATUS.EMV_STA_APP_SELECTED -> {
                        Log.i("EMV_STATUS_5", "EMV_STA_APP_SELECTED")
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_REQ_SEL_ACCOUNTTYPE -> {
                        Log.i("EMV_STATUS_6", "EMV_REQ_SEL_ACCOUNTTYPE")
                        emvInterface.setAccountTypeSelected(0)
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_STA_APP_INITIALIZED -> {
                        Log.i("EMV_STATUS_7", "EMV_STA_APP_INITIALIZED")
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_STA_READ_APP_DATA_COMPLETED -> {
                        Log.i("EMV_STATUS_10", "EMV_REQ_APP_DATA_COMPLETED")
                        val s9f12: String = TagUtils.getTagV2(0x9F12, emvInterface)
                        val s50: String = TagUtils.getTagV2(0x50, emvInterface)
                        Log.d("EMV_STATUS_10", "issuer name s9f12: $s9f12")
                        Log.d("EMV_STATUS_10", "issuer name s50: $s50")
                        val panNumber: String = TagUtils.getTagV2(0x5A, emvInterface)
                        val track2: String = TagUtils.getTagV2(0x57, emvInterface)
                        Log.d(
                            "EMV_STATUS_10",
                            "posEntryMode:" + TagUtils.getTagV2(0x9F39, emvInterface)
                        )
                        Log.d("EMV_STATUS_10", "panNumber: $panNumber")
                        Log.d("EMV_STATUS_10", "track2: $track2")
                        Log.i("EMV_STATUS_10", "Pan Number : $panNumber")
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_REQ_CARD_CONFIRM -> {
                        Log.i("EMV_STATUS_24", "EMV_REQ_CARD_CONFIRM")
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_STA_DATA_AUTH_COMPLETED -> {
                        Log.i("EMV_STATUS_11", "EMV_STA_DATA_AUTH_COMPLETED")
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_STA_PROCESS_RESTRICT_COMPLETED -> {
                        Log.i("EMV_STATUS_12", "EMV_STA_PROCESS_RESTRICT_COMPLETED")
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_REQ_ONLINE_PIN -> {
                        Log.i("EMV_STATUS_14", "EMV_REQ_ONLINE_PIN")
                        emvInterface.setOnlinePinEntered(
                            0,
                            byteArrayOf(1, 1, 1, 1, 1, 1),
                            6
                        )

                        emvInterface.setPinByPassConfirmed(1)
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_REQ_OFFLINE_PIN -> {
                        Log.i("EMV_STATUS_15", "EMV_REQ_OFFLINE_PIN")
                        val pin = ByteArray(4)
                        emvInterface.setOfflinePinEntered(
                            EMV_CONSTANTS.EMV_OPERRESULTS.EMV_OPER_OK,
                            pin,
                            6
                        )
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_REQ_CONFIRM_BYPASS_PIN -> {
                        Log.i("EMV_STATUS_17", "EMV_REQ_CONFIRM_BYPASS_PIN")
                        val resultBypassPin = emvInterface.setPinByPassConfirmed(0)
                        if (resultBypassPin >= 0) emvInterface.process()
                    }

                    EMV_STATUS.EMV_REQ_OFFLINE_PIN_LAST -> {
                        Log.i("EMV_STATUS_16", "EMV_REQ_OFFLINE_PIN_LAST")
                        val pin = ByteArray(4)
                        emvInterface.setOfflinePinEntered(
                            EMV_CONSTANTS.EMV_OPERRESULTS.EMV_OPER_OK,
                            pin,
                            6
                        )
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_STA_CARDHOLDER_VERIFY_COMPLETED -> {
                        Log.i("EMV_STATUS_18", "EMV_STA_CARDHOLDER_VERIFY_COMPLETED")
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_REQ_CONFIRM_FORCE_ONLINE -> {
                        Log.i("EMV_STATUS_19", "EMV_REQ_CONFIRM_FORCE_ONLINE")
                        val ret: Int = emvInterface.setForceOnline(0)
                        Log.i("EMV_STATUS_19", "Is force online? : ${ret >= 0}")
                        if (ret >= 0) emvInterface.process()
                    }

                    EMV_STATUS.EMV_STA_RISK_MANAGEMENT_COMPLETED -> {
                        Log.i("EMV_STATUS_20", "EMV_STA_RISK_MANAGEMENT_COMPLETED")
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_REQ_GO_ONLINE -> {
                        Log.i("EMV_STATUS_22", "EMV_REQ_GO_ONLINE")
                        val data = ByteArray(256)
                        val recvFiled55 = ByteArray(256)
                        var index = 0
                        setTlvData()
                        for (i in tag.indices) {
                            val len = emvInterface.getTagData(tag[i], data)
                            if (len > 0) {
                                if (tag[i] > 0xFF) {
                                    recvFiled55[index++] = (tag[i] / 0xff).toByte()
                                }
                                recvFiled55[index++] = (tag[i] and 0xff).toByte()
                                recvFiled55[index++] = len.toByte()
                                System.arraycopy(data, 0, recvFiled55, index, len)
                                index += len
                            }
                        }
                        val filed55 = ByteArray(index)
                        emvInterface.setOnlineResult(
                            EMV_CONSTANTS.EMV_ONLINERESULTS.EMV_ONLINE_SUCC_ACCEPT,
                            recvFiled55,
                            recvFiled55.size
                        )

                        val size = BytesUtil.byteArray2HexString(filed55).length
                        val tlvData = BytesUtil.byteArray2HexString(recvFiled55).take(size)
                        emvInterface.setICCTagData(0x989898, HexDump.hexStringToByteArray(tlvData))
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_REQ_ISSUER_REFERRAL -> {
                        Log.i("EMV_STATUS_23", "EMV_REQ_ISSUER_REFERRAL")
                        emvInterface.setIssrefResult(1) // 0 = refuse , 1 = allow
                        emvInterface.process()
                    }

                    EMV_STATUS.EMV_ERR -> {
                        Log.e("EMV_STATUS_-100", "EMV_ERR")
                        emvInterface.process()
                    }

                    else -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(1000)
                            if (!smartCardHandlerThread?.isAlive!!) {
                                emvInterface.processExit()
                                smCardReader.close(0)
                                return@launch
                            }
                            //start processing again
                            emvInterface.process()
                        }
                    }
                }
            }

            EmvInterface.STATUS_COMPLETION -> {
                when (retCode) {
                    EMV_STATUS.EMV_COMPLETED -> {
                        Log.i("EMV_STATUS_100", "EMV_COMPLETED")
                        smartEmvOnCompletion()
                    }

                    EMV_STATUS.EMV_ACCEPTED_OFFLINE -> {
                        Log.i("EMV_STATUS_101", "EMV_ACCEPTED_OFFLINE")
                        Log.d(
                            TAG,
                            "EMV_ACCEPTED_OFFLINE" + TagUtils.getTagDataStr(0x8A, emvInterface)
                        )
                    }

                    EMV_STATUS.EMV_DENIALED_OFFLINE -> {
                        Log.i("EMV_STATUS_102", "EMV_DENIALED_OFFLINE")
                        stateDataEmv()
                    }

                    EMV_STATUS.EMV_ACCEPTED_ONLINE -> {
                        Log.i("EMV_STATUS_103", "EMV_ACCEPTED_ONLINE")
                        stateDataEmv()
                    }


                    EMV_STATUS.EMV_DENIALED_ONLINE -> {
                        Log.i("EMV_STATUS_104", "EMV_DENIALED_ONLINE")
                        Log.d(
                            TAG,
                            "EMV_DENIALED_ONLINE" + TagUtils.getTagDataStr(0x8A, emvInterface)
                        )
                    }

                    EMV_STATUS.EMV_SEEPHONE -> {
                        Log.i("EMV_STATUS_107", "EMV_SEEPHONE")
                    }

                    EMV_STATUS.EMV_SEEPHONE_AUTH_DEVICE -> {
                        Log.i("EMV_STATUS_118", "EMV_SEEPHONE_AUTH_DEVICE")
                    }

                    EMV_STATUS.EMV_PAYPASS_TRY_AGAIN -> {
                        Log.i("EMV_STATUS_114", "EMV_PAYPASS_TRY_AGAIN")
                    }

                    else -> {
                        emvInterface.process()
                    }
                }
            }

            EmvInterface.STATUS_ERROR -> {
                val cHandler = CoroutineExceptionHandler { _, exception ->
                    Log.e(TAG, "EmvInterface.STATUS_ERROR: $exception")
                }
                CoroutineScope(Dispatchers.IO).launch(cHandler) {
                    searchCardResultSzzt.emit(
                        SearchCardResultSzzt.OnError(
                            EMVErrorMessage.getMessage(
                                retCode
                            )
                        )
                    )
                }
            }

            else -> {

                CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    if (!smartCardHandlerThread?.isAlive!!) {
                        emvInterface.processExit()
                        smCardReader.close(0)
                        return@launch
                    }
                    //start processing again
                    emvInterface.process()
                }
            }
        }
    }

    private fun stateDataEmv() {
        val byteData = ByteArray(256)
        CoroutineScope(Dispatchers.IO).launch {
            val dataTest = emvInterface.getTagData(0x989898, byteData)
            val emvData = HexDump.decBytesToHex(byteData, dataTest)

            Log.d("ISI DATA EMVDATA: ", emvData)

            val result = smartCardOutput(emvData).szztCardData
            when {
                result.expired -> {
                    searchCardResultSzzt.emit(SearchCardResultSzzt.Expired)
                }

                result.cardNumber == "" -> {
                    searchCardResultSzzt.emit(SearchCardResultSzzt.OnError(message = "Mohon Ulangi lagi"))
                }

                else -> {
                    searchCardResultSzzt.emit(SearchCardResultSzzt.FindICCard(result))
                }
            }
            stopEmv()
        }
    }

    private fun setTlvData() {
        /**
         * Replace Country Code with 0360
         */
        emvInterface.setICCTagData(
            SzztEmVTag.terminalCountryCode,
            HexDump.hexStringToByteArray(Constant.baseCountryCode)
        )

        /**
         * Replace Currency Code with 0360
         */
        emvInterface.setICCTagData(
            SzztEmVTag.transactionCurrencyCode,
            HexDump.hexStringToByteArray(Constant.baseCurrencyCode)
        )

        /**
         * Replace IFD become serial number device
         */
        if (systemManager != null) {
            val serialNumberDevice = systemManager?.deviceInfo?.sn

            emvInterface.setICCTagData(
                SzztEmVTag.interfaceDeviceSerialNumber,
                serialNumberDevice?.toByteArray(Charsets.UTF_8)
            )
        }

        /**
         * Set Tag 9F09 with Value 0002
         * for reproduce TVR 0880048000
         */

        emvInterface.setICCTagData(
            SzztEmVTag.applicationVersionNumber,
            HexDump.hexStringToByteArray("0002")
        )

        /**
         * Set Tag 9F53 with Value 00
         * for reproduce TVR 0880048000
         */

        emvInterface.setICCTagData(
            SzztEmVTag.unknown,
            HexDump.hexStringToByteArray("00")
        )

        /**
         * Set Tag 9F33 with value E0F8C8 -- E0F800
         * for reproduce request to B24
         */
        emvInterface.setICCTagData(
            SzztEmVTag.terminalCapabilities,
            HexDump.hexStringToByteArray("E0F8C8")
        )

    }

    private fun smartCardOutput(emvData: String): SearchCardResultSzzt.FindICCard {
        val tagData = ByteArray(100)
        val tagDataLength = emvInterface.getTagData(0x57, tagData)
        val cardNumberAndTrack2Data =
            StringUtility.getStringFormat(tagData, tagDataLength)
                .replace("\\s".toRegex(), "")
        val cardNumber =
            cardNumberAndTrack2Data.substringBefore("D")
                .filter { f -> f.isDigit() }
        val expired = cardNumberAndTrack2Data.substringAfter("D").take(4)
        var t2Data =
            cardNumberAndTrack2Data.filter { f -> f.isLetter() || f.isDigit() }
        val track2IsOdd = t2Data.length % 2 != 0

        if (track2IsOdd) {
            t2Data += "0"
        }
        Log.d("ISI DATA POST: ", emvData)
        val data = SzztCardData(
            mode = Constant.POS_ENTRY_MODE_DIP,
            cardNumber = cardNumber,
            trackData = t2Data,
            message = "Success",
            expired = Util.isCardExpired(expired),
            emvData = emvData
        )
        return SearchCardResultSzzt.FindICCard(data)
    }

    private fun smartEmvOnCompletion() {
        if (smartTransType.toInt() and 0xFF == 0xF4) {
            val temp = ByteArray(32)
            var amount = "0"
            emvInterface.getICCTagData(0x9F79, temp)
            val ret = emvInterface.getTagData(0x9F79, temp)
            if (ret >= 0) {
                amount = HexDump.decBytesToHex(temp, ret)
            }
            val resAmount = String.format(
                "%.2f",
                amount.toInt() * 1.0 / 100
            )
            Log.d(TAG, "STATUS_COMPLETION: $resAmount")
        } else if (smartTransType.toInt() and 0xFF == 0xF0) {
            val sum = ByteArray(1)
            emvInterface.getTransLog(0x101, sum)
            println("sum:" + sum[0])
            val fmtDol = ByteArray(60)
            val recVal = ByteArray(256)
            for (i in 1..sum[0]) {
                Log.d(TAG, "fmt_dol:" + HexDump.decBytesToHex(fmtDol))
                val len: Int = emvInterface.getTransLog(i, recVal)
                Log.d(TAG, "recVal:" + HexDump.decBytesToHex(recVal, len))
                val stringBuffer = StringBuffer()
                var index = 0
                stringBuffer.append("9A03")
                stringBuffer.append(HexDump.toHexString(recVal, index, 3))
                index += 3
                stringBuffer.append("9F2103")
                stringBuffer.append(HexDump.toHexString(recVal, index, 3))
                index += 3
                stringBuffer.append("9F0206")
                stringBuffer.append(HexDump.toHexString(recVal, index, 6))
                index += 6
                index += 6
                stringBuffer.append("9F1A02")
                stringBuffer.append(HexDump.toHexString(recVal, index, 2))
                index += 2
                stringBuffer.append("5F2A02")
                stringBuffer.append(HexDump.toHexString(recVal, index, 2))
                index += 2
                stringBuffer.append("9F4E14")
                stringBuffer.append(HexDump.toHexString(recVal, index, 20))
                index += 20
                stringBuffer.append("9C01")
                stringBuffer.append(HexDump.toHexString(recVal, index, 1))
                index += 1
                stringBuffer.append("9F3602")
                stringBuffer.append(HexDump.toHexString(recVal, index, 2))
                Log.d(TAG, "STATUS_COMPLETION: $stringBuffer")
            }
        } else if (smartTransType.toInt() and 0xFF == 0xF2) {
            val cardSN = "0" + HexDump.decBytesToHex(TagUtils.getTag(0x5F34, emvInterface))
            Log.d(
                TAG, "cardSN:$cardSN"
            )
            val cardNO = HexDump.decBytesToHex(TagUtils.getTag(0x5A, emvInterface))
            Log.d(TAG, "cardNO:$cardNO")
        }
    }

    fun stopEmv() {
        searchCardResultSzzt = MutableStateFlow(SearchCardResultSzzt.Waiting)
        try {
            magCardReader.close()
            smCardReader.close(0)
            emvInterface.processExit()
            smartCardHandlerThread?.quitSafely()
        } catch (e: Exception) {
            Log.d(TAG, e.message.toString())
        }
    }

    private fun setEMVParam() {
        emvInterface.clearAidParam()
        emvInterface.clearCAPKParam()
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "setEMVParam error: $exception")
        }
        CoroutineScope(Dispatchers.IO).launch(handler) {
            var index = 0
            for (param: ByteArray in EMVSzztParam.aidMockSzzt) {
                val nret = emvInterface.updateAidParam(1, param)
                Log.d(
                    TAG,
                    "AID [$index] = ${EMVSzztParam.aidMockSzzt[index++]} , final result is : $nret"
                )
            }
            index = 0
            for (param: ByteArray in EMVSzztParam.capkMockSzzt) {
                val nret = emvInterface.updateCAPKParam(1, param)
                Log.d(
                    TAG,
                    "CAPK [$index] = ${EMVSzztParam.capkMockSzzt[index++]} , final result is : $nret"
                )
            }

            val nret = emvInterface.setTerminalParam(EMVSzztParam.termMockSzzt)
            Log.d(TAG, "setEmvParam: mTermInfo = ${EMVSzztParam.termMockSzzt} status : $nret")
        }
    }

    companion object {
        private const val TAG = "SzztUtil"
    }
}


