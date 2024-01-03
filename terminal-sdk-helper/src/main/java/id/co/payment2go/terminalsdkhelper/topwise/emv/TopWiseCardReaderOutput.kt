package id.co.payment2go.terminalsdkhelper.topwise.emv

import id.co.payment2go.terminalsdkhelper.core.util.CardReadOutput

sealed class TopWiseCardReaderOutput {
    data class ReadMagCard(val cardReadOutput: CardReadOutput) : TopWiseCardReaderOutput()
    data class ReadICCard(val cardReadOutput: CardReadOutput) : TopWiseCardReaderOutput()
    data class OnError(val message: String) : TopWiseCardReaderOutput()
    object Loading : TopWiseCardReaderOutput()
}