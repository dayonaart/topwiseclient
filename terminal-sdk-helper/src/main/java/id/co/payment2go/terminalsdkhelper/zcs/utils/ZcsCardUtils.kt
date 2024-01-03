package id.co.payment2go.terminalsdkhelper.zcs.utils

import android.content.Context
import android.util.Log
import com.zcs.sdk.SdkResult
import com.zcs.sdk.card.CardReaderManager
import com.zcs.sdk.card.CardReaderTypeEnum
import com.zcs.sdk.card.CardSlotNoEnum
import com.zcs.sdk.card.ICCard
import com.zcs.sdk.card.MagCard
import com.zcs.sdk.emv.EmvData
import com.zcs.sdk.emv.EmvHandler
import com.zcs.sdk.emv.EmvTermParam
import com.zcs.sdk.emv.EmvTransParam
import com.zcs.sdk.listener.OnSearchCardListener
import com.zcs.sdk.util.StringUtils
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.TermLog
import id.co.payment2go.terminalsdkhelper.core.toCurrentFormat
import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsUtility.checkTrack
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsUtility.loadCAPK1408
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsUtility.loadCAPK1984
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsUtility.loadNSICCS1408
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsUtility.loadNSICCS1984
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.io.IOException
import java.util.Date


object ZcsCardUtils : ZcsEmvListener {
    private const val TAG = "ZcsCardUtils"
    private val _readTagField =
        ZcsEmvTag::class.java.fields.filter { !it.name.contains("INSTANCE") && !it.name.contains("ATCAP") }
            .drop(1)
    private lateinit var magCard: MagCard
    private val emvTag = IntArray(_readTagField.size)
    private val sendTLVTAG = ByteArray(_readTagField.size)
    override lateinit var icCard: ICCard
    private const val readTimeout = 60 * 1000
    private lateinit var zcsReaderOutput: MutableStateFlow<ZcsCardReaderOutput?>
    override lateinit var emvHandler: EmvHandler
    private val emvTransParam: EmvTransParam = EmvTransParam()
    private val emvTermParam: EmvTermParam = EmvTermParam()
    lateinit var STAN: String

    init {
        _readTagField.forEachIndexed { i, v ->
            val tagInt = v.get(v.name) as Int
            emvTag[i] = tagInt
            sendTLVTAG[i] = tagInt.toByte()
        }
    }

    fun initCardReader(
        cardReaderManager: CardReaderManager,
        onSearchCardListener: OnSearchCardListener,
        emvHandler: EmvHandler,
        transactionAmount: Long,
        transactionOtherAmount: Long,
        stan: String,
    ) {
        this.emvHandler = emvHandler
        val fixAmount = BytesUtil.toBCDAmountBytes(transactionAmount)
        val fixAnotherAmount = BytesUtil.toBCDAmountBytes(transactionOtherAmount)
        emvTransParam.amountAuth = BytesUtil.byteArray2HexString(fixAmount)
        emvTransParam.amountOther = BytesUtil.byteArray2HexString(fixAnotherAmount)
        emvTransParam.isForceOnline = 0x00
        emvTransParam.transTime = Date().toCurrentFormat("yy/MM/dd hh:mm:ss")
        STAN = Util.addZerosToNumber(stan, 8)
        magCard = cardReaderManager.magCard
        icCard = cardReaderManager.icCard
        zcsReaderOutput = MutableStateFlow(null)
        cardReaderManager.searchCard(
            CardReaderTypeEnum.MAG_IC_CARD,
            readTimeout,
            onSearchCardListener
        )
    }

    val collectData = callbackFlow<Resource<CardReadOutput>> {
        zcsReaderOutput.collectLatest {
            when (it) {
                is ZcsCardReaderOutput.ReadICCard -> {
                    Log.d(TAG, ZcsLogAscii.successEmv)
                    this.send(Resource.Success(data = it.cardReadOutput))
                }

                is ZcsCardReaderOutput.ReadMagCard -> {
                    Log.d(TAG, ZcsLogAscii.successEmv)
                    this.send(Resource.Success(data = it.cardReadOutput))
                }

                ZcsCardReaderOutput.Loading -> {
                    Log.d(TAG, ZcsLogAscii.loading)
                    this.send(Resource.Loading())
                }

                is ZcsCardReaderOutput.OnError -> {
                    Log.d(TAG, ZcsLogAscii.errorEmv)
                    this.send(Resource.Error(message = it.message))
                }

                else -> {

                }
            }
        }
    }

