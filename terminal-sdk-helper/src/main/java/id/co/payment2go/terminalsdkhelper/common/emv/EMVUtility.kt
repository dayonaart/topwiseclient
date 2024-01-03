package id.co.payment2go.terminalsdkhelper.common.emv

import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface EMVUtility {

    fun searchCard(stan: Long, amount: Long): Flow<Resource<CardReadOutput>>

    suspend fun startEMV(stan: Long, onFinish: (CardReadOutput?) -> Unit)

    fun stopEMVSearch()
}