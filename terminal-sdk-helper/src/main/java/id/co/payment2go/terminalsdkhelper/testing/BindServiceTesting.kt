package id.co.payment2go.terminalsdkhelper.testing

import android.content.Context
import android.util.Log
import id.co.payment2go.terminalsdkhelper.common.BindService

class BindServiceTesting(
    private val context: Context
) : BindService {
    override suspend fun bindServiceSDK() {
        Log.d("TAG", "bindServiceSDK: TESTING BIND")
    }

    override fun disconnectSDK() {
        TODO("Not yet implemented")
    }

}