package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto

data class CardPaymentResponse(
    val amount: Long,
    val responseCode: String,
    val responseMessage: String,
    val coreJournal: String?,
    val transactionId: String?,
    val fromAccount: String?
)

fun CardPaymentResponseDto.toCardPaymentResponse(): CardPaymentResponse {
    return CardPaymentResponse(
        amount = amount,
        responseCode = responseCode,
        responseMessage = responseMessage,
        coreJournal = coreJournal,
        transactionId = transactionId,
        fromAccount = fromAccount
    )
}