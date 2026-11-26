
package com.gamebooster.boost

import android.content.Context
import com.gamebooster.core.BatteryManager
import com.gamebooster.core.ThermalGuard
import com.gamebooster.game.GameDetector
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object BoostController {

    sealed class Result {
        data object Started : Result()
        data object AlreadyRunning : Result()
        data object GameNotInstalled : Result()
        data object DeviceTooHot : Result()
        data object BatteryUnsafe : Result()
        data class Failed(val reason: String) : Result()
    }

    @Volatile
    private var running = false

    private val operationMutex = Mutex()

    suspend fun start(
        context: Context,
        profile: BoostProfileManager.Profile
    ): Result = operationMutex.withLock {

        if (running || GameBoostSession.isActive()) {
            running = true
            return@withLock Result.AlreadyRunning
        }

        val appContext = context.applicationContext

        if (!GameDetector.isCODMInstalled(appContext)) {
            return@withLock Result.GameNotInstalled
        }

        if (!ThermalGuard.isSafe(appContext)) {
            return@withLock Result.DeviceTooHot
        }

        if (!BatteryManager.isSafe(appContext)) {
            return@withLock Result.BatteryUnsafe
        }

        try {
            val started = GameBoostSession.start(
                appContext,
                profile
            )

            if (started) {
                running = true
                Result.Started
            } else {
                Result.Failed("Unable to start boost session")
            }
        } catch (exception: Exception) {
            Result.Failed(
                exception.message ?: "Unknown error"
            )
        }
    }

    suspend fun stop(context: Context): Boolean =
        operationMutex.withLock {

            try {
                val stopped = GameBoostSession.stop(
                    context.applicationContext
                )

                running = GameBoostSession.isActive()
                stopped || !running
            } catch (_: Exception) {
                running = GameBoostSession.isActive()
                false
            }
        }

    fun isRunning(): Boolean {
        return running || GameBoostSession.isActive()
    }
}
