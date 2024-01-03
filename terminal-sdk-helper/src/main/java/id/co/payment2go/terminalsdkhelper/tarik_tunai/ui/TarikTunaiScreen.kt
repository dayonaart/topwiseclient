/**
 * Represents the screen that is shown during the payment process.
 * @param route The route associated with the payment screen.
 * @author Samuel Mareno
 */
sealed class TarikTunaiScreen(val route: String) {
    /**
     * Represents the card entry screen during the payment process.
     */
    object CardEntry : TarikTunaiScreen("TARIK_TUNAI/CARD_ENTRY")


    /**
     * Represents the PIN entry screen during the payment process.
     */
    object PinEntry : TarikTunaiScreen("TARIK_TUNAI/PIN_ENTRY")
}
