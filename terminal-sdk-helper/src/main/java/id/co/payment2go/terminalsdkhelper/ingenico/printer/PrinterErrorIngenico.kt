package id.co.payment2go.terminalsdkhelper.ingenico.printer

import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_BMBLACK
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_BUFOVERFLOW
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_BUSY
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_COMMERR
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_CUTPOSITIONERR
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_LIFTHEAD
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_LOWTEMP
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_LOWVOL
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_MOTORERR
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_NOBM
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_NOT_INIT
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_OVERHEAT
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_PAPERENDED
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_PAPERENDING
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_PAPERJAM
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_PARAM
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_PENOFOUND
import com.usdk.apiservice.aidl.printer.PrinterError.ERROR_WORKON
import id.co.payment2go.terminalsdkhelper.common.printer.PrinterErrorDesc

class PrinterErrorIngenico : PrinterErrorDesc {

    override fun getDescription(error: Int): String {
        return when (error) {
            ERROR_NOT_INIT -> return "Printer not init"
            ERROR_PARAM -> return "Parameter error"
            ERROR_BMBLACK -> return "Black mark detector detected black signal"
            ERROR_BUFOVERFLOW -> return "Operation place in buffer mode out of range"
            ERROR_BUSY -> return "Printer is busy"
            ERROR_COMMERR -> return "Pedestal is normal, but communication failed"
            ERROR_CUTPOSITIONERR -> return "Paper cut knife is not in original place"
            ERROR_LIFTHEAD -> return "Printer head uplift"
            ERROR_LOWTEMP -> return "Low temperature protect"
            ERROR_LOWVOL -> return "Low voltage protect"
            ERROR_MOTORERR -> return "Motor fault"
            ERROR_NOBM -> return "Black mark not found"
            ERROR_OVERHEAT -> return "Printer head is too hot"
            ERROR_PAPERENDED -> return "No paper"
            ERROR_PAPERENDING -> return "Paper will be exhausted"
            ERROR_PAPERJAM -> return "Paper jam"
            ERROR_PENOFOUND -> return "Automatic positioning did not find the alignment position"
            ERROR_WORKON -> return "Printer power source is in open state"
            else -> "Unknown error"
        }
    }
}