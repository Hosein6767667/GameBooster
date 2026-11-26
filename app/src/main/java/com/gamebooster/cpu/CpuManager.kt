package com.gamebooster.cpu

import android.content.Context
import com.gamebooster.core.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CpuManager {

    data class CpuInfo(
        val coreCount: Int,
        val availableCores: List<Int>,
        val currentFrequenciesMhz: List<Long>,
        val governors: List<String>,
        val rootAvailable: Boolean
    )

    suspend fun getInfo(context: Context): CpuInfo =
        withContext(Dispatchers.IO) {

            val coreCount =
                Runtime.getRuntime().availableProcessors()

            val cores =
                (0 until coreCount).toList()

            val rootAvailable =
                RootManager.isRootAvailable()

            val frequencies =
                readFrequencies(cores)

            val governors =
                readGovernors(cores)

            CpuInfo(
                coreCount = coreCount,
                availableCores = cores,
                currentFrequenciesMhz = frequencies,
                governors = governors,
                rootAvailable = rootAvailable
            )
        }

    suspend fun getCurrentFrequencyMhz(core: Int): Long {
        if (core < 0) {
            return -1L
        }

        val result = RootManager.execute(
            "cat /sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq"
        )

        if (!result.success) {
            return -1L
        }

        return result.output
            .trim()
            .toLongOrNull()
            ?.div(1000L)
            ?: -1L
    }

    suspend fun getGovernor(core: Int): String {
        if (core < 0) {
            return ""
        }

        val result = RootManager.execute(
            "cat /sys/devices/system/cpu/cpu$core/cpufreq/scaling_governor"
        )

        if (!result.success) {
            return ""
        }

        return result.output.trim()
    }

    suspend fun setGovernor(
        core: Int,
        governor: String
    ): Boolean {

        if (core < 0 || governor.isBlank()) {
            return false
        }

        if (!isSafeGovernorName(governor)) {
            return false
        }

        val result = RootManager.execute(
            "echo $governor > /sys/devices/system/cpu/cpu$core/cpufreq/scaling_governor"
        )

        return result.success
    }

    suspend fun isGovernorAvailable(
        core: Int,
        governor: String
    ): Boolean {

        if (core < 0 || governor.isBlank()) {
            return false
        }

        if (!isSafeGovernorName(governor)) {
            return false
        }

        val result = RootManager.execute(
            "cat /sys/devices/system/cpu/cpu$core/cpufreq/scaling_available_governors"
        )

        if (!result.success) {
            return false
        }

        return result.output
            .split(Regex("\\s+"))
            .any { it == governor }
    }

    private suspend fun readFrequencies(
        cores: List<Int>
    ): List<Long> {

        val result = mutableListOf<Long>()

        for (core in cores) {
            result.add(
                getCurrentFrequencyMhz(core)
            )
        }

        return result
    }

    private suspend fun readGovernors(
        cores: List<Int>
    ): List<String> {

        val result = mutableListOf<String>()

        for (core in cores) {
            result.add(
                getGovernor(core)
            )
        }

        return result
    }

    private fun isSafeGovernorName(
        governor: String
    ): Boolean {

        return governor.matches(
            Regex("[a-zA-Z0-9_.-]+")
        )
    }
}