package id.co.payment2go.terminalsdkhelper.payments.debit_card.ui

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
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.core_ui.EventUi
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.ApiResult
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.Bin
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CardPayment
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.CardRepository
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.UnidentifiedCard
import id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.util.StanManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
class CardViewModel @Inject constructor(
    private val emvUtility: EMVUtility,
    private val cardRepository: CardRepository,
    private val sharedPreferences: SharedPreferences,
    private val stanManager: StanManager,
    private val deviceManagerUtility: DeviceManagerUtility,
    private val resultDao: ResultDao,
    private val pinpadUtility: PinpadUtility
) : ViewModel() {

    private val TAG = "CardViewModel"

    private val _state = MutableStateFlow(CardUiState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EventUi>()
    val eventFlow = _eventFlow.asSharedFlow()
    private var job: Job? = null

    init {
        disableButton()
        resetDatabase()
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

    fun setAmount(amount: Long) {
        _state.update { it.copy(totalAmount = amount) }
    }

    fun setTransactionId(id: String) {
        Log.d(TAG, "setTransactionId: $id")
        _state.update { it.copy(transactionId = id) }
    }

    fun setDescription(desc: String) {
        val newDesc = desc.replace("&", "dan")
        _state.update { it.copy(description = newDesc) }
    }

    fun setRefNumber(refNum: String) {
        _state.update { it.copy(refNumber = refNum) }
    }

    private fun searchCard() {
        viewModelScope.launch {
            try {
                withTimeout(1.minutes) {
                    emvUtility.searchCard(
                        stan = stanManager.getCurrentStan(),
                        amount = state.value.totalAmount
                    ).collectLatest { resource ->
                        Log.d(TAG, "searchCard: $resource")
                        when (resource) {
                            is Resource.Loading -> {
                                _state.update { state ->
                                    state.copy(
                                        isLoading = true,
                                        loadingMessage = "Generating EMV",
                                        popUpMessage = "",
                                        isPopUpShowing = false,
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
                                        isPopUpShowing = false,
                                        popUpMessage = "",
                                        cardNumber = resource.data.cardNo,
                                        track2Data = resource.data.track2Data,
                                        originEmv = resource.data.EMVData,
                                        posent = resource.data.posEntryMode,
                                        maskedCardNumber = Util.maskCardNumber(resource.data.cardNo),
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
                                        isPopUpShowing = true
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
                        isPopUpShowing = true,
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
                                    isPopUpShowing = false,
                                    isLoading = true,
                                    popUpMessage = "",
                                    loadingMessage = "Mengecek Kartu",
                                    isCardInserted = false
                                )
                            }
                        }

                        is ApiResult.Success -> {
                            val isOnUs = result.data?.isOnUs
                            if (isOnUs != state.value.onlyOnUs) {
                                _state.update {
                                    it.copy(
                                        isPopUpShowing = true,
                                        isLoading = false,
                                        loadingMessage = "",
                                        popUpMessage = "Kartu yang anda masukkan tidak didukung"
                                    )
                                }
                                return@collectLatest
                            }
                            _state.update {
                                it.copy(
                                    checkedBin = result.data!!,
                                    isPopUpShowing = false,
                                    isLoading = false,
                                    isCardInserted = true
                                )
                            }
                        }

                        is ApiResult.Error -> {
                            Log.d(TAG, "checkBinRanges Error: " + result.message)
                            _state.update {
                                it.copy(
                                    isPopUpShowing = true,
                                    isLoading = false,
                                    isTimeoutConnection = result.isTimeoutConnection,
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
                        isPopUpShowing = true,
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
                    postCardPayment()
                }

                is OnPinPadResult.OnCancel -> {
                    cancelTransaction(cancelledByUser = true)
                }

                is OnPinPadResult.OnError -> {
                    _state.update {
                        it.copy(
                            popUpMessage = "Pinpad error: ${result.error}",
                            isPopUpShowing = true
                        )
                    }
                }
            }
        }
    }

    fun dismissPopUp() {
        _state.update { it.copy(isPopUpShowing = false, isLoading = false) }
    }

    private fun postCardPayment() {
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
                        isPopUpShowing = true,
                        pins = listOf(),
                        popUpMessage = "MID dan TID tidak boleh kosong"
                    )
                }
                return@launch
            }
            if (toAccount.isEmpty()) {
                _state.update { cardState ->
                    cardState.copy(
                        isLoading = false,
                        isPopUpShowing = true,
                        pins = listOf(),
                        popUpMessage = "Nomor rekening tidak boleh kosong"
                    )
                }
                return@launch
            }
            if (toAccount.length % 16 != 0) {
                val padding = 16 - (toAccount.length % 16)
                toAccount = toAccount.padStart(toAccount.length + padding, '0')
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
                addProperty("accountNum", accountNum)
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
                addProperty("Amt", state.value.totalAmount.toString())
                addProperty("ICC", state.value.emvData)
                addProperty("Track2", state.value.track2Data)
            }.toString()
            val secData = pinpadUtility.encryptData(dataToEncrypt).data ?: "Encrypt Error"

            val cardPayment = CardPayment(
                amount = state.value.totalAmount.toString(),
                cardMasked = state.value.maskedCardNumber,
                emvData = state.value.emvData,
                idTransaction = state.value.transactionId,
                kodeAgen = sharedPreferences.getString(Constant.AGENT_CODE, "") ?: "",
                mid = sharedPreferences.getString(Constant.MERCHANT_ID, "") ?: "",
                tid = sharedPreferences.getString(Constant.TERMINAL_ID, "") ?: "",
                description = state.value.description,
                nii = state.value.checkedBin.NII,
                pinBlock = state.value.pinBlock,
                poscon = "00", // Currently the default value is 00
                posent = state.value.posent,
                refNum = state.value.refNumber,
                stan = stanManager.getCurrentStan(),
                track2Data = state.value.track2Data,
                toAccount = toAccount,
                dataJson = dataJson,
                secData = secData,
                iid = "2",
                fld3 = "311000"
            )
            // Simpan transaksi ID untuk digunakan pengecekan reversal
            sharedPreferences.edit()
                .putString(Constant.REVERSAL_TRANSACTION_ID_KEY, state.value.transactionId)
                .putString(Constant.REVERSAL_AMOUNT_KEY, state.value.totalAmount.toString())
                .apply()
            delay(500L)
            cardRepository.postCardPayment(cardPayment).collectLatest { apiResult ->
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
                        val responseCode = apiResult.data?.get("RSPC")?.asStringOrNull() ?: ""
                        val responseMessage = apiResult.data?.get("RSPM")?.asStringOrNull() ?: ""
                        val isPinBlocked = responseCode == "75"
                        val incorrectPin = responseCode == "55"

                        if (responseCode != "00") {
                            sharedPreferences.edit()
                                .putString(Constant.REVERSAL_TRANSACTION_ID_KEY, "")
                                .putString(Constant.REVERSAL_AMOUNT_KEY, "")
                                .apply()
                        }
                        if (incorrectPin) {
                            _state.update { cardState ->
                                cardState.copy(
                                    isLoading = false,
                                    isPopUpShowing = true,
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
                            addProperty(
                                "transactionId",
                                apiResult.data.get("TXNID")?.asStringOrNull() ?: ""
                            )
                            addProperty("refNum", state.value.refNumber)
                            addProperty(
                                "accountNumber",
                                apiResult.data.get("FROMACCOUNT")?.asStringOrNull()
                            )
                            addProperty(
                                "coreJurnal",
                                apiResult.data.get("COREJOURNAL")?.asStringOrNull() ?: ""
                            )
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
                                "MTID",
                                sharedPreferences.getString(Constant.MERCHANT_ID, "")
                            )

                        }
                        val secDataFromResponse = apiResult.data.get("SEC")?.asStringOrNull() ?: ""
                        val decryptedMessage =
                            pinpadUtility.decryptData(secDataFromResponse).data ?: "{}"
                        val decryptedJson = JsonParser.parseString(decryptedMessage).asJsonObject
                        val mergedJson = Util.mergeJsonObjects(jsonObject, decryptedJson)
                        mergedJson.addProperty(
                            "BTID",
                            sharedPreferences.getString(Constant.BANK_TERMINAL_ID, "") ?: ""
                        )
                        _state.update { it.copy(isLoading = false, loadingMessage = "") }
                        resultDao.insertResult(ResultModel(mergedJson.toString()))
                        _eventFlow.emit(EventUi.Finish)
                    }

                    is ApiResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                loadingMessage = "",
                                isTimeoutConnection = apiResult.isTimeoutConnection,
                                pins = listOf(),
                                popUpMessage = apiResult.message ?: "",
                                isPopUpShowing = true
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
                addProperty(
                    "isTimeoutConnection",
                    state.value.isTimeoutConnection
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