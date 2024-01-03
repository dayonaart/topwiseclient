package id.co.payment2go.terminalsdkhelper.common

interface BindService {
    suspend fun bindServiceSDK()
    fun disconnectSDK()
}