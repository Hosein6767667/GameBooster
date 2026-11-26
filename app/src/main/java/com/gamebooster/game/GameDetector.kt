package com.gamebooster.game

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object GameDetector {

    private val codmPackages = listOf(
        "com.activision.callofduty.shooter",
        "com.garena.game.codm"
    )

    fun isCODMInstalled(context: Context): Boolean {
        return getCODMPackage(context) != null
    }

    fun getCODMPackage(context: Context): String? {
        val packageManager = context.packageManager

        for (packageName in codmPackages) {
            try {
                if (Build.VERSION.SDK_INT >= 33) {
                    packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.ApplicationInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getApplicationInfo(packageName, 0)
                }

                return packageName

            } catch (_: PackageManager.NameNotFoundException) {
                continue
            }
        }

        return null
    }

    fun getInstalledCODMName(context: Context): String? {
        val packageName = getCODMPackage(context)
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
                    context.packageManager.getApplicationInfo(
                        packageName,
                        0
                    )
                }

            context.packageManager
                .getApplicationLabel(applicationInfo)
                .toString()

        } catch (_: Exception) {
            null
        }
    }
}