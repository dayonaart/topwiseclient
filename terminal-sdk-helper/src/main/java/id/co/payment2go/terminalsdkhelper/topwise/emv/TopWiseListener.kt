package id.co.payment2go.terminalsdkhelper.topwise.emv

import android.os.RemoteException
import android.util.Log
import com.topwise.cloudpos.aidl.card.AidlCheckCard
import com.topwise.cloudpos.aidl.emv.level2.AidlEmvL2
import com.topwise.cloudpos.aidl.emv.level2.EmvCallback
import com.topwise.cloudpos.aidl.emv.level2.EmvCandidateItem
import com.topwise.cloudpos.aidl.emv.level2.EmvKernelConfig
import com.topwise.cloudpos.aidl.emv.level2.EmvTerminalInfo
import com.topwise.cloudpos.aidl.magcard.TrackData
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad
import com.topwise.cloudpos.aidl.system.AidlSystem
import com.topwise.cloudpos.struct.BytesUtil
import com.topwise.toptool.api.convert.IConvert
import com.topwise.toptool.api.packer.IPacker
import com.topwise.toptool.api.packer.ITlv
import com.topwise.toptool.api.packer.TlvException
import com.topwise.toptool.api.utils.AppLog
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.topwise.emv.TopWiseUtility.checkTrack
import id.co.payment2go.terminalsdkhelper.topwise.emv.aid.installAidTerminal
import id.co.payment2go.terminalsdkhelper.topwise.emv.capk.installCapkTerminal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.topwise.cloudpos.aidl.card.AidlCheckCardListener.Stub as CardListener


enum class TopWiseCheckTrack { EXPIRED, IS_IC_CARD, SUCCESS }
data class TopWiseTrack2Data(val res: String = "", val topWiseCheckTrack: TopWiseCheckTrack)

object TopWiseListener : CardListener() {
    lateinit var topWiseCardReaderOutput: MutableStateFlow<TopWiseCardReaderOutput?>
    lateinit var cardManager: AidlCheckCard
    lateinit var emvManager: AidlEmvL2
    lateinit var iConvert: IConvert
    lateinit var iPacker: IPacker
    lateinit var pinpad: AidlPinpad
    lateinit var deviceSystem: AidlSystem
    private val emvKernelConfig: EmvKernelConfig = setEmvKernelConfig()
    var amount: Long = 0
    var stan: Long = 0
    private val TAG: String
        get() = "EmvTopWiseListener"


    override fun onFindMagCard(p0: TrackData?) {
        Log.d(TAG, "onFindMagCard -> ${p0?.cardno}")
        val output = p0?.secondTrackData?.checkTrack()

        CoroutineScope(Dispatchers.IO).launch {
            topWiseCardReaderOutput.emit(value = TopWiseCardReaderOutput.Loading)
            when (output?.topWiseCheckTrack) {

                TopWiseCheckTrack.EXPIRED -> {
                    topWiseCardReaderOutput.emit(TopWiseCardReaderOutput.OnError(message = output.res))
                }

                TopWiseCheckTrack.IS_IC_CARD -> {
                    topWiseCardReaderOutput.emit(TopWiseCardReaderOutput.OnError(message = output.res))
                }

                TopWiseCheckTrack.SUCCESS -> {
                    topWiseCardReaderOutput.emit(
                        value = TopWiseCardReaderOutput.ReadMagCard(
                            cardReadOutput = CardReadOutput(
                                cardNo = p0.cardno,
                                track2Data = output.res,
                                posEntryMode = Constant.POS_ENTRY_MODE_SWIPE
                            )
                        )
                    )
                }

                else -> {
                    topWiseCardReaderOutput.emit(TopWiseCardReaderOutput.OnError(message = "Card Not Found"))
                }
            }
        }
    }

    override fun onSwipeCardFail() {
        Log.d(TAG, "onSwipeCardFail -> ")
        CoroutineScope(Dispatchers.IO).launch {
            topWiseCardReaderOutput.emit(value = TopWiseCardReaderOutput.OnError(message = "Failed To Swipe Card"))
        }
    }

