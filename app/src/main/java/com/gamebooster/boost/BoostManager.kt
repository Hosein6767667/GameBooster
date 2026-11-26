package com.gamebooster.boost

import android.content.Context
import com.gamebooster.core.ThermalGuard
import com.gamebooster.cpu.CpuPolicyController

object BoostManager {

    sealed class Result {
        data object Applied : Result()
        data object RootUnavailable : Result()
        data object DeviceTooHot : Result()
        data object AlreadyActive : Result()
        data object Failed : Result()
    }

    @Volatile
    private var active = false

    suspend fun apply(
        context: Context,
        profile: BoostProfileManager.Profile
    ): Result {

        if (active) return Result.AlreadyActive

        if (!ThermalGuard.isSafe(context)) {
            return Result.DeviceTooHot
        }

        if (profile == BoostProfileManager.Profile.BALANCED) {
            active = true
            return Result.Applied
        }

        if (!CpuPolicyController.isRootAvailable()) {
            return Result.RootUnavailable
        }

        return try {
            val applied =
                CpuPolicyController.applyPerformanceGovernor()

            if (applied) {
                active = true
                Result.Applied
            } else {
                Result.Failed
            }
        } catch (_: Exception) {
            Result.Failed
        }
    }

    suspend fun restore(): Boolean {
        return try {
            val restored =
                CpuPolicyController.restoreOriginalPolicies()

            active = false
            restored
        } catch (_: Exception) {
            false
        }
    }

    fun isActive(): Boolean = active
}