    suspend fun readMagCard() {
        zcsReaderOutput.emit(ZcsCardReaderOutput.Loading)
        val res = magCard.magReadData
        if (res.resultcode == SdkResult.SDK_OK) {
            val output = res.tk2.checkTrack()
            when (output.zcsCheckTrack) {
                ZcsCheckTrack.EXPIRED -> {
                    zcsReaderOutput.emit(ZcsCardReaderOutput.OnError(message = output.res))
                }

                ZcsCheckTrack.IS_IC_CARD -> {
//                    TermLog.d(TAG, output.res)
                    zcsReaderOutput.emit(ZcsCardReaderOutput.OnError(message = output.res))
                }

                ZcsCheckTrack.SUCCESS -> {
                    zcsReaderOutput.emit(
                        ZcsCardReaderOutput.ReadMagCard(
                            cardReadOutput = CardReadOutput(
                                cardNo = res.cardNo,
                                track2Data = output.res,
                                posEntryMode = Constant.POS_ENTRY_MODE_SWIPE,
                            )
                        )
                    )
                }
            }

        } else {
            zcsReaderOutput.emit(
                ZcsCardReaderOutput.OnError(message = "Card Not Found")
            )
        }
    }

    suspend fun readICCard(context: Context) {
        zcsReaderOutput.emit(ZcsCardReaderOutput.Loading)
        val resetIcCard = icCard.icCardReset(CardSlotNoEnum.SDK_ICC_USERCARD)
        if (resetIcCard != SdkResult.SDK_OK) {
            TermLog.d(TAG, "CANNOT RESET SDK ICC USER CARD $resetIcCard")
            zcsReaderOutput.emit(ZcsCardReaderOutput.OnError(message = "Mohon ulangi kembali"))
        } else {
            val recvLen = IntArray(1)
            val recvData = ByteArray(256)
            val ret =
                icCard.icExchangeAPDU(
                    CardSlotNoEnum.SDK_ICC_USERCARD,
                    sendTLVTAG,
                    recvData,
                    recvLen
                )
            if (ret == SdkResult.SDK_OK) {
                startEmv(context)
            } else {
                TermLog.d(TAG, "CANNOT SEND icExchangeAPDU $ret")
                zcsReaderOutput.emit(ZcsCardReaderOutput.OnError(message = "Mohon ulangi kembali"))
            }
        }
    }

    private suspend fun startEmv(context: Context) {
        emvHandler.delAllApp()
        emvHandler.delAllCapk()
        val emvPath = EmvTermParam.emvParamFilePath
        try {
            if (!File(emvPath).exists()) {
                ZcsFileUtil.doCopy(context, "emv", EmvTermParam.emvParamFilePath)
            }
        } catch (e: IOException) {
//            TermLog.d(TAG, "${e.message}")
            zcsReaderOutput.emit(ZcsCardReaderOutput.OnError(message = "${e.message}"))
            return
        }
        emvTransParam.transKernalType = EmvData.KERNAL_EMV_PBOC
        emvHandler.transParamInit(emvTransParam)
        emvHandler.kernelInit(emvTermParam)

        //LOAD AID AND CAPK
        if (!loadNSICCS1408()) {
            zcsReaderOutput.emit(ZcsCardReaderOutput.OnError(message = "Mohon ulangi kembali"))
            return
        }
        if (!loadNSICCS1984()) {
            zcsReaderOutput.emit(ZcsCardReaderOutput.OnError(message = "Mohon ulangi kembali"))
            return
        }
        if (!loadCAPK1408()) {
            TermLog.d(TAG, "startEmv -> CANNOT LOAD CAPK 1408")
            return
        }
        if (!loadCAPK1984()) {
            TermLog.d(TAG, "startEmv -> CANNOT LOAD CAPK 1984")
            return
        }
        val pucIsEcTrans = ByteArray(1)
        val pucBalance = ByteArray(6)
        val pucTransResult = ByteArray(1)
        val ret =
            emvHandler.emvTrans(emvTransParam, this, pucIsEcTrans, pucBalance, pucTransResult)
        when (pucTransResult[0]) {
            EmvData.APPROVE_M -> {
                TermLog.d(TAG, "TRANS STATUS: APPROVED")
            }

            EmvData.ONLINE_M -> {
                TermLog.d(TAG, "TRANS STATUS: ONLINE")
            }

            EmvData.DECLINE_M -> {
                TermLog.d(TAG, "TRANS STATUS: DECLINE")
            }

            else -> {
                TermLog.d(TAG, "TRANS STATUS:")
            }
        }
        if (ret == SdkResult.SDK_OK) {
            getEmvData()
        } else {
            TermLog.d(TAG, "TRANS STATUS: CANNOT REGISTER TRANSACTION PARAMS $ret")
            zcsReaderOutput.emit(ZcsCardReaderOutput.OnError(message = "Mohon ulangi kembali"))
            return
        }
    }