    override fun onFindICCard() {
        Log.d(TAG, "onFindICCard -> ")
        CoroutineScope(Dispatchers.IO).launch {
            topWiseCardReaderOutput.emit(value = TopWiseCardReaderOutput.Loading)
            initEmv()
            while (true) {

                /** Selecting Candidate */
                var candListCount: Int
                var selectedAppIndex = 0
                candListCount = emvManager.EMV_AppGetCandListCount()
                if (candListCount > 1 && 1.toByte() == emvKernelConfig.getbCardHolderConfirm()) {
                    val strDisplayName = arrayOfNulls<String>(candListCount)
                    for (i in 0 until candListCount) {
                        val emvCandidateItem: EmvCandidateItem? =
                            emvManager.EMV_AppGetCandListItem(i)
                        if (emvCandidateItem != null) {
                            if (String(emvCandidateItem.aucDisplayName) == "NSICCS") {
                                strDisplayName[i] = String(emvCandidateItem.aucDisplayName)
                                AppLog.d(TAG, "AppGetCandListCount= " + strDisplayName[i])
                                selectedAppIndex = i
                                break
                            }
                        }
                    }
                }
                val resultSelectCandidate = emvManager.EMV_AppGetCandListItem(selectedAppIndex)
                if (resultSelectCandidate == null) {
                    println("error selecting candidate")
                }

                /** The terminal issues the SELECT command */
                AppLog.d(TAG, "emvProcess check EMV_AppFinalSelect ")
                var emvRest = emvManager.EMV_AppFinalSelect(resultSelectCandidate)
                AppLog.d(TAG, "EMV_AppFinalSelect emvRet : $emvRest")

                @Suppress("DEPRECATED_IDENTITY_EQUALS")
                if (9 === emvRest || 10 === emvRest || 21 === emvRest || 20 === emvRest || 18 === emvRest) {
                    candListCount = emvManager.EMV_AppGetCandListCount()
                    AppLog.d(TAG, "emvProcess  EMV_AppGetCandListCount $candListCount")
                    if (candListCount > 1) {
                        emvManager.EMV_AppDelCandListItem(selectedAppIndex)
                        continue
                    } else {
                        println("Approved")
                    }
                } else if (emvRest !== 0) {
                    println("Aid Final Select Failed")
                }

                /** Set amount , other amount */
                emvManager.EMV_SetTLVData(
                    0x9F02,
                    BytesUtil.hexString2Bytes(
                        String.format(
                            "%012d",
                            java.lang.Long.valueOf(amount)
                        )
                    )
                )


                /** Set some transaction data. Transaction Type , Transaction Date , Transaction Time */
                //Set parameters according to each AID. Terminal floor limit, Trans currency code   ... ...
                AppLog.d(TAG, "emvProcess check setTransDataFromAid ")
                setTLV()

                //Initiate Application Processing
                //The terminal issues the GET PROCESSING OPTIONS command
                AppLog.d(TAG, "emvProcess check EMV_GPOProc ")
                emvRest = emvManager.EMV_GPOProc()
                AppLog.d(TAG, "emvProcess PGO res $emvRest")

                val lastSW: Int = emvManager.EMV_GetLastStatusWord()
                AppLog.d(TAG, "emvProcess GPO GetLastStatusWord lastSW $lastSW")
                if (lastSW != 0x9000) {
                    candListCount = emvManager.EMV_AppGetCandListCount()
                    AppLog.d(TAG, "emvProcess GPO GetCandListCount  $candListCount")
                    if (candListCount > 1) {
                        emvManager.EMV_AppDelCandListItem(selectedAppIndex)
                        continue
                    } else {
                        println("get data emvRest : $emvRest")
                    }
                } else {
                    println("get data emvRest : $emvRest")
                }

                break
            }

            emvManager.EMV_ReadRecordData()
            val pan: ByteArray = emvManager.EMV_GetTLVData(0x5A)
            val output = BytesUtil.bytes2HexString(emvManager.EMV_GetTLVData(0x57)).checkTrack()

            val cardNo = BytesUtil.bytes2HexString(pan).uppercase().replace("F", "")
            installCapkTerminal(iPacker, iConvert, emvManager)

            emvManager.EMV_OfflineDataAuth()
            emvManager.EMV_TerminalRiskManagement()
            emvManager.EMV_ProcessingRestrictions()
            emvManager.EMV_CardHolderVerify()
            emvManager.EMV_TermActionAnalyze()

            val tag: IntArray = intArrayOf(
                TopWiseEmvTag.cryptogramInformationData,
                TopWiseEmvTag.amountAuthorisedNumeric,
                TopWiseEmvTag.amountOtherNumeric,
                TopWiseEmvTag.applicationCryptogram,
                TopWiseEmvTag.applicationInterchangeProfile,
                TopWiseEmvTag.applicationTransactionCounter,
                TopWiseEmvTag.issuerApplicationData,
                TopWiseEmvTag.terminalCapabilities,
                TopWiseEmvTag.transactionCurrencyCode,
                TopWiseEmvTag.terminalCountryCode,
                TopWiseEmvTag.terminalVerificationResults,
                TopWiseEmvTag.transactionDate,
                TopWiseEmvTag.transactionType,
                TopWiseEmvTag.unpredictableNumber,
                TopWiseEmvTag.cardholderVerificationMethodResults,
                TopWiseEmvTag.applicationPrimaryAccountNumberSequenceNumber,
                TopWiseEmvTag.dedicatedFileName,
                TopWiseEmvTag.applicationVersionNumber,
                TopWiseEmvTag.interfaceDeviceSerialNumber,
                TopWiseEmvTag.terminalType,
                TopWiseEmvTag.unknown,
                TopWiseEmvTag.cardholderName,
                TopWiseEmvTag.t2Data,
                TopWiseEmvTag.applicationPrimaryAccountNumber,
                TopWiseEmvTag.transactionSequenceCounter
            )

            val emvData = iConvert.bcdToStr(getValueList(tag, emvManager))

            Log.d(TAG, "onFindICCard: $emvData")
            when(output.topWiseCheckTrack){

                TopWiseCheckTrack.EXPIRED -> {
                    topWiseCardReaderOutput.emit(TopWiseCardReaderOutput.OnError(message = output.res))
                }

                TopWiseCheckTrack.IS_IC_CARD -> {
                    topWiseCardReaderOutput.emit(TopWiseCardReaderOutput.OnError(message = output.res))
                }

                TopWiseCheckTrack.SUCCESS -> {
                    topWiseCardReaderOutput.emit(
                        value = TopWiseCardReaderOutput.ReadICCard(
                            cardReadOutput = CardReadOutput(
                                cardNo = cardNo,
                                EMVData = emvData,
                                posEntryMode = Constant.POS_ENTRY_MODE_DIP,
                            )
                        )
                    )
                }
            }

        }
    }

