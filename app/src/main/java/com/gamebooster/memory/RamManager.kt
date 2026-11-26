package com.gamebooster.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Build

object RamManager {

    data class RamInfo(
        val totalMb: Long,
        val availableMb: Long,
        val usedMb: Long,
        val usagePercent: Int,
        val lowMemory: Boolean
    )

    fun getInfo(context: Context): RamInfo {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

        val memoryInfo = ActivityManager.MemoryInfo()

        activityManager.getMemoryInfo(memoryInfo)

        val totalMb =
            memoryInfo.totalMem / MB

        val availableMb =
            memoryInfo.availMem / MB

        val usedMb =
            (memoryInfo.totalMem - memoryInfo.availMem)
                .coerceAtLeast(0L) / MB

        val usagePercent =
            if (totalMb > 0L) {
                ((usedMb * 100L) / totalMb)
                    .toInt()
                    .coerceIn(0, 100)
            } else {
                0
            }

        return RamInfo(
            totalMb = totalMb,
            availableMb = availableMb,
            usedMb = usedMb,
            usagePercent = usagePercent,
            lowMemory = memoryInfo.lowMemory
        )
    }

    fun getAvailableRamMb(context: Context): Long {
        return getInfo(context).availableMb
    }

    fun getUsedRamMb(context: Context): Long {
        return getInfo(context).usedMb
    }

    fun getUsagePercent(context: Context): Int {
        return getInfo(context).usagePercent
    }

    fun isLowMemory(context: Context): Boolean {
        return getInfo(context).lowMemory
    }

    fun isRamPressureHigh(context: Context): Boolean {
        val info = getInfo(context)

        return info.lowMemory ||
            info.usagePercent >= HIGH_USAGE_PERCENT
    }

    fun getMemoryClassMb(context: Context): Int {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

        return activityManager.memoryClass
    }

    fun getLargeMemoryClassMb(context: Context): Int {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            activityManager.largeMemoryClass
        } else {
            activityManager.memoryClass
        }
    }

    private const val MB = 1024L * 1024L
    private const val HIGH_USAGE_PERCENT = 90
}