package com.gamebooster.game

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

object GameLauncher {

    fun launchCODM(context: Context): Boolean {
        val packageName = GameDetector.getCODMPackage(context)
            ?: return false

        return try {
            val launchIntent =
                context.packageManager.getLaunchIntentForPackage(packageName)
                    ?: return false

            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )

            context.startActivity(launchIntent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isCODMRunning(context: Context): Boolean {
        val packageName = GameDetector.getCODMPackage(context)
            ?: return false

        return try {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

            activityManager.appTasks.any { task ->
                task.taskInfo.topActivity?.packageName == packageName
            }
        } catch (_: Exception) {
            false
        }
    }

    fun getCODMVersion(context: Context): String? {
        val packageName = GameDetector.getCODMPackage(context)
            ?: return null

        return try {
            val packageInfo =
                if (Build.VERSION.SDK_INT >= 33) {
                    context.packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(packageName, 0)
                }

            packageInfo.versionName
        } catch (_: Exception) {
            null
        }
    }

    fun getCODMUid(context: Context): Int? {
        val packageName = GameDetector.getCODMPackage(context)
            ?: return null

        return try {
            val applicationInfo =
                if (Build.VERSION.SDK_INT >= 33) {
                    context.packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.ApplicationInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getApplicationInfo(packageName, 0)
                }

            applicationInfo.uid
        } catch (_: Exception) {
            null
        }
    }
}