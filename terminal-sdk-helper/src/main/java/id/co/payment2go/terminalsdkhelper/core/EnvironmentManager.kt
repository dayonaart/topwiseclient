package id.co.payment2go.terminalsdkhelper.core

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class EnvironmentManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {
    private val TAG = "EnvironmentManager"

    companion object {
        const val ENVIRONMENT_KEY = "ENVIRONMENT_KEY"
    }

    fun setEnvironment(type: EnvironmentType) {
        TermLog.d(TAG, "setEnvironment -> $type")
        sharedPreferences.edit().putString(ENVIRONMENT_KEY, type.name).apply()
    }

    fun getEnvironment(): EnvironmentType {
        val environment = sharedPreferences.getString(ENVIRONMENT_KEY, "DEV") ?: "DEV"
        TermLog.d(TAG, "getEnvironment -> $environment")
        return EnvironmentType.valueOf(environment)
    }

    fun restartApplication() {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}