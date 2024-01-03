package id.co.payment2go.terminalsdkhelper.payment_service.webservice.dto.perform_logon


data class LogonRequestDto(
    val mMID: String,
    val mTID: String,
    val serialNo: String,
    val iid: String = "2",
)