    override fun onFindRFCard() {
        TODO("Not yet implemented")
    }

    override fun onTimeout() {
        Log.d(TAG, "onTimeout -> ")
        CoroutineScope(Dispatchers.IO).launch {
            topWiseCardReaderOutput.emit(TopWiseCardReaderOutput.OnError(message = "Timeout Reader"))
        }
    }

    override fun onCanceled() {
        Log.d(TAG, "onCanceled -> ")
        CoroutineScope(Dispatchers.IO).launch {
            topWiseCardReaderOutput.emit(TopWiseCardReaderOutput.OnError(message = "Reader Canceled"))
        }
    }

    override fun onError(p0: Int) {
        Log.d(TAG, "onError -> $p0")
        CoroutineScope(Dispatchers.IO).launch {
            topWiseCardReaderOutput.emit(value = TopWiseCardReaderOutput.OnError(message = "Error Card Reader $p0"))
        }
    }

    private fun initEmv() {
        var initEmv: Int = emvManager.EMV_Initialize()
        Log.d(TAG, "EMV_Initialize Success retcode is : ${resultCodeEmv(initEmv)}")
        initEmv = emvManager.EMV_SetKernelType(0x00.toByte())
        Log.d(TAG, "EMV_SetKernelType Success retcode is : ${resultCodeEmv(initEmv)}")
        initEmv = emvManager.EMV_SetCallback(emvCallback)
        Log.d(TAG, "EMV_SetCallback Success retcode is : ${resultCodeEmv(initEmv)}")
        initEmv = emvManager.EMV_SetKernelConfig(setEmvKernelConfig())
        Log.d(TAG, "EMV_SetKernelConfig Success retcode is : ${resultCodeEmv(initEmv)}")
        initEmv = emvManager.EMV_SetTerminalInfo(setEmvTerminalInfo())
        Log.d(TAG, "EMV_SetTerminalInfo Success retcode is : ${resultCodeEmv(initEmv)}")
        initEmv = emvManager.EMV_SetSupport_PBOC(0.toByte(), 0.toByte(), 0)
        Log.d(TAG, "EMV_SetSupport_PBOC Success retcode is : ${resultCodeEmv(initEmv)}")
        installAidTerminal(iPacker, iConvert, emvManager)
        initEmv = emvManager.EMV_AppCandidateBuild(0.toByte())
        Log.d(TAG, "EMV_AppCandidateBuild Success retcode is : ${resultCodeEmv(initEmv)}")
    }

