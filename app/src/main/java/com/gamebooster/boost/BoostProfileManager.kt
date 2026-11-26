package com.gamebooster.boost

import android.content.Context
import com.gamebooster.core.SettingsStorage

object BoostProfileManager {

    enum class Profile {
        BALANCED,
        PERFORMANCE,
        MAXIMUM
    }

    data class EffectiveProfile(
        val profile: Profile,
        val cpuPerformance: Boolean,
        val thermalProtection: Boolean,
        val reduceBackgroundLoad: Boolean,
        val maxCpuFrequency: Boolean
    )

    fun load(
        context: Context,
        profile: Profile
    ): EffectiveProfile {

        val settings = SettingsStorage.load(context)

        return when (profile) {

            Profile.BALANCED -> EffectiveProfile(
                profile = profile,
                cpuPerformance = false,
                thermalProtection = true,
                reduceBackgroundLoad = false,
                maxCpuFrequency = false
            )

            Profile.PERFORMANCE -> EffectiveProfile(
                profile = profile,
                cpuPerformance = settings.cpuPerformance,
                thermalProtection = true,
                reduceBackgroundLoad = settings.reduceBackgroundLoad,
                maxCpuFrequency = false
            )

            Profile.MAXIMUM -> EffectiveProfile(
                profile = profile,
                cpuPerformance = settings.cpuPerformance,
                thermalProtection = true,
                reduceBackgroundLoad = settings.reduceBackgroundLoad,
                maxCpuFrequency = false
            )
        }
    }
}