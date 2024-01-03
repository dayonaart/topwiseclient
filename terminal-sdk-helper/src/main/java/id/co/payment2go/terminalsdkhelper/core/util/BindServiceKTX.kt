package id.co.payment2go.terminalsdkhelper.core.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BoundService(
    private val context: Context,
    val name: ComponentName?,
    val service: IBinder?,
    val connection: ServiceConnection
) {
    fun unbind() {
        context.unbindService(connection)
    }
}

suspend fun Context.bindServiceAndWait(intent: Intent, flags: Int) =
    suspendCoroutine { continuation ->
        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                continuation.resume(BoundService(this@bindServiceAndWait, name, service, this))
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // ignore, not much we can do
            }

        }
        this.bindService(intent, conn, flags)
    }