
package com.gamebooster.boost

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.gamebooster.MainActivity
import com.gamebooster.R
import com.gamebooster.core.SettingsStorage
import com.gamebooster.core.ThermalGuard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class GameBoostService : Service() {

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val stopping = AtomicBoolean(false)
    private val startInProgress = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        stopping.set(false)

        val profileName = intent?.getStringExtra(EXTRA_PROFILE)
            ?: BoostProfileManager.Profile.PERFORMANCE.name

        val profile = runCatching {
            BoostProfileManager.Profile.valueOf(profileName)
        }.getOrDefault(BoostProfileManager.Profile.PERFORMANCE)

        startForeground(
            NOTIFICATION_ID,
            createNotification("Starting booster...")
        )

        if (!startInProgress.compareAndSet(false, true)) {
            return START_NOT_STICKY
        }

        serviceScope.launch {
            try {
                when (val result = BoostController.start(applicationContext, profile)) {
                    BoostController.Result.Started -> {
                        updateNotification("Boost protection active")
                        monitorThermalState()
                    }

                    BoostController.Result.AlreadyRunning -> {
                        updateNotification("Booster already running")
                        monitorThermalState()
                    }

                    BoostController.Result.GameNotInstalled -> {
                        updateNotification("Call of Duty: Mobile not found")
                        stopServiceSafely()
                    }

                    BoostController.Result.DeviceTooHot -> {
                        updateNotification("Device temperature is too high")
                        stopServiceSafely()
                    }

                    BoostController.Result.BatteryUnsafe -> {
                        updateNotification("Battery condition is unsafe")
                        stopServiceSafely()
                    }

                    is BoostController.Result.Failed -> {
                        updateNotification("Boost failed: ${result.reason}")
                        stopServiceSafely()
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                updateNotification("Booster encountered an error")
                stopServiceSafely()
            } finally {
                startInProgress.set(false)
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun monitorThermalState() {
        while (!stopping.get()) {
            delay(3000)

            val settings = SettingsStorage.load(applicationContext)

            if (settings.thermalProtection &&
                !ThermalGuard.isSafe(applicationContext)
            ) {
                BoostController.stop(applicationContext)
                updateNotification("Stopped: device temperature is high")
                stopServiceSafely()
                return
            }
        }
    }

    private fun stopServiceSafely() {
        if (!stopping.compareAndSet(false, true)) return

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopping.set(true)

        serviceScope.launch {
            try {
                BoostController.stop(applicationContext)
            } finally {
                serviceScope.cancel()
            }
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Game Booster",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Game performance and thermal protection"
                setShowBadge(false)
            }

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE
                } else {
                    0
                }
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_booster)
            .setContentTitle("Game Booster")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(message: String) {
        getSystemService(NotificationManager::class.java).notify(
            NOTIFICATION_ID,
            createNotification(message)
        )
    }

    companion object {
        private const val CHANNEL_ID = "game_booster_channel"
        private const val NOTIFICATION_ID = 1001
        private const val EXTRA_PROFILE = "boost_profile"

        fun start(
            context: Context,
            profile: BoostProfileManager.Profile
        ) {
            val intent = Intent(
                context,
                GameBoostService::class.java
            ).putExtra(EXTRA_PROFILE, profile.name)

            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(
                Intent(context, GameBoostService::class.java)
            )
        }
    }
}
