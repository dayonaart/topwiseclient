package id.co.payment2go.terminalsdkhelper.payment_service.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.common.DeviceType
import id.co.payment2go.terminalsdkhelper.core.DeviceTypeManager
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico
import id.co.payment2go.terminalsdkhelper.sunmi.BindServiceSunmi
import id.co.payment2go.terminalsdkhelper.szzt.BindServiceSzzt
import id.co.payment2go.terminalsdkhelper.testing.BindServiceTesting
import id.co.payment2go.terminalsdkhelper.topwise.BindServiceTopWise
import id.co.payment2go.terminalsdkhelper.verifone.BindServiceVerifone
import id.co.payment2go.terminalsdkhelper.zcs.BindServiceZcs
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BindServiceModule {

    @Provides
    @Singleton
    fun provideBindService(
        @ApplicationContext context: Context,
        deviceTypeManager: DeviceTypeManager
    ): BindService {
        return when (deviceTypeManager.getDeviceType()) {
            DeviceType.INGENICO -> BindServiceIngenico(context)
            DeviceType.SUNMI -> BindServiceSunmi(context)
            DeviceType.ZCS -> BindServiceZcs(context)
            DeviceType.VERIFONE -> BindServiceVerifone(context)
            DeviceType.SZZT -> BindServiceSzzt(context)
            DeviceType.TOPWISE -> BindServiceTopWise(context)
            DeviceType.TESTING -> BindServiceTesting(context)
        }
    }
}