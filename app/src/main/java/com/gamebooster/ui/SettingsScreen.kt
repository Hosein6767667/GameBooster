
package com.gamebooster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamebooster.core.SettingsStorage

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var thermalProtection by remember { mutableStateOf(true) }
    var cpuPerformance by remember { mutableStateOf(true) }
    var keepScreenAwake by remember { mutableStateOf(false) }
    var reduceBackgroundLoad by remember { mutableStateOf(true) }
    var settingsLoaded by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val settings = SettingsStorage.load(context)

        thermalProtection = settings.thermalProtection
        cpuPerformance = settings.cpuPerformance
        keepScreenAwake = settings.keepScreenAwake
        reduceBackgroundLoad = settings.reduceBackgroundLoad

        settingsLoaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← BACK")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SETTINGS",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingItem(
            title = "Thermal Protection",
            description = "Stops performance changes when the device becomes dangerously hot.",
            checked = thermalProtection,
            onCheckedChange = {
                thermalProtection = it
                saveMessage = null
            }
        )

        HorizontalDivider()

        SettingItem(
            title = "CPU Performance",
            description = "Uses supported CPU performance policies when available.",
            checked = cpuPerformance,
            onCheckedChange = {
                cpuPerformance = it
                saveMessage = null
            }
        )

        HorizontalDivider()

        SettingItem(
            title = "Keep Screen Awake",
            description = "Keeps the screen active while the booster screen is being used.",
            checked = keepScreenAwake,
            onCheckedChange = {
                keepScreenAwake = it
                saveMessage = null
            }
        )

        HorizontalDivider()

        SettingItem(
            title = "Reduce Background Load",
            description = "Enables supported background-load policies where Android allows them.",
            checked = reduceBackgroundLoad,
            onCheckedChange = {
                reduceBackgroundLoad = it
                saveMessage = null
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val settings = BoosterSettings(
                    thermalProtection = thermalProtection,
                    cpuPerformance = cpuPerformance,
                    keepScreenAwake = keepScreenAwake,
                    reduceBackgroundLoad = reduceBackgroundLoad
                )

                val saved = SettingsStorage.save(context, settings)

                saveMessage = if (saved) {
                    "Settings saved successfully."
                } else {
                    "Could not save settings."
                }
            },
            enabled = settingsLoaded,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "SAVE SETTINGS",
                fontSize = 15.sp
            )
        }

        saveMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Thermal safety is also enforced by the booster service.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
