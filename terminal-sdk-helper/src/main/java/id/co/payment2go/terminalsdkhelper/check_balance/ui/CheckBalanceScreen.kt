package id.co.payment2go.terminalsdkhelper.check_balance.ui

sealed class CheckBalanceScreen(val route: String) {

    /**
     * Represents the card entry screen during the check balance process.
     */
    object CardEntry : CheckBalanceScreen("CB/CARD_ENTRY")

    /**
     * Represents the select entry screen during the check balance process.
     */
    object SelectEntry: CheckBalanceScreen("CB/SELECT_ENTRY")

    /**
     * Represents the PIN entry screen during the check balance process.
     */
    object PinEntry : CheckBalanceScreen("CB/PIN_ENTRY")
}