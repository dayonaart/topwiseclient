package id.co.payment2go.terminalsdkhelper.ingenico.emv

import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import com.usdk.apiservice.aidl.emv.*
import com.usdk.apiservice.aidl.emv.KernelID.*
import id.co.payment2go.terminalsdkhelper.common.emv.EMVUtility
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.model.Session
import id.co.payment2go.terminalsdkhelper.common.model.TLV
import id.co.payment2go.terminalsdkhelper.common.model.TLVList
import id.co.payment2go.terminalsdkhelper.common.model.TransactionConfig
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.toCurrentFormat
import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico
import id.co.payment2go.terminalsdkhelper.ingenico.emv.ktx.EMVResult
import id.co.payment2go.terminalsdkhelper.ingenico.emv.ktx.EMVStartResult
import id.co.payment2go.terminalsdkhelper.ingenico.emv.ktx.searchCardAndAwait
import id.co.payment2go.terminalsdkhelper.ingenico.emv.ktx.startEmvAndAwait
import id.co.payment2go.terminalsdkhelper.ingenico.emv.util.EmvParameterInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.*
import kotlin.time.Duration.Companion.seconds

class EMVUtilityIngenico(
    bindService: BindServiceIngenico,
    private val deviceManagerUtility: DeviceManagerUtility
) : EMVUtility {
    private val TAG = "EMVIngenicoUtility"
    private var cardOutput: CardReadOutput? = null
    private var cardRecord: CardRecord? = null
    private var isEMVProcess = false
    private val transactionConfig: TransactionConfig = TransactionConfig()
    private var finalCardReadOutput: CardReadOutput? = null
    private var isRefund = false
    private val emv = bindService.emv
    private var authAmount: Long = 0

    private val emvParameterInitializer =
        EmvParameterInitializer(
            emv,
            transactionConfig
        )

    override fun searchCard(stan: Long, amount: Long): Flow<Resource<CardReadOutput>> {
        return flow {
            try {
                authAmount = amount
                val bundle = Bundle()
                when (val result = emv.searchCardAndAwait(bundle, 60)) {
                    is EMVResult.CardSwiped -> {
                        emit(Resource.Loading())
                        val cardNumber = result.bundle.getString("PAN") ?: ""
                        var track2 = result.bundle.getString("TRACK2") ?: ""

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

                    is EMVResult.CardInsert -> {
                        emit(Resource.Loading())
                        delay(5.seconds) // EMV Sunmi need cool down
                        var resultCardOutput: CardReadOutput? = null
                        startEMV(stan) { cardReadOutput ->
                            if (cardReadOutput == null) return@startEMV
                            resultCardOutput = cardReadOutput
                            resultCardOutput = resultCardOutput?.copy(
                                posEntryMode = Constant.POS_ENTRY_MODE_DIP
                            )
                        }
                        while (resultCardOutput == null) {
                            delay(100L)
                        }
                        if (Util.isCardExpired(resultCardOutput!!.cardExpiry)) {
                            emit(Resource.Error("Kartu debit Anda sudah melewati masa aktif kartu (Kadaluwarsa). Silahkan melakukan penggantian kartu debit di kantor cabang terdekat."))
                            return@flow
                        }
                        emit(Resource.Success(resultCardOutput!!))
                    }

                    is EMVResult.CardPass -> {
                        searchCard(stan, amount).collect {
                            emit(it)
                        }
                    }

                    is EMVResult.Timeout -> {
                        searchCard(stan, amount).collect {
                            emit(it)
                        }
                    }

                    is EMVResult.Error -> {
                        searchCard(stan, amount).collect {
                            emit(it)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun startEMV(stan: Long, onFinish: (CardReadOutput?) -> Unit) {
        finalCardReadOutput = null

        val param = Bundle()
        param.putByte("flagPSE", EMV.PSE_AID_LIST)
        param.putByte("flagCtlAsCb", EMV.ENABLE_CONTACTLESS_CARD_SELECT_APP)
        param.putBoolean("flagICCLog", false)
        param.putBoolean("supportICCard", transactionConfig.contactIcCardSupported)
        param.putBoolean("supportRFCard", transactionConfig.rfCardSupported)
        param.putBoolean("supportMagCard", transactionConfig.magCardSupported)

        isEMVProcess = true
        emv.startEmvAndAwait(param) { emvStartResult ->
            when (emvStartResult) {
                is EMVStartResult.OnInit -> {
                    Log.d(TAG, "----- onInitEMV -----")

                    try {
                        handleInitEMV()
                    } catch (e: Exception) {
                        Log.d(TAG, "onInitEMV: ${e.localizedMessage}")
                    }
                }

                is EMVStartResult.OnWaitCard -> {
                    Log.d(TAG, "----- onWaitCard -----")
                    Log.d(TAG, "flag: ${emvStartResult.flag}")

                }

                is EMVStartResult.OnCardChecked -> {
                    Log.d(TAG, "----- onCardChecked -----")
                    Log.d(TAG, "cardType: ${emvStartResult.cardType}")
                }

                is EMVStartResult.OnAppSelect -> {
                    Log.d(TAG, "----- onAppSelect -----")
                    Log.d(TAG, "aids.size: ${emvStartResult.aids.size}")

                    handleAppSelect(emvStartResult.aids.toList())
                }

                is EMVStartResult.OnFinalSelect -> {
                    Log.d(TAG, "----- onFinalSelect -----")
                    Log.d(TAG, "kernelID: ${emvStartResult.finalData.kernelID}")
                    Log.d(
                        TAG, "AID: ${
                            BytesUtil.byteArray2HexString(
                                emvStartResult.finalData.aid
                            )
                        }"
                    )
                    handleFinalSelect(emvStartResult.finalData)
                }

                is EMVStartResult.OnReadRecord -> {
                    Log.d(TAG, "----- onReadRecord -----")
                    val pan =
                        BytesUtil.byteArray2HexString(
                            emvStartResult.cardRecord!!.pan
                        )
                    Log.d(TAG, "PAN: $pan")

                    handleReadRecord(emvStartResult.cardRecord)
                }

                is EMVStartResult.OnCardHolderVerify -> {
                    Log.d(TAG, "----- onCardHolderVerify -----")
                    Log.d(TAG, "CVM:" + emvStartResult.cvmMethod?.cvm)
                    Log.d(TAG, "CertType:" + emvStartResult.cvmMethod?.certType)
                    Log.d(TAG, "CertNo:" + emvStartResult.cvmMethod?.certNo)
                    Log.d(TAG, "PINTimes:" + emvStartResult.cvmMethod?.pinTimes)
                    emvStartResult.cvmMethod?.let { handleCardHolderVerify(it) }

                }

                is EMVStartResult.OnOnlineProcess -> {
                    Log.d(TAG, "----- onOnlineProcess -----")
                    emvStartResult.transData
                    Log.d(TAG, "ACType:" + emvStartResult.transData?.acType)
                    Log.d(TAG, "CVM:" + emvStartResult.transData?.cvm)
                    Log.d(TAG, "FlowType:" + emvStartResult.transData?.flowType)
                    Log.d(TAG, "TVR: " + emvStartResult.transData?.tlvData)
                    finalizingRequestForEMV()
                    isEMVProcess = false
                    stopEMVSearch() // TODO: EMV ONLY FOR BALANCE INQUIRY
                }

                is EMVStartResult.OnEndProcess -> {
                    Log.d(TAG, "----- onEndProcess -----")
                    Log.d(TAG, "${emvStartResult.resultCode}")
                    emvStartResult.resultCode
                    onFinish(finalCardReadOutput)
                }

                is EMVStartResult.OnObtainData -> {
                    Log.d(TAG, "----- OnObtainData -----")

                }

                is EMVStartResult.OnSendOut -> {
                    Log.d(TAG, "----- OnSendOut -----")
                }

                is EMVStartResult.OnVerifyOfflinePin -> {
                    Log.d(TAG, "----- OnVerifyOfflinePin -----")

                }
            }
        }
    }

//    private suspend fun handleWaitCard(stan: Long, flag: Int, onFinish: (CardReadOutput?) -> Unit) {
//        when (flag) {
//            WaitCardFlag.NORMAL -> searchCard(stan, onFinish)
//            WaitCardFlag.ISS_SCRIPT_UPDATE, WaitCardFlag.SHOW_CARD_AGAIN -> {
//                transactionConfig.magCardSupported = false
//                transactionConfig.contactIcCardSupported = false
//                searchCard(stan, onFinish)
//            }
//
//            else -> {
//                emv.stopEMV()
//                emv.halt()
//                if (isEMVProcess) emv.stopProcess()
//            }
//        }
//    }


    /**
     * Handle init EMV.
     */
    private fun handleInitEMV() {
        emv.manageAID(ActionFlag.CLEAR, null, true)

        for ((key, value) in EMVData.getMockAids()) {
            emv.manageAID(ActionFlag.ADD, key, value)
        }

        emv.setDOL(DOLType.DDOL, "9F3704")
    }

    /**
     * Handle app select.
     */
    @Throws(RemoteException::class)
    fun handleAppSelect(aids: List<CandidateAID>) {
        aids.let {
            aids[0].let {
                val tlvList = TLVList()
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_AID, aids[0].aid
                    )
                )
                emv.responseEvent(tlvList.toString())
            }
        }
    }

    /**
     * Handle final select.
     */
    @Throws(RemoteException::class)
    fun handleFinalSelect(finalData: FinalData) {
        try {
            val kernelId: Byte = finalData.kernelID

            // Set transaction type(9C)
            emv.setTLV(kernelId.toInt(), EMVTag.EMV_TAG_TM_TRANSTYPE, "00")

            // Init EMV parameters
            val aid =
                BytesUtil.byteArray2HexString(finalData.aid)
            val pid =
                BytesUtil.byteArray2HexString(finalData.pid)

            try {
                emvParameterInitializer.initEmvParameters(
                    aid,
                    kernelId,
                    pid,
                    Session.ACCOUNT_SERVICE_ENTRY_MODE_CONTACT
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Set gpo to EMV kernel
            val tlvList = TLVList()
            tlvList.addTLV(EMVTag.DEF_TAG_GAC_CONTROL, byteArrayOf(EMV.GAC_NORMAL))

            emv.responseEvent(tlvList.toString())
        } catch (e: Exception) {
            stopEMVSearch()
        }
    }

    /**
     * Handle read record.
     */
    @Throws(RemoteException::class)
    fun handleReadRecord(cardRecord: CardRecord) {
        this.cardRecord = cardRecord

        // Save the expiration.
        var dateExpiration =
            BytesUtil.byteArray2HexString(
                cardRecord.expiry
            )
        dateExpiration = dateExpiration.substring(2, 4) + dateExpiration.substring(4, 6)

        // Save card number.
        val cardNumber = Util.getDigits(
            BytesUtil.byteArray2HexString(
                cardRecord.pan
            )
        )

        onConfirmCardRecord(cardNumber, dateExpiration)

    }

    @Throws(RemoteException::class)
    private fun handleCardHolderVerify(cvmMethod: CVMMethod) {
        when (cvmMethod.cvm) {
            CVMFlag.EMV_CVMFLAG_OFFLINEPIN.toByte() -> {
            }

            CVMFlag.EMV_CVMFLAG_ONLINEPIN.toByte() -> {
//                cardOutput?.setRFU1("ONLINE")
//                inputOnlinePin()
                emv.responseEvent(
                    TLV.fromData(
                        EMVTag.DEF_TAG_CHV_STATUS, byteArrayOf(0x01)
                    ).toString()
                )

            }

            CVMFlag.EMV_CVMFLAG_SIGNATURE.toByte() -> {
//                _CardOutput.setRFU1("Signature")
//                _CardOutput.setPinType("signature")
//                if (_CardOutput.getRID().equalsIgnoreCase(Constant.VisaRID)) {
//                    _CardOutput.setRFU1("Signature VISA")
//                    successResponse.processSignature()
//                }
//                //if transaction config has pin input enable than may be we go for Select Verification option
//                byePassOnlinePin()
            }

            CVMFlag.EMV_CVMFLAG_NOCVM.toByte() -> {
//                _CardOutput.setRFU1("NOCVM")
//                if (transactionConfig.isPinInputNeeded()) successResponse.SelectVerificationOption() else {
//                    successResponse.processSignature()
//                    byePassOnlinePin()
            }
        }
    }


    private fun finalizingRequestForEMV() {
        if (isEMVProcess) {
            addRequestForEMV()
        } else {
            //handle required params in case of SWIPE etc.

//            String PANSeqNo = "00";
//            _CardOutput.setPANSEQ(PANSeqNo);
        }
    }

    private fun addRequestForEMV() {
        try {
            val isContactLessTxn =
                cardOutput?.insertMode.equals(
                    Constant.POS_ENTRY_MODE_CONTACTLESS,
                    true
                )

            val track2Data: String = emv.getTLV(EMVTag.EMV_TAG_IC_TRACK2DATA)

            val tlvList = TLVList()

            val cryptogramInformationData: String =
                emv.getTLV(EMVTag.EMV_TAG_IC_CID) //Cryptogram Information Data
            if (cryptogramInformationData.isNotEmpty()) {
                tlvList.addTLV(
                    TLV.fromData(
                        "9F27",
                        BytesUtil.hexString2Bytes(
                            cryptogramInformationData
                        )
                    )
                )
            }

            val authAmount: String = emv.getTLV(EMVTag.EMV_TAG_TM_AUTHAMNTN) //Authorized Amount
            tlvList.addTLV(
                TLV.fromData(
                    EMVTag.EMV_TAG_TM_AUTHAMNTN,
                    BytesUtil.hexString2Bytes(
                        authAmount
                    )
                )
            )

            val otherAmount: String = emv.getTLV(EMVTag.EMV_TAG_TM_OTHERAMNTN) //Other Amount
            tlvList.addTLV(
                TLV.fromData(
                    EMVTag.EMV_TAG_TM_OTHERAMNTN,
                    BytesUtil.hexString2Bytes(
                        otherAmount
                    )
                )
            )

            val acquirerRequestCryptogram: String = emv.getTLV(EMVTag.EMV_TAG_IC_AC)
            if (acquirerRequestCryptogram.isNotEmpty()) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_IC_AC,
                        BytesUtil.hexString2Bytes(
                            acquirerRequestCryptogram
                        )
                    )
                )
            }

            val applicationInterchangeProfile: String =
                emv.getTLV("82") //Application Interchange Profile
            if (applicationInterchangeProfile.isNotEmpty()) {
                tlvList.addTLV(
                    TLV.fromData(
                        "82",
                        BytesUtil.hexString2Bytes(
                            applicationInterchangeProfile
                        )
                    )
                )
            }

            val applicationTransactionCounter: String =
                emv.getTLV(EMVTag.EMV_TAG_IC_ATC) //Application Transaction Counter
            if (applicationTransactionCounter.isNotEmpty()) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_IC_ATC,
                        BytesUtil.hexString2Bytes(
                            applicationTransactionCounter
                        )
                    )
                )
            }

            if (isContactLessTxn) {
                val tag9F7C: String = emv.getTLV(EMVTag.M_TAG_TM_9F7C)
                if (tag9F7C.isNotEmpty()) {
                    tlvList.addTLV(
                        TLV.fromData(
                            EMVTag.M_TAG_TM_9F7C,
                            BytesUtil.hexString2Bytes(
                                tag9F7C
                            )
                        )
                    )
                }
                val tag9F6E: String = emv.getTLV("9F6E")
                if (tag9F6E.isNotEmpty()) {
                    tlvList.addTLV(
                        TLV.fromData(
                            "9F6E",
                            BytesUtil.hexString2Bytes(
                                tag9F6E
                            )
                        )
                    )
                }
            }

            val issuerAppData: String =
                emv.getTLV(EMVTag.EMV_TAG_IC_ISSAPPDATA) //Issuer Application Data
            if (issuerAppData.isNotEmpty()) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_IC_ISSAPPDATA,
                        BytesUtil.hexString2Bytes(
                            issuerAppData
                        )
                    )
                )
            }

            val terminalCapabilities: String =
                emv.getTLV(EMVTag.EMV_TAG_TM_CAP) //Terminal Capabilities
            if (terminalCapabilities.isNotEmpty()) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_CAP,
                        BytesUtil.hexString2Bytes(
                            terminalCapabilities
                        )
                    )
                )
            }

            val txnCurrencyCode: String =
                Constant.baseCurrencyCode // Terminal - Transaction Currency Code
            if (txnCurrencyCode.isNotEmpty()) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_CURCODE,
                        BytesUtil.hexString2Bytes(
                            txnCurrencyCode
                        )
                    )
                )
            }

            val terminalCountryCode: String = Constant.baseCountryCode //Terminal Country Code
            if (terminalCountryCode.isNotEmpty()) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_CNTRYCODE,
                        BytesUtil.hexString2Bytes(
                            terminalCountryCode
                        )
                    )
                )
            }

            val terminalVerificationResult: String =
                emv.getTLV(EMVTag.EMV_TAG_TM_TVR) //Terminal Verification Result
            if (terminalVerificationResult.isNotEmpty() && terminalVerificationResult != "0000000000") {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_TVR,
                        BytesUtil.hexString2Bytes(
                            terminalVerificationResult
                        )
                    )
                )
            }

            val txnDate: String = emv.getTLV(EMVTag.EMV_TAG_TM_TRANSDATE) //Transaction Date
            if (txnDate.isNotEmpty()) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_TRANSDATE,
                        BytesUtil.hexString2Bytes(
                            txnDate
                        )
                    )
                )
            }

            val txnStatusInformation: String =
                emv.getTLV(EMVTag.EMV_TAG_TM_TSI) //Transaction Status Information
            val txnType: String = emv.getTLV(EMVTag.EMV_TAG_TM_TRANSTYPE) //Transaction Type
            if (txnType.isNotEmpty()) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_TRANSTYPE,
                        BytesUtil.hexString2Bytes(
                            txnType
                        )
                    )
                )
            }

            val unpredictableNo: String =
                emv.getTLV(EMVTag.EMV_TAG_TM_UNPNUM) //Unpredictable Number
            if (unpredictableNo.isNotEmpty()) tlvList.addTLV(
                TLV.fromData(
                    EMVTag.EMV_TAG_TM_UNPNUM,
                    BytesUtil.hexString2Bytes(
                        unpredictableNo
                    )
                )
            )

            val cardHolderVerificationMethod: String =
                emv.getTLV(EMVTag.EMV_TAG_TM_CVMRESULT) //Cardholder Verification Method
            if (cardHolderVerificationMethod.isNotEmpty() && cardHolderVerificationMethod != "000000") {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_CVMRESULT,
                        BytesUtil.hexString2Bytes(
                            cardHolderVerificationMethod
                        )
                    )
                )
            }

            val panSeqNo: String = emv.getTLV(EMVTag.EMV_TAG_IC_PANSN) //PAN Sequence No.

            if (!isContactLessTxn && panSeqNo.isNotEmpty()) {
                if (!cardOutput?.RID?.startsWith("A000000065")!!) {
                    tlvList.addTLV(
                        TLV.fromData(
                            EMVTag.EMV_TAG_IC_PANSN,
                            BytesUtil.hexString2Bytes(
                                panSeqNo
                            )
                        )
                    )
                }
            }
            val tacDefault: String = emv.getTLV("DF918110")
            val tacTACDenial: String = emv.getTLV("DF918111")
            val tacOnline: String = emv.getTLV("DF918112")

            if (cardOutput?.cardAID.equals("A0000006021010", true)) { // for debit only
                var cardDetails: String? = emv.getTLV(EMVTag.EMV_TAG_IC_PAN) //Card Details.
                if (cardDetails.isNullOrEmpty().not()) {
                    cardDetails = if (cardDetails?.length == 16) {
                        BytesUtil.toSpecificSizeString(
                            cardDetails, 16, "F", "left"
                        )
                    } else {
                        BytesUtil.toSpecificSizeString(
                            cardDetails, 20, "F", "left"
                        )
                    }
                    tlvList.addTLV(
                        TLV.fromData(
                            EMVTag.EMV_TAG_IC_PAN,
                            BytesUtil.hexString2Bytes(
                                cardDetails
                            )
                        )
                    )
                } else if (cardOutput != null && cardOutput?.cardNo?.length!! > 0) {
                    cardDetails =
                        BytesUtil.toSpecificSizeString(
                            cardOutput?.cardNo, 20, "F", "left"
                        )
                    tlvList.addTLV(
                        TLV.fromData(
                            EMVTag.EMV_TAG_IC_PAN,
                            BytesUtil.hexString2Bytes(
                                cardDetails
                            )
                        )
                    )
                }
            }

            val appIdentification: String? = emv.getTLV("84") //Application Identification
            if (appIdentification.isNullOrEmpty().not()) {
                if (!isContactLessTxn) {
                    tlvList.addTLV(
                        TLV.fromData(
                            "84",
                            BytesUtil.hexString2Bytes(
                                appIdentification
                            )
                        )
                    )
                } else if (!cardOutput?.cardAppName?.lowercase(Locale.ROOT)?.startsWith("visa")!!) {
                    tlvList.addTLV(
                        TLV.fromData(
                            "84",
                            BytesUtil.hexString2Bytes(
                                appIdentification
                            )
                        )
                    )
                }
            }

            val appVersionNumber: String = emv.getTLV(EMVTag.EMV_TAG_TM_APPVERNO)
            if (appVersionNumber.isNotEmpty() && !isContactLessTxn) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_APPVERNO,
                        BytesUtil.hexString2Bytes(
                            appVersionNumber
                        )
                    )
                )
            }

            var serialNo: String = deviceManagerUtility.getSerialNumberDevice()
            if (serialNo.length > 8) {
                serialNo = serialNo.substring(0, 8)
            }

            val deviceSerialNumber: String =
                Util.toHex(serialNo) //Terminal - Interface Device Serial Number
            if (deviceSerialNumber.isNotEmpty() && !isContactLessTxn) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_IFDSN,
                        BytesUtil.hexString2Bytes(
                            deviceSerialNumber
                        )
                    )
                )
            }

            val terminalType: String? = emv.getTLV(EMVTag.EMV_TAG_TM_TERMTYPE) //Terminal Type
            if (terminalType.isNullOrEmpty().not() && !isContactLessTxn) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.EMV_TAG_TM_TERMTYPE,
                        BytesUtil.hexString2Bytes(
                            terminalType
                        )
                    )
                )
            }

            var tag9F53: String = emv.getTLV(EMVTag.M_TAG_TM_9F53)
            //CVR (Card Verification Result) or TxnCategoryCode
            if (tag9F53.isNotEmpty() && !isContactLessTxn) {
                tlvList.addTLV(
                    TLV.fromData(
                        EMVTag.M_TAG_TM_9F53,
                        BytesUtil.hexString2Bytes(
                            tag9F53
                        )
                    )
                )
            } else if (cardOutput != null && cardOutput?.transactionCategoryCode.isNullOrEmpty()
                    .not() && cardOutput?.transactionCategoryCode?.length!! > 0 && !isContactLessTxn
            ) { //
                tag9F53 = cardOutput?.transactionCategoryCode!!
                if (!cardOutput?.RID?.startsWith("A000000065")!!) tlvList.addTLV(
                    TLV.fromData(
                        "9F53",
                        BytesUtil.hexString2Bytes(
                            tag9F53
                        )
                    )
                )
            }
            tlvList.addTLV(
                TLV.fromData(
                    EMVTag.EMV_TAG_IC_CHNAME,
                    BytesUtil.hexString2Bytes(
                        emv.getTLV(EMVTag.EMV_TAG_IC_CHNAME)
                    )
                )
            )
            tlvList.addTLV(
                TLV.fromData(
                    EMVTag.EMV_TAG_IC_TRACK2DATA,
                    BytesUtil.hexString2Bytes(
                        emv.getTLV(EMVTag.EMV_TAG_IC_TRACK2DATA)
                    )
                )
            )
            tlvList.addTLV(
                TLV.fromData(
                    EMVTag.EMV_TAG_IC_PAN,
                    BytesUtil.hexString2Bytes(
                        emv.getTLV(EMVTag.EMV_TAG_IC_PAN)
                    )
                )
            )

            val iacOnline: String = emv.getTLV("9F0F")
            val iacDefault: String = emv.getTLV("9F0D")
            val iacDenial: String = emv.getTLV("9F0E")

            cardOutput?.let {
                it.cardTransactionCount = applicationTransactionCounter
                it.EMVData = tlvList.toString()
                it.track2Data = track2Data
                it.transactionCertificate = acquirerRequestCryptogram
                it.TVRData = terminalVerificationResult
                it.TSIData = txnStatusInformation
                it.txnCategoryCode = tag9F53
                it.PANSEQ = panSeqNo
                it.terminalCapability = terminalCapabilities
                it.additionalTerminalCapability = "F000F0A001"
                it.txnDate = txnDate
                it.txnAmount = authAmount
                it.otherAmount = otherAmount
                it.applicationInterchangeProfile = applicationInterchangeProfile
                it.cardHolderVerificationMethod = cardHolderVerificationMethod
                it.issuerApplicationData = issuerAppData
                it.unpredictableNumber = unpredictableNo
                it.TACDefault = tacDefault
                it.TACDenial = tacTACDenial
                it.TACOnline = tacOnline
                it.IACDefault = iacDefault
                it.IACDenial = iacDenial
                it.IACOnline = iacOnline
            }

            Log.d(TAG, "CardOutput: $cardOutput")
        } catch (ex: Exception) {
            Log.e("EMV Error", ex.localizedMessage?.toString() ?: "")
        }
    }


    /**
     * Show card record view.
     */
    private fun onConfirmCardRecord(pan: String, date: String) {
        // Here we are managing our Output
//        val entryMode = session?.accountEntryMode!!
        val firstCurrencyCode = ""
        val aid = ""
        val cardHolderName = Util.hexToAscii(emv.getTLV(EMVTag.EMV_TAG_IC_CHNAME))
        val applicationName = Util.hexToAscii(emv.getTLV(EMVTag.EMV_TAG_IC_APNAME))
        val track2 = ""

//        try {
//            if (!(session?.cardholderName != null && session?.cardholderName?.length!! > 0)) {
//                cardHolderName = Util.hexToAscii(emv.getTLV("5F20")) ?: ""
//                if (cardHolderName.isNotEmpty()) {
//                    cardHolderName = cardHolderName.trim().replace("/", "")
//                    session!!.cardholderName = cardHolderName
//                }
//
//                applicationName = Util.hexToAscii(emv.getTLV("9F12")) ?: ""
//                session!!.issuerMessage = applicationName
//            } else {
//                cardHolderName = session?.cardholderName ?: ""
//            }
//
//            track2 = session?.track2Data ?: ""
//
//            aid = session?.aid ?: ""
//
//            session?.aid = aid
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

        val newCardOutput = CardReadOutput().apply {
            cardAID = aid
            cardAppName = applicationName
            cardExpiry = date
            currencyCode = firstCurrencyCode
//            insertModeCode = entryMode
//            insertMode = cardCustomInsertMode(entryMode)
            customerName = cardHolderName
//            cardTransactionCount = transactionCount
        }

        cardOutput = newCardOutput
        cardOutput!!.transactionCategoryCode = "00"

        if (!newCardOutput.insertMode.equals("swipe", ignoreCase = true)) {
            newCardOutput.cardNo = pan
            newCardOutput.track2Data = track2
        }

        finalCardReadOutput = cardOutput


        // for ic card or rf card, response the card result to emv.
        val tlvList = TLVList()


        tlvList.addTLV(
            EMVTag.EMV_TAG_TM_AUTHAMNTN,
            BytesUtil.toBCDAmountBytes(authAmount)
        )
        tlvList.addTLV(
            EMVTag.EMV_TAG_TM_OTHERAMNTN,
            BytesUtil.toBCDAmountBytes(
                0L
            )
        )
        tlvList.addTLV(
            EMVTag.EMV_TAG_TM_TRANSDATE,
            BytesUtil.hexString2ByteArray(
                Date().toCurrentFormat("yyMMdd")
            )
        )
        tlvList.addTLV(
            EMVTag.EMV_TAG_TM_TRANSTIME,
            BytesUtil.hexString2ByteArray(
                Date().toCurrentFormat("HHmmss")
            )
        )
//            tlvList.addTLV(
//                EMVTag.EMV_TAG_TM_TRSEQCNTR,
//                BytesUtil.hexString2ByteArray(session?.systemTraceAuditNumber ?: "00000000")
//            )
        if (isRefund) {
            tlvList.addTLV(
                EMVTag.DEF_TAG_SERVICE_TYPE,
                byteArrayOf(0x20)
            )
        } else {
            tlvList.addTLV(
                EMVTag.DEF_TAG_SERVICE_TYPE,
                byteArrayOf(0x00)
            )
        }
        tlvList.addTLV(
            EMVTag.DEF_TAG_START_RECOVERY,
            byteArrayOf(0x00.toByte())
        ) // 0- false, 1- true

        // Accumulated amount.
        tlvList.addTLV(
            EMVTag.DEF_TAG_ACCUMULATE_AMOUNT,
            BytesUtil.toBCDAmountBytes(
                0L
            )
        )

        // Pan in black.
        tlvList.addTLV(
            EMVTag.DEF_TAG_PAN_IN_BLACK,
            byteArrayOf(0x0.toByte())
        ) // 0- false, 1- true


        emv.responseEvent(tlvList.toString())
    }

    override fun stopEMVSearch() {
        emv.stopProcess()
        emv.stopSearch()
        emv.halt()
    }
}