    private suspend fun getEmvData() {
        val field55 = emvHandler.packageTlvList(emvTag)
        val emvData = StringUtils.convertBytesToHex(field55)
        val data = _readTagField.mapIndexed { index, field ->
            val tlvTag = emvHandler.getTlvData(emvTag[index])
            if (field.name == "CARDNAME")
                "${field.name to String(tlvTag)}\n"
            else
                "${field.name to BytesUtil.byteArray2HexString(tlvTag)}\n"
        }
        TermLog.d(TAG, "TLV DATA ->\n$data")

        val checkTrack = getTlvData(ZcsEmvTag.T2D).checkTrack()
        when (checkTrack.zcsCheckTrack) {
            ZcsCheckTrack.EXPIRED -> {
                TermLog.d(TAG, checkTrack.res)
                zcsReaderOutput.emit(ZcsCardReaderOutput.OnError(message = checkTrack.res))
            }

            ZcsCheckTrack.IS_IC_CARD -> {
                zcsReaderOutput.emit(ZcsCardReaderOutput.OnError(message = checkTrack.res))
            }

            ZcsCheckTrack.SUCCESS -> {
                zcsReaderOutput.emit(
                    ZcsCardReaderOutput.ReadICCard(
                        cardReadOutput = CardReadOutput(
                            cardNo = getTlvData(ZcsEmvTag.PAN),
                            posEntryMode = Constant.POS_ENTRY_MODE_DIP,
                            track2Data = getTlvData(ZcsEmvTag.T2D),
                            EMVData = emvData,
                            customerName = getTlvData(ZcsEmvTag.CARDNAME),
                            TVRData = getTlvData(ZcsEmvTag.TVR),
                            txnAmount = getTlvData(ZcsEmvTag.TRAMOUNT),
                            otherAmount = getTlvData(ZcsEmvTag.TRANOTHERAMOUNT),
                            terminalCapability = getTlvData(ZcsEmvTag.TCAP),
                            additionalTerminalCapability = getTlvData(ZcsEmvTag.ATCAP),
                            transactionCertificate = getTlvData(ZcsEmvTag.AC),
                            PANSEQ = getTlvData(ZcsEmvTag.PANSEQ),
                            applicationInterchangeProfile = getTlvData(ZcsEmvTag.AIP),
                            cardHolderVerificationMethod = getTlvData(ZcsEmvTag.CVM),
                            issuerApplicationData = getTlvData(ZcsEmvTag.IAD),
                            unpredictableNumber = getTlvData(ZcsEmvTag.UNPREDICTABLE_NUMB),
                            txnDate = getTlvData(0x9A),
                            txnCategoryCode = getTlvData(0x9F53),
                            TSIData = getTlvData(0x9B),
                            IACOnline = getTlvData(0x9F0F),
                            IACDefault = getTlvData(0x9F0D),
                            IACDenial = getTlvData(0x9F0E),
                        )
                    )
                )
            }
        }
    }

    private fun getTlvData(tag: Int): String {
        return try {
            val tagByte = emvHandler.getTlvData(tag)
            if (tag == ZcsEmvTag.CARDNAME) {
                return String(tagByte)
            }
            BytesUtil.byteArray2HexString(tagByte)
        } catch (e: Exception) {
            ""
        }
    }
}