package id.co.payment2go.terminalsdkhelper.program_pemerintah.data

import com.google.gson.JsonObject
import id.co.payment2go.terminalsdkhelper.program_pemerintah.data.model.BansosInquiryRequestDto
import id.co.payment2go.terminalsdkhelper.program_pemerintah.data.model.BansosPaymentRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ProgramPemerintahService {
    @POST("AndroidApi/ApiHost/ProcessBankEnquiry")
    suspend fun postInquiryIntent(@Body bansosInquiryRequestDto: BansosInquiryRequestDto): JsonObject
    @POST("AndroidApi/ApiHost/ProcessBankEnquiry")
    suspend fun postInquiry(@Body bansosPaymentRequestDto: BansosPaymentRequestDto): JsonObject

    @POST("AndroidApi/ApiHost/BankPaymentRequest")
    suspend fun postPayment(@Body bansosPaymentRequestDto: BansosPaymentRequestDto): JsonObject
}