    /**
     * set init EmvKernelConfig
     * @return
     */
    private fun setEmvKernelConfig(): EmvKernelConfig {
        val emvKernelConfig = EmvKernelConfig()
        emvKernelConfig.setbPSE(1.toByte())
        emvKernelConfig.setbCardHolderConfirm(1.toByte())
        emvKernelConfig.setbPreferredDisplayOrder(0.toByte())
        emvKernelConfig.setbLanguateSelect(1.toByte())
        emvKernelConfig.setbRevocationOfIssuerPublicKey(1.toByte())
        emvKernelConfig.setbDefaultDDOL(1.toByte())
        emvKernelConfig.setbBypassPINEntry(1.toByte())
        emvKernelConfig.setbSubBypassPINEntry(1.toByte())
        emvKernelConfig.setbGetdataForPINTryCounter(1.toByte())
        emvKernelConfig.setbFloorLimitCheck(1.toByte())
        emvKernelConfig.setbRandomTransSelection(1.toByte())
        emvKernelConfig.setbVelocityCheck(1.toByte())
        emvKernelConfig.setbTransactionLog(1.toByte())
        emvKernelConfig.setbExceptionFile(1.toByte())
        emvKernelConfig.setbTerminalActionCode(1.toByte())
        emvKernelConfig.setbDefaultActionCodeMethod(0x01.toByte())
        emvKernelConfig.setbTACIACDefaultSkipedWhenUnableToGoOnline(0.toByte())
        emvKernelConfig.setbCDAFailureDetectedPriorTerminalActionAnalysis(1.toByte())
        emvKernelConfig.setbCDAMethod(0x00.toByte())
        emvKernelConfig.setbForcedOnline(0.toByte())
        emvKernelConfig.setbForcedAcceptance(0.toByte())
        emvKernelConfig.setbAdvices(0.toByte())
        emvKernelConfig.setbIssuerReferral(1.toByte())
        emvKernelConfig.setbBatchDataCapture(0.toByte())
        emvKernelConfig.setbOnlineDataCapture(1.toByte())
        emvKernelConfig.setbDefaultTDOL(1.toByte())
        emvKernelConfig.setbTerminalSupportAccountTypeSelection(1.toByte())
        return emvKernelConfig
    }

    private fun setEmvTerminalInfo(): EmvTerminalInfo {
        val emvTerminalInfo = EmvTerminalInfo()
        val countryCode: ByteArray = iConvert.strToBcd(
            String.format("%04d", "360".toInt()),
            IConvert.EPaddingPosition.PADDING_RIGHT
        )
        emvTerminalInfo.unTerminalFloorLimit = 20000
        emvTerminalInfo.unThresholdValue = 10000
//        val terminalId = "12345678"
//        emvTerminalInfo.aucTerminalID = terminalId
        emvTerminalInfo.aucIFDSerialNumber = deviceSystem.serialNo
        emvTerminalInfo.aucTerminalCountryCode = countryCode
//        val mercherId = "132456789012345"
//        emvTerminalInfo.aucMerchantID = mercherId
        emvTerminalInfo.aucMerchantCategoryCode = byteArrayOf(0x00, 0x01)
        emvTerminalInfo.aucMerchantNameLocation = byteArrayOf(0x30, 0x30, 0x30, 0x31) //"0001"
        emvTerminalInfo.aucTransCurrencyCode = countryCode
        emvTerminalInfo.ucTransCurrencyExp = 2.toByte()
        emvTerminalInfo.aucTransRefCurrencyCode = countryCode
        emvTerminalInfo.ucTransRefCurrencyExp = 2.toByte()
        emvTerminalInfo.ucTerminalEntryMode = 0x05.toByte()
//        emvTerminalInfo.aucTerminalAcquireID = "123456"
        emvTerminalInfo.aucAppVersion = byteArrayOf(0x00, 0x030)
        emvTerminalInfo.aucDefaultDDOL = byteArrayOf(0x9F.toByte(), 0x37, 0x04)
        emvTerminalInfo.aucDefaultTDOL = byteArrayOf(0x9F.toByte(), 0x37, 0x04)
        emvTerminalInfo.aucTACDenial = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
        emvTerminalInfo.aucTACOnline = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
        emvTerminalInfo.aucTACDefault = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
        emvTerminalInfo.ucTerminalType = 0x22.toByte()
        emvTerminalInfo.aucTerminalCapabilities =
            byteArrayOf(0xE0.toByte(), 0xF8.toByte(), 0xC8.toByte()) //E0F8C8
        emvTerminalInfo.aucAddtionalTerminalCapabilities =
            byteArrayOf(0xFF.toByte(), 0x80.toByte(), 0xF0.toByte(), 0x00.toByte(), 0x01)
        emvTerminalInfo.ucTargetPercentage = 20.toByte()
        emvTerminalInfo.ucMaxTargetPercentage = 50.toByte()
        emvTerminalInfo.ucAccountType = 0.toByte()
        emvTerminalInfo.ucIssuerCodeTableIndex = 0.toByte()
        return emvTerminalInfo
    }

