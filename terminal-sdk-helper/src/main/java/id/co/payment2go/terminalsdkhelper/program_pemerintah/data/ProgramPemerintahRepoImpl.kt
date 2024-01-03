package id.co.payment2go.terminalsdkhelper.program_pemerintah.data

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.ApiResult
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.util.StanManager
import id.co.payment2go.terminalsdkhelper.program_pemerintah.data.model.BansosPaymentRequestDto
import id.co.payment2go.terminalsdkhelper.program_pemerintah.data.model.toBansosInquiryRequestDto
import id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.ProgramPemerintahRepository
import id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.model.BansosInquiryRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class ProgramPemerintahRepoImpl @Inject constructor(
    private val programPemerintah: ProgramPemerintahService,
    private val sharedPreferences: SharedPreferences,
    private val pinpadUtility: PinpadUtility,
    private val stanManager: StanManager
) : ProgramPemerintahRepository {

    companion object {
        private const val TAG = "ProgramPemerintahRepo"
    }

    private var bansosInquiryRequestTemp: BansosInquiryRequest? = null

    override fun postBansosInquiryIntent(bansosInquiryRequest: BansosInquiryRequest): Flow<ApiResult<JsonObject>> =
        flow {
            emit(ApiResult.Loading())
            bansosInquiryRequestTemp = null
            try {
                val result =
                    programPemerintah.postInquiryIntent(bansosInquiryRequest.toBansosInquiryRequestDto())

                bansosInquiryRequestTemp = bansosInquiryRequest

                emit(ApiResult.Success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is UnknownHostException -> emit(ApiResult.Error("Tidak ada koneksi Internet"))
                    is ConnectException -> emit(ApiResult.Error("Tidak dapat terhubung ke server"))
                    else -> emit(ApiResult.Error(e.message ?: "Unknown error occurred"))
                }
            }
        }

    override fun postBansosInquiry(
        fld3: String,
        fld48: String,
        fld4: String,
        idTransaction: String,
        refNum: String,
        narasi: String,
    ): Flow<ApiResult<JsonObject>> {
        return flow {
            emit(ApiResult.Loading())
            try {
                if (bansosInquiryRequestTemp == null) {
                    emit(ApiResult.Error("Inquiry request is null"))
                    return@flow
                }
                var newEmv = bansosInquiryRequestTemp!!.emvData.substringBeforeLast("9F41")
                newEmv += "9F4104${
                    Util.addZerosToNumber(
                        number = stanManager.getCurrentStan().toString(),
                        desiredDigits = 8
                    )
                }"
                Log.d(TAG, "postBansosInquiry: newEmv = $newEmv")

                val bansosInquiryRequest = BansosPaymentRequestDto(
                    cardMasked = bansosInquiryRequestTemp!!.cardMasked,
                    idTransaction = idTransaction,
                    refNum = refNum,
                    kodeAgen = sharedPreferences.getString(Constant.AGENT_CODE, "") ?: "",
                    mid = sharedPreferences.getString(Constant.MERCHANT_ID, "") ?: "",
                    tid = sharedPreferences.getString(Constant.TERMINAL_ID, "") ?: "",
                    nii = bansosInquiryRequestTemp!!.nii,
                    pinBlock = bansosInquiryRequestTemp!!.pinBlock,
                    poscon = "00", // Currently the default value is 00
                    posent = bansosInquiryRequestTemp!!.posent,
                    stan = stanManager.getCurrentStan(),
                    fld3 = fld3,
                    fld48 = fld48,
                    iid = "2",
                    txnType = bansosInquiryRequestTemp!!.txnType,
                    secData = bansosInquiryRequestTemp!!.secData,
                    narasi = narasi
                )
                val result =
                    programPemerintah.postInquiry(bansosInquiryRequest)
                stanManager.increaseStan()
                emit(ApiResult.Success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is UnknownHostException -> emit(ApiResult.Error("Tidak ada koneksi Internet"))
                    is ConnectException -> emit(ApiResult.Error("Tidak dapat terhubung ke server"))
                    else -> emit(ApiResult.Error(e.message ?: "Unknown error occurred"))
                }
            }
        }
    }

    override fun postBansosPayment(
        fld3: String,
        fld48: String,
        fld4: String,
        idTransaction: String,
        refNum: String,
        narasi: String,
        pinBlock: String,
    ): Flow<ApiResult<JsonObject>> {
        return flow {
            emit(ApiResult.Loading())
            try {
                if (bansosInquiryRequestTemp == null) {
                    emit(ApiResult.Error("Inquiry request is null"))
                    return@flow
                }
                var newEmv = bansosInquiryRequestTemp!!.emvData.substringBeforeLast("9F41")
                newEmv += "9F4104${
                    Util.addZerosToNumber(
                        number = stanManager.getCurrentStan().toString(),
                        desiredDigits = 8
                    )
                }"

                val dataToEncrypt = JsonObject().apply {
                    addProperty("Amt", fld4)
                    addProperty("ICC", newEmv)
                    addProperty("Track2", bansosInquiryRequestTemp!!.track2Data)
                }.toString()
                val secData = pinpadUtility.encryptData(dataToEncrypt).data ?: "Encrypt Error"

                val bansosPaymentRequest = BansosPaymentRequestDto(
                    idTransaction = idTransaction,
                    refNum = refNum,
                    cardMasked = bansosInquiryRequestTemp!!.cardMasked,
                    kodeAgen = sharedPreferences.getString(Constant.AGENT_CODE, "") ?: "",
                    mid = sharedPreferences.getString(Constant.MERCHANT_ID, "") ?: "",
                    tid = sharedPreferences.getString(Constant.TERMINAL_ID, "") ?: "",
                    nii = bansosInquiryRequestTemp!!.nii,
                    pinBlock = pinBlock,
                    poscon = "00", // Currently the default value is 00
                    posent = bansosInquiryRequestTemp!!.posent,
                    stan = stanManager.getCurrentStan(),
                    fld3 = fld3,
                    fld48 = fld48,
                    iid = "2",
                    txnType = bansosInquiryRequestTemp!!.txnType,
                    secData = secData,
                    narasi = narasi
                )
                val result = programPemerintah.postPayment(bansosPaymentRequest)
                stanManager.increaseStan()
                result.addProperty("cardMasked", bansosInquiryRequestTemp!!.cardMasked)
                result.addProperty(
                    "MTID",
                    sharedPreferences.getString(Constant.TERMINAL_ID, "")
                )
                result.addProperty(
                    "MMID",
                    sharedPreferences.getString(Constant.MERCHANT_ID, "")
                )
                result.addProperty(
                    "BMID",
                    sharedPreferences.getString(Constant.BANK_MERCHANT_ID, "")
                )
                result.addProperty(
                    "BTID",
                    sharedPreferences.getString(Constant.BANK_TERMINAL_ID, "")
                )

                val secDataFromResponse = result.get("SEC").asString
                val decryptedData = pinpadUtility.decryptData(secDataFromResponse)
                if (decryptedData.data == null) {
                    emit(ApiResult.Error("Decrypt Error: ${decryptedData.message}"))
                    return@flow
                }
                val decryptedDataJson = JsonParser.parseString(decryptedData.data).asJsonObject
                val mergedJson = Util.mergeJsonObjects(result, decryptedDataJson)
                Log.d(TAG, "postBansosPayment: mergedJson = $mergedJson")

                emit(ApiResult.Success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is UnknownHostException -> emit(ApiResult.Error("Tidak ada koneksi Internet"))
                    is ConnectException -> emit(ApiResult.Error("Tidak dapat terhubung ke server"))
                    else -> emit(ApiResult.Error(e.message ?: "Unknown error occurred"))
                }
            }
        }
    }
}