package com.gamebooster.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object RootManager {

    data class CommandResult(
        val success: Boolean,
        val output: String = "",
        val error: String = "",
        val exitCode: Int = -1
    )

    suspend fun isRootAvailable(): Boolean {
        val result = execute("id")
        return result.success &&
            result.output.contains("uid=0")
    }

    suspend fun execute(command: String): CommandResult =
        withContext(Dispatchers.IO) {

            if (!isAllowed(command)) {
                return@withContext CommandResult(
                    success = false,
                    error = "Command not allowed"
                )
            }

            var process: Process? = null

            try {
                process = ProcessBuilder(
                    "su",
                    "-c",
                    command
                )
                    .redirectErrorStream(false)
                    .start()

                val output = StringBuilder()
                val error = StringBuilder()

                val stdoutThread = Thread {
                    BufferedReader(
                        InputStreamReader(process.inputStream)
                    ).useLines { lines ->
                        lines.forEach {
                            output.append(it).append('\n')
                        }
                    }
                }

                val stderrThread = Thread {
                    BufferedReader(
                        InputStreamReader(process.errorStream)
                    ).useLines { lines ->
                        lines.forEach {
                            error.append(it).append('\n')
                        }
                    }
                }

                stdoutThread.start()
                stderrThread.start()

                val finished = process.waitFor(
                    COMMAND_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
                )

                if (!finished) {
                    process.destroyForcibly()

                    return@withContext CommandResult(
                        success = false,
                        output = output.toString().trim(),
                        error = "Command timeout",
                        exitCode = -1
                    )
                }

                stdoutThread.join(1000)
                stderrThread.join(1000)

                val exitCode = process.exitValue()

                CommandResult(
                    success = exitCode == 0,
                    output = output.toString().trim(),
                    error = error.toString().trim(),
                    exitCode = exitCode
                )

            } catch (exception: Exception) {

                CommandResult(
                    success = false,
                    error = exception.message
                        ?: "Root command failed"
                )

            } finally {
                process?.destroy()
            }
        }

    private fun isAllowed(command: String): Boolean {

        if (command.isBlank()) {
            return false
        }

        val forbidden = listOf(
            ";",
            "&&",
            "||",
            "|",
            "`",
            "$(",
            "\n",
            "\r"
        )

        if (forbidden.any { command.contains(it) }) {
            return false
        }

        if (command == "id") {
            return true
        }

        if (
            command.startsWith("cat ") &&
            command.contains(
                "/sys/devices/system/cpu/"
            )
        ) {
            return true
        }

        if (
            command.startsWith("echo ") &&
            command.contains(
                " > /sys/devices/system/cpu/"
            )
        ) {
            return true
        }

        return false
    }

    private const val COMMAND_TIMEOUT_SECONDS = 3L
}