    private fun setTLV() {
        AppLog.d(TAG, "emvProcess setTransData = ")
        try {
            // Transaction Type
            val transType = ByteArray(1)
            transType[0] = 0x00
            emvManager.EMV_SetTLVData(TopWiseEmvTag.transactionType, transType)

            // The getRandom function returns a fixed 8 byte random number
            val random: ByteArray = pinpad.random
            val unpredictableNum = ByteArray(4)
            System.arraycopy(random, 0, unpredictableNum, 0, 4)
            emvManager.EMV_SetTLVData(TopWiseEmvTag.unpredictableNumber, unpredictableNum)

            // Transaction Sequence Counter
            emvManager.EMV_SetTLVData(
                TopWiseEmvTag.transactionSequenceCounter,
                BytesUtil.hexString2Bytes(stan.toString())
            )

            // Transaction Currency Code
            emvManager.EMV_SetTLVData(
                TopWiseEmvTag.transactionCurrencyCode,
                BytesUtil.hexString2Bytes("0360")
            )

            // Terminal Country Code
            emvManager.EMV_SetTLVData(
                TopWiseEmvTag.terminalCountryCode,
                BytesUtil.hexString2Bytes("0360")
            )

        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun getValueList(tags: IntArray?, emv: AidlEmvL2): ByteArray? {
        if (tags == null || tags.isEmpty()) {
            return null
        }
        val tlv: ITlv = iPacker.tlv
        val tlvList = tlv.createTlvDataObjectList()
        for (tag in tags) {
            try {
                var value: ByteArray? = emv.EMV_GetTLVData(tag)
                if (value == null || value.isEmpty()) {
                    value = if (tag == 0x9f03) {
                        ByteArray(6)
                    } else {
                        continue
                    }
                }
                val obj = tlv.createTlvDataObject()
                obj.setTag(tag)
                obj.value = value
                tlvList.addDataObj(obj)
            } catch (e: Exception) {
                e.printStackTrace()
                continue
            }
        }
        try {
            return tlv.pack(tlvList)
        } catch (e: TlvException) {
            e.printStackTrace()
        }
        return null
    }


    private val emvCallback = object : EmvCallback.Stub() {
        override fun cGetOnlinePin(
            p0: Boolean,
            p1: ByteArray?,
            p2: Int,
            p3: BooleanArray?
        ): Int {
            AppLog.d(TAG, "cGetOnlinePin Is allow PIN entry bypass: $p0")
            AppLog.d(TAG, "cGetOnlinePin PAN: " + BytesUtil.bytes2HexString(p1))
            AppLog.d(TAG, "cGetOnlinePin PAN length: $p2")
            return 1
        }

        override fun cGetPlainTextPin(
            p0: Boolean,
            p1: ByteArray?,
            p2: Int,
            p3: BooleanArray?
        ): Int = 0

        override fun cDisplayPinVerifyStatus(p0: Int): Int {
            AppLog.d(TAG, "cDisplayPinVerifyStatus")
            AppLog.d(TAG, "The number of remaining PIN tries: $p0")
            return 1
        }

        override fun cCheckCredentials(
            p0: Int,
            p1: ByteArray?,
            p2: Int,
            p3: BooleanArray?
        ): Int = 0

        override fun cIssuerReferral(p0: ByteArray?, p1: Int): Int = 0

        override fun cGetTransLogAmount(p0: ByteArray?, p1: Int, p2: Int): Int = 0

        override fun cCheckExceptionFile(p0: ByteArray?, p1: Int, p2: Int): Int = 0

        override fun cRFU1(): Int = 0

        override fun cRFU2(): Int = 0

        override fun cRFU3(): Int = 0

        override fun cRFU4(): Int = 0
    }

    private fun resultCodeEmv(emvCode: Int): String {
        val readTagField =
            TopWiseEmvCode::class.java.fields.filter {
                !it.name.contains("INSTANCE")
            }.drop(1)
        val name = readTagField.first { it.get(it.name) == emvCode }
        return name.name
    }
}

