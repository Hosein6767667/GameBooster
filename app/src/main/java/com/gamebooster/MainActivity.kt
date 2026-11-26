package com.gamebooster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import com.gamebooster.ui.AboutScreen
import com.gamebooster.ui.GameBoosterTheme
import com.gamebooster.ui.HomeScreen
import com.gamebooster.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            GameBoosterTheme {

                var currentScreen by rememberSaveable {
                    mutableStateOf("home")
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
                            onBack = {
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