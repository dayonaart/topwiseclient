/**
 * Represents different screens of the payment process.
 *
 * @param route The route associated with the payment screen.
 * @author Samuel Mareno
 */
sealed class PaymentScreen(val route: String) {
    /**
     * Represents the card entry screen during the payment process.
     */
    object CardEntry : PaymentScreen("PAYMENT/CARD_ENTRY")

    /**
     * Represents the payment confirmation (debit) screen during the payment process.
     */
    object PaymentConfirmationDebit : PaymentScreen("PAYMENT/CONFIRMATION_DEBIT")

    /**
     * Represents the PIN entry screen during the payment process.
     */
    object PinEntry : PaymentScreen("PAYMENT/PIN_ENTRY")
}
