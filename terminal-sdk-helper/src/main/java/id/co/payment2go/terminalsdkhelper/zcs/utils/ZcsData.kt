package id.co.payment2go.terminalsdkhelper.zcs.utils

import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput

enum class ZcsMsg(val code: Int) { MSG_CARD_APDU(2003), OTHER(0) }
enum class ZcsTransStatus { APPROVE, ONLINE, DECLINE, OTHER }

const val KEY_APDU = "APDU"


sealed class ZcsCardReaderOutput {
    data class ReadMagCard(val cardReadOutput: CardReadOutput) : ZcsCardReaderOutput()
    data class ReadICCard(val cardReadOutput: CardReadOutput) : ZcsCardReaderOutput()
    data class OnError(val message: String) : ZcsCardReaderOutput()
    object Loading : ZcsCardReaderOutput()
}

enum class ZcsCheckTrack { EXPIRED, IS_IC_CARD, SUCCESS }
data class ZcsTrack2Data(val res: String = "", val zcsCheckTrack: ZcsCheckTrack)

