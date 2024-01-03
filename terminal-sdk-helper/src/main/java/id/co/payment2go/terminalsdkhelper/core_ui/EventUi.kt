package id.co.payment2go.terminalsdkhelper.core_ui

sealed class EventUi {
    object RetryPinEntry: EventUi()
    object Finish: EventUi()
}
