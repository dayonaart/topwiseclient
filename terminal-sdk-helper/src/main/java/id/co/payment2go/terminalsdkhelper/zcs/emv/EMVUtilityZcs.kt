package id.co.payment2go.terminalsdkhelper.zcs.emv

import android.util.Log
import com.zcs.sdk.card.CardInfoEntity
import com.zcs.sdk.card.CardReaderTypeEnum
import com.zcs.sdk.card.CardSlotNoEnum
import com.zcs.sdk.listener.OnSearchCardListener
import id.co.payment2go.terminalsdkhelper.common.emv.EMVUtility
import id.co.payment2go.terminalsdkhelper.core.TermLog
import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.zcs.BindServiceZcs
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsCardUtils
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsLogAscii
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class EMVUtilityZcs(
    bindService: BindServiceZcs,
) : EMVUtility, OnSearchCardListener {
    private val TAG = "EMVUtilityZcs"
    private val cardReader = bindService.cardReader
    private val zcsCardUtil = ZcsCardUtils
    private val ctx = bindService.ctx
    private val emvHandler = bindService.emvHandler
    override fun searchCard(stan: Long, amount: Long): Flow<Resource<CardReadOutput>> {
        Log.d(TAG, ZcsLogAscii.startEmv)
        zcsCardUtil.initCardReader(
            cardReader,
            this,
            emvHandler,
            amount,
            0,
            stan.toString()
        )
        TermLog.d(TAG, "searchCard -> Please Swipe or Deep your card")
        return zcsCardUtil.collectData.flowOn(Dispatchers.IO)
    }

    override suspend fun startEMV(stan: Long, onFinish: (CardReadOutput?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun stopEMVSearch() {
        CoroutineScope(Dispatchers.IO).launch {
            cardReader.icCard.icCardPowerDown(CardSlotNoEnum.SDK_ICC_USERCARD)
            cardReader.magCard.magCardClose()
            cardReader.magCard.magClearData()
            cardReader.cancelSearchCard()
            cardReader.closeCard()
            Log.d(TAG, ZcsLogAscii.stopEmv)
        }
    }

    override fun onCardInfo(p0: CardInfoEntity?) {
        CoroutineScope(Dispatchers.IO).launch {
            when (p0?.cardExistslot) {
                CardReaderTypeEnum.PSIM1 -> TODO()
                CardReaderTypeEnum.PSIM2 -> TODO()
                CardReaderTypeEnum.PSIM3 -> TODO()
                CardReaderTypeEnum.MAG_CARD -> {
                    ZcsCardUtils.readMagCard()
                    stopEMVSearch()

                }

                CardReaderTypeEnum.IC_CARD -> {
                    ZcsCardUtils.readICCard(ctx)
                    stopEMVSearch()
                }

                CardReaderTypeEnum.RF_CARD -> TODO()
                CardReaderTypeEnum.MAG_IC_CARD -> TODO()
                CardReaderTypeEnum.MAG_RF_CARD -> TODO()
                CardReaderTypeEnum.IC_RF_CARD -> TODO()
                CardReaderTypeEnum.MAG_IC_RF_CARD -> TODO()
                null -> {
                    stopEMVSearch()
                }
            }
        }
    }

    override fun onError(p0: Int) {
//        TermLog.d(TAG, "onError: $p0")
    }

    override fun onNoCard(p0: CardReaderTypeEnum?, p1: Boolean) {
//        TermLog.d(TAG, "onNoCard: $p0")
    }
}