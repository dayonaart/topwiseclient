package id.co.payment2go.terminalsdkhelper.szzt

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.szzt.sdk.device.Device
import com.szzt.sdk.device.DeviceManager
import com.szzt.sdk.device.DeviceManager.DeviceManagerListener.EVENT_SERVICE_CONNECTED
import com.szzt.sdk.device.DeviceManager.DeviceManagerListener.EVENT_SERVICE_DISCONNECTED
import com.szzt.sdk.device.DeviceManager.DeviceManagerListener.EVENT_SERVICE_VERSION_NOT_COMPATABLE
import com.szzt.sdk.device.emv.EmvInterface
import com.szzt.sdk.device.printer.Printer
import com.szzt.sdk.system.SystemManager
import id.co.payment2go.terminalsdkhelper.R
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.core.util.BoundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class BindServiceSzzt(
    private val context: Context
) : BindService, DeviceManager.DeviceManagerListener {

    @Suppress("PrivatePropertyName")
    private val TAG = "SZZT SDK"
    lateinit var mDeviceManager: DeviceManager
    lateinit var mSystemManager: SystemManager
    private var boundService: BoundService? = null
    var mPrint: Printer? = null
    private var isConnect = false
    var getInstance: Context = context


    override suspend fun bindServiceSDK() {
        coroutineScope {
            async(Dispatchers.Main) {
                mDeviceManager = DeviceManager.createInstance(context)
                mDeviceManager.start(this@BindServiceSzzt)
                getSystemManager()
                val printers = mDeviceManager.getDeviceByType(Device.TYPE_PRINTER)
                mPrint = printers?.get(0) as Printer
            }.join()
        }
    }

    fun isDeviceManagerConnected(): Boolean = isConnect

    override fun disconnectSDK() {
        mDeviceManager.stop()
        boundService?.unbind()
    }


    fun getEmvInterface(): EmvInterface = mDeviceManager.emvInterface


    private fun getSystemManager(): SystemManager {
        mSystemManager = SystemManager.getInstance(
            context
        ) { i ->
            Log.d(TAG, "SystemManager -->  SystemManagerListener serviceEventNotify（）！！$i")
            0
        }
        return mSystemManager
    }

    override fun deviceEventNotify(p0: Device?, p1: Int): Int {
        return 0
    }

    override fun serviceEventNotify(p0: Int): Int {
        when (p0) {
            EVENT_SERVICE_CONNECTED -> {
                isConnect = true
            }

            EVENT_SERVICE_VERSION_NOT_COMPATABLE -> {
                Toast.makeText(
                    this@BindServiceSzzt.context,
                    context.getString(R.string.sdk_version_is_not_compatible),
                    Toast.LENGTH_SHORT
                ).show()
            }

            EVENT_SERVICE_DISCONNECTED -> {
                isConnect = false
                mDeviceManager.start(this)
            }
        }
        return 0
    }
}