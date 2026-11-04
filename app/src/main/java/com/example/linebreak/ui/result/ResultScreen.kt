package com.example.linebreak.ui.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linebreak.model.GameState
import com.example.linebreak.sound.SoundManager

@Composable
fun ResultScreen(
    gameState: GameState,
    bestScore: Int,
    soundManager: SoundManager,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val strokeColor = MaterialTheme.colorScheme.outline
    val dangerColor = MaterialTheme.colorScheme.error

    val isNewBest = gameState.isNewBestScore || (gameState.score > 0 && gameState.score >= bestScore)

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
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Game Over Reason
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "RUN ENDED",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = gameState.gameOverReason?.displayMessage ?: "Line breached!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = dangerColor,
                    textAlign = TextAlign.Center
                )
            }

            // Score Banner & Stats Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isNewBest) {
                    Box(
                        modifier = Modifier
                            .background(primaryColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                            .border(1.dp, primaryColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "★ NEW ALL-TIME BEST! ★",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = primaryColor
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Text(
                    text = "${gameState.score}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 54.sp
                    ),
                    color = if (isNewBest) primaryColor else MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "FINAL SCORE",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 2x2 Stats Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor, RoundedCornerShape(16.dp))
                        .border(1.dp, strokeColor, RoundedCornerShape(16.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "LINES BROKEN",
                            value = "${gameState.linesBroken}",
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                        StatItem(
                            label = "HIGHEST STREAK",
                            value = "${gameState.highestStreak}",
                            valueColor = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(strokeColor)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "PERFECT BREAKS",
                            value = "${gameState.perfectBreaks}",
                            valueColor = primaryColor
                        )
                        StatItem(
                            label = "LOCAL BEST",
                            value = "${maxOf(bestScore, gameState.score)}",
                            valueColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Buttons: Play Again & Home
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        soundManager.playButtonClick()
                        onPlayAgain()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "PLAY AGAIN",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        soundManager.playButtonClick()
                        onHome()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor)
                ) {
                    Text(
                        text = "HOME",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 10.sp,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = valueColor
        )
    }
}
