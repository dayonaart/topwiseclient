package id.co.payment2go.terminalsdkhelper.payment_service.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.common.DeviceType
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.DeviceTypeManager
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.PinpadIngenicoUtility
import id.co.payment2go.terminalsdkhelper.sunmi.BindServiceSunmi
import id.co.payment2go.terminalsdkhelper.sunmi.pinpad.PinpadSunmiUtility
import id.co.payment2go.terminalsdkhelper.szzt.BindServiceSzzt
import id.co.payment2go.terminalsdkhelper.szzt.pinpad.PinpadSzztUtility
import id.co.payment2go.terminalsdkhelper.testing.BindServiceTesting
import id.co.payment2go.terminalsdkhelper.testing.pinpad.PinpadTestingUtility
import id.co.payment2go.terminalsdkhelper.topwise.BindServiceTopWise
import id.co.payment2go.terminalsdkhelper.topwise.pinpad.PinpadTopWiseUtility
import id.co.payment2go.terminalsdkhelper.verifone.BindServiceVerifone
import id.co.payment2go.terminalsdkhelper.verifone.pinpad.PinpadVerifoneUtility
import id.co.payment2go.terminalsdkhelper.zcs.BindServiceZcs
import id.co.payment2go.terminalsdkhelper.zcs.pinpad.PinpadZcsUtility
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PinpadModule {

    @Provides
    @Singleton
    fun providePinpadUtility(
        app: Application,
        bindService: BindService,
        deviceTypeManager: DeviceTypeManager
    ): PinpadUtility {
        return when (deviceTypeManager.getDeviceType()) {
            DeviceType.INGENICO -> {
                bindService as BindServiceIngenico
                PinpadIngenicoUtility(app, bindService)
            }

            DeviceType.SUNMI -> {
                bindService as BindServiceSunmi
                PinpadSunmiUtility(bindService)
            }

            DeviceType.ZCS -> {
                bindService as BindServiceZcs
                PinpadZcsUtility(bindService)
            }

            DeviceType.VERIFONE -> {
                bindService as BindServiceVerifone
                PinpadVerifoneUtility(bindService)
            }

            DeviceType.SZZT -> {
                bindService as BindServiceSzzt
                PinpadSzztUtility(bindService)
            }

            DeviceType.TOPWISE -> {
                bindService as BindServiceTopWise
                PinpadTopWiseUtility(bindService)
            }

            DeviceType.TESTING -> {
                bindService as BindServiceTesting
                PinpadTestingUtility(bindService)
            }

        }
    }
}