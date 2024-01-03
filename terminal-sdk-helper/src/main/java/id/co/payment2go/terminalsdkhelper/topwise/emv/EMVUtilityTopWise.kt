package id.co.payment2go.terminalsdkhelper.topwise.emv

import id.co.payment2go.terminalsdkhelper.common.emv.EMVUtility
import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.topwise.BindServiceTopWise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class EMVUtilityTopWise(
    private val bindService: BindServiceTopWise
) : EMVUtility {
    override fun searchCard(stan: Long, amount: Long): Flow<Resource<CardReadOutput>> {
        TopWiseUtility.initCardReader(
            bindService.cardManager,
            bindService.emvManager,
            bindService.iConvert,
            bindService.iPacker,
            stan,
            amount,
            bindService.pinpad,
            bindService.deviceSystem
        )
        return TopWiseUtility.collectData.flowOn(Dispatchers.IO)
    }

    override suspend fun startEMV(stan: Long, onFinish: (CardReadOutput?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun stopEMVSearch() {
        bindService.emvManager.cancelCheckCard()
        bindService.emvManager.EMV_FreeCallback()
    }
}