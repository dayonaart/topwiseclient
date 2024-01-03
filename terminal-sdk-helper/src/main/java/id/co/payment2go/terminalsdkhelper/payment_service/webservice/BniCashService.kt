package id.co.payment2go.terminalsdkhelper.payment_service.webservice

import com.google.gson.JsonObject
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.RequestBodyDto
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.ResponseBodyDto
import retrofit2.http.Body
import retrofit2.http.POST

interface BniCashService {
    @POST("AndroidApi/v1/PST/Request")
    suspend fun postRequest(@Body requestBodyDto: RequestBodyDto): ResponseBodyDto

    @POST("AndroidApi/v1/PST/ReversalData")
    suspend fun postReversalData(@Body jsonObject: JsonObject): JsonObject
}