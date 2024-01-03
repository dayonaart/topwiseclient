package id.co.payment2go.terminalsdkhelper.ingenico.emv.ktx

import android.os.Bundle
import com.usdk.apiservice.aidl.emv.CAPublicKey
import com.usdk.apiservice.aidl.emv.CVMMethod
import com.usdk.apiservice.aidl.emv.CandidateAID
import com.usdk.apiservice.aidl.emv.CardRecord
import com.usdk.apiservice.aidl.emv.EMVEventHandler
import com.usdk.apiservice.aidl.emv.FinalData
import com.usdk.apiservice.aidl.emv.OfflinePinVerifyResult
import com.usdk.apiservice.aidl.emv.TransData
import com.usdk.apiservice.aidl.emv.UEMV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun UEMV.startEmvAndAwait(bundle: Bundle, onResult: (EMVStartResult) -> Unit) {
    return withContext(Dispatchers.IO) {
        this@startEmvAndAwait.startProcess(bundle, object : EMVEventHandler.Stub() {
            override fun onInitEMV() {
                onResult(EMVStartResult.OnInit)
            }

            override fun onWaitCard(flag: Int) {
                onResult(EMVStartResult.OnWaitCard(flag))
            }

            override fun onCardChecked(cardType: Int) {
                onResult(EMVStartResult.OnCardChecked(cardType))
            }

            override fun onAppSelect(reselect: Boolean, aids: MutableList<CandidateAID>) {
                onResult(EMVStartResult.OnAppSelect(reselect, aids))
            }

            override fun onFinalSelect(finalData: FinalData) {
                onResult(EMVStartResult.OnFinalSelect(finalData))
            }

            override fun onReadRecord(cardRecord: CardRecord?) {
                onResult(EMVStartResult.OnReadRecord(cardRecord))
            }

            override fun onCardHolderVerify(cvmMethod: CVMMethod?) {
                onResult(EMVStartResult.OnCardHolderVerify(cvmMethod))
            }

            override fun onOnlineProcess(transData: TransData?) {
                onResult(EMVStartResult.OnOnlineProcess(transData))
            }

            override fun onEndProcess(resultCode: Int, transData: TransData?) {
                onResult(EMVStartResult.OnEndProcess(resultCode, transData))
            }

            override fun onVerifyOfflinePin(
                flag: Int,
                random: ByteArray?,
                caPublicKey: CAPublicKey?,
                offlinePinVerifyResult: OfflinePinVerifyResult?
            ) {
                onResult(
                    EMVStartResult.OnVerifyOfflinePin(
                        flag,
                        random,
                        caPublicKey,
                        offlinePinVerifyResult
                    )
                )
            }

            override fun onObtainData(command: Int, data: ByteArray?) {
                onResult(EMVStartResult.OnObtainData(command, data))
            }

            override fun onSendOut(command: Int, data: ByteArray?) {
                onResult(EMVStartResult.OnSendOut(command, data))
            }

        })
    }
}