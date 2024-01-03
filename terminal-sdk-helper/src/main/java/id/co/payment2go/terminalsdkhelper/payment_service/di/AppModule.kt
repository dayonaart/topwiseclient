package id.co.payment2go.terminalsdkhelper.payment_service.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.core.DeviceTypeManager
import id.co.payment2go.terminalsdkhelper.core.EncryptionHelper
import id.co.payment2go.terminalsdkhelper.core.EnvironmentManager
import id.co.payment2go.terminalsdkhelper.core.JsonUtility
import id.co.payment2go.terminalsdkhelper.core.LogonManager
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.BniDebitService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEncryptionHelper(): EncryptionHelper = EncryptionHelper()

    @Provides
    @Singleton
    fun provideShredPrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("bni_agen_46", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideEnvironmentManager(app: Application, sharedPreferences: SharedPreferences) =
        EnvironmentManager(app, sharedPreferences)

    @Provides
    @Singleton
    fun provideDeviceTypeManager(): DeviceTypeManager = DeviceTypeManager()

    @Provides
    @Singleton
    fun provideLogonManager(
        pinpadUtility: PinpadUtility,
        bniDebitService: BniDebitService,
        sharedPreferences: SharedPreferences,
        deviceManagerUtility: DeviceManagerUtility
    ): LogonManager = LogonManager(
        pinpadUtility = pinpadUtility,
        bniDebitService = bniDebitService,
        sharedPrefs = sharedPreferences,
        deviceManagerUtility = deviceManagerUtility
    )

    @Provides
    @Singleton
    fun provideJsonUtility(sharedPreferences: SharedPreferences) = JsonUtility(sharedPreferences)
}