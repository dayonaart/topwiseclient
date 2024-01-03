package id.co.payment2go.terminalsdkhelper.verifone.emv.set_param

import android.os.RemoteException
import android.util.Log
import com.vfi.smartpos.deviceservice.aidl.IEMV
import com.vfi.smartpos.deviceservice.constdefine.ConstIPBOC

object EmvParamInit {

    private const val OPERATION_ADD_PARAM = 1
    private const val OPERATION_CLEAR_PARAM = 3

    fun installEmvParam(emv: IEMV) {
        try {
            var isSuccess: Boolean
            try {
                isSuccess = emv.updateAID(OPERATION_CLEAR_PARAM, 1, null)
                Log.d("CLEAR_IC_AID", "Clear AID (smart AID):$isSuccess")
                isSuccess = emv.updateAID(OPERATION_CLEAR_PARAM, 2, null)
                Log.d("CLEAR_CTLS_AID", "Clear AID (CTLS):$isSuccess")
                isSuccess = emv.updateRID(OPERATION_CLEAR_PARAM, null)
                Log.d("CLEAR_RID", "Clear RID :$isSuccess")
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            /** Add AID per index */
            for (x in 0 until EMVVerifoneParam.aidMockSzzt.size) {
                try {
                    isSuccess = emv.updateAID(
                        OPERATION_ADD_PARAM,
                        ConstIPBOC.updateAID.aidType.smart_card,
                        EMVVerifoneParam.aidMockSzzt[x]
                    )
                    if (isSuccess) {
                        Log.d("AID", "update AID success ")
                    } else {
                        Log.e("AID", "update AID fails ")
                    }
                } catch (e: RemoteException) {
                    Log.e("AID","error adding capk:" + e.message)
                    e.printStackTrace()
                }
            }

            /** Add CAPK per index */
            for (x in 0 until EMVVerifoneParam.capkMockSzzt.size) {
                try {
                    val bRet: Boolean =
                        emv.updateRID(OPERATION_ADD_PARAM, EMVVerifoneParam.capkMockSzzt[x])
                    if (bRet) {
                        Log.d("CAPK", "update RID success ")
                    } else {
                        Log.e("CAPK", "update RID fails ")
                    }
                } catch (e: Exception) {
                    Log.e("CAPK","error adding capk:" + e.message)
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}