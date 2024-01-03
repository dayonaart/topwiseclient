package id.co.payment2go.terminalsdkhelper.payments.debit_card.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.payment2go.terminalsdkhelper.core.EncryptionHelper
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.BniCashService
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.BniDebitService
import id.co.payment2go.terminalsdkhelper.payments.debit_card.data.CardRepositoryImpl
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.CardRepository
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.util.EncryptPINBlock
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.util.StanManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CardModule {

    @Provides
    @Singleton
    fun provideCardRepository(
        bniDebitService: BniDebitService,
        sharedPreferences: SharedPreferences
    ): CardRepository {
        return CardRepositoryImpl(
            bniDebitService,
            sharedPrefs = sharedPreferences
        )
    }

    @Provides
    @Singleton
    fun provideEncryptPINBlock(encryptionHelper: EncryptionHelper) =
        EncryptPINBlock(encryptionHelper)


    @Provides
    @Singleton
    fun provideStanManager(sharedPreferences: SharedPreferences): StanManager {
        return StanManager(sharedPreferences)
    }
}