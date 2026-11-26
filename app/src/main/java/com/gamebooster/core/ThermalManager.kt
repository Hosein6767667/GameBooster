package com.gamebooster.core

import android.content.Context
import android.os.Build
import android.os.PowerManager

object ThermalManager {

    data class ThermalInfo(
        val status: Int,
        val statusName: String,
        val safeForPerformance: Boolean
    )

    fun getInfo(context: Context): ThermalInfo {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return ThermalInfo(
                status = -1,
                statusName = "UNAVAILABLE",
                safeForPerformance = true
            )
        }

        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val status = powerManager.currentThermalStatus

        val name = when (status) {
            PowerManager.THERMAL_STATUS_NONE -> "NORMAL"
            PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"
            PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
            PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE"
            PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "EMERGENCY"
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "SHUTDOWN"
            else -> "UNKNOWN"
        }

        return ThermalInfo(
            status = status,
            statusName = name,
            safeForPerformance =
                status < PowerManager.THERMAL_STATUS_SEVERE
        )
    }

    fun shouldReduceLoad(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false
        }

        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager

        return powerManager.currentThermalStatus >=
                PowerManager.THERMAL_STATUS_SEVERE
    }
}