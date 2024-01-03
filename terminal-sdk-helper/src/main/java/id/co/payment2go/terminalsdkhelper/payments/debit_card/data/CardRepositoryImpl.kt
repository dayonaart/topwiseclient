package id.co.payment2go.terminalsdkhelper.payments.debit_card.data

import android.content.SharedPreferences
import com.google.gson.JsonObject
import id.co.payment2go.terminalsdkhelper.check_balance.data.model.CardCheckBalance
import id.co.payment2go.terminalsdkhelper.check_balance.data.model.toCardCheckBalanceDto
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.ApiResult
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.BniDebitService
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.Bin
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.BinDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CardPayment
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CheckedBin
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.InitDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.toCardPaymentDto
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.CardRepository
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.UnidentifiedCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor(
    private val bniDebitService: BniDebitService,
    private val sharedPrefs: SharedPreferences
) : CardRepository {
    override fun postCardPayment(cardPayment: CardPayment): Flow<ApiResult<JsonObject>> {
        return flow {
            emit(ApiResult.Loading())
            try {
                val result = bniDebitService.postCardPayment(cardPayment.toCardPaymentDto())
                emit(ApiResult.Success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is UnknownHostException -> emit(ApiResult.Error("Tidak ada koneksi Internet"))
                    is ConnectException -> emit(ApiResult.Error("Tidak dapat terhubung ke server"))
                    is SocketTimeoutException -> emit(
                        ApiResult.Error(
                            "Koneksi Terputus=Silahkan cek Mutasi Rekening",
                            isTimeoutConnection = true
                        )
                    )

                    else -> emit(ApiResult.Error(e.message ?: "Unknown error occurred"))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun checkBinRanges(bin: Bin): Flow<ApiResult<CheckedBin>> = flow {
        emit(ApiResult.Loading())
        try {
            val result = bniDebitService.checkBinRanges(BinDto(bin.number))
            if (result.binName == null) {
                throw UnidentifiedCard("Kartu yang anda masukkan tidak didukung")
            }
            emit(ApiResult.Success(result.toCheckedBin()))
        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                is UnknownHostException -> emit(ApiResult.Error("Tidak ada koneksi Internet"))
                is ConnectException -> emit(ApiResult.Error("Tidak dapat terhubung ke server"))
                is SocketTimeoutException -> emit(
                    ApiResult.Error(
                        "Koneksi Terputus=Silahkan lakukan login ulang",
                        isTimeoutConnection = true
                    )
                )

                else -> emit(ApiResult.Error(e.message ?: "Unknown error occurred"))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun checkBalance(checkBalance: CardCheckBalance): Flow<ApiResult<JsonObject>> {
        return flow {
            emit(ApiResult.Loading())
            try {
                val result = bniDebitService.checkBalance(checkBalance.toCardCheckBalanceDto())
                emit(ApiResult.Success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is UnknownHostException -> emit(ApiResult.Error("Tidak ada koneksi Internet"))
                    is ConnectException -> emit(ApiResult.Error("Tidak dapat terhubung ke server"))
                    else -> emit(ApiResult.Error(e.message ?: "Unknown error occurred"))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun init(serialNumber: String): ApiResult<Boolean> {
        return try {
            val result = bniDebitService.init(InitDto(serialNumber))
            if (result.mmid == null || result.mtid == null || result.kodeAgen == null) {
                return ApiResult.Error("Perangkat tidak terdaftar", false)
            } else {
                sharedPrefs.edit()
                    .putString(Constant.MERCHANT_ID, result.mmid)
                    .putString(Constant.TERMINAL_ID, result.mtid)
                    .putString(Constant.BANK_MERCHANT_ID, result.bmid)
                    .putString(Constant.BANK_TERMINAL_ID, result.btid)
                    .putString(Constant.KEY_1, result.key1)
                    .putString(Constant.KEY_2, result.key2)
                    .putString(Constant.AGENT_CODE, result.kodeAgen)
                    .putString(Constant.HSM_FILTER_KEY, result.hsmFilter)
                    .apply()
                ApiResult.Success(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                is UnknownHostException -> (ApiResult.Error("Tidak ada koneksi internet", false))
                is ConnectException -> (ApiResult.Error("Tidak dapat terhubug ke server", false))
                else -> (ApiResult.Error("Unknown error occurred", false))
            }
            ApiResult.Success(false)
        }
    }

}