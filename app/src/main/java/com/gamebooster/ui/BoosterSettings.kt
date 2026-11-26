package com.gamebooster.ui

data class BoosterSettings(
    val thermalProtection: Boolean = true,
    val keepScreenAwake: Boolean = false,
    val cpuPerformance: Boolean = true,
    val maxCpuFrequency: Boolean = false,
    val reduceBackgroundLoad: Boolean = true
)