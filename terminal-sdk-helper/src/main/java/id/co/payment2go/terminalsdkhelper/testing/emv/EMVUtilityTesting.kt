package id.co.payment2go.terminalsdkhelper.testing.emv

import android.util.Log
import id.co.payment2go.terminalsdkhelper.common.emv.EMVUtility
import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.testing.BindServiceTesting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EMVUtilityTesting(
    bindService: BindServiceTesting,
) : EMVUtility {
    override fun searchCard(stan: Long, amount: Long): Flow<Resource<CardReadOutput>> {
        return flow {
            emit(Resource.Success(data = CardReadOutput("1234567890123456")))
        }
    }

    override suspend fun startEMV(stan: Long, onFinish: (CardReadOutput?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun stopEMVSearch() {
        Log.d("TAG", "stopEMVSearch: ")
    }
}