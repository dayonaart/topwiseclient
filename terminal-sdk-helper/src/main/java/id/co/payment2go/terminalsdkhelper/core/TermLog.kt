package id.co.payment2go.terminalsdkhelper.core

import android.util.Log


object TermLog {
    fun d(tag: String, message: String) {
        val stackTraceElement =
            Thread.currentThread().stackTrace.find { it.className.contains(tag) } ?: return
        Log.d(tag, "$message -> \n$stackTraceElement")
    }
}