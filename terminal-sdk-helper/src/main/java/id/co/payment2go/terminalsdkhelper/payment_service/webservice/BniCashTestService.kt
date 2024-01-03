package id.co.payment2go.terminalsdkhelper.payment_service.webservice

import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.RequestBodyDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.ResponseBodyDto
import retrofit2.http.Body
import retrofit2.http.POST

interface BniCashTestService {
    @POST("AndroidApi/v1/PST/Request")
    suspend fun postRequest(@Body requestBodyDto: RequestBodyDto): ResponseBodyDto
}