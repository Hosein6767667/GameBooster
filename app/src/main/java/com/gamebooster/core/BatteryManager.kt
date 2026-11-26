package com.gamebooster.core

import android.content.Context
import android.content.Intent
import android.os.BatteryManager as AndroidBatteryManager

object BatteryManager {

    data class BatteryInfo(
        val level: Int,
        val temperatureCelsius: Float,
        val voltageMv: Int,
        val isCharging: Boolean,
        val health: Int
    )

    fun getInfo(context: Context): BatteryInfo {
        val intent = context.registerReceiver(
            null,
            android.content.IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
            )
        )

        if (intent == null) {
            return BatteryInfo(
                level = -1,
                temperatureCelsius = -1f,
                voltageMv = -1,
                isCharging = false,
                health = AndroidBatteryManager.BATTERY_HEALTH_UNKNOWN
            )
        }

        val level = intent.getIntExtra(
            AndroidBatteryManager.EXTRA_LEVEL,
            -1
        )

        val scale = intent.getIntExtra(
            AndroidBatteryManager.EXTRA_SCALE,
            100
        )

        val temperature = intent.getIntExtra(
            AndroidBatteryManager.EXTRA_TEMPERATURE,
            -1
        )

        val voltage = intent.getIntExtra(
            AndroidBatteryManager.EXTRA_VOLTAGE,
            -1
        )

        val status = intent.getIntExtra(
            AndroidBatteryManager.EXTRA_STATUS,
            -1
        )

        val health = intent.getIntExtra(
            AndroidBatteryManager.EXTRA_HEALTH,
            AndroidBatteryManager.BATTERY_HEALTH_UNKNOWN
        )

        val percentage =
            if (level >= 0 && scale > 0) {
                ((level * 100f) / scale)
                    .coerceIn(0f, 100f)
                    .toInt()
            } else {
                -1
            }

        val temperatureCelsius =
            if (temperature >= 0) {
                temperature / 10f
            } else {
                -1f
            }

        val isCharging =
            status == AndroidBatteryManager.BATTERY_STATUS_CHARGING ||
            status == AndroidBatteryManager.BATTERY_STATUS_FULL

        return BatteryInfo(
            level = percentage,
            temperatureCelsius = temperatureCelsius,
            voltageMv = voltage,
            isCharging = isCharging,
            health = health
        )
    }

    fun isSafe(context: Context): Boolean {
        val info = getInfo(context)

        if (info.temperatureCelsius < 0f) {
            return true
        }

        return info.temperatureCelsius < MAX_SAFE_TEMPERATURE_CELSIUS
    }

    fun isHealthy(context: Context): Boolean {
        val health = getInfo(context).health

        return health == AndroidBatteryManager.BATTERY_HEALTH_GOOD ||
            health == AndroidBatteryManager.BATTERY_HEALTH_UNKNOWN
    }

    private const val MAX_SAFE_TEMPERATURE_CELSIUS = 45f
}