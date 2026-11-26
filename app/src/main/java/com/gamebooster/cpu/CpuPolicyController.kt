package com.gamebooster.cpu

import com.gamebooster.core.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CpuPolicyController {

    private val lock = Any()

    private val originalPolicies =
        mutableMapOf<Int, CpuPolicyManager.CpuPolicy>()

    @Volatile
    private var policiesCaptured = false

    suspend fun captureOriginalPolicies(): Boolean =
        withContext(Dispatchers.IO) {

            synchronized(lock) {
                if (policiesCaptured) return@withContext true
            }

            if (!RootManager.isRootAvailable()) {
                return@withContext false
            }

            val policies =
                CpuPolicyManager.readAllPolicies()
                    .filter { it.available }

            if (policies.isEmpty()) {
                return@withContext false
            }

            synchronized(lock) {
                originalPolicies.clear()
                policies.forEach {
                    originalPolicies[it.core] = it
                }
                policiesCaptured = true
            }

            true
        }

    suspend fun applyPerformanceGovernor(): Boolean =
        withContext(Dispatchers.IO) {

            if (!captureOriginalPolicies()) {
                return@withContext false
            }

            CpuPolicyManager.applyPerformanceGovernor(
                governor = "performance"
            )
        }

    suspend fun restoreOriginalPolicies(): Boolean =
        withContext(Dispatchers.IO) {

            val policies = synchronized(lock) {
                if (!policiesCaptured) {
                    return@withContext false
                }

                originalPolicies.values.toList()
            }

            if (policies.isEmpty()) {
                return@withContext false
            }

            val restored =
                CpuPolicyManager.restorePolicies(policies)

            if (restored) {
                synchronized(lock) {
                    originalPolicies.clear()
                    policiesCaptured = false
                }
            }

            restored
        }

    suspend fun isRootAvailable(): Boolean =
        RootManager.isRootAvailable()
}