package id.co.payment2go.terminalsdkhelper.payment_service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterUtility
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.DeviceTypeManager
import id.co.payment2go.terminalsdkhelper.core.EnvironmentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Inject

@AndroidEntryPoint
class PaymentService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var notification: Notification
    private var isForegroundServiceStarted = false

    @Inject
    lateinit var printerUtility: Lazy<PrinterUtility>

    @Inject
    lateinit var environmentManager: EnvironmentManager

    @Inject
    lateinit var deviceTypeManager: DeviceTypeManager

    override fun onCreate() {
        super.onCreate()

        notification =
            NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("GST Payment Service ${environmentManager.getEnvironment()}")
                .setContentText("${deviceTypeManager.getDeviceType().name} Service is running")
                .setSmallIcon(androidx.constraintlayout.widget.R.drawable.abc_cab_background_internal_bg)
                .setUsesChronometer(false)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isForegroundServiceStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    Constant.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(Constant.NOTIFICATION_ID, notification)
            }
            isForegroundServiceStarted = true
        }

        val printValue = intent?.extras?.getString("PRINT_DATA")
        if (printValue != null) {
            try {
                printerUtility.get().buildPrintTemplate(printValue)
            } catch (e: Exception) {
                sendBroadcast(
                    Intent("com.example.paymentservice.RESPONSE_BROADCAST").putExtra(
                        "PRINT_RESPONSE",
                        "jsonFormatError"
                    )
                )
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            Constant.NOTIFICATION_CHANNEL_ID,
            "GST Payment Service",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationChannel.description = "GST Payment Service"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun onDestroy() {
        isForegroundServiceStarted = false
        super.onDestroy()
        serviceScope.cancel()
    }
}