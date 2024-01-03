package id.co.payment2go.terminalsdkhelper.program_pemerintah.domain

import com.google.gson.JsonObject
import id.co.payment2go.terminalsdkhelper.payment_service.webservice.ApiResult
import id.co.payment2go.terminalsdkhelper.program_pemerintah.domain.model.BansosInquiryRequest
import kotlinx.coroutines.flow.Flow

interface ProgramPemerintahRepository {

    fun postBansosInquiryIntent(bansosInquiryRequest: BansosInquiryRequest): Flow<ApiResult<JsonObject>>
    fun postBansosInquiry(
        fld3: String,
        fld48: String,
        fld4: String,
        idTransaction: String,
        refNum: String,
        narasi: String,
    ): Flow<ApiResult<JsonObject>>

    fun postBansosPayment(
        fld3: String,
        fld48: String,
        fld4: String,
        idTransaction: String,
        refNum: String,
        narasi: String,
        pinBlock: String,
    ): Flow<ApiResult<JsonObject>>
}