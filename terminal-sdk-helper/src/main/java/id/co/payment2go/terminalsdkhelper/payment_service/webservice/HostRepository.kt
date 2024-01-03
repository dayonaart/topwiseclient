package id.co.payment2go.terminalsdkhelper.payment_service.webservice

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.EncryptionHelper
import id.co.payment2go.terminalsdkhelper.core.JsonUtility
import id.co.payment2go.terminalsdkhelper.core.LogonManager
import id.co.payment2go.terminalsdkhelper.core.asStringOrNull
import id.co.payment2go.terminalsdkhelper.core.toCurrentFormat
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.RequestBodyDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.ResponseBodyDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONException
import org.json.JSONObject
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.Calendar
import java.util.Date

class HostRepository(
    private val bniCashService: BniCashService,
    private val bniDebitService: BniDebitService,
    private val pinpadUtility: PinpadUtility,
    private val sharedPrefs: SharedPreferences,
    private val encryptionHelper: EncryptionHelper,
    private val logonManager: LogonManager,
    private val jsonUtility: JsonUtility
) {

    private val TAG = "HostRepository"
    fun postRequest(
        param: String,
        body: String,
        refNumber: String?,
        trxType: String?,
        sofType: String?,
        idTransaction: String?,
        totalAmount: String?,
        description: String?
    ): Flow<ApiResult<ResponseBodyDto>> = flow {
        emit(ApiResult.Loading())
        try {
            val mid = sharedPrefs.getString(Constant.MERCHANT_ID, "")
            val tid = sharedPrefs.getString(Constant.TERMINAL_ID, "")
            if (mid.isNullOrEmpty() || tid.isNullOrEmpty()) {
                emit(ApiResult.Error("MID dan TID tidak boleh kosong"))
                return@flow
            }

            val isGetParam = param == "getParam"
            if (isGetParam) {
                val isKeyInjected = sharedPrefs.getBoolean(Constant.IS_KEY_INJECTED, false)
                if (!isKeyInjected) {
                    emit(ApiResult.Error("Key belum diinject"))
                    return@flow
                }
            }
            val isParamRequestLogin = param == "auth"
            if (isParamRequestLogin) {
                // checking logon
                val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                val lastLogonDay = sharedPrefs.getInt(Constant.LAST_LOGON_DAY, -1)
                if (currentDay != lastLogonDay) {
                    // request Logon
                    val result = logonManager.performLogon()
                    if (result is Resource.Success) {
                        sharedPrefs.edit().putInt(Constant.LAST_LOGON_DAY, currentDay).apply()
                    } else {
                        emit(ApiResult.Error(result.message ?: "Unknown error occurred"))
                        return@flow
                    }
                }
            }

            val jsonToEncrypt = jsonUtility.findKeysAndCreateNewJSON(body)
            val newBodyJson = JsonParser.parseString(body).asJsonObject
            jsonUtility.removeKeys(newBodyJson)

            val secData = pinpadUtility.encryptData(jsonToEncrypt)
            if (secData is Resource.Error) {
                emit(ApiResult.Error(secData.message ?: "Terjadi kesalahan saat meng-encrypt data"))
                return@flow
            }
            val totalAmountJson = JsonObject().apply {
                addProperty("TotalAmount", totalAmount)
            }
            val secDataHeader = pinpadUtility.encryptData(totalAmountJson.toString())

            val request = RequestBodyDto(
                agentCode = sharedPrefs.getString(Constant.AGENT_CODE, "") ?: "",
                mid = mid,
                tid = tid,
                param = param,
                body = newBodyJson.toString(),
                refNumber = refNumber ?: "",
                trxType = trxType
                    ?: "NONTRANSAKSI", // INQUIRY, PAYMENT, NONTRANSAKSI (MANDATORY)
                sofType = sofType ?: "", // TUNAI, KARTU, NONPAYMENT
                idTransaction = idTransaction ?: "",
//                totalAmount = totalAmount ?: "",
                description = description ?: "",
                txnDate = Date().toCurrentFormat("yyyy-MM-dd HH:mm:ss"),
                serverKey = encryptionHelper.encryptXOR(mid, tid),
                secData = secData.data ?: "",
                secHeader = secDataHeader.data ?: ""
            )
            var response = bniCashService.postRequest(request)

            val secDataResponse = response.secData

            // kita abaikan dec secData untuk param detailPengumuman
            val isParamDetailPengumuman = response.param == "detailPengumuman"
            val isParamRemittanceSearch = response.param == "remittanceSearch"
            if (secDataResponse != null && !isParamDetailPengumuman && !isParamRemittanceSearch) {
                val decryptedSecData = pinpadUtility.decryptData(secDataResponse)
                if (decryptedSecData is Resource.Error) {
                    emit(ApiResult.Error(secData.message ?: "Terjadi kesalahan"))
                    return@flow
                }
                val clearedDecryptedSecData =
                    Util.removeTextAfterLastCurlyBrace(decryptedSecData.data!!)
                Log.d("HostRepository", "clearedDecryptedSecData: $clearedDecryptedSecData")
                val jsonDecryptedSecData =
                    Gson().fromJson(clearedDecryptedSecData, JsonObject::class.java)
                Log.d("HostRepository", "jsonDecryptedSecData: $jsonDecryptedSecData")
                val jsonBody = Gson().fromJson(response.body, JsonObject::class.java)
                val mergedBodyJson =
                    Util.mergeJsonObjects(jsonBody, jsonDecryptedSecData).toString()
                Log.d("HostRepository", "mergedBodyJson: $mergedBodyJson")
                response = response.copy(
                    body = mergedBodyJson
                )
                Log.d("HostRepository", "newResponse data class: $response")
            }

            val isParamLoginResponse = response.param == "auth"
            if (isParamLoginResponse) {

                val jsonBodyRequest = JSONObject(request.body)
                val ipAddress = jsonBodyRequest.getString("ip_address")
                sharedPrefs.edit().putString(Constant.IP_ADDRESS_KEY, ipAddress).apply()
                val browserAgent = jsonBodyRequest.getString("browser_agent")
                sharedPrefs.edit().putString(Constant.BROWSER_AGENT_KEY, browserAgent).apply()
                val idAPI = jsonBodyRequest.getString("id_api")
                sharedPrefs.edit().putString(Constant.ID_API_KEY, idAPI).apply()
                val ipServer = jsonBodyRequest.getString("ip_server")
                sharedPrefs.edit().putString(Constant.IP_SERVER_KEY, ipServer).apply()
                val reqID = jsonBodyRequest.getString("req_id")
                sharedPrefs.edit().putString(Constant.REQ_ID_KEY, reqID).apply()

                try {
                    val jsonBodyResponse = JSONObject(response.body)

                    val jsonDataUser = jsonBodyResponse.getJSONObject("dataUser")
                    val accountNumResponse = jsonDataUser.getString("accountNum")
                    val kodeLoketResponse = jsonDataUser.getString("kode_loket")
                    val kodeMitra = jsonDataUser.getString("kode_mitra")
                    val kodeCabang = jsonDataUser.getString("kode_cabang")

                    val sessionResponse = jsonBodyResponse.getString("session")
                    sharedPrefs.edit().putString(Constant.SESSION_KEY, sessionResponse).apply()
                    sharedPrefs.edit().putString(Constant.ACCOUNT_NUM_KEY, accountNumResponse)
                        .apply()
                    sharedPrefs.edit().putString(Constant.KODE_LOKET_KEY, kodeLoketResponse).apply()
                    sharedPrefs.edit().putString(Constant.KODE_MITRA_KEY, kodeMitra).apply()
                    sharedPrefs.edit().putString(Constant.KODE_CABANG_KEY, kodeCabang).apply()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            // perlakuan khusus untuk param RemittenceSearch
            if (secDataResponse != null && isParamRemittanceSearch) {
                val decryptedSecData = pinpadUtility.decryptData(secDataResponse).data ?: "{}"
                val clearedDecryptedSecData = Util.removeTextAfterLastCurlyBrace(decryptedSecData)

                val plainBodyJsonObject = JsonParser.parseString(response.body).asJsonObject
                val extendedJsonObject =
                    JsonParser.parseString(clearedDecryptedSecData).asJsonObject
                val s06RecordOutputPlain = plainBodyJsonObject
                    ?.getAsJsonObject("data")
                    ?.getAsJsonArray("s06Record_output")
                val s06RecordOutputExtended = extendedJsonObject
                    ?.getAsJsonObject("data")
                    ?.getAsJsonArray("s06Record_output")
                if (s06RecordOutputPlain != null && s06RecordOutputExtended != null) {
                    s06RecordOutputPlain.forEachIndexed { index, jsonElement ->
                        if (jsonElement.isJsonObject) {
                            jsonElement.asJsonObject.addProperty(
                                "amount",
                                s06RecordOutputExtended[index].asJsonObject.get("amount")
                                    .asStringOrNull()
                            )
                        }
                    }
                    // add kode_loket
                    plainBodyJsonObject.getAsJsonObject("result").addProperty(
                        "kode_loket",
                        "${
                            extendedJsonObject.getAsJsonObject("result").get("kode_loket")
                                .asStringOrNull()
                        }"
                    )
                }

                Log.d(TAG, "remmitence: $plainBodyJsonObject")
                response = response.copy(
                    body = plainBodyJsonObject.toString()
                )
            }

            // tidak ada lagi pengecekan untuk dilakukan reversal
            val oldTransactionId = sharedPrefs.getString(Constant.REVERSAL_TRANSACTION_ID_KEY, "")
            if (oldTransactionId == idTransaction && sofType == "KARTU" && trxType == "PAYMENT") {
                sharedPrefs.edit().remove(Constant.REVERSAL_TRANSACTION_ID_KEY).apply()
                sharedPrefs.edit().remove(Constant.REVERSAL_AMOUNT_KEY).apply()
            }

            val newResponse = response.copy(
                tid = sharedPrefs.getString(Constant.BANK_TERMINAL_ID, "") ?: "",
                mid = sharedPrefs.getString(Constant.BANK_MERCHANT_ID, "") ?: ""
            )
            emit(ApiResult.Success(newResponse))
        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                is UnknownHostException -> emit(ApiResult.Error("Tidak ada koneksi Internet"))
                is ConnectException -> emit(ApiResult.Error("Tidak dapat terhubung ke server"))
                else -> emit(ApiResult.Error("${e.message}"))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun postBankPayment(body: JsonObject): Flow<ApiResult<JsonObject>> {
        return flow {
            emit(ApiResult.Loading())
            try {
                val response = bniDebitService.postBankPayment(body)
                response.addProperty("TID", sharedPrefs.getString(Constant.BANK_TERMINAL_ID, ""))
                response.addProperty("MID", sharedPrefs.getString(Constant.BANK_MERCHANT_ID, ""))
                emit(ApiResult.Success(response))
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is UnknownHostException -> emit(ApiResult.Error("Tidak ada koneksi Internet"))
                    is ConnectException -> emit(ApiResult.Error("Tidak dapat terhubung ke server"))
                    else -> emit(ApiResult.Error("Unknown error occurred"))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun reversal(): Flow<ApiResult<String>> {
        return flow {
            val oldTransactionId = sharedPrefs.getString(Constant.REVERSAL_TRANSACTION_ID_KEY, "")
            val oldAmount = sharedPrefs.getString(Constant.REVERSAL_AMOUNT_KEY, "")

            if (oldTransactionId.isNullOrEmpty() || oldAmount.isNullOrEmpty()) {
                val statusJson = JsonObject().apply {
                    addProperty("RSPC", "00")
                    addProperty("RSPM", "Tidak ada reversal")
                }.toString()
                emit(ApiResult.Success(statusJson))
                return@flow
            }
            emit(ApiResult.Loading())
            try {
                val mid = sharedPrefs.getString(Constant.MERCHANT_ID, "") ?: ""
                val tid = sharedPrefs.getString(Constant.TERMINAL_ID, "") ?: ""

                val json = JsonObject().apply {
                    addProperty("MID", mid)
                    addProperty("TID", tid)
                    addProperty("Param", "")
                    addProperty("Body", "")
                    addProperty("RefNumber", "")
                    addProperty("IDTransaction", oldTransactionId)
                    addProperty("KodeAgen", sharedPrefs.getString(Constant.AGENT_CODE, ""))
                    addProperty("SofType", "NONPAYMENT")
                    addProperty("MaskedCard", "")
                    addProperty("TotalAmount", oldAmount)
                    addProperty("TXNDate", Date().toCurrentFormat("yyyy-MM-dd HH:mm:ss"))
                    addProperty("Narasi", "")
                    addProperty("TrxType", "NONTRANSAKSI")
                    addProperty("ServerKey", encryptionHelper.encryptXOR(mid, tid))
                }

                val result = bniCashService.postReversalData(json)

                val error = result.get("Error").asStringOrNull()
                val message = result.get("Message").asStringOrNull()

                if (error.equals("false", true) && message != null) {
                    if (message.contains("core jurnal", true)) {
                        val statusJson = JsonObject().apply {
                            addProperty("RSPC", "00")
                            addProperty("RSPM", message)
                            addProperty("Nominal", oldAmount)
                        }.toString()
                        sharedPrefs.edit().remove(Constant.REVERSAL_AMOUNT_KEY).apply()
                        sharedPrefs.edit().remove(Constant.REVERSAL_TRANSACTION_ID_KEY).apply()
                        emit(ApiResult.Success(statusJson))
                    } else {
                        val statusJson = JsonObject().apply {
                            addProperty("RSPC", "00")
                            addProperty("RSPM", "Tidak ada reversal")
                        }.toString()
                        sharedPrefs.edit().remove(Constant.REVERSAL_AMOUNT_KEY).apply()
                        sharedPrefs.edit().remove(Constant.REVERSAL_TRANSACTION_ID_KEY).apply()
                        emit(ApiResult.Success(statusJson))
                    }
                } else {
                    val statusJson = JsonObject().apply {
                        addProperty("RSPC", "12")
                        addProperty("RSPM", message)
                        addProperty("Nominal", oldAmount).toString()
                    }.toString()
                    sharedPrefs.edit().remove(Constant.REVERSAL_AMOUNT_KEY).apply()
                    sharedPrefs.edit().remove(Constant.REVERSAL_TRANSACTION_ID_KEY).apply()
                    emit(ApiResult.Error(statusJson))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val statusJson = JsonObject()

                when (e) {
                    is UnknownHostException -> emit(
                        ApiResult.Success(
                            statusJson.apply {
                                addProperty("RSPC", "-1")
                                addProperty("RSPM", "Tidak ada koneksi Internet")
                            }.toString()
                        )
                    )

                    is ConnectException -> emit(
                        ApiResult.Success(
                            statusJson.apply {
                                addProperty("RSPC", "-1")
                                addProperty("RSPM", "Tidak dapat terhubung ke server")
                            }.toString()
                        )
                    )

                    else -> emit(
                        ApiResult.Success(
                            statusJson.apply {
                                addProperty("RSPC", "-1")
                                addProperty("RSPM", e.message ?: "Unknown error occurred")
                            }.toString()
                        )
                    )
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}