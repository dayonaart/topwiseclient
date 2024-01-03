package id.co.payment2go.terminalsdkhelper.payments.debit_card.domain

import com.google.gson.JsonObject
import id.co.payment2go.terminalsdkhelper.check_balance.data.model.CardCheckBalance
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.ApiResult
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.BniDebitService
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.Bin
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CardPayment
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CardPaymentResponse
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CheckedBin
import kotlinx.coroutines.flow.Flow

/**
 * A repository that handles card payments.
 */
interface CardRepository {

    /**
     * Posts a card payment to the server using the [BniDebitService].
     *
     * @param cardPayment The [CardPayment] object representing the card payment to be posted.
     * @return A [Flow] of [ApiResult]<[CardPaymentResponse]> that emits the result of the card payment.
     */
    fun postCardPayment(cardPayment: CardPayment): Flow<ApiResult<JsonObject>>

    /**
     * Checks the ranges of a given bin number using the [BniDebitService].
     *
     * @param bin The [Bin] object representing the bin number to be checked.
     * @return A [Flow] of [ApiResult]<[CheckedBin]> that emits the result of the bin range check.
     * @throws UnidentifiedCard if the bin number is not supported.
     */
    fun checkBinRanges(bin: Bin): Flow<ApiResult<CheckedBin>>

    fun checkBalance(checkBalance: CardCheckBalance): Flow<ApiResult<JsonObject>>

    suspend fun init(serialNumber: String): ApiResult<Boolean>
}