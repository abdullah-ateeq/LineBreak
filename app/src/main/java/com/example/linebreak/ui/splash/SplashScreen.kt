package com.example.linebreak.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val lineProgress = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(0.8f) }
    val alphaAnim = remember { Animatable(0f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val lineStrokeColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    LaunchedEffect(Unit) {
        launch {
            lineProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500)
            )
        }
        launch {
            delay(300)
            pulseScale.animateTo(
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
        delay(1400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(width = 220.dp, height = 50.dp)) {
                val startX = 0f
                val endX = size.width * lineProgress.value
                val centerY = size.height / 2f

                // Incoming line
                drawLine(
                    color = lineStrokeColor,
                    start = Offset(startX, centerY),
                    end = Offset(endX, centerY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Weak point in the middle
                if (lineProgress.value >= 0.5f) {
                    val midX = size.width / 2f
                    // Outer pulsing ring
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.35f),
                        radius = 16.dp.toPx() * pulseScale.value,
                        center = Offset(midX, centerY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Core point
                    drawCircle(
                        color = primaryColor,
                        radius = 6.dp.toPx(),
                        center = Offset(midX, centerY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "LINEBREAK",
                style = MaterialTheme.typography.displayMedium.copy(
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "PRECISION REFLEX ARCADE",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
