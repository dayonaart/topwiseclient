package com.example.clientbni

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clientbni.constant.Properties
import com.google.gson.Gson
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.payment2go.terminalsdkhelper.common.emv.EMVUtility
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.pinpad.OnPinPadResult
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterUtility
import id.co.payment2go.terminalsdkhelper.common.system.device.DeviceManagerUtility
import id.co.payment2go.terminalsdkhelper.core.Constant
import id.co.payment2go.terminalsdkhelper.core.TermLog
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.BniCashService
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.BniDebitService
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.perform_logon.LogonRequestDto
import id.co.payment2go.terminalsdkhelper.payments.debit_card.ui.DebitActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val deviceManagerUtility: Lazy<DeviceManagerUtility>,
    private val printerUtility: Lazy<PrinterUtility>,
    private val pinpadUtility: Lazy<PinpadUtility>,
    private val sharedPreferences: Lazy<SharedPreferences>,
    private val debitService: Lazy<BniDebitService>,
    private val cashService: Lazy<BniCashService>,
    private val emvUtility: Lazy<EMVUtility>,
) :
    ViewModel() {
    //    38343342323144353639304545434330
    private val TAG = "MainViewModel"
    var masterKeyController by mutableStateOf("484455B474A6C6115FF62236D8A09C74")
    var tdkKeyController by mutableStateOf("")
    var pinKeyController by mutableStateOf("")
    var encryptController by mutableStateOf(Properties.printText)
    var decryptController by mutableStateOf("")
    var injectResultMessage by mutableStateOf("")
    var encryptResult by mutableStateOf<String?>(null)
    var decryptResult by mutableStateOf<String?>(null)
    var loading by mutableStateOf(false)
    var postLogonResponse by mutableStateOf<String?>(null)

    fun masterKeyOnchange(t: String) {
        masterKeyController = t
    }

    fun encryptOnchange(t: String) {
        encryptController = t
    }

    fun decryptOnchange(t: String) {
        decryptController = t
    }

    suspend fun encryptData() = viewModelScope.async(Dispatchers.IO) {
        val res = pinpadUtility.get().encryptData(encryptController)
        encryptResult = res.data
        decryptController = res.data ?: ""
    }.join()

    suspend fun decryptData() = viewModelScope.async(Dispatchers.IO) {
        val res = pinpadUtility.get().decryptData(decryptController)
        decryptResult = res.data
    }.join()

    fun print() {
        val status = printerUtility.get().buildPrintTemplate(Properties.printText)
        TermLog.d(TAG, "print -> $status")
    }

    suspend fun postingLogon() {
        try {
            loading = true
            val logonResult = debitService.get().postLogon(
                LogonRequestDto(
                    mMID = sharedPreferences.get().getString(Constant.MERCHANT_ID, "") ?: "",
                    mTID = sharedPreferences.get().getString(Constant.TERMINAL_ID, "") ?: "",
                    serialNo = deviceManagerUtility.get().getSerialNumberDevice()
                )
            )
            postLogonResponse = Gson().toJson(logonResult)
            loading = false
        } catch (e: Exception) {
            postLogonResponse = e.localizedMessage
            loading = false
        }
    }

    suspend fun postLogon() {
        injectResultMessage = ""
        loading = true
        viewModelScope.async(Dispatchers.IO) {
            loading = true
            val result =
                pinpadUtility.get()
                    .injectMasterKey(BytesUtil.hexString2ByteArray(masterKeyController))
            if (result is Resource.Error) {
                injectResultMessage += "* Inject master key failed: ${result.message}\n"
                return@async
            }
            injectResultMessage += "* Inject master key success\n"
            val logonResult = debitService.get().postLogon(
                LogonRequestDto(
                    mMID = sharedPreferences.get().getString(Constant.MERCHANT_ID, "") ?: "",
                    mTID = sharedPreferences.get().getString(Constant.TERMINAL_ID, "") ?: "",
                    serialNo = deviceManagerUtility.get().getSerialNumberDevice()
                )
            )
            if (logonResult.secData != null) {
                val dataKey = logonResult.secData!!.dataKey
                tdkKeyController = dataKey.substring(1, dataKey.length)
                val injectDataKey =
                    pinpadUtility.get()
                        .injectDataKey(BytesUtil.hexString2ByteArray(tdkKeyController))
                injectResultMessage += if (injectDataKey is Resource.Success) {
                    "* Inject data key success\n"
                } else {
                    "* Inject data key failed: ${injectDataKey.message}\n"
                }
                val pinKey = logonResult.secData!!.pinKey
                pinKeyController = pinKey.substring(1, pinKey.length)
                val injectPinKey =
                    pinpadUtility.get()
                        .injectPinKey(BytesUtil.hexString2ByteArray(pinKeyController))
                injectResultMessage += if (injectPinKey is Resource.Success) {
                    "* Inject pin key success\n"
                } else {
                    "* Inject data key failed: ${injectPinKey.message}\n"
                }
            } else {
                Log.d("GSTPaymentService", "Gagal mendapatkan session key")
            }
        }.join()
        loading = false
    }

    fun openDebitScreen(context: Context) {
        val intent = Intent(context, DebitActivity::class.java)
        intent.putExtra("TOTAL_AMOUNT", "45000")
        intent.putExtra("TRANSACTION_ID", "BNI3483847829347")
        intent.putExtra("DESCRIPTION", "Contoh narasi")
        intent.putExtra("REF_NUM", "89929438230498")
        context.startActivity(intent)
    }

    fun openPinpad() {
        pinpadUtility.get().showPinPad(false, "1946340010000177") { onPinPadResult ->
            when (onPinPadResult) {
                OnPinPadResult.OnCancel -> {
                    Log.d(TAG, "openPinpad: cancel")
                }

                is OnPinPadResult.OnConfirm -> {
                    val result = Util.onConfirmPinpadCase(onPinPadResult.data)
                    Log.d(TAG, "openPinpad: $result")
                }

                is OnPinPadResult.OnError -> {
                    Log.d(TAG, "openPinpad: ${onPinPadResult.error}")
                }

                is OnPinPadResult.OnInput -> {
                }

                else -> {}
            }
        }
    }

    fun readCard() {
        viewModelScope.launch {
            emvUtility.get().searchCard(10, 20000).collectLatest {
                when (it) {
                    is Resource.Error -> {
                        TermLog.d(TAG, "readCard -> ${it.message}")
                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        TermLog.d(TAG, "readCard -> ${it.data}")
                    }
                }
            }
        }
    }

    fun injectDummyKey() {
        injectResultMessage = ""
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                pinpadUtility.get()
                    .injectMasterKey(BytesUtil.hexString2ByteArray(masterKeyController))
            if (result is Resource.Error) {
                injectResultMessage += "* Inject master key failed: ${result.message}\n"
                return@launch
            }
            injectResultMessage += "* Inject master key success\n"

            val injectPinKey =
                pinpadUtility.get()
                    .injectPinKey(BytesUtil.hexString2ByteArray(pinKeyController))

            injectResultMessage += if (injectPinKey is Resource.Success) {
                "* Inject pin key success\n"
            } else {
                "* Inject data key failed: ${injectPinKey.message}\n"
            }
            val injectDataKey =
                pinpadUtility.get()
                    .injectDataKey(BytesUtil.hexString2ByteArray(tdkKeyController))
            injectResultMessage += if (injectDataKey is Resource.Success) {
                "* Inject data key success\n"
            } else {
                "* Inject data key failed: ${injectDataKey.message}\n"
            }

        }
    }

}
