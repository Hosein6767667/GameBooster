package com.gamebooster.core

import android.content.Context
import android.os.Build
import android.os.PowerManager

object PerformanceManager {

    data class DevicePerformanceInfo(
        val manufacturer: String,
        val model: String,
        val androidVersion: Int,
        val cpuCores: Int,
        val powerSaveMode: Boolean,
        val thermalStatus: Int,
        val performanceAllowed: Boolean
    )

    fun getInfo(context: Context): DevicePerformanceInfo {
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val powerSaveMode =
            powerManager.isPowerSaveMode

        val thermalStatus =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                powerManager.currentThermalStatus
            } else {
                PowerManager.THERMAL_STATUS_NONE
            }

        val thermalSafe =
            thermalStatus < PowerManager.THERMAL_STATUS_SEVERE

        val performanceAllowed =
            !powerSaveMode && thermalSafe

        return DevicePerformanceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.SDK_INT,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            powerSaveMode = powerSaveMode,
            thermalStatus = thermalStatus,
            performanceAllowed = performanceAllowed
        )
    }

    fun isPerformanceAllowed(context: Context): Boolean {
        return getInfo(context).performanceAllowed
    }

    fun isPowerSaveMode(context: Context): Boolean {
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager

        return powerManager.isPowerSaveMode
    }

    fun getCpuCoreCount(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
}