package id.co.payment2go.terminalsdkhelper.common

/**
 *Custom exception class for unknown devices.
 *
 * @param message The error message indicating the unrecognized device manufacturer.
 * @author Samuel Mareno
 */
class UnknownDevice(message: String) : Throwable(message)
