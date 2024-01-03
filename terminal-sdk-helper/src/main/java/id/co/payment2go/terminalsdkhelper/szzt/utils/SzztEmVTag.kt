package id.co.payment2go.terminalsdkhelper.szzt.utils

object SzztEmVTag {
    /**
     * Terminal Source
     */
    private object Terminal

    /**
     * ICC Source
     */
    private object ICC

    /**
     *
     * Sequence Counter	Counter maintained by the terminal that is incremented by one for each transaction
     *
     * @property Terminal
     * @return 0x9F41
     *
     */
    const val transactionSequenceCounter = 0x9F41

    /**
     *
     *Indicates the environment of the terminal, its communications capability, and its operational control
     *
     * @property Terminal
     * @return 0x9F35
     */
    const val terminalType = 0x9F35

    /**
     *
     * Unique and permanent serial number assigned to the IFD by the manufacturer
     *
     * @property Terminal
     * @return 0x9F1E
     */
    const val interfaceDeviceSerialNumber = 0x9F1E

    /**
     *
     * Version number assigned by the payment system for the application
     *
     * @property Terminal
     * @return 0x9F09
     */
    const val applicationVersionNumber = 0x9F09


    /**
     *
     * Indicates the results of the last CVM performed
     *
     * @property Terminal
     * @return 0x9F34
     */
    const val cardholderVerificationMethodResults = 0x9F34

    /**
     *
     * Value to provide variability and uniqueness to the generation of a cryptogram
     *
     * @property Terminal
     * @return 0x9F37
     */
    const val unpredictableNumber = 0x9F37


    /**
     *
     * Authorised amount of the transaction (excluding adjustments)
     *
     * @property Terminal
     * @return 0x9F02
     */
    const val amountAuthorisedNumeric = 0x9F02

    /**
     *
     * Secondary amount associated with the transaction representing a cashback amount
     *
     * @property Terminal
     * @return 0x9F03
     */
    const val amountOtherNumeric = 0x9F03


    /**
     *
     * Indicates the card data input, CVM, and security capabilities of the terminal
     *
     * @property Terminal
     * @return 0x9F33
     */
    const val terminalCapabilities = 0x9F33

    /**
     *
     * Indicates the currency code of the transaction according to ISO 4217
     *
     * @property Terminal
     * @return 0x5F2A
     */
    const val transactionCurrencyCode = 0x5F2A

    /**
     *
     * Indicates the country of the terminal, represented according to ISO 3166
     *
     * @property Terminal
     * @return 0x9F1A
     */
    const val terminalCountryCode = 0x9F1A

    /**
     *
     * Status of the different functions as seen from the terminal
     *
     * @property Terminal
     * @return 0x95
     */
    const val terminalVerificationResults = 0x95

    /**
     *
     * Local date that the transaction was authorised
     *
     * @property Terminal
     * @return 0x9A
     */
    const val transactionDate = 0x9A

    /**
     *
     * Indicates the type of financial transaction, represented by the first two digits of ISO 8583:1987 Processing Code
     *
     * @property Terminal
     * @return 0x9C
     */
    const val transactionType = 0x9C


    /**
     *Contains the data elements of track 2 according to ISO/IEC 7813, excluding start sentinel, end sentinel, and Longitudinal Redundancy Check (LRC), as follows: Primary Account Number (n, var. up to 19) Field Separator (Hex 'D') (b) Expiration Date (YYMM) (n 4) Service Code (n 3) Discretionary Data (defined by individual payment systems) (n, var.) Pad with one Hex 'F' if needed to ensure whole bytes (b)
     *
     *@return 0x57
     *@property ICC
     */
    const val t2Data = 0x57

    /**
     *
     * 	Valid cardholder account number
     *
     * 	@property ICC
     * 	@return 0x5A
     */
    const val applicationPrimaryAccountNumber = 0x5A

    /**
     *
     * Cryptogram returned by the ICC in response of the GENERATE AC command
     *
     * @property ICC
     * @return 0x9F26
     */
    const val applicationCryptogram = 0x9F26

    /**
     *
     * Indicates the capabilities of the card to support specific functions in the application
     *
     * @property ICC
     * @return 0x82
     */
    const val applicationInterchangeProfile = 0x82

    /**
     *
     * Counter maintained by the application in the ICC (incrementing the ATC is managed by the ICC)
     *
     * @property ICC
     * @return 0x9F36
     */
    const val applicationTransactionCounter = 0x9F36

    /**
     *
     * Contains proprietary application data for transmission to the issuer in an online transaction
     *
     * @property ICC
     * @return 0x9F10
     */
    const val issuerApplicationData = 0x9F10


    /**
     *
     * indicates the type of cryptogram and the actions to be performed by the terminal
     *
     * @property ICC
     * @return 0x9F27
     */
    const val cryptogramInformationData = 0x9F27


    /**
     *
     * Identifies the name of the DF as described in ISO/IEC 7816-4
     *
     * @property ICC
     * @return 0x84
     */
    const val dedicatedFileName = 0x84

    /**
     *
     * Identifies and differentiates cards with the same PAN
     *
     * @property ICC
     * @return 0x5F34
     */
    const val applicationPrimaryAccountNumberSequenceNumber = 0x5F34


    /**
     *
     * Indicates cardholder name according to ISO 7813
     *
     * @property ICC
     * @return 0x5F20
     */
    const val cardholderName = 0x5F20

    /**
     *
     * Unknown tag in context-specific class (payment system range)
     */
    const val unknown = 0x9F53
    const val nsiccs = 0x9F12
}