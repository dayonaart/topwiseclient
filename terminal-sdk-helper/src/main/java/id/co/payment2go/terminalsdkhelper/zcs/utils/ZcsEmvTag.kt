package id.co.payment2go.terminalsdkhelper.zcs.utils

import java.time.LocalDate


object ZcsEmvTag {

    /**
     * Additional Terminal Capabilities
     *
     * Indicates the data input and output capabilities of the terminal
     *
     * @return 0x9F40
     *
     * @author Dayona
     *
     */
    const val ATCAP = 0x9F40


    ///EMV TAG REQUIRED BY MIDDLEWARE


    /**
     *
     * Cryptogram Information Data
     *
     * indicates the type of cryptogram and the actions to be performed by the terminal
     *
     * @return 0x9F27
     *
     * @author Dayona
     */
    const val CID = 0x9F27


    /**
     * Amount, Authorised (Numeric)
     *
     * Authorised amount of the transaction (excluding adjustments)
     *
     * @return 0x9F02
     *
     * @author Dayona
     */
    const val TRAMOUNT = 0x9F02

    /**
     * Amount, Other (Numeric)
     *
     * Secondary amount associated with the transaction representing a cashback amount
     *
     * @return 0x9F03
     *
     * @author Dayona
     */
    const val TRANOTHERAMOUNT = 0x9F03

    /**
     * Application Cryptogram
     *
     * Cryptogram returned by the ICC in response of the GENERATE AC command
     * @return 0x9F26
     *
     * @author Dayona
     */
    const val AC = 0x9F26


    /**
     * Application Interchange Profile
     *
     * Indicates the capabilities of the card to support specific functions in the application
     *
     * @return 0x82
     * @author Dayona
     * */
    const val AIP = 0x82

    /**
     * Application Transaction Counter (ATC)
     *
     * Counter maintained by the application in the ICC (incrementing the ATC is managed by the ICC)
     *
     * @return 0x9F36
     *
     * @author Dayona
     */
    const val ATRCD = 0x9F36


    /**
     * Issuer Application Data
     *
     * Contains proprietary application data for transmission to the issuer in an online transaction
     *
     * @return 0x9F10
     *
     * @author Dayona
     */
    const val IAD = 0x9F10

    /**
     * Terminal Capabilities
     *
     * Indicates the card data input, CVM, and security capabilities of the terminal
     *
     * @return 0x9F33
     *
     * @author Dayona
     */
    const val TCAP = 0x9F33

    /**
     * Transaction Currency Code
     *
     * Indicates the currency code of the transaction according to ISO 4217
     *
     * @return 0x5F2A
     *
     * @author Dayona
     */
    const val TRCURCD = 0x5F2A

    /**
     * Terminal Country Code
     *
     * Indicates the country of the terminal, represented according to ISO 3166
     *
     * @return 0x9F1A
     *
     * @author Dayona
     */
    const val TCOC = 0x9F1A


    /**
     * Terminal Verification Results
     *
     * Status of the different functions as seen from the terminal
     *
     * @return 0x95


    @author Dayona    */
    const val TVR = 0x95


    /**
     * Transaction Date
     *
     * Local date that the transaction was authorised
     *
     * @return 0x9A


    @author Dayona    */
    const val TRDATE = 0x9A

    /**
     * Transaction Type
     *
     * Indicates the type of financial transaction, represented by the first two digits of ISO 8583:1987 Processing Code
     *
     * @return 0x9C


    @author Dayona    */
    const val TRTYPE = 0x9C

    /**
     * Unpredictable Number
     *
     * Value to provide variability and uniqueness to the generation of a cryptogram
     *
     * @return 0x9F37
     *
     * @author Dayona
     */
    const val UNPREDICTABLE_NUMB = 0x9F37


    /**
     * Cardholder Verification Method (CVM) Results
     *
     * Indicates the results of the last CVM performed
     *
     * @return 0x9F34
     *
     * @author Dayona
     */
    const val CVM = 0x9F34

    /**
     * Application Primary Account Number (PAN) Sequence Number
     *
     * Identifies and differentiates cards with the same PAN
     *
     * @return 0x5F34
     *
     * @author Dayona
     */
    const val PANSEQ = 0x5F34


    /**
     * Dedicated File (DF) Name
     *
     * Identifies the name of the DF as described in ISO/IEC 7816-4
     *
     * @return 0x84
     * @author Dayona
     */
    const val DFNAME = 0x84

