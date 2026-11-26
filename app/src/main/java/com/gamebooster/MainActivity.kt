package com.gamebooster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.gamebooster.core.SettingsStorage
import com.gamebooster.ui.AboutScreen
import com.gamebooster.ui.GameBoosterTheme
import com.gamebooster.ui.HomeScreen
import com.gamebooster.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedSettings =
            SettingsStorage.load(this)

        setContent {

            GameBoosterTheme {

                var currentScreen by rememberSaveable {
                    mutableStateOf("home")
                }

                var settings by remember {
                    mutableStateOf(savedSettings)
                }

                when (currentScreen) {

                    "home" -> {
                        HomeScreen(
                            onOpenSettings = {
                                currentScreen = "settings"
                            },
                            onOpenAbout = {
                                currentScreen = "about"
                            }
                        )
                    }

                    "settings" -> {
                        SettingsScreen(
                            initialSettings = settings,

                            onBack = {
                                currentScreen = "home"
                            },

                            onSave = { newSettings ->
                                settings = newSettings

                                SettingsStorage.save(
                                    this@MainActivity,
                                    newSettings
                                )

                                currentScreen = "home"
                            }
                        )
                    }

                    "about" -> {
                        AboutScreen(
                            onBack = {
                                currentScreen = "home"
                            }
                        )
                    }

                    else -> {
                        currentScreen = "home"
                    }
                }
            }
        }
    }
}