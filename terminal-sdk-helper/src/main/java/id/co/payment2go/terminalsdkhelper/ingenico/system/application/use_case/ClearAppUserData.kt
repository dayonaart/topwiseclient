package id.co.payment2go.terminalsdkhelper.ingenico.system.application.use_case

import com.usdk.apiservice.aidl.system.application.AppDataObserver
import id.co.payment2go.terminalsdkhelper.ingenico.BindServiceIngenico

class ClearAppUserData(
    private val bindService: BindServiceIngenico
) {
    private val application = bindService.application
    operator fun invoke(packageName: String) {
        application.clearApplicationUserData(packageName, object : AppDataObserver.Stub() {

            override fun onRemoveCompleted(packageName: String, success: Boolean) {

            }
        })
    }
}