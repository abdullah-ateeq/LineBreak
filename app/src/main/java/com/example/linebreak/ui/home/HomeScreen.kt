package com.example.linebreak.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linebreak.data.UserPreferences
import com.example.linebreak.sound.SoundManager

@Composable
fun HomeScreen(
    preferences: UserPreferences,
    soundManager: SoundManager,
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val strokeColor = MaterialTheme.colorScheme.outline
    val lineBaseColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    // Animated preview loop
    val infiniteTransition = rememberInfiniteTransition(label = "preview_anim")
    val previewProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "preview_y"
    )

    val weakPointPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

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
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Settings and About
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundManager.playButtonClick()
                        onSettingsClick()
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(surfaceColor)
                        .border(1.dp, strokeColor, CircleShape)
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        // Minimalist gear / settings icon
                        drawCircle(
                            color = primaryColor,
                            radius = size.minDimension / 4f,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        for (i in 0 until 6) {
                            val angle = (i * 60f) * (Math.PI / 180f).toFloat()
                            val cx = size.width / 2f + kotlin.math.cos(angle) * (size.minDimension / 2.5f)
                            val cy = size.height / 2f + kotlin.math.sin(angle) * (size.minDimension / 2.5f)
                            drawCircle(color = primaryColor, radius = 2.dp.toPx(), center = Offset(cx, cy))
                        }
                    }
                }

                IconButton(
                    onClick = {
                        soundManager.playButtonClick()
                        onAboutClick()
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(surfaceColor)
                        .border(1.dp, strokeColor, CircleShape)
                ) {
                    Text(
                        text = "i",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = primaryColor
                    )
                }
            }

            // Central Area: Title, Animated Preview, Instruction
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "LINEBREAK",
                    style = MaterialTheme.typography.displayLarge.copy(
                        letterSpacing = 8.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(3.dp)
                        .background(primaryColor, RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Minimal animated line/weak-point preview canvas
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(170.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor.copy(alpha = 0.7f))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val y = previewProgress * (canvasH - 36.dp.toPx()) + 18.dp.toPx()

                        val left = Offset(24.dp.toPx(), y - 10.dp.toPx())
                        val right = Offset(canvasW - 24.dp.toPx(), y + 10.dp.toPx())

                        // Line
                        drawLine(
                            color = lineBaseColor,
                            start = left,
                            end = right,
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Weak point at 0.52
                        val wpX = left.x + (right.x - left.x) * 0.52f
                        val wpY = left.y + (right.y - left.y) * 0.52f
                        val wpCenter = Offset(wpX, wpY)

                        // Outer pulsing glow ring
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.35f),
                            radius = 14.dp.toPx() * weakPointPulse,
                            center = wpCenter,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Center diamond/dot
                        drawCircle(
                            color = primaryColor,
                            radius = 5.5.dp.toPx(),
                            center = wpCenter
                        )

                        // Danger threshold preview line at bottom
                        drawLine(
                            color = Color(0xFFFF3366).copy(alpha = 0.5f),
                            start = Offset(0f, canvasH - 12.dp.toPx()),
                            end = Offset(canvasW, canvasH - 12.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Break the weak point before the line reaches you.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Bottom Area: Stats and Large Play Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Best Stats Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(surfaceColor, RoundedCornerShape(12.dp))
                        .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
                        .padding(vertical = 12.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BEST SCORE",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp, letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${preferences.bestScore}",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = primaryColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(strokeColor)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BEST STREAK",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp, letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${preferences.highestStreak}",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Large Play Button
                Button(
                    onClick = {
                        soundManager.playButtonClick()
                        onPlayClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "PLAY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
