package com.gamebooster.cpu

import com.gamebooster.core.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CpuPolicyManager {

    data class CpuPolicy(
        val core: Int,
        val governor: String,
        val minFrequencyMhz: Long,
        val maxFrequencyMhz: Long,
        val available: Boolean
    )

    suspend fun readPolicy(core: Int): CpuPolicy =
        withContext(Dispatchers.IO) {

            if (core < 0) {
                return@withContext unavailablePolicy(core)
            }

            val base =
                "/sys/devices/system/cpu/cpu$core/cpufreq"

            val governor = readValue("$base/scaling_governor")

            val minFrequency =
                readValue("$base/scaling_min_freq")
                    .toLongOrNull()
                    ?.div(1000L) ?: -1L

            val maxFrequency =
                readValue("$base/scaling_max_freq")
                    .toLongOrNull()
                    ?.div(1000L) ?: -1L

            CpuPolicy(
                core = core,
                governor = governor,
                minFrequencyMhz = minFrequency,
                maxFrequencyMhz = maxFrequency,
                available = governor.isNotBlank()
            )
        }

    suspend fun readAllPolicies(): List<CpuPolicy> =
        withContext(Dispatchers.IO) {
            val count = Runtime.getRuntime().availableProcessors()

            (0 until count).map { core ->
                readPolicy(core)
            }
        }

    suspend fun applyPerformanceGovernor(
        governor: String = "performance"
    ): Boolean = withContext(Dispatchers.IO) {

        if (!isValidGovernorName(governor)) {
            return@withContext false
        }

        var changed = false
        val count = Runtime.getRuntime().availableProcessors()

        for (core in 0 until count) {
            val available = readAvailableGovernors(core)

            if (governor !in available) continue

            val result = RootManager.execute(
                "echo $governor > /sys/devices/system/cpu/cpu$core/cpufreq/scaling_governor"
            )

            if (result.success) changed = true
        }

        changed
    }

    suspend fun restorePolicies(
        policies: List<CpuPolicy>
    ): Boolean = withContext(Dispatchers.IO) {

        var restored = false

        for (policy in policies) {
            if (
                !policy.available ||
                policy.core < 0 ||
                !isValidGovernorName(policy.governor)
            ) continue

            val result = RootManager.execute(
                "echo ${policy.governor} > /sys/devices/system/cpu/cpu${policy.core}/cpufreq/scaling_governor"
            )

            if (result.success) restored = true
        }

        restored
    }

    suspend fun getAvailableGovernors(core: Int): List<String> =
        withContext(Dispatchers.IO) {
            readAvailableGovernors(core)
        }

    private suspend fun readAvailableGovernors(
        core: Int
    ): List<String> {
        if (core < 0) return emptyList()

        val result = RootManager.execute(
            "cat /sys/devices/system/cpu/cpu$core/cpufreq/scaling_available_governors"
        )

        if (!result.success) return emptyList()

        return result.output
            .trim()
            .split(Regex("\\s+"))
            .filter { isValidGovernorName(it) }
    }

    private suspend fun readValue(path: String): String {
        val result = RootManager.execute("cat $path")
        return if (result.success) result.output.trim() else ""
    }

    private fun isValidGovernorName(value: String): Boolean =
        value.matches(Regex("[a-zA-Z0-9_.-]+"))

    private fun unavailablePolicy(core: Int) = CpuPolicy(
        core = core,
        governor = "",
        minFrequencyMhz = -1L,
        maxFrequencyMhz = -1L,
        available = false
    )
}