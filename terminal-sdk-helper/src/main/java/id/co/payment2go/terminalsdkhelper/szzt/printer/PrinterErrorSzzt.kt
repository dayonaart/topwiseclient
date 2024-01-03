package id.co.payment2go.terminalsdkhelper.szzt.printer

import com.szzt.sdk.device.printer.Printer.*
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterErrorDesc

object PrinterErrorSzzt {

    fun getDescription(error: Int): String {
        return when(error) {
            STATUS_NO_PAPER -> "cannot find paper in the printer"
            STATUS_UNKNOWN -> "The device status is unknown"
            STATUS_HARDERR -> "The hardware on the device has an error"
            STATUS_OVERHEAT -> "The device temperature is very hot"
            STATUS_LOWVOL -> "The device power on the printer will soon run out"
            STATUS_PAPERJAM -> "the paper in the device is stuck"
            STATUS_BUSY -> "the device is performing other tasks"
            STATUS_LIFTHEAD -> "The valve on the printer head is open, please close it again!"
            STATUS_CUTPOSITIONERR -> "Paper cutter is not in place"
            STATUS_LOWTEMP -> "low temperature protection or AD error"
            else -> "Unknown error device"
        }
    }
}