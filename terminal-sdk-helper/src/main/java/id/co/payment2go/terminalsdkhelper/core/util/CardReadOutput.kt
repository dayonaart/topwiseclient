package id.co.payment2go.terminalsdkhelper.core.util


data class CardReadOutput(
    var cardNo: String = "",
    var insertMode: String = "",
    var cardExpiry: String = "",
    var cardAID: String = "",
    var cardAppName: String = "",
    var customerName: String = "",
    var currencyCode: String = "",
    var cardTransactionCount: String = "",
    var EMVData: String = "",
    var PANSEQ: String = "",
    var TVRData: String = "",
    var TSIData: String = "", //Transaction Status Information
    var track2Data: String = "",
    var transactionCategoryCode: String = "",
    var transactionCertificate: String = "",
    var STAN: String = "",
    var insertModeCode: String = "",
    var pinType: String = "",
    var RID: String = "",
    var pubKIndex: String = "",
    var pubKeyExist: Boolean = false,
    var pinVerificationResult: String = "",
    var allAIDs: List<id.co.payment2go.terminalsdkhelper.common.model.AIDFile> = listOf(),
    var terminalCapability: String = "",
    var additionalTerminalCapability: String = "",
    var txnDate: String = "",
    var txnAmount: String = "",
    var otherAmount: String = "",
    var TACDenial: String = "",
    var TACOnline: String = "",
    var TACDefault: String = "",
    var IACDenial: String = "",
    var IACOnline: String = "",
    var IACDefault: String = "",
    var applicationInterchangeProfile: String = "",
    var cardHolderVerificationMethod: String = "",
    var issuerApplicationData: String = "",
    var txnCategoryCode: String = "",
    var unpredictableNumber: String = "",
    var RFU1: String = "",
    var pinBlock: String = "",
    var posEntryMode: String = ""
)