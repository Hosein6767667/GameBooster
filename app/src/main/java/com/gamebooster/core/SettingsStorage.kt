package com.gamebooster.core

import android.content.Context
import com.gamebooster.ui.BoosterSettings

object SettingsStorage {

    private const val PREFS_NAME = "game_booster_settings"

    fun load(context: Context): BoosterSettings {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return BoosterSettings(
            thermalProtection = prefs.getBoolean("thermal_protection", true),
            keepScreenAwake = prefs.getBoolean("keep_screen_awake", false),
            cpuPerformance = prefs.getBoolean("cpu_performance", true),
            maxCpuFrequency = prefs.getBoolean("max_cpu_frequency", false),
            reduceBackgroundLoad = prefs.getBoolean("reduce_background_load", true)
        )
    }

    fun save(
        context: Context,
        settings: BoosterSettings
    ): Boolean {
        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .putBoolean("thermal_protection", settings.thermalProtection)
            .putBoolean("keep_screen_awake", settings.keepScreenAwake)
            .putBoolean("cpu_performance", settings.cpuPerformance)
            .putBoolean("max_cpu_frequency", settings.maxCpuFrequency)
            .putBoolean("reduce_background_load", settings.reduceBackgroundLoad)
            .commit()
    }
}