package id.co.payment2go.terminalsdkhelper.program_pemerintah.ui

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
import id.co.payment2go.terminalsdkhelper.core.asStringOrNull
import id.co.payment2go.terminalsdkhelper.core.db.activity_result.ResultDao
import id.co.payment2go.terminalsdkhelper.core.db.activity_result.ResultModel
import id.co.payment2go.terminalsdkhelper.core.db.card_result.CardResultDao
import id.co.payment2go.terminalsdkhelper.core.db.card_result.CardResultModel
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.core_ui.EventUi
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.ApiResult
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.Bin
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.CardRepository
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.UnidentifiedCard
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.util.StanManager
import id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.ProgramPemerintahRepository
import id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.model.BansosInquiryRequest
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
class ProgramPemerintahViewModel @Inject constructor(
    private val emvUtility: EMVUtility,
    private val cardRepository: CardRepository,
    private val programPemerintahRepository: ProgramPemerintahRepository,
    private val sharedPreferences: SharedPreferences,
    private val stanManager: StanManager,
    private val deviceManagerUtility: DeviceManagerUtility,
    private val resultDao: ResultDao,
    private val pinpadUtility: PinpadUtility,
    private val cardResultDao: CardResultDao
) : ViewModel() {

    companion object {
        private const val TAG = "ProgramPemerintahVM"
    }

    private val _state = MutableStateFlow(ProgramPemerintahUiState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EventUi>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var job: Job? = null

    init {
        resetDatabase()
        disableButton()
        searchCard()
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

    fun setTransactionId(transactionIdIntent: String) {
        _state.update { state ->
            state.copy(transactionId = transactionIdIntent)
        }
    }

    fun setRefNum(refNumIntent: String) {
        _state.update { state ->
            state.copy(refNumber = refNumIntent)
        }
    }

    fun setDescription(description: String) {
        _state.update { state ->
            state.copy(description = description)
        }
    }


    private fun searchCard() {
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
                                        maskedCardNumber = Util.maskCardNumber(resource.data.cardNo),
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
                                        popUpMessage = resource.message ?: "Unknown error occurred",
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
                            Log.d(TAG, "checkBinRanges Loading")
                            _state.update {
                                it.copy(
                                    isErrorPopUpShowing = false,
                                    isLoading = true,
                                    popUpMessage = "",
                                    loadingMessage = "Checking card",
                                    isCardInserted = false
                                )
                            }
                        }

                        is ApiResult.Success -> {
                            val isOnUs = result.data?.isOnUs
                            if (isOnUs != state.value.onlyOnUs) {
                                _state.update {
                                    it.copy(
                                        isErrorPopUpShowing = true,
                                        isLoading = false,
                                        loadingMessage = "",
                                        popUpMessage = "Kartu yang anda masukkan tidak didukung"
                                    )
                                }
                                return@collectLatest
                            }
                            _state.update {
                                it.copy(
                                    checkedBin = result.data,
                                    isErrorPopUpShowing = false,
                                    isLoading = false,
                                    isCardInserted = true
                                )
                            }
                            cardResultDao.insertResult(
                                CardResultModel(
                                    JsonObject().apply {
                                        addProperty("cardNumber", state.value.cardNumber)
                                    }.toString()
                                )
                            )
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
        pinpadUtility.showPinPad(disorder = false, cardNumber = state.value.cardNumber) { result ->
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

    fun dismissPopUp() {
        _state.update { it.copy(isErrorPopUpShowing = false, isLoading = false) }
    }

    private fun postCardToHost() {
        job?.cancel()
        job = viewModelScope.launch {
            val mid = sharedPreferences.getString(Constant.MERCHANT_ID, "")
            val tid = sharedPreferences.getString(Constant.TERMINAL_ID, "")
            val toAccount = sharedPreferences.getString(Constant.ACCOUNT_NUM_KEY, "")
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
            if (toAccount.isNullOrEmpty()) {
                _state.update { cardState ->
                    cardState.copy(
                        isLoading = false,
                        isErrorPopUpShowing = true,
                        pins = listOf(),
                        popUpMessage = "Nomor rekening tidak boleh kosong"
                    )
                }
                return@launch
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

            val dataJson = JsonObject().apply {
                addProperty("accountNum", toAccount)
                addProperty(
                    "browser_agent",
                    sharedPreferences.getString(Constant.BROWSER_AGENT_KEY, "")
                )
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
                addProperty("Amt", "0")
                addProperty("ICC", state.value.emvData)
                addProperty("Track2", state.value.track2Data)
            }.toString()
            val secData = pinpadUtility.encryptData(dataToEncrypt).data ?: "Encrypt Error"

            val bansosInquiryRequest = BansosInquiryRequest(
                amount = "0",
                idTransaction = state.value.transactionId,
                refNum = state.value.refNumber,
                description = state.value.description,
                cardMasked = state.value.maskedCardNumber,
                emvData = state.value.emvData,
                kodeAgen = sharedPreferences.getString(Constant.AGENT_CODE, "") ?: "",
                mid = sharedPreferences.getString(Constant.MERCHANT_ID, "") ?: "",
                tid = sharedPreferences.getString(Constant.TERMINAL_ID, "") ?: "",
                nii = state.value.checkedBin.NII,
                pinBlock = state.value.pinBlock,
                poscon = "00", // Currently the default value is 00
                posent = state.value.posent,
                stan = stanManager.getCurrentStan(),
                track2Data = state.value.track2Data,
                fld3 = state.value.fld3,
                fld48 = state.value.fld48,
                iid = "2",
                txnType = "429",
                toAccount = toAccount,
                dataJson = dataJson,
                secData = secData,
            )
            programPemerintahRepository.postBansosInquiryIntent(bansosInquiryRequest)
                .collectLatest { apiResult ->
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
                            val message = "\"Transaksi ditolak, Kartu Debit yang dimasukan bukan Kartu Debit Program Pemerintah.\""
                            val responseCode = apiResult.data?.get("RSPC").toString()
                            var responseMessage = if(responseCode.replace("\"", "") == "06") message else apiResult.data?.get("RSPM").toString()
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
                            val jsonObject = JsonObject().apply {
                                addProperty("isPinBlocked", isPinBlocked)
                                addProperty("responseCode", responseCode)
                                addProperty("responseMessage", responseMessage)
                                addProperty(
                                    "bank",
                                    if (state.value.checkedBin.isOnUs) {
                                        "Bank Negara Indonesia"
                                    } else {
                                        "Other Bank"
                                    }
                                ) // hardcode because currently we only accept BNI Debit Card
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
                                addProperty("fld48", apiResult.data?.get("FLD48").toString())
                                addProperty("emvData", apiResult.data?.get("EMVD").toString())
                            }
                            val secDataFromResponse =
                                apiResult.data?.get("SEC")?.asStringOrNull() ?: ""
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

    fun setCardInserted(value: Boolean) {
        _state.update { it.copy(isCardInserted = value) }
    }

    fun cancelTransaction(cancelledByUser: Boolean) {
        viewModelScope.launch {
            val cancelJson = JsonObject().apply {
                addProperty("isCancelledByUser", cancelledByUser)
                addProperty(
                    "errorMessage",
                    state.value.popUpMessage.ifEmpty { "Transaction cancelled by user" }
                )
            }
            resultDao.insertResult(ResultModel(cancelJson.toString()))
            _eventFlow.emit(EventUi.Finish)
        }
    }

    override fun onCleared() {
        enableButton()
        emvUtility.stopEMVSearch()
        super.onCleared()
    }

}