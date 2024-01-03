package com.example.clientbni

import android.app.Application
import android.content.Intent
import android.os.Build
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.core.EnvironmentManager
import id.co.payment2go.terminalsdkhelper.payment_service.PaymentService
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.CardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject
    lateinit var bindService: BindService

    @Inject
    lateinit var cardRepository: CardRepository

    @Inject
    lateinit var deviceManagerUtility: Lazy<DeviceManagerUtility>

    @Inject
    lateinit var environmentManager: Lazy<EnvironmentManager>

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val TAG = "GSTPaymentService"

    override fun onCreate() {
        super.onCreate()
        coroutineScope.launch {
            bindService.bindServiceSDK()
            cardRepository.init(deviceManagerUtility.get().getSerialNumberDevice())
            launch(Dispatchers.Main) {
                val intent = Intent(this@MyApplication, PaymentService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        }
    }
}