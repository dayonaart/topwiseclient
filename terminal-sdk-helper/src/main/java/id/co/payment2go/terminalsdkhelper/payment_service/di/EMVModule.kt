package id.co.payment2go.terminalsdkhelper.payment_service.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.common.DeviceType
import id.co.payment2go.terminalsdkhelper.common.emv.EMVUtility
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.core.DeviceTypeManager
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico
import id.co.payment2go.terminalsdkhelper.ingenico.emv.EMVUtilityIngenico
import id.co.payment2go.terminalsdkhelper.sunmi.BindServiceSunmi
import id.co.payment2go.terminalsdkhelper.sunmi.emv.EMVUtilitySunmi
import id.co.payment2go.terminalsdkhelper.szzt.BindServiceSzzt
import id.co.payment2go.terminalsdkhelper.szzt.emv.EMVUtilitySzzt
import id.co.payment2go.terminalsdkhelper.testing.BindServiceTesting
import id.co.payment2go.terminalsdkhelper.testing.emv.EMVUtilityTesting
import id.co.payment2go.terminalsdkhelper.topwise.BindServiceTopWise
import id.co.payment2go.terminalsdkhelper.topwise.emv.EMVUtilityTopWise
import id.co.payment2go.terminalsdkhelper.verifone.BindServiceVerifone
import id.co.payment2go.terminalsdkhelper.verifone.emv.EMVUtilityVerifone
import id.co.payment2go.terminalsdkhelper.zcs.BindServiceZcs
import id.co.payment2go.terminalsdkhelper.zcs.emv.EMVUtilityZcs
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EMVModule {

    @Provides
    @Singleton
    fun provideEMVUtility(
        bindService: BindService,
        deviceManagerUtility: DeviceManagerUtility,
        deviceTypeManager: DeviceTypeManager
    ): EMVUtility {
        return when (deviceTypeManager.getDeviceType()) {
            DeviceType.INGENICO -> {
                bindService as BindServiceIngenico
                EMVUtilityIngenico(bindService, deviceManagerUtility)
            }

            DeviceType.SUNMI -> {
                bindService as BindServiceSunmi
                EMVUtilitySunmi(bindService)
            }

            DeviceType.ZCS -> {
                bindService as BindServiceZcs
                EMVUtilityZcs(bindService)
            }

            DeviceType.VERIFONE -> {
                bindService as BindServiceVerifone
                EMVUtilityVerifone(bindService)
            }

            DeviceType.SZZT -> {
                bindService as BindServiceSzzt
                EMVUtilitySzzt(bindService)
            }

            DeviceType.TOPWISE -> {
                bindService as BindServiceTopWise
                EMVUtilityTopWise(bindService)
            }

            DeviceType.TESTING -> {
                bindService as BindServiceTesting
                EMVUtilityTesting(bindService)
            }

        }
    }
}