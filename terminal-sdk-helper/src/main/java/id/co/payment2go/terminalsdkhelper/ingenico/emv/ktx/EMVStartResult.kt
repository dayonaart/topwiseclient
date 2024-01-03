package id.co.payment2go.terminalsdkhelper.ingenico.emv.ktx

import com.usdk.apiservice.aidl.emv.CAPublicKey
import com.usdk.apiservice.aidl.emv.CVMMethod
import com.usdk.apiservice.aidl.emv.CandidateAID
import com.usdk.apiservice.aidl.emv.CardRecord
import com.usdk.apiservice.aidl.emv.FinalData
import com.usdk.apiservice.aidl.emv.OfflinePinVerifyResult
import com.usdk.apiservice.aidl.emv.TransData

sealed class EMVStartResult {
    object OnInit : EMVStartResult()
    data class OnWaitCard(val flag: Int) : EMVStartResult()
    data class OnCardChecked(val cardType: Int) : EMVStartResult()
    data class OnAppSelect(val reselect: Boolean, val aids: MutableList<CandidateAID>) :
        EMVStartResult()

    data class OnFinalSelect(val finalData: FinalData) : EMVStartResult()
    data class OnReadRecord(val cardRecord: CardRecord?) : EMVStartResult()
    data class OnVerifyOfflinePin(
        val flag: Int,
        val random: ByteArray?,
        val caPublicKey: CAPublicKey?,
        val offlinePinVerifyResult: OfflinePinVerifyResult?
    ) : EMVStartResult()

    data class OnObtainData(val command: Int, val data: ByteArray?) : EMVStartResult()
    data class OnSendOut(val command: Int, val data: ByteArray?) : EMVStartResult()
    data class OnCardHolderVerify(val cvmMethod: CVMMethod?) : EMVStartResult()
    data class OnOnlineProcess(val transData: TransData?) : EMVStartResult()
    data class OnEndProcess(val resultCode: Int, val transData: TransData?) : EMVStartResult()
//    data class OnVerifyOfflinePin(val finalData: FinalData): EMVStartResult()
//    data class OnSendOut(val finalData: FinalData): EMVStartResult()
}
