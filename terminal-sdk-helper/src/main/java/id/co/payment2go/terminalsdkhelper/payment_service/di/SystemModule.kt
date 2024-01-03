package id.co.payment2go.terminalsdkhelper.payment_service.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.common.DeviceType
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.core.DeviceTypeManager
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico
import id.co.payment2go.terminalsdkhelper.ingenico.system.application.ApplicationUseCase
import id.co.payment2go.terminalsdkhelper.ingenico.system.application.use_case.ClearAppUserData
import id.co.payment2go.terminalsdkhelper.ingenico.system.device.DeviceManagerUtilityIngenico
import id.co.payment2go.terminalsdkhelper.sunmi.BindServiceSunmi
import id.co.payment2go.terminalsdkhelper.sunmi.system.device.DeviceManagerUtilitySunmi
import id.co.payment2go.terminalsdkhelper.szzt.BindServiceSzzt
import id.co.payment2go.terminalsdkhelper.szzt.system.device.DeviceManagerUtilitySzzt
import id.co.payment2go.terminalsdkhelper.testing.BindServiceTesting
import id.co.payment2go.terminalsdkhelper.testing.system.device.DeviceManagerUtilityTesting
import id.co.payment2go.terminalsdkhelper.topwise.BindServiceTopWise
import id.co.payment2go.terminalsdkhelper.topwise.system.device.DeviceManagerUtilityTopWise
import id.co.payment2go.terminalsdkhelper.verifone.BindServiceVerifone
import id.co.payment2go.terminalsdkhelper.verifone.system.device.DeviceManagerUtilityVerifone
import id.co.payment2go.terminalsdkhelper.zcs.BindServiceZcs
import id.co.payment2go.terminalsdkhelper.zcs.system.device.DeviceManagerUtilityZcs
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SystemModule {

    @Provides
    @Singleton
    fun provideApplicationIngenicoUseCase(bindService: BindService): ApplicationUseCase {
        bindService as BindServiceIngenico
        return ApplicationUseCase(
            clearAppUserData = ClearAppUserData(bindService)
        )
    }

    @Provides
    @Singleton
    fun provideDeviceManagerUtility(
        bindService: BindService,
        deviceTypeManager: DeviceTypeManager
    ): DeviceManagerUtility {
        return when (deviceTypeManager.getDeviceType()) {
            DeviceType.INGENICO -> {
                bindService as BindServiceIngenico
                DeviceManagerUtilityIngenico(bindService)
            }

            DeviceType.SUNMI -> {
                bindService as BindServiceSunmi
                DeviceManagerUtilitySunmi(bindService)
            }

            DeviceType.ZCS -> {
                bindService as BindServiceZcs
                DeviceManagerUtilityZcs(bindService)
            }

            DeviceType.VERIFONE -> {
                bindService as BindServiceVerifone
                DeviceManagerUtilityVerifone(bindService)
            }

            DeviceType.SZZT -> {
                bindService as BindServiceSzzt
                DeviceManagerUtilitySzzt(bindService)
            }

            DeviceType.TOPWISE -> {
                bindService as BindServiceTopWise
                DeviceManagerUtilityTopWise(bindService)
            }

            DeviceType.TESTING -> {
                bindService as BindServiceTesting
                DeviceManagerUtilityTesting(bindService)
            }

        }
    }
}