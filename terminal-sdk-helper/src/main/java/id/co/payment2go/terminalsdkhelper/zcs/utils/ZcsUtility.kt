package id.co.payment2go.terminalsdkhelper.zcs.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.zcs.sdk.SdkResult
import com.zcs.sdk.emv.EmvApp
import com.zcs.sdk.emv.EmvCapk
import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.core.TermLog
import id.co.payment2go.terminalsdkhelper.core.util.Util
import java.time.LocalDate
import java.time.ZoneId

object ZcsUtility {
    private const val TAG = "ZcsUtility"

    fun String.checkTrack(): ZcsTrack2Data {
        val output = this.replace("=", "D")
        val expired = output.substringAfter("D").take(4)
        var t2Data = output.filter { f -> f.isLetter() || f.isDigit() }
        val track2IsOdd = t2Data.length % 2 != 0
        if (track2IsOdd) {
            t2Data += "0"
        }
//        TermLog.d(TAG, "checkTrack -> $t2Data")
        if (Util.isIcCard(t2Data) && this.contains("=")) {
            return ZcsTrack2Data(
                res = "Silahkan masukkan kartu debit ber-chip Nasabah Anda.",
                zcsCheckTrack = ZcsCheckTrack.IS_IC_CARD
            )
        }
        if (Util.isCardExpired(expired)) {
            return ZcsTrack2Data(
                res = "Kartu debit Anda sudah melewati masa aktif kartu (Kadaluwarsa). Silahkan melakukan penggantian kartu debit di kantor cabang terdekat.",
                zcsCheckTrack = ZcsCheckTrack.EXPIRED
            )
        }
        return ZcsTrack2Data(res = t2Data, zcsCheckTrack = ZcsCheckTrack.SUCCESS)
    }

    fun loadNSICCS1984(): Boolean {
        val ea = EmvApp()
        ea.aid = ZCSNSICCS1984.DATA.rid
        ea.floorLimit = 0
        ea.settDOL("9F02065F2A029A039C0195059F3704")
        ea.settDOL("9F3704")
        ea.tacDefault = "DC4000A800"
        ea.tacDenial = "0010000000"
        ea.tacOnline = "DC4004F800"
        ea.termCapCVMReq = "8000040000"
        ea.termCapNoCVMReq = "8000040000"
        ea.version = "0002"
        val addApp = ZcsCardUtils.emvHandler.addApp(ea)
        return addApp == SdkResult.SDK_OK
    }

    fun loadNSICCS1408(): Boolean {
        val ea = EmvApp()
        ea.aid = ZCSNSICCS1408.DATA.rid
        ea.floorLimit = 0
        ea.settDOL("9F02065F2A029A039C0195059F3704")
        ea.settDOL("9F3704")
        ea.tacDefault = "DC4000A800"
        ea.tacDenial = "0010000000"
        ea.tacOnline = "DC4004F800"
        ea.termCapCVMReq = "8000040000"
        ea.termCapNoCVMReq = "8000040000"
        val addApp = ZcsCardUtils.emvHandler.addApp(ea)
        return addApp == SdkResult.SDK_OK
    }


    fun loadCAPK1984(): Boolean {
        val capk = EmvCapk()
        val today = LocalDate.now(ZoneId.systemDefault())
        if (today.isAfter(ZCSNSICCS1984.DATA.valid)) return false
        capk.rid = ZCSNSICCS1984.DATA.rid
        capk.keyID = ZCSNSICCS1984.DATA.index
        capk.modul = ZCSNSICCS1984.DATA.modulus
        capk.checkSum = ZCSNSICCS1984.DATA.checksum
        capk.expDate = "${ZCSNSICCS1984.DATA.valid}".replace("-", "")
        capk.exponent = ZCSNSICCS1984.DATA.exponent
        val addCapk = ZcsCardUtils.emvHandler.addCapk(capk)
        TermLog.d(TAG, "loadCAPK1984 -> $addCapk")
        return addCapk == SdkResult.SDK_OK
    }

    fun loadCAPK1408(): Boolean {
        val capk = EmvCapk()
        val today = LocalDate.now(ZoneId.systemDefault())
        if (today.isAfter(ZCSNSICCS1408.DATA.valid)) return false
        capk.rid = ZCSNSICCS1408.DATA.rid
        capk.keyID = ZCSNSICCS1408.DATA.index
        capk.modul = ZCSNSICCS1408.DATA.modulus
        capk.checkSum = ZCSNSICCS1408.DATA.checksum
        capk.expDate = "${ZCSNSICCS1408.DATA.valid}".replace("-", "")
        capk.exponent = ZCSNSICCS1408.DATA.exponent
        val addCapk = ZcsCardUtils.emvHandler.addCapk(capk)
        TermLog.d(TAG, "loadCAPK1408 -> $addCapk")
        return addCapk == SdkResult.SDK_OK
    }

    fun setTlv() {
        ZcsCardUtils.emvHandler.setTlvData(
            ZcsEmvTag.AVN,
            BytesUtil.hexString2Bytes("0009")
        )
    }

    fun Context.setDisableNavigation(isEnable: Boolean){
        when(isEnable){
            true -> {
                val intentData = arrayListOf(
                    Intent("custom.DISABLE_BACK"),
                    Intent("custom.DISABLE_HOME"),
                    Intent("custom.DISABLE_RECENT")
                )

                for (data in intentData){
                    this.sendBroadcast(data)
                }

            }
            false -> {
                val intentEnableData = arrayListOf(
                    Intent("custom.ENABLE_BACK"),
                    Intent("custom.ENABLE_HOME"),
                    Intent("custom.ENABLE_RECENT")
                )

                for (data in intentEnableData){
                    this.sendBroadcast(data)
                }
            }
        }
    }
}