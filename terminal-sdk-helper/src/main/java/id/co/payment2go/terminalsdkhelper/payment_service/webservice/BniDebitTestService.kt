package id.co.payment2go.terminalsdkhelper.payment_service.webservice

import com.google.gson.JsonObject
import id.co.payment2go.terminalsdkhelper.check_balance.data.model.CardCheckBalanceResponseDto
import id.co.payment2go.terminalsdkhelper.check_balance.data.model.CardCheckBalanceTestDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.BinDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CardPaymentDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CardPaymentResponseDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.CheckedBinResponse
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.InitDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.InitResponseDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.perform_logon.LogonRequestDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.perform_logon.LogonResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * This interface defines the API endpoints for BNI Debit service.
 * It provides methods to interact with various API endpoints related to BNI Debit transactions.
 */
interface BniDebitTestService {

    /**
     * Performs a Bank Identification Number (BIN) range check using the provided BIN information.
     *
     * @param binDto The data transfer object representing the Bank Identification Number (BIN) to be checked.
     * @return A [CheckedBinResponse] containing the result of the BIN range check.
     */
    @POST("AndroidApi/ApiHost/BinRanges")
    suspend fun checkBinRanges(@Body binDto: BinDto): CheckedBinResponse

    /**
     * Initiates a payment transaction with the provided initialization data.
     *
     * @param initDto The data transfer object containing the initialization data for the transaction.
     * @return An [InitResponseDto] containing the response data from the initiation request.
     */
    @POST("AndroidApi/ApiHost/InitStep1")
    suspend fun init(@Body initDto: InitDto): InitResponseDto

    /**
     * Posts a card payment transaction using the provided card payment data.
     *
     * @param cardPayment The data transfer object representing the card payment information.
     * @return A [CardPaymentResponseDto] containing the response data from the card payment request.
     */
    @POST("AndroidApi/ApiHost/PaymentStatus")
    suspend fun postCardPaymentTest(@Body cardPayment: CardPaymentDto): JsonObject

    /**
     * Checks the balance of a card using the provided card balance data.
     *
     * @param cardCheckBalance The data transfer object representing the card balance information.
     * @return A [CardCheckBalanceResponseDto] containing the response data from the balance check request.
     */
    @POST("AndroidApi/ApiHost/InfoSaldo")
    suspend fun checkBalance(@Body cardCheckBalance: CardCheckBalanceTestDto): JsonObject

    @POST("AndroidApi/ApiHost/BankPaymentRequest")
    suspend fun postBankPayment(@Body body: JsonObject): JsonObject

    @POST("AndroidApi/ApiHost/Logon")
    suspend fun postLogon(@Body body: LogonRequestDto): LogonResponseDto
}
