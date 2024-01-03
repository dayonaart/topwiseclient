package com.example.clientbni

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class PaymentBroadcastReceiver : BroadcastReceiver() {
    private val TAG = this::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        val responsePrint = intent.getStringExtra(CommonUtility.PRINT_RESPONSE)
        val responseDebit = intent.getStringExtra(CommonUtility.DEBIT_RESPONSE)
        val responseQr = intent.getStringExtra(CommonUtility.QR_RESPONSE)

        if (responsePrint != null) {
            Log.d(TAG, responsePrint)
            Toast.makeText(context, responsePrint, Toast.LENGTH_LONG).show()
        }
        if (responseDebit != null) {
            Toast.makeText(context, responseDebit, Toast.LENGTH_LONG).show()
        }
        if (responseQr != null) {
            Toast.makeText(context, responseQr, Toast.LENGTH_LONG).show()

        }
    }
}