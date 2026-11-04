package com.example.linebreak.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linebreak.data.ThemeMode
import com.example.linebreak.data.UserPreferences
import com.example.linebreak.sound.SoundManager

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    soundManager: SoundManager,
    onThemeChange: (ThemeMode) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onResetStats: () -> Unit,
    onBackClick: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val strokeColor = MaterialTheme.colorScheme.outline
    val dangerColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Bar with Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            soundManager.playButtonClick()
                            onBackClick()
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(surfaceColor)
                            .border(1.dp, strokeColor, CircleShape)
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
                            // Back chevron
                            val w = size.width
                            val h = size.height
                            drawLine(
                                color = primaryColor,
                                start = Offset(w * 0.7f, h * 0.15f),
                                end = Offset(w * 0.3f, h * 0.5f),
                                strokeWidth = 2.5.dp.toPx()
                            )
                            drawLine(
                                color = primaryColor,
                                start = Offset(w * 0.3f, h * 0.5f),
                                end = Offset(w * 0.7f, h * 0.85f),
                                strokeWidth = 2.5.dp.toPx()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Settings Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor, RoundedCornerShape(18.dp))
                        .border(1.dp, strokeColor, RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Theme Mode Selector
                    Column {
                        Text(
                            text = "APPEARANCE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ThemeMode.values().forEach { mode ->
                                val isSelected = preferences.themeMode == mode
                                val itemBg = if (isSelected) primaryColor else Color.Transparent
                                val itemColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(itemBg)
                                        .clickable {
                                            soundManager.playButtonClick()
                                            onThemeChange(mode)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.name,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        ),
                                        color = itemColor
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(strokeColor)
                    )

                    // Sound Effects Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SOUND FX",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tone chimes and impact feedback",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.soundEnabled,
                            onCheckedChange = {
                                onSoundChange(it)
                                if (it) soundManager.playButtonClick()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = primaryColor
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(strokeColor)
                    )

                    // Haptics Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HAPTIC FEEDBACK",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tactile tap & vibration impacts",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.hapticsEnabled,
                            onCheckedChange = {
                                onHapticsChange(it)
                                soundManager.playButtonClick()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = primaryColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Reset Best Score Button
                OutlinedButton(
                    onClick = {
                        soundManager.playButtonClick()
                        showResetDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, dangerColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "RESET BEST SCORE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = dangerColor
                    )
                }
            }
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = {
                    Text(
                        text = "Reset Scores?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = "This will permanently clear your best score and streak records. Continue?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            soundManager.playButtonClick()
                            onResetStats()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = dangerColor)
                    ) {
                        Text("RESET")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}
