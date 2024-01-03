package id.co.payment2go.terminalsdkhelper.payment_service.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.payment2go.terminalsdkhelper.common.BindService
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico
import id.co.payment2go.terminalsdkhelper.ingenico.scanner.ScannerIngenicoUtility
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScannerIngenicoModule {

    @Provides
    @Singleton
    fun provideScannerIngenicoUtility(
        bindService: BindService,
        app: Application
    ): ScannerIngenicoUtility {
        bindService as BindServiceIngenico
        return ScannerIngenicoUtility(bindService, app)
    }
}