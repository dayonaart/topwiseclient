package id.co.payment2go.terminalsdkhelper.payments.debit_card.domain

/**
 * Represents an exception that occurs when an unidentified card is encountered.
 * This exception is thrown when an attempt to process an unknown or unrecognized card is made.
 *
 * @param message The error message describing the reason for the unidentified card exception.
 */
class UnidentifiedCard(message: String) : Throwable(message)
