package id.co.payment2go.terminalsdkhelper.topwise.emv

import android.util.Log
import com.topwise.cloudpos.aidl.card.AidlCheckCard
import com.topwise.cloudpos.aidl.emv.level2.AidlEmvL2
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad
import com.topwise.cloudpos.aidl.system.AidlSystem
import com.topwise.toptool.api.convert.IConvert
import com.topwise.toptool.api.packer.IPacker
import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.core.util.Util
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsLogAscii
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object TopWiseUtility {
    private const val TAG = "EmvTopWiseUtility"
    private lateinit var topWiseCardReaderOutput: MutableStateFlow<TopWiseCardReaderOutput?>

    fun initCardReader(
        cardManager: AidlCheckCard,
        emvManager: AidlEmvL2,
        iConvert: IConvert,
        iPacker: IPacker,
        stan: Long,
        amount: Long,
        aidlPinpad: AidlPinpad,
        deviceSystem: AidlSystem
    ) {
        topWiseCardReaderOutput = MutableStateFlow(null)
        TopWiseListener.cardManager = cardManager
        TopWiseListener.topWiseCardReaderOutput = topWiseCardReaderOutput
        TopWiseListener.emvManager = emvManager
        TopWiseListener.iConvert = iConvert
        TopWiseListener.iPacker = iPacker
        TopWiseListener.amount = amount
        TopWiseListener.stan = stan
        TopWiseListener.pinpad = aidlPinpad
        TopWiseListener.deviceSystem = deviceSystem
        Log.d(TAG, ZcsLogAscii.startEmv)
        CoroutineScope(Dispatchers.IO).launch {
            cardManager.checkCard(true, true, false, 60 * 1000, TopWiseListener)
        }
    }

    val collectData = callbackFlow<Resource<CardReadOutput>> {
        topWiseCardReaderOutput.collectLatest {
            when (it) {
                is TopWiseCardReaderOutput.ReadICCard -> {
                    Log.d(TAG, ZcsLogAscii.successEmv)
                    this.send(Resource.Success(data = it.cardReadOutput))
                }

                is TopWiseCardReaderOutput.ReadMagCard -> {
                    Log.d(TAG, ZcsLogAscii.successEmv)
                    this.send(Resource.Success(data = it.cardReadOutput))
                }

                TopWiseCardReaderOutput.Loading -> {
                    Log.d(TAG, ZcsLogAscii.loading)
                    this.send(Resource.Loading())
                }

                is TopWiseCardReaderOutput.OnError -> {
                    Log.d(TAG, ZcsLogAscii.errorEmv)
                    this.send(Resource.Error(message = it.message))
                }

                else -> {

                }
            }
        }
    }

    fun String.checkTrack(): TopWiseTrack2Data {
        val output = this.replace("=", "D")
        val expired = output.substringAfter("D").take(4)
        var t2Data = output.filter { f -> f.isLetter() || f.isDigit() }
        val track2IsOdd = t2Data.length % 2 != 0
        if (track2IsOdd) {
            t2Data += "0"
        }
        if (Util.isIcCard(t2Data) && this.contains("=")) {
            return TopWiseTrack2Data(
                res = "Silahkan masukkan kartu debit ber-chip Nasabah Anda.",
                topWiseCheckTrack = TopWiseCheckTrack.IS_IC_CARD
            )
        }
        if (Util.isCardExpired(expired)) {
            return TopWiseTrack2Data(
                res = "Kartu debit Anda sudah melewati masa aktif kartu (Kadaluwarsa). Silahkan melakukan penggantian kartu debit di kantor cabang terdekat.",
                topWiseCheckTrack = TopWiseCheckTrack.EXPIRED
            )
        }
        return TopWiseTrack2Data(res = t2Data, topWiseCheckTrack = TopWiseCheckTrack.SUCCESS)
    }
}