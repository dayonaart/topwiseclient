package id.co.payment2go.terminalsdkhelper.program_pemerintah.ui

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.color.utilities.MaterialDynamicColors.onError
import com.google.gson.Gson
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
import kotlinx.coroutines.Dispatchers
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
class ProgramPemerintahPaymentViewModel@Inject constructor(
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
        private const val TAG = "ProgramPemerintahPinVM"
    }

    private val _state = MutableStateFlow(ProgramPemerintahUiState())
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

    fun setFld4(fld4Intent: String) {
        _state.update { state ->
            state.copy(fld4 = fld4Intent)
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

    fun setCardNumber(cardNumberIntent: String){
        _state.update { state ->
            state.copy(cardNumber = cardNumberIntent)
        }
    }

    fun setDescription(description: String) {
        _state.update { state ->
            state.copy(description = description)
        }
    }

    fun dismissPopUp() {
        _state.update { it.copy(isErrorPopUpShowing = false, isLoading = false) }
    }


    fun showPinpad() {
        viewModelScope.launch{
            val jsonDB = JsonParser.parseString(cardResultDao.getResult()?.result).asJsonObject
            setCardNumber(jsonDB.get("cardNumber").asStringOrNull() ?: "")
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
                        programPemerintahBansosPayment()
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
        }}

    fun programPemerintahBansosPayment() {
        viewModelScope
            .launch(Dispatchers.IO) {
                val response =
                    programPemerintahRepository
                        .postBansosPayment(
                            fld3 = state.value.fld3,
                            fld4 = state.value.fld4,
                            fld48 = state.value.fld48,
                            idTransaction = state.value.transactionId,
                            refNum = state.value.refNumber,
                            narasi = state.value.description,
                            pinBlock = state.value.pinBlock
                        )
                response.collectLatest { res ->
                    when (res) {
                        is ApiResult.Success -> {
                            Log.d(TAG, res.data.toString())
                            stanManager.increaseStan()
                            val responseCode = res.data?.get("RSPC").toString()
                            val responseMessage = res.data?.get("RSPM").toString()
                            val isPinBlocked = responseCode == "\"75\""
                            val incorrectPin = responseCode == "\"55\""
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
                            _state.update { it.copy(isLoading = false) }
                            resultDao.insertResult(ResultModel(res.data.toString()))
                            cardResultDao.deleteResult()
                            _eventFlow.emit(EventUi.Finish)
                        }

                        is ApiResult.Error -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    pins = listOf(),
                                    popUpMessage = res.message ?: "",
                                    isErrorPopUpShowing = true
                                )
                            }
                        }

                        is ApiResult.Loading -> {
                            _state.update {
                                it.copy(
                                    isLoading = true,
                                    loadingMessage = "Authorizing to server"
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
                addProperty("cardNumber", state.value.cardNumber)

            }
            resultDao.insertResult(ResultModel(cancelJson.toString()))
            cardResultDao.insertResult(CardResultModel(cancelJson.toString()))
            _eventFlow.emit(EventUi.Finish)
        }
    }

    override fun onCleared() {
        enableButton()
        emvUtility.stopEMVSearch()
        super.onCleared()
    }

}