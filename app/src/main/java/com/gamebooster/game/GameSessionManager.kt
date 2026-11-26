package com.gamebooster.game

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GameSessionManager {

    private const val PREFS_NAME = "game_session_data"
    private const val KEY_START_TIME = "session_start_time"
    private const val KEY_TOTAL_TIME = "total_play_time"
    private const val KEY_SESSION_COUNT = "session_count"
    private const val KEY_LAST_SESSION = "last_session_duration"

    data class SessionInfo(
        val isActive: Boolean,
        val currentDurationSeconds: Long,
        val totalPlayTimeSeconds: Long,
        val sessionCount: Int,
        val lastSessionDurationSeconds: Long
    )

    private var activeStartTime: Long = 0L

    private fun preferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    suspend fun startSession(context: Context): Boolean =
        withContext(Dispatchers.IO) {
            synchronized(this@GameSessionManager) {
                if (activeStartTime > 0L) {
                    return@synchronized false
                }

                activeStartTime = SystemClock.elapsedRealtime()

                preferences(context).edit()
                    .putLong(KEY_START_TIME, System.currentTimeMillis())
                    .apply()

                true
            }
        }

    suspend fun endSession(context: Context): Long =
        withContext(Dispatchers.IO) {
            synchronized(this@GameSessionManager) {
                if (activeStartTime <= 0L) {
                    return@synchronized 0L
                }

                val duration =
                    (SystemClock.elapsedRealtime() - activeStartTime) / 1000L

                activeStartTime = 0L

                val prefs = preferences(context)

                val totalTime =
                    prefs.getLong(KEY_TOTAL_TIME, 0L) + duration

                val sessionCount =
                    prefs.getInt(KEY_SESSION_COUNT, 0) + 1

                prefs.edit()
                    .remove(KEY_START_TIME)
                    .putLong(KEY_TOTAL_TIME, totalTime)
                    .putInt(KEY_SESSION_COUNT, sessionCount)
                    .putLong(KEY_LAST_SESSION, duration)
                    .apply()

                duration
            }
        }

    fun getSessionInfo(context: Context): SessionInfo {
        synchronized(this@GameSessionManager) {
            val prefs = preferences(context)

            val active = activeStartTime > 0L

            val currentDuration = if (active) {
                (SystemClock.elapsedRealtime() - activeStartTime) / 1000L
            } else {
                0L
            }

            return SessionInfo(
                isActive = active,
                currentDurationSeconds = currentDuration,
                totalPlayTimeSeconds = prefs.getLong(KEY_TOTAL_TIME, 0L),
                sessionCount = prefs.getInt(KEY_SESSION_COUNT, 0),
                lastSessionDurationSeconds = prefs.getLong(
                    KEY_LAST_SESSION,
                    0L
                )
            )
        }
    }

    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600L
        val minutes = (seconds % 3600L) / 60L
        val remainingSeconds = seconds % 60L

        return "%02d:%02d:%02d".format(
            hours,
            minutes,
            remainingSeconds
        )
    }
}