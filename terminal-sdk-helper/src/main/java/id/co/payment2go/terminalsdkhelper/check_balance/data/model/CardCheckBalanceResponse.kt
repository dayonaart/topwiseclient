package id.co.payment2go.terminalsdkhelper.check_balance.data.model

data class CardCheckBalanceResponse(
    val amount: Long,
    val responseCode: String,
    val responseMessage: String,
    val coreJournal: String?,
    val transactionId: String?,
    val fromAccount: String?
)

fun CardCheckBalanceResponseDto.toCardCheckBalanceResponse(): CardCheckBalanceResponse {
    return CardCheckBalanceResponse(
        amount = amount,
        responseCode = responseCode,
        responseMessage = responseMessage,
        coreJournal = coreJournal,
        transactionId = transactionId,
        fromAccount = fromAccount
    )
}