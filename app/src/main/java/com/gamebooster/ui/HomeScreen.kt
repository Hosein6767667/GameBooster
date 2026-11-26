package com.gamebooster.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamebooster.boost.BoostProfileManager
import com.gamebooster.boost.GameBoostService

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current

    var selectedProfile by remember {
        mutableStateOf(BoostProfileManager.Profile.PERFORMANCE)
    }

    var serviceRunning by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "GAME BOOSTER",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "CALL OF DUTY: MOBILE",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        StatusCard(serviceRunning)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "BOOST PROFILE",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            ProfileButton(
                title = "BALANCED",
                selected = selectedProfile == BoostProfileManager.Profile.BALANCED,
                modifier = Modifier.weight(1f)
            ) {
                selectedProfile = BoostProfileManager.Profile.BALANCED
            }

            Spacer(modifier = Modifier.width(8.dp))

            ProfileButton(
                title = "PERFORMANCE",
                selected = selectedProfile == BoostProfileManager.Profile.PERFORMANCE,
                modifier = Modifier.weight(1f)
            ) {
                selectedProfile = BoostProfileManager.Profile.PERFORMANCE
            }

            Spacer(modifier = Modifier.width(8.dp))

            ProfileButton(
                title = "MAXIMUM",
                selected = selectedProfile == BoostProfileManager.Profile.MAXIMUM,
                modifier = Modifier.weight(1f)
            ) {
                selectedProfile = BoostProfileManager.Profile.MAXIMUM
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                if (serviceRunning) {
                    GameBoostService.stop(context)
                    serviceRunning = false
                } else {
                    GameBoostService.start(
                        context,
                        selectedProfile
                    )
                    serviceRunning = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (serviceRunning) {
                    Color(0xFF8B1E1E)
                } else {
                    MaterialTheme.colorScheme.primary
                },
                contentColor = if (serviceRunning) {
                    Color.White
                } else {
                    Color(0xFF00140A)
                }
            )
        ) {
            Text(
                text = if (serviceRunning) "STOP BOOST" else "START BOOST",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f)
            ) {
                Text("SETTINGS")
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick = onOpenAbout,
                modifier = Modifier.weight(1f)
            ) {
                Text("ABOUT")
            }
        }
    }
}

@Composable
private fun StatusCard(
    running: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "STATUS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (running) "BOOST ACTIVE" else "READY",
                color = if (running) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (running) {
                    "Performance protection is active."
                } else {
                    "Select a profile and start the booster."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ProfileButton(
    title: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color(0xFF00140A)
            )
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp
            )
        }
    }
}