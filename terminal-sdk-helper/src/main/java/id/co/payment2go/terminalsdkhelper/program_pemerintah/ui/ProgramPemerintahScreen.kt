package id.co.payment2go.terminalsdkhelper.program_pemerintah.ui

sealed class ProgramPemerintahScreen(val route: String) {

    /**
     * Represents the card entry screen during the payment process.
     */
    object CardEntry : ProgramPemerintahScreen("PP/CARD_ENTRY")

    /**
     * Represents the PIN entry screen during the payment process.
     */
    object PinEntry : ProgramPemerintahScreen("PP/PIN_ENTRY")
}