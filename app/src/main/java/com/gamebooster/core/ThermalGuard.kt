package com.gamebooster.core

import android.content.Context
import android.os.Build
import android.os.PowerManager

object ThermalGuard {

    fun getStatus(context: Context): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return PowerManager.THERMAL_STATUS_NONE
        }

        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager

        return powerManager.currentThermalStatus
    }

    fun isSafe(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true
        }

        val status = getStatus(context)

        return status < PowerManager.THERMAL_STATUS_SEVERE
    }

    fun isCritical(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false
        }

        val status = getStatus(context)

        return status >= PowerManager.THERMAL_STATUS_CRITICAL
    }
}