package com.gamebooster.core

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager

object DeviceMonitor {

    data class DeviceStatus(
        val ramTotalMb: Long,
        val ramAvailableMb: Long,
        val ramUsedMb: Long,
        val ramUsagePercent: Int,
        val batteryPercent: Int,
        val batteryTemperatureCelsius: Float,
        val thermalStatus: Int,
        val cpuCores: Int,
        val cpuFrequenciesMhz: List<Long>,
        val powerSaveMode: Boolean
    )

    fun getStatus(context: Context): DeviceStatus {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRamMb =
            memoryInfo.totalMem / MB

        val availableRamMb =
            memoryInfo.availMem / MB

        val usedRamMb =
            (memoryInfo.totalMem - memoryInfo.availMem) / MB

        val ramUsagePercent =
            if (memoryInfo.totalMem > 0) {
                ((usedRamMb * 100L) / totalRamMb)
                    .toInt()
                    .coerceIn(0, 100)
            } else {
                0
            }

        val batteryIntent =
            context.registerReceiver(
                null,
                android.content.IntentFilter(
                    android.content.Intent.ACTION_BATTERY_CHANGED
                )
            )

        val batteryPercent =
            if (batteryIntent != null) {
                val level = batteryIntent.getIntExtra(
                    BatteryManager.EXTRA_LEVEL,
                    -1
                )

                val scale = batteryIntent.getIntExtra(
                    BatteryManager.EXTRA_SCALE,
                    -1
                )

                if (level >= 0 && scale > 0) {
                    ((level * 100f) / scale)
                        .toInt()
                        .coerceIn(0, 100)
                } else {
                    -1
                }
            } else {
                -1
            }

        val batteryTemperatureCelsius =
            if (batteryIntent != null) {
                val rawTemperature = batteryIntent.getIntExtra(
                    BatteryManager.EXTRA_TEMPERATURE,
                    -1
                )

                if (rawTemperature >= 0) {
                    rawTemperature / 10f
                } else {
                    -1f
                }
            } else {
                -1f
            }

        val powerManager =
            context.getSystemService(Context.POWER_SERVICE)
                    as PowerManager

        val thermalStatus =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                powerManager.currentThermalStatus
            } else {
                PowerManager.THERMAL_STATUS_NONE
            }

        val cpuFrequencies =
            readCpuFrequencies()

        return DeviceStatus(
            ramTotalMb = totalRamMb,
            ramAvailableMb = availableRamMb,
            ramUsedMb = usedRamMb,
            ramUsagePercent = ramUsagePercent,
            batteryPercent = batteryPercent,
            batteryTemperatureCelsius = batteryTemperatureCelsius,
            thermalStatus = thermalStatus,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            cpuFrequenciesMhz = cpuFrequencies,
            powerSaveMode = powerManager.isPowerSaveMode
        )
    }

    private fun readCpuFrequencies(): List<Long> {
        val result = mutableListOf<Long>()

        val coreCount =
            Runtime.getRuntime().availableProcessors()

        for (core in 0 until coreCount) {
            val path =
                "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq"

            try {
                val value =
                    java.io.File(path).readText().trim().toLong()

                result.add(value / 1000L)
            } catch (_: Exception) {
                result.add(-1L)
            }
        }

        return result
    }

    private const val MB = 1024L * 1024L
}