    /**
     * Application Version Number
     *
     * Version number assigned by the payment system for the application
     *
     * @return 0x9F09
     *
     * @author Dayona
     */
    const val AVN = 0x9F09

    /**
     * Interface Device (IFD) Serial Number
     *
     * Unique and permanent serial number assigned to the IFD by the manufacturer
     *
     * @return 0x9F1E
     *
     * @author Dayona
     */
    const val IFD = 0x9F1E


    /**
     * Terminal Type
     *
     *Indicates the environment of the terminal, its communications capability, and its operational control
     *
     * @return 0x9F35
     *
     * @author Dayona
     */
    const val TTYPE = 0x9F35

    /**
     * UNKNOWN
     *
     * Unknown tag in context-specific class (payment system range)
     */
    const val UNKNOWN = 0x9F53


    /**
     * Cardholder Name
     * Indicates cardholder name according to ISO 7813
     *
     * @return 0x5F20
     *
     * @author Dayona
     */
    const val CARDNAME = 0x5F20

    /**
     * Track 2 Equivalent Data
     *
     *Contains the data elements of track 2 according to ISO/IEC 7813, excluding start sentinel, end sentinel, and Longitudinal Redundancy Check (LRC), as follows: Primary Account Number (n, var. up to 19) Field Separator (Hex 'D') (b) Expiration Date (YYMM) (n 4) Service Code (n 3) Discretionary Data (defined by individual payment systems) (n, var.) Pad with one Hex 'F' if needed to ensure whole bytes (b)
     *
     * @return 0x57
     * @author Dayona    *@property ICC
     */
    const val T2D = 0x57

    /**
     * Application Primary Account Number (PAN)
     *
     * 	Valid cardholder account number
     *
     * 	@return 0x5A
     * 	@author Dayona
     */
    const val PAN = 0x5A

    /**
     * Transaction Sequence Counter
     *
     * Sequence Counter	Counter maintained by the terminal that is incremented by one for each transaction
     *
     * @return 0x9F41
     *
     * @author Dayona
     *
     */
    const val TSC = 0x9F41
//    const val nsiccs = 0x9F12
    ///EMV TAG REQUIRED BY MIDDLEWARE

}


/**
 * NSICCS CA Public Key 1984 bit – Production
 *
 * Mulai tanggal 1 Januari 2022 semua terminal NSICCS non-ATM sudah memasukan (load) NSICCS CA Public Key 1984-bit. Maksimum masa berlaku untuk Issuer Public Key Certificate adalah 31 Desember 2030
 *
 *
 * @author Dayona
 */
enum class ZCSNSICCS1984(
    val rid: String = "A0000006021010",
    val valid: LocalDate = LocalDate.parse("2030-12-31"),
    val index: Byte = 0x09.toByte(),
    //3C822B8A2BEC48181ED08E08EC5C9C9D4B7F8792
    val checksum: String = "1CAB162A1BE81492BB952C2846617B756F833C07",
    val exponent: String = "03",
    val modulus: String = "A517A338854E0856EE4AFDBF4BDA5DD3 F9EB3895CBD8971B1E58A8EB167BF9935E0752DAEA7EAFB25E79D601EB201895 A93F8B0A16D95A230366C05FEC55858C94D6097B2FB1EDDD2C6A3647DD0B71BC1DCDDC68B4E9ECC919FB544070952443159733471292993AB23E5B8C00E6A8526DF04A0B6E65E0F9D0378F71497E12FA83540B49FC05D0A86DC3D66FC4BB291A69B2EBB98D057C8F1EE7CB8E942FD05E9E4FAD0361BC184C13418C313C042C547DEF41310BA1850EF59CAF8CC7B14DAEE72FA4689C1047434024D565A3FA46ED CA3F53E236235268C893F268AA24AB2D20EB7AE06FF3123318041CB23E30839C58DFD4991D7C88CB",
) { DATA }

enum class ZCSNSICCS1408(
    val rid: String = "A0000006021010",
    val valid: LocalDate = LocalDate.parse("2026-12-31"),
    val index: Byte = 0x05.toByte(),
    //3C822B8A2BEC48181ED08E08EC5C9C9D4B7F8792
    val checksum: String = "1CAB162A1BE81492BB952C2846617B756F833C07",
    val exponent: String = "03",
    val modulus: String = "B48CC63D71A486DFC920608A3E42D7C305472BF76B8E50C8C02FB8387E788F72931A29DC15F913E7D69E43AD4C38A5C4317E36D15DE5F49FA2327D9754799D2484A6E156941ACA9632417E5C92931A85E1BB5F2A2C1B847D5008C7B30591F1ACBF3B98DFB0CF2849B6C7CDC7435AEA85 F3A58BAC3B8C990416A5E19EC4EA08DC91CEF2FBE5940FA6622926D2AD0523D109A7024EB1035BBE37260B30F41AA52E EB36E60DD37120B9401C3850920F0E03",
) { DATA }

/**
 * SIMPLE ASCII FOR LOGCAT
 * @author Dayona
 */
object ZcsLogAscii {
    const val startEmv =
        "'  ____  _____   _     ____  _____  ___  _   _   ____ \n" +
                " / ___||_   _| / \\   |  _ \\|_   _||_ _|| \\ | | / ___|\n" +
                " \\___ \\  | |  / _ \\  | |_) | | |   | | |  \\| || |  _ \n" +
                "  ___) | | | / ___ \\ |  _ <  | |   | | | |\\  || |_| |\n" +
                " |____/  |_|/_/   \\_\\|_| \\_\\ |_|  |___||_| \\_| \\____|\n" +
                "  _____  __  __ __     __                            \n" +
                " | ____||  \\/  |\\ \\   / /                            \n" +
                " |  _|  | |\\/| | \\ \\ / /                             \n" +
                " | |___ | |  | |  \\ V /                              \n" +
                " |_____||_|  |_|   \\_/                               \n" +
                "                                                     '"
    const val successEmv = "'  ____   _   _   ____  ____  _____  ____  ____  \n" +
            " / ___| | | | | / ___|/ ___|| ____|/ ___|/ ___| \n" +
            " \\___ \\ | | | || |   | |    |  _|  \\___ \\\\___ \\ \n" +
            "  ___) || |_| || |___| |___ | |___  ___) |___) |\n" +
            " |____/  \\___/  \\____|\\____||_____||____/|____/ \n" +
            "  _____  __  __ __     __                       \n" +
            " | ____||  \\/  |\\ \\   / /                       \n" +
            " |  _|  | |\\/| | \\ \\ / /                        \n" +
            " | |___ | |  | |  \\ V /                         \n" +
            " |_____||_|  |_|   \\_/                          \n" +
            "                                                '"
    const val errorEmv = "'  _____  ____   ____    ___   ____  \n" +
            " | ____||  _ \\ |  _ \\  / _ \\ |  _ \\ \n" +
            " |  _|  | |_) || |_) || | | || |_) |\n" +
            " | |___ |  _ < |  _ < | |_| ||  _ < \n" +
            " |_____||_| \\_\\|_| \\_\\ \\___/ |_| \\_\\\n" +
            "  _____  __  __ __     __           \n" +
            " | ____||  \\/  |\\ \\   / /           \n" +
            " |  _|  | |\\/| | \\ \\ / /            \n" +
            " | |___ | |  | |  \\ V /             \n" +
            " |_____||_|  |_|   \\_/              \n" +
            "                                    '"
    const val stopEmv = "'  ____  _____  ___   ____   ____   _____  ____  \n" +
            " / ___||_   _|/ _ \\ |  _ \\ |  _ \\ | ____||  _ \\ \n" +
            " \\___ \\  | | | | | || |_) || |_) ||  _|  | | | |\n" +
            "  ___) | | | | |_| ||  __/ |  __/ | |___ | |_| |\n" +
            " |____/  |_|  \\___/ |_|    |_|    |_____||____/ \n" +
            "  _____  __  __ __     __                       \n" +
            " | ____||  \\/  |\\ \\   / /                       \n" +
            " |  _|  | |\\/| | \\ \\ / /                        \n" +
            " | |___ | |  | |  \\ V /                         \n" +
            " |_____||_|  |_|   \\_/                          \n" +
            "                                                '"
    const val loading = "'  _                    _ _             \n" +
            " | |    ___   __ _  __| (_)_ __   __ _ \n" +
            " | |   / _ \\ / _` |/ _` | | '_ \\ / _` |\n" +
            " | |__| (_) | (_| | (_| | | | | | (_| |\n" +
            " |_____\\___/ \\__,_|\\__,_|_|_| |_|\\__, |\n" +
            "                                 |___/ '"
}