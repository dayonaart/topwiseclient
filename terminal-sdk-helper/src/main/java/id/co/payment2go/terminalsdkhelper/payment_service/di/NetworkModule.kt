package id.co.payment2go.terminalsdkhelper.payment_service.di

import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.EncryptionHelper
import id.co.payment2go.terminalsdkhelper.core.EnvironmentManager
import id.co.payment2go.terminalsdkhelper.core.EnvironmentType
import id.co.payment2go.terminalsdkhelper.core.JsonUtility
import id.co.payment2go.terminalsdkhelper.core.LogonManager
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.BniCashService
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.BniDebitService
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.HostRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        val timeoutInSeconds = 90L
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient()
            .newBuilder()
            .addInterceptor(logging)
            .connectTimeout(timeoutInSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutInSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutInSeconds, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("CASH")
    fun provideRetrofitCash(
        okHttpClient: OkHttpClient,
        environmentManager: EnvironmentManager
    ): Retrofit {
        val baseUrl =
            when (environmentManager.getEnvironment()) {
                EnvironmentType.DEV -> {
                    "http://edcwebdev.hq.bni.co.id:8001/VasHost/"
                }

                EnvironmentType.UAT -> {
                    "http://edcauthuat.hq.bni.co.id:8002/"
                }

                EnvironmentType.PROD -> {
                    "https://xxx.xxx.xx.xx"
                }
            }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }

    @Provides
    @Singleton
    @Named("DEBIT")
    fun provideRetrofitDebit(
        okHttpClient: OkHttpClient,
        environmentManager: EnvironmentManager
    ): Retrofit {
        val baseUrl =
            when (environmentManager.getEnvironment()) {
                EnvironmentType.DEV -> {
                    "http://edcwebdev.hq.bni.co.id:8001/AposHost/"
                }

                EnvironmentType.UAT -> {
                    "http://edcauthuat.hq.bni.co.id:8003/"
                }

                EnvironmentType.PROD -> {
                    "https://xxx.xxx.xx.xx"
                }
            }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }

    @Provides
    @Singleton
    fun provideBNICashService(@Named("CASH") retrofit: Retrofit): BniCashService {
        return retrofit.create(BniCashService::class.java)
    }

    @Provides
    @Singleton
    fun provideBNIDebitService(@Named("DEBIT") retrofit: Retrofit): BniDebitService {
        return retrofit.create(BniDebitService::class.java)
    }

    @Provides
    @Singleton
    fun provideHostRepository(
        bniCashService: BniCashService,
        bniDebitService: BniDebitService,
        sharedPreferences: SharedPreferences,
        pinpadUtility: PinpadUtility,
        encryptionHelper: EncryptionHelper,
        logonManager: LogonManager,
        jsonUtility: JsonUtility
    ): HostRepository =
        HostRepository(
            bniCashService = bniCashService,
            encryptionHelper = encryptionHelper,
            sharedPrefs = sharedPreferences,
            bniDebitService = bniDebitService,
            pinpadUtility = pinpadUtility,
            logonManager = logonManager,
            jsonUtility = jsonUtility
        )
}
