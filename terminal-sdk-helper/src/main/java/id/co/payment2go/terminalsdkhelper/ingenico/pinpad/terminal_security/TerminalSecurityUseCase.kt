package id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security

import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security.use_case.DecryptData
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security.use_case.EncryptData
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security.use_case.LoadDataEncryptionKey
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security.use_case.LoadMACEncryptionKey
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security.use_case.LoadMasterKey
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.terminal_security.use_case.LoadSessionKey

data class TerminalSecurityUseCase(
    val loadMasterKey: LoadMasterKey,
    val loadDataEncryptionKey: LoadDataEncryptionKey,
    val loadSessionKey: LoadSessionKey,
    val loadMACEncryptionKey: LoadMACEncryptionKey,
    val encryptData: EncryptData,
    val decryptData: DecryptData
)
