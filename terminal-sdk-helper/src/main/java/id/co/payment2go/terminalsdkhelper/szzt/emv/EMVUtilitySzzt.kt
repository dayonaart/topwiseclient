package id.co.payment2go.terminalsdkhelper.szzt.emv

import android.os.HandlerThread
import android.util.Log
import com.szzt.sdk.device.Device
import com.szzt.sdk.device.card.MagneticStripeCardReader
import com.szzt.sdk.device.card.SmartCardReader
import com.szzt.sdk.device.emv.EmvInterface
import com.szzt.sdk.device.pinpad.PinPad
import com.szzt.sdk.system.SystemManager
import id.co.payment2go.terminalsdkhelper.common.emv.EMVUtility
import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.szzt.BindServiceSzzt
import id.co.payment2go.terminalsdkhelper.szzt.utils.SearchCardResultSzzt
import id.co.payment2go.terminalsdkhelper.szzt.utils.SmartCardThread
import id.co.payment2go.terminalsdkhelper.szzt.utils.SzztUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn

class EMVUtilitySzzt(
    bindService: BindServiceSzzt
) : EMVUtility, SzztUtil {
    @Suppress("PrivatePropertyName")
    private val TAG = "EMVUtilitySzzt"
    override var smartCardHandlerThread: HandlerThread? = null
    override lateinit var smartCardThread: SmartCardThread
    private val deviceManager = bindService.mDeviceManager
    override val magCardReader = getMagneticStripeCardReader()
    override val smCardReader = getSmartCardReader()
    override val emvInterface: EmvInterface = bindService.getEmvInterface()
    override var smartTransType = EmvInterface.TRANS_GOODS_SERVICE
    override var searchCardResultSzzt =
        MutableStateFlow<SearchCardResultSzzt>(SearchCardResultSzzt.Waiting)
    override var systemManager: SystemManager? =
        if (bindService.isDeviceManagerConnected()) bindService.mSystemManager else null
    override var pinPad: PinPad =
        bindService.mDeviceManager.getDeviceByType(Device.TYPE_PINPAD).toList()[0] as PinPad

    private fun getMagneticStripeCardReader(): MagneticStripeCardReader {
        val magStripCards = deviceManager
            .getDeviceByType(Device.TYPE_MAGSTRIPECARDREADER)
        return magStripCards[0] as MagneticStripeCardReader
    }

    private fun getSmartCardReader(): SmartCardReader {
        val smartCardReaders = deviceManager.getDeviceByType(Device.TYPE_SMARTCARDREADER)
        return smartCardReaders[0] as SmartCardReader
    }


    override fun searchCard(stan: Long, amount: Long): Flow<Resource<CardReadOutput>> {
        return collectData.flowOn(Dispatchers.IO)
    }

    private val collectData = callbackFlow<Resource<CardReadOutput>> {
        searchCardResultSzzt.collectLatest {
            when (it) {
                is SearchCardResultSzzt.FindICCard -> {
                    Log.d(TAG, "SearchCardResultSzzt.FindICCard ")
                    this.send(
                        Resource.Success(
                            data = CardReadOutput(
                                cardNo = it.szztCardData.cardNumber,
                                track2Data = it.szztCardData.trackData,
                                posEntryMode = it.szztCardData.mode,
                                EMVData = it.szztCardData.emvData
                            )
                        )
                    )
                }

                is SearchCardResultSzzt.FindMagCard -> {
                    Log.d(TAG, "SearchCardResultSzzt.FindMagCard ")
                    this.send(
                        Resource.Success(
                            data = CardReadOutput(
                                cardNo = it.szztCardData.cardNumber,
                                track2Data = it.szztCardData.trackData,
                                posEntryMode = it.szztCardData.mode
                            )
                        )
                    )
                }

                SearchCardResultSzzt.Waiting -> {
                    searchCard()
                }

                SearchCardResultSzzt.Loading -> {
                    this.send(Resource.Loading())
                }

                SearchCardResultSzzt.Expired -> {
                    this.send(Resource.Error("Kartu debit Anda sudah melewati masa aktif kartu (Kadaluwarsa). Silahkan melakukan penggantian kartu debit di kantor cabang terdekat."))
                }

                is SearchCardResultSzzt.OnError -> {
                    this.send(Resource.Error(it.message))
                    stopEmv()
                }

            }
        }
    }

    override suspend fun startEMV(stan: Long, onFinish: (CardReadOutput?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun stopEMVSearch() {
        stopEmv()
    }
}