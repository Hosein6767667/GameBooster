
package com.gamebooster.boost

import android.content.Context
import com.gamebooster.core.BatteryManager
import com.gamebooster.core.ThermalGuard
import com.gamebooster.cpu.CpuPolicyController
import com.gamebooster.game.GameDetector
import com.gamebooster.game.GameSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object GameBoostSession {

    data class SessionState(
        val active: Boolean,
        val gameInstalled: Boolean,
        val performanceGovernorApplied: Boolean,
        val sessionDurationSeconds: Long,
        val profile: BoostProfileManager.Profile
    )

    private val sessionMutex = Mutex()

    @Volatile
    private var active = false

    @Volatile
    private var governorApplied = false

    @Volatile
    private var currentProfile =
        BoostProfileManager.Profile.PERFORMANCE

    suspend fun start(
        context: Context,
        profile: BoostProfileManager.Profile
    ): Boolean = withContext(Dispatchers.IO) {
        sessionMutex.withLock {
            val appContext = context.applicationContext

            if (active) return@withLock true

            if (!GameDetector.isCODMInstalled(appContext)) {
                return@withLock false
            }

            if (!ThermalGuard.isSafe(appContext) ||
                !BatteryManager.isSafe(appContext)
            ) {
                return@withLock false
            }

            val effectiveProfile =
                BoostProfileManager.load(appContext, profile)

            val cpuApplied =
                if (effectiveProfile.cpuPerformance) {
                    CpuPolicyController.applyPerformanceGovernor()
                } else {
                    false
                }

            try {
                GameSessionManager.startSession(appContext)

                governorApplied = cpuApplied
                currentProfile = effectiveProfile.profile
                active = true

                true
            } catch (exception: Exception) {
                if (cpuApplied) {
                    CpuPolicyController.restoreOriginalPolicies()
                }
                governorApplied = false
                active = false
                false
            }
        }
    }

    suspend fun stop(
        context: Context
    ): Boolean = withContext(Dispatchers.IO) {
        sessionMutex.withLock {
            val appContext = context.applicationContext

            if (!active) return@withLock true

            var restored = true

            if (governorApplied) {
                restored =
                    CpuPolicyController.restoreOriginalPolicies()
            }

            if (!restored) {
                return@withLock false
            }

            try {
                GameSessionManager.endSession(appContext)
            } catch (_: Exception) {
                return@withLock false
            }

            governorApplied = false
            active = false
            currentProfile = BoostProfileManager.Profile.PERFORMANCE

            true
        }
    }

    fun getState(context: Context): SessionState {
        val session =
            GameSessionManager.getSessionInfo(context)

        return SessionState(
            active = active,
            gameInstalled = GameDetector.isCODMInstalled(context),
            performanceGovernorApplied = governorApplied,
            sessionDurationSeconds = session.currentDurationSeconds,
            profile = currentProfile
        )
    }

    fun isActive(): Boolean = active

    fun getCurrentProfile(): BoostProfileManager.Profile =
        currentProfile
}
