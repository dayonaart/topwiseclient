package id.co.payment2go.terminalsdkhelper.tarik_tunai

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.payment2go.terminalsdkhelper.common.emv.EMVUtility
import id.co.payment2go.terminalsdkhelper.common.pinpad.OnPinPadResult
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.asBooleanOrNull
import id.co.payment2go.terminalsdkhelper.core.asStringOrNull
import id.co.payment2go.terminalsdkhelper.core.db.activity_result.ResultDao
import id.co.payment2go.terminalsdkhelper.core.db.activity_result.ResultModel
import id.co.payment2go.terminalsdkhelper.core.db.card_result.CardResultDao
import id.co.payment2go.terminalsdkhelper.core.db.card_result.CardResultModel
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.core_ui.EventUi
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.ApiResult
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.HostRepository
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.Bin
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CheckedBin
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.CardRepository
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.UnidentifiedCard
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.util.StanManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class TarikTunaiViewModel @Inject constructor(
    private val emvUtility: EMVUtility,
    private val cardRepository: CardRepository,
    private val hostRepository: HostRepository,
    private val sharedPreferences: SharedPreferences,
    private val pinpadUtility: PinpadUtility,
    private val stanManager: StanManager,
    private val deviceManagerUtility: DeviceManagerUtility,
    private val resultDao: ResultDao,
    private val cardResultDao: CardResultDao
) : ViewModel() {

    companion object {
        private const val TAG = "TarikTunaiViewModel"
    }

    private val _state = MutableStateFlow(TarikTunaiUiState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EventUi>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var job: Job? = null

    init {
        resetDatabase()
        disableButton()
    }

    private fun resetDatabase() {
        viewModelScope.launch {
            resultDao.deleteResult()
        }
    }

    private fun disableButton() {
        deviceManagerUtility.setActiveButtonNavigation(false)
    }

    private fun enableButton() {
        deviceManagerUtility.setActiveButtonNavigation(true)
    }

    fun setFld3(fld3Intent: String) {
        _state.update { state ->
            state.copy(fld3 = fld3Intent)
        }
    }

    fun setFld48(fld48Intent: String) {
        _state.update { state ->
            state.copy(fld48 = fld48Intent)
        }
    }

    fun setFld43(fld43Intent: String) {
        _state.update { state ->
            state.copy(fld43 = fld43Intent)
        }
    }

    fun setFld4(fld4Intent: String) {
        _state.update { state ->
            state.copy(fld4 = fld4Intent)
        }
    }

    fun setMustOffUs(mustOffUs: Boolean) {
        _state.update { state ->
            state.copy(mustOffUs = mustOffUs)
        }
    }

    fun setBiayaLoket(biayaLoketIntent: String) {
        _state.update { state ->
            state.copy(biayaLoket = biayaLoketIntent)
        }
    }

    fun setTrxTypeId(trxTypeIdIntent: String) {
        _state.update { state ->
            state.copy(trxTypeId = trxTypeIdIntent)
        }
    }

    fun setRefNum(refNumIntent: String) {
        _state.update { state ->
            state.copy(refNum = refNumIntent)
        }
    }

    fun setNarasi(narasiIntent: String) {
        _state.update { state ->
            state.copy(narasi = narasiIntent)
        }
    }

    fun setTransactionId(transactionIdIntent: String) {
        _state.update { state ->
            state.copy(transactionId = transactionIdIntent)
        }
    }

    fun setCardNumber(cardNumberIntent: String){
        _state.update { state ->
            state.copy(cardNumber = cardNumberIntent)
        }
    }

    fun setMaskedCardNumber(maskedCardNumberIntent: String){
        _state.update { state ->
            state.copy(maskedCardNumber = maskedCardNumberIntent)
        }
    }

    fun setPosent(posentIntent: String){
        _state.update { state ->
            state.copy(posent = posentIntent)
        }
    }

    fun setCheckedBin(checkedBin: CheckedBin){
        _state.update { state ->
            state.copy(checkedBin = checkedBin)
        }
    }

    fun setOriginEmv(originEmvIntent: String){
        _state.update { state ->
            state.copy(originEmv = originEmvIntent)
        }
    }

    fun setTrack2Data(track2DataIntent: String){
        _state.update { state ->
            state.copy(track2Data = track2DataIntent)
        }
    }

    fun searchCard() {
        viewModelScope.launch {
            try {
                withTimeout(1.minutes) {
                    emvUtility.searchCard(
                        stan = stanManager.getCurrentStan(),
                        amount = 0L
                    ).collectLatest { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                _state.update { state ->
                                    state.copy(
                                        isLoading = true,
                                        loadingMessage = "Generating EMV",
                                        popUpMessage = "",
                                        isErrorPopUpShowing = false,
                                        isCardInserted = false
                                    )
                                }
                            }

                            is Resource.Success -> {
                                if (resource.data == null) return@collectLatest
                                _state.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        loadingMessage = "",
                                        isErrorPopUpShowing = false,
                                        popUpMessage = "",
                                        cardNumber = resource.data.cardNo,
                                        track2Data = resource.data.track2Data,
                                        originEmv = resource.data.EMVData,
                                        posent = resource.data.posEntryMode
                                    )
                                }
                                checkBinRanges(
                                    state.value.cardNumber.substring(0, 10).toLongOrNull() ?: 0L
                                )
                            }

                            is Resource.Error -> {
                                _state.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        popUpMessage = resource.message ?: "Terjadi Kesalahan",
                                        isErrorPopUpShowing = true
                                    )
                                }
                            }

                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        popUpMessage = "Kartu yang anda masukkan tidak didukung",
                        isErrorPopUpShowing = true,
                        isCardInserted = false
                    )
                }
            }
        }
    }

    private fun checkBinRanges(value: Long) {
        viewModelScope.launch {
            try {
                cardRepository.checkBinRanges(Bin(value)).collectLatest { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            _state.update {
                                it.copy(
                                    isErrorPopUpShowing = false,
                                    isLoading = true,
                                    popUpMessage = "",
                                    loadingMessage = "Mengecek kartu",
                                    isCardInserted = false
                                )
                            }
                        }

                        is ApiResult.Success -> {
                            val checkedBin = result.data!!
                            if (state.value.mustOffUs && !checkedBin.isOnUs) {
                                _state.update {
                                    it.copy(
                                        isErrorPopUpShowing = true,
                                        isLoading = false,
                                        popUpMessage = "Kartu yang anda masukkan tidak didukung"
                                    )
                                }
                                return@collectLatest
                            }
                            if (!state.value.mustOffUs && checkedBin.isOnUs) {
                                _state.update {
                                    it.copy(
                                        isErrorPopUpShowing = true,
                                        isLoading = false,
                                        popUpMessage = "Kartu yang anda masukkan tidak didukung"
                                    )
                                }
                                return@collectLatest
                            }

                            _state.update {
                                it.copy(
                                    checkedBin = checkedBin,
                                    isErrorPopUpShowing = false,
                                    isLoading = false,
                                    isCardInserted = true,
                                    maskedCardNumber = Util.maskCardNumber(it.cardNumber)
                                )
                            }
                            cardResultDao.insertResult(
                                CardResultModel(
                                    JsonObject().apply {
                                        addProperty("cardNumber", state.value.cardNumber)
                                        addProperty(
                                            "maskedCardNumber",
                                            state.value.maskedCardNumber
                                        )
                                        addProperty("track2Data", state.value.track2Data)
                                        addProperty("originEmv", state.value.originEmv)
                                        addProperty("posent", state.value.posent)
                                        addProperty("fld3", state.value.fld3)
                                        addProperty("fld48", state.value.fld48)
                                        addProperty("fld43", state.value.fld43)
                                        addProperty("binName", state.value.checkedBin.binName)
                                        addProperty("cardType", state.value.checkedBin.cardType)
                                        addProperty("NII", state.value.checkedBin.NII)
                                        addProperty("isOnUs", state.value.checkedBin.isOnUs)
                                    }.toString()
                                )
                            )

                            _eventFlow.emit(EventUi.Finish)
                        }

                        is ApiResult.Error -> {
                            Log.d(TAG, "checkBinRanges Error: " + result.message)
                            _state.update {
                                it.copy(
                                    isErrorPopUpShowing = true,
                                    isLoading = false,
                                    loadingMessage = "",
                                    popUpMessage = result.message ?: "Unknown error occurred"
                                )
                            }
                        }
                    }
                }
            } catch (e: UnidentifiedCard) {
                _state.update {
                    it.copy(
                        isErrorPopUpShowing = true,
                        popUpMessage = e.message.toString()
                    )
                }
            }
        }
    }

    fun showPinpad() {
        viewModelScope.launch {
            val jsonDB = JsonParser.parseString(cardResultDao.getResult()?.result).asJsonObject
            setCardNumber(jsonDB.get("cardNumber").asStringOrNull() ?: "")
            setMaskedCardNumber(jsonDB.get("maskedCardNumber").asStringOrNull() ?: "")
            setPosent(jsonDB.get("posent").asStringOrNull() ?: "")
            setTrack2Data(jsonDB.get("track2Data").asStringOrNull() ?: "")
            setOriginEmv(jsonDB.get("originEmv").asStringOrNull() ?: "")
            setCheckedBin(CheckedBin(
                binName = jsonDB.get("binName").asStringOrNull() ?: "",
                cardType = jsonDB.get("cardType").asStringOrNull() ?: "",
                NII = jsonDB.get("NII").asStringOrNull() ?: "",
                isOnUs = jsonDB.get("isOnUs").asBooleanOrNull()
            ))
            pinpadUtility.showPinPad(
                disorder = false,
                cardNumber = state.value.cardNumber
            ) { result ->
                when (result) {
                    is OnPinPadResult.OnInput -> {
                        Log.d(TAG, "showPinpad: ${result.p0}")
                    }

                    is OnPinPadResult.OnConfirm -> {
                        val data = Util.onConfirmPinpadCase(result.data)
                        _state.update {
                            it.copy(pinBlock = data)
                        }
                        postCardToHost()
                    }

                    is OnPinPadResult.OnCancel -> {
                        cancelTransaction(cancelledByUser = true)

                    }

                    is OnPinPadResult.OnError -> {
                        _state.update {
                            it.copy(
                                popUpMessage = "Pinpad error: ${result.error}",
                                isErrorPopUpShowing = true
                            )
                        }
                    }
                }
            }
        }
    }

    private fun postCardToHost() {
        job?.cancel()
        job = viewModelScope.launch {
            val mid = sharedPreferences.getString(Constant.MERCHANT_ID, "")
            val tid = sharedPreferences.getString(Constant.TERMINAL_ID, "")
            val accountNum = sharedPreferences.getString(Constant.ACCOUNT_NUM_KEY, "") ?: ""
            var toAccount = accountNum
            if (mid.isNullOrEmpty() || tid.isNullOrEmpty()) {
                _state.update { cardState ->
                    cardState.copy(
                        isLoading = false,
                        isErrorPopUpShowing = true,
                        pins = listOf(),
                        popUpMessage = "MID dan TID tidak boleh kosong"
                    )
                }
                return@launch
            }
            if (accountNum.isEmpty()) {
                _state.update { cardState ->
                    cardState.copy(
                        isLoading = false,
                        isErrorPopUpShowing = true,
                        pins = listOf(),
                        popUpMessage = "Nomer Rekening tidak boleh kosong"
                    )
                }
                return@launch
            }
            if (toAccount.length % 16 != 0) {
                val padding = 16 - (toAccount.length % 16)
                toAccount = toAccount.padStart(toAccount.length + padding, '0')
            }

            //get Result from DB
            val jsonDB = JsonParser.parseString(cardResultDao.getResult()?.result).asJsonObject
            _state.update {
                it.copy(
                    cardNumber = jsonDB.get("cardNumber").asStringOrNull() ?: "",
                    maskedCardNumber = jsonDB.get("maskedCardNumber").asStringOrNull() ?: "",
                    track2Data = jsonDB.get("track2Data").asStringOrNull() ?: "",
                    originEmv = jsonDB.get("originEmv").asStringOrNull() ?: "",
                    posent = jsonDB.get("posent").asStringOrNull() ?: "",
                    checkedBin = CheckedBin(
                        binName = jsonDB.get("binName").asStringOrNull() ?: "",
                        cardType = jsonDB.get("cardType").asStringOrNull() ?: "",
                        NII = jsonDB.get("NII").asStringOrNull() ?: "",
                        isOnUs = jsonDB.get("isOnUs").asBooleanOrNull()
                    )
                )
            }

            if (state.value.posent == Constant.POS_ENTRY_MODE_DIP) {
                _state.update {
                    val stanForEMV = "9F4104${
                        Util.addZerosToNumber(
                            number = stanManager.getCurrentStan().toString(),
                            desiredDigits = 8
                        )
                    }"
                    it.copy(emvData = it.originEmv + stanForEMV)
                }
            }

            val nominal =
                (state.value.fld4.toLongOrNull() ?: 0L) - (state.value.biayaLoket.toLongOrNull()
                    ?: 0L)
            val dataJson = JsonObject().apply {
                addProperty("no_rekening", state.value.cardNumber)
                addProperty(
                    "browser_agent",
                    sharedPreferences.getString(Constant.BROWSER_AGENT_KEY, "")
                )
                addProperty("nominal", nominal)
                addProperty("biaya_loket", state.value.biayaLoket)
                addProperty("trxtype_id", state.value.trxTypeId)
                addProperty("reffNum", state.value.refNum)
                addProperty("narasi", state.value.narasi)
                addProperty("ip_address", sharedPreferences.getString(Constant.IP_ADDRESS_KEY, ""))
                addProperty("id_api", sharedPreferences.getString(Constant.ID_API_KEY, ""))
                addProperty("ip_server", sharedPreferences.getString(Constant.IP_SERVER_KEY, ""))
                addProperty("req_id", sharedPreferences.getString(Constant.REQ_ID_KEY, ""))
                addProperty("session", sharedPreferences.getString(Constant.SESSION_KEY, ""))
                addProperty("kode_loket", sharedPreferences.getString(Constant.KODE_LOKET_KEY, ""))
                addProperty("kode_mitra", sharedPreferences.getString(Constant.KODE_MITRA_KEY, ""))
                addProperty(
                    "kode_cabang",
                    sharedPreferences.getString(Constant.KODE_CABANG_KEY, "")
                )
            }.toString()

            val dataToEncrypt = JsonObject().apply {
                addProperty("Amt", state.value.fld4)
                addProperty("ICC", state.value.emvData)
                addProperty("Track2", state.value.track2Data)
            }.toString()
            val secData = pinpadUtility.encryptData(dataToEncrypt).data ?: "Encrypt Error"

            val jsonObjectRequest = JsonObject().apply {
                addProperty("AMT", state.value.fld4)
                addProperty("IdTransaction", state.value.transactionId)
                addProperty("REFNO", state.value.refNum)
                addProperty("narasi", state.value.narasi)
                addProperty("CardMasked", jsonDB.get("maskedCardNumber").asStringOrNull() ?: "")
                addProperty("KodeAgen", sharedPreferences.getString(Constant.AGENT_CODE, ""))
                addProperty("MMID", mid)
                addProperty("MTID", tid)
                addProperty("NII", state.value.checkedBin.NII)
                addProperty("PINB", state.value.pinBlock)
                addProperty("POSCON", "00")
                addProperty("POSENT", state.value.posent)
                addProperty("STAN", stanManager.getCurrentStan())
                addProperty("FLD3", state.value.fld3)
                addProperty("FLD43", state.value.fld43)
                addProperty("FLD48", state.value.fld48)
                addProperty("IID", "2")
                addProperty("TXNTYPE", "492")
                addProperty("DJson", dataJson)
                addProperty("toAccount", toAccount)
                addProperty("SEC", secData)

            }
            hostRepository.postBankPayment(jsonObjectRequest).collectLatest { apiResult ->
                when (apiResult) {
                    is ApiResult.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true,
                                loadingMessage = "Authorizing to server"
                            )
                        }
                    }

                    is ApiResult.Success -> {
                        stanManager.increaseStan()
                        val responseCode = apiResult.data?.get("RSPC")?.asStringOrNull()
                        val responseMessage = apiResult.data?.get("RSPM")?.asStringOrNull()
                        val isPinBlocked = responseCode == "75"
                        val incorrectPin = responseCode == "55"
                        if (incorrectPin) {
                            _state.update { cardState ->
                                cardState.copy(
                                    isLoading = false,
                                    isErrorPopUpShowing = true,
                                    pins = listOf(),
                                    popUpMessage = "PIN Kartu Debit yang dimasukkan salah. Kesalahan memasukkan PIN sebanyak 3x akan menyebabkan kartu Anda terblokir."
                                )
                            }
                            return@collectLatest
                        }
                    val jsonObject = apiResult.data!!.apply {
                            addProperty("isPinBlocked", isPinBlocked)
                            addProperty("responseCode", responseCode)
                            addProperty("responseMessage", responseMessage)
                            addProperty("bank", state.value.checkedBin.binName)
                            addProperty("cardType", state.value.checkedBin.cardType)
                            addProperty("cardNumber", state.value.maskedCardNumber)
                            addProperty(
                                "BTID",
                                sharedPreferences.getString(Constant.BANK_TERMINAL_ID, "") ?: ""
                            )
                            addProperty(
                                "BMID",
                                sharedPreferences.getString(Constant.BANK_MERCHANT_ID, "") ?: ""
                            )
                            addProperty(
                                "MTID",
                                sharedPreferences.getString(Constant.MERCHANT_ID, "") ?: ""
                            )
                            addProperty(
                                "MMID",
                                sharedPreferences.getString(Constant.MERCHANT_ID, "") ?: ""
                            )
                        }

                        val secDataFromResponse = apiResult.data.get("SEC").asStringOrNull() ?: ""
                        val decryptedData =
                            pinpadUtility.decryptData(secDataFromResponse).data ?: "{}"
                        Log.d(TAG, "postCardToHost: $jsonObject")
                        val mergedJson = Util.mergeJsonObjects(
                            jsonObject,
                            JsonParser.parseString(decryptedData).asJsonObject
                        )
                        Log.d(TAG, "mergedJson: $mergedJson")

                        _state.update { it.copy(isLoading = false) }
                        resultDao.insertResult(ResultModel(mergedJson.toString()))
                        cardResultDao.deleteResult()
                        _eventFlow.emit(EventUi.Finish)
                    }

                    is ApiResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                pins = listOf(),
                                popUpMessage = apiResult.message ?: "",
                                isErrorPopUpShowing = true
                            )
                        }
                    }
                }
            }
        }
    }

    fun cancelTransaction(cancelledByUser: Boolean) {
        viewModelScope.launch {
            val cancelJson = JsonObject().apply {
                addProperty("isCancelledByUser", cancelledByUser)
                addProperty(
                    "errorMessage",
                    state.value.popUpMessage.ifEmpty { "Transaction cancelled by user" }
                )
                addProperty("cardNumber", state.value.cardNumber)
                addProperty("maskedCardNumber",state.value.maskedCardNumber)
                addProperty("track2Data", state.value.track2Data)
                addProperty("originEmv", state.value.originEmv)
                addProperty("posent", state.value.posent)
                addProperty("fld3", state.value.fld3)
                addProperty("fld48", state.value.fld48)
                addProperty("fld43", state.value.fld43)
                addProperty("binName", state.value.checkedBin.binName)
                addProperty("cardType", state.value.checkedBin.cardType)
                addProperty("NII", state.value.checkedBin.NII)
                addProperty("isOnUs", state.value.IsOnUs)
            }
            resultDao.insertResult(ResultModel(cancelJson.toString()))
            cardResultDao.insertResult(CardResultModel(cancelJson.toString()))
            _eventFlow.emit(EventUi.Finish)
        }
    }

    fun dismissPopUp() {
        _state.update {
            it.copy(
                isErrorPopUpShowing = false,
                isLoading = false,
                popUpMessage = "",
                loadingMessage = ""
            )
        }
    }

    override fun onCleared() {
        enableButton()
        emvUtility.stopEMVSearch()
        super.onCleared()
    }

}