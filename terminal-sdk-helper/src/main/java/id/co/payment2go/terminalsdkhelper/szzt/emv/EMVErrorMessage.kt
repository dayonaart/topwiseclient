package id.co.payment2go.terminalsdkhelper.szzt.emv

import com.szzt.sdk.device.emv.EMV_STATUS.*

object EMVErrorMessage {

    /**
     * List Of Error Message in EMV Szzt Process
     * @param retCode errorCode result in EMV Process
     */

    @Suppress("SpellCheckingInspection")
    fun getMessage(retCode: Int): String {
        return when (retCode) {
            EMV_FAIL -> "[$retCode] Failed to process step emv"
            EMV_NULPORTING -> "[$retCode] Porting interface is null"
            EMV_GETTERMPARAMERR -> "[$retCode] Failed to load terminal configuration parameters"
            EMV_GETTERMAPPSERR -> "[$retCode] Failed to load terminal application list"
            EMV_ALLOCBUFFERERR -> "[$retCode] Allocation of global cache space is insufficient"
            EMV_UNINITIALIZED -> "[$retCode] Kernel uninitialized"
            EMV_NONIDLE -> "[$retCode] Kernel non idle state"
            EMV_AMOUNTOVER -> "[$retCode] Transaction amount exceeded"
            EMV_AMOUNTZERO -> "[$retCode] Transaction amount is zero"
            EMV_QPBOCUNSUPPORTED -> "[$retCode] Terminal transaction attributes do not support QPBOC"
            EMV_CARDTYPEILL -> "[$retCode] Card communication mode not allowed"
            EMV_PROCESSOVER -> "[$retCode] EMV flow over"
            EMV_UNKNOWNSTATUS -> "[$retCode] EMV Unknown flow state"
            EMV_APPSELUNDONE -> "[$retCode] Kernel not completed application selection"
            EMV_PBOCLOGSFINOTFOUND -> "[$retCode] Transaction log entry not found"
            EMV_PBOCLOGFMTNOTFOUND -> "[$retCode] Transaction log format not found"
            EMV_PBOCLOGRDRECFAILED -> "[$retCode] Transaction log read failed"
            EMV_PBOCLOGRECNOINV -> "[$retCode] Invalid transaction log number"
            EMV_SELCANDIDATENOTREQ -> "[$retCode] The kernel doesn't request a list of candidate applications"
            EMV_SELACCOUNTNOTREQ -> "[$retCode] The kernel doesn't request an account type"
            EMV_CHECKIDNOTREQ -> "[$retCode] The kernel doesn't request the cardholders identification"
            EMV_ONLINEPINNOTREQ -> "[$retCode] The kernel didn't request to enter the online PIN"
            EMV_OFFLINEPINNOTREQ -> "[$retCode] The kernel didn't request to enter the offline PIN"
            EMV_BYPASSPINNOTREQ -> "[$retCode] The kernel didn't request  confirmation whether to skip the PIN input"
            EMV_FORCEONLINENOTREQ -> "[$retCode] Kernel doesn't request confirmation to force Online"
            EMV_GOONLINENOTREQ -> "[$retCode] Kernel doesn't request online authorization"
            EMV_ISSREFERRALNOTREQ -> "[$retCode] The kernel doesn't request the issuer reference"
            SDK_EMVBASE_EMV_ERR_BASE -> "[$retCode] SDK base error"
            SDK_EMV_TORN -> "[$retCode] Error, Flashcard transaction detected"
            SDK_EMV_TransTryAgain -> "[$retCode] Error Reissue Transaction"
            SDK_EMV_UserSelect -> "[$retCode] User manually selects an application"
            SDK_EMV_SeePhone -> "[$retCode] Used for GPO return 6985 or cardholder verification to take CDCVM"
            SDK_EMV_SwitchInterface -> "[$retCode] Transaction request transfer interface"
            SDK_EMV_ReadCardAgain -> "[$retCode] Read card again"
            SDK_EMV_AppBlock -> "[$retCode] App is locked"
            SDK_EMV_NeedMagCard -> "[$retCode] Trading via magnetic stripe channel"
            SDK_EMV_AppTimeOut -> "[$retCode] APDU interaction timeout"
            SDK_EMV_CardBlock -> "[$retCode] Card locked"
            SDK_EMV_CancelTrans -> "[$retCode] User cancel transaction"
            SDK_EMV_NotAccept -> "[$retCode] Emv not accept"
            SDK_EMV_IccDataRedund -> "[$retCode] Card return data redundancy"
            SDK_EMV_NoAppSel -> "[$retCode] The application list is empty, and there are no applications to choose from"
            SDK_EMV_IccReturnErr -> "[$retCode] Card returned data error"
            SDK_EMV_IccDataFormatErr -> "[$retCode] Card returned data format error"
            SDK_EMV_TransTerminate -> "[$retCode] Process Termination"
            AS_ERR -> "[$retCode] Application selection error"
            AS_SELSW6A81 -> "[$retCode] Select return 6A81"
            AS_SELERR -> "[$retCode] Emv select error"
            AS_FCINO61 -> "[$retCode] 61 data missing"
            AS_SELSW6A82 -> "[$retCode] Application Selection Reply 6a82"
            IA_ERR -> "[$retCode] Application initialization error"
            IA_DATADUPLICATION -> "[$retCode] GPO returned data has duplicates"
            IA_PDOLDATAERR -> "[$retCode] PDOL data check error"
            RD_ERR -> "[$retCode] Reading application data error"
            RD_CHECKMANDATORYDATAERR -> "[$retCode] Necessary data check error"
            RD_MSD_ATC_LENERR -> "[$retCode] The length of the MSD mode's atc is incorrect"
            DA_ERR -> "[$retCode] Offline data authentication error"
            RE_ERR -> "[$retCode] Processing limit error"
            CV_ERR -> "[$retCode] Cardholder verification error"
            CV_TERMINATE_OTHERCARD -> "[$retCode] Transaction terminated, please change card"
            RM_ERR -> "[$retCode] Terminal riskmanagement error"
            AA_ERR -> "[$retCode] Terminal action analysis error"
            CO_ERR -> "[$retCode]  Transaction completion error"
            CO_SENDTAPCARDERR -> "[$retCode] Secondary card capture and reading failure"
            CO_RRPERR -> "[$retCode] RRP (Relay Resistance Protocol) failed"
            CO_CCCERR -> "[$retCode] CCC (Compute Cryptographic Checksum) failed"
            CO_PREGACBALANCEREADERR -> "[$retCode] Failed to read balance before GAC"
            CO_TRANSANALYSERR -> "[$retCode] Transaction result analysis failed"
            CO_POSTGACPUTDATAERR -> "[$retCode] PUT DATA failed after GAC"
            CL_ERR -> "[$retCode] QPBOC (combination of the PBOC DC application with improved transaction speed and the EC small-value payment application) transation error"
            else -> "[EMV_STATUS = $retCode] Unknown Error EMV Process"
        }
    }
}