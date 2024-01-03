package id.co.payment2go.terminalsdkhelper.szzt.utils

sealed class SearchCardResultSzzt {
    data class FindMagCard(val szztCardData: SzztCardData) : SearchCardResultSzzt()
    data class FindICCard(val szztCardData: SzztCardData) : SearchCardResultSzzt()
    object Waiting : SearchCardResultSzzt()
    object Expired : SearchCardResultSzzt()
    data class OnError(val message: String) : SearchCardResultSzzt()
    object Loading : SearchCardResultSzzt()
}