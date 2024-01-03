package id.co.payment2go.terminalsdkhelper.topwise.emv

object TopWiseEmvCode {
    const val EMV_OK: Int = 0
    const val EMV_APPROVED: Int = 1
    const val EMV_FORCE_APPROVED: Int = 2
    const val EMV_DECLINED: Int = 3
    const val EMV_NOT_ALLOWED: Int = 5
    const val EMV_NO_ACCEPTED: Int = 6
    const val EMV_TERMINATED: Int = 7
    const val EMV_CARD_BLOCKED: Int = 8
    const val EMV_APP_BLOCKED: Int = 9
    const val EMV_NO_APP: Int = 10
    const val EMV_FALLBACK: Int = 11
    const val EMV_CAPK_EXPIRED: Int = 12
    const val EMV_CAPK_CHECKSUM_ERROR: Int = 13
    const val EMV_AID_DUPLICATE: Int = 14
    const val EMV_CERTIFICATE_RECOVER_FAILED: Int = 15
    const val EMV_DATA_AUTH_FAILED: Int = 16
    const val EMV_UN_RECOGNIZED_TAG: Int = 17
    const val EMV_DATA_NOT_EXISTS: Int = 18
    const val EMV_DATA_LENGTH_ERROR: Int = 19
    const val EMV_INVALID_TLV: Int = 20
    const val EMV_INVALID_RESPONSE: Int = 21
    const val EMV_DATA_DUPLICATE: Int = 22
    const val EMV_MEMORY_NOT_ENOUGH: Int = 23
    const val EMV_MEMORY_OVERFLOW: Int = 24
    const val EMV_PARAMETER_ERROR: Int = 25
    const val EMV_ICC_ERROR: Int = 26
    const val EMV_NO_MORE_DATA: Int = 27
    const val EMV_CAPK_NO_FOUND: Int = 28
    const val EMV_AID_NO_FOUND: Int = 29
    const val EMV_FORMAT_ERROR: Int = 30
    const val EMV_ONLINE_REQUEST: Int = 31 //online request -by wfh20190805
    const val EMV_SELECT_NEXT_AID: Int = 32 //Select next AID
    const val EMV_TRY_AGAIN: Int = 33 //Try Again. ICC read failed.
    const val EMV_SEE_PHONE: Int =
        34 //Status Code returned by IC card is 6986 please see phone. GPO 6986 CDCVM.
    const val EMV_TRY_OTHER_INTERFACE: Int = 35 //Try other Interface -by wfh20190805
    const val EMV_ICC_ERR_LAST_RECORD: Int = 36
    const val EMV_CANCEL: Int = 254
    const val EMV_OTHER_ERROR: Int = 255
}