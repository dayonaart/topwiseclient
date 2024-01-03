package id.co.payment2go.terminalsdkhelper.zcs.utils

import com.zcs.sdk.card.CardSlotNoEnum
import com.zcs.sdk.card.ICCard
import com.zcs.sdk.emv.EmvHandler
import com.zcs.sdk.emv.OnEmvListener


interface ZcsEmvListener : OnEmvListener {
    private val TAG: String
        get() = "ZcsEmvListener"

    val emvHandler: EmvHandler
    val icCard: ICCard
    override fun onExchangeApdu(p0: ByteArray?): ByteArray? {
        return icCard.icExchangeAPDU(CardSlotNoEnum.SDK_ICC_USERCARD, p0)
    }

    override fun onSelApp(p0: Array<out String>?): Int {
//        TermLog.d(TAG, "onSelApp: $p0")
        return 0
    }

    override fun onConfirmCardNo(p0: String?): Int {
        return 0
    }

    override fun onInputPIN(p0: Byte): Int {
//        TermLog.d(TAG, "onInputPIN: $p0")
        return 0
    }

    override fun onCertVerify(p0: Int, p1: String?): Int {
//        TermLog.d(TAG, "onCertVerify: $p1")
        return 0
    }

    override fun onlineProc(): Int {
        val authRespCode = ByteArray(3)
        val issuerResp = ByteArray(512)
        val issuerRespLen = IntArray(1)
        return emvHandler.separateOnlineResp(
            authRespCode, issuerResp,
            issuerRespLen[0]
        )
    }
}