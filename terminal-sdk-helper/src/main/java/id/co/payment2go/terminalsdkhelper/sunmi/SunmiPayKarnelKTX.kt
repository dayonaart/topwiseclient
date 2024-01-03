package id.co.payment2go.terminalsdkhelper.sunmi

import android.content.Context
import sunmi.paylib.SunmiPayKernel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun SunmiPayKernel.initPaySDKAwait(context: Context): SunmiPayKernel {
    return suspendCoroutine {
        this.initPaySDK(context, object : SunmiPayKernel.ConnectCallback {
            override fun onConnectPaySDK() {
                it.resume(this@initPaySDKAwait)
            }

            override fun onDisconnectPaySDK() {
            }

        })
    }
}