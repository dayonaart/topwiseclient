package id.co.payment2go.terminalsdkhelper.program_pemerintah.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.util.StanManager
import id.co.payment2go.terminalsdkhelper.program_pemerintah.data.ProgramPemerintahRepoImpl
import id.co.payment2go.terminalsdkhelper.program_pemerintah.data.ProgramPemerintahService
import id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.ProgramPemerintahRepository
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProgramPemerintahModule {

    @Provides
    @Singleton
    fun provideProgramPemerintahService(@Named("DEBIT") retrofit: Retrofit): ProgramPemerintahService {
        return retrofit.create(ProgramPemerintahService::class.java)
    }

    @Provides
    @Singleton
    fun provideProgramPemerintahRepository(
        service: ProgramPemerintahService,
        sharedPrefs: SharedPreferences,
        stanManager: StanManager,
        pinpadUtility: PinpadUtility
    ): ProgramPemerintahRepository {
        return ProgramPemerintahRepoImpl(
            programPemerintah = service,
            sharedPreferences = sharedPrefs,
            pinpadUtility = pinpadUtility,
            stanManager = stanManager
        )
    }
}