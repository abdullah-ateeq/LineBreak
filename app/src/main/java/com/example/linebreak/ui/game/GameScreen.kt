package com.example.linebreak.ui.game

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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linebreak.engine.GamePhysics
import com.example.linebreak.model.LineItem
import com.example.linebreak.model.PrecisionTier
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameOver: () -> Unit,
    onQuitToHome: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val density = LocalDensity.current.density
    var hasNavigatedToResult by remember { mutableStateOf(false) }

    // Start game on launch
    LaunchedEffect(Unit) {
        hasNavigatedToResult = false
        viewModel.startGame()
    }

    // Handle game over navigation with short delay for crash feedback
    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver && !hasNavigatedToResult) {
            delay(850)
            hasNavigatedToResult = true
            onGameOver()
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val dangerColor = MaterialTheme.colorScheme.error
    val lineStrokeColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    val backgroundColor = MaterialTheme.colorScheme.background

    // Pulse animation for weak points
    val infiniteTransition = rememberInfiniteTransition(label = "game_pulses")
    val wpPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wp_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .onGloballyPositioned { coordinates ->
                val w = coordinates.size.width.toFloat()
                val h = coordinates.size.height.toFloat()
                if (w > 0 && h > 0) {
                    viewModel.setPlayfieldDimensions(w, h, density)
                }
            }
            .pointerInput(gameState.isPlaying, gameState.isPaused, gameState.isGameOver) {
                detectTapGestures { offset ->
                    viewModel.onTapPlayfield(offset.x, offset.y)
                }
            }
    ) {
        // Main Compose Canvas Playfield
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val dangerY = viewModel.getDangerLineY(canvasH)

            // Screen shake translation
            val shake = gameState.shakeIntensity
            val shakeX = if (shake > 0) sin(System.currentTimeMillis() * 0.05f) * shake else 0f
            val shakeY = if (shake > 0) cos(System.currentTimeMillis() * 0.06f) * shake else 0f

            translate(left = shakeX, top = shakeY) {
                // Background subtle grid/accent line
                drawDangerLine(
                    dangerY = dangerY,
                    canvasW = canvasW,
                    dangerColor = dangerColor,
                    flashAmount = gameState.dangerLineFlash,
                    density = density
                )

                // Draw lines and weak points
                for (line in gameState.activeLines) {
                    drawLineWithWeakPoint(
                        line = line,
                        canvasW = canvasW,
                        dangerY = dangerY,
                        density = density,
                        lineColor = lineStrokeColor,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        pulseScale = wpPulse
                    )
                }

                // Draw Particles
                for (particle in gameState.particles) {
                    drawCircle(
                        color = particle.color.copy(alpha = particle.alpha),
                        radius = particle.size,
                        center = Offset(particle.x, particle.y)
                    )
                }

                // Draw Miss Ripples (wrong tap location)
                for (ripple in gameState.missRipples) {
                    drawCircle(
                        color = dangerColor.copy(alpha = ripple.alpha),
                        radius = ripple.radius,
                        center = Offset(ripple.x, ripple.y),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = dangerColor.copy(alpha = ripple.alpha * 0.4f),
                        radius = ripple.radius * 0.5f,
                        center = Offset(ripple.x, ripple.y)
                    )
                }

                // Draw Score Pops
                for (pop in gameState.scorePops) {
                    val paint = android.graphics.Paint().apply {
                        color = pop.color.copy(alpha = pop.alpha).hashCode()
                        textSize = (22f * pop.scale * density)
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        setShadowLayer(8f * density, 0f, 0f, pop.color.copy(alpha = 0.5f * pop.alpha).hashCode())
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        pop.text,
                        pop.x,
                        pop.y,
                        paint
                    )
                    if (pop.subText != null) {
                        val subPaint = android.graphics.Paint().apply {
                            color = pop.color.copy(alpha = pop.alpha * 0.9f).hashCode()
                            textSize = (13f * pop.scale * density)
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            pop.subText,
                            pop.x,
                            pop.y - 22f * density,
                            subPaint
                        )
                    }
                }
            }
        }

        // HUD Overlay
        HUDOverlay(
            gameState = gameState,
            onPauseClick = {
                viewModel.soundManager.playButtonClick()
                viewModel.pauseGame()
            }
        )

        // Pause Modal
        if (gameState.isPaused) {
            PauseDialog(
                onResume = {
                    viewModel.soundManager.playButtonClick()
                    viewModel.resumeGame()
                },
                onRestart = {
                    viewModel.soundManager.playButtonClick()
                    viewModel.startGame()
                },
                onQuit = {
                    viewModel.soundManager.playButtonClick()
                    onQuitToHome()
                }
            )
        }
    }
}

private fun DrawScope.drawDangerLine(
    dangerY: Float,
    canvasW: Float,
    dangerColor: Color,
    flashAmount: Float,
    density: Float
) {
    val flashColor = if (flashAmount > 0.05f) {
        Color.White.copy(alpha = flashAmount.coerceIn(0f, 0.85f))
    } else null

    // Subtle danger glow zone below danger line
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                dangerColor.copy(alpha = 0.12f + flashAmount * 0.3f),
                dangerColor.copy(alpha = 0.02f)
            ),
            startY = dangerY,
            endY = dangerY + 80.dp.toPx()
        ),
        topLeft = Offset(0f, dangerY),
        size = androidx.compose.ui.geometry.Size(canvasW, 80.dp.toPx())
    )

    // Danger Line itself
    drawLine(
        color = flashColor ?: dangerColor.copy(alpha = 0.85f),
        start = Offset(0f, dangerY),
        end = Offset(canvasW, dangerY),
        strokeWidth = (2.5f + flashAmount * 3f) * density
    )

    // Tick markers along danger line
    val tickStep = 24.dp.toPx()
    var currentX = 12.dp.toPx()
    while (currentX < canvasW) {
        drawLine(
            color = dangerColor.copy(alpha = 0.45f),
            start = Offset(currentX, dangerY),
            end = Offset(currentX + 8.dp.toPx(), dangerY + 6.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
        currentX += tickStep
    }
}

private fun DrawScope.drawLineWithWeakPoint(
    line: LineItem,
    canvasW: Float,
    dangerY: Float,
    density: Float,
    lineColor: Color,
    primaryColor: Color,
    secondaryColor: Color,
    pulseScale: Float
) {
    val marginPx = GamePhysics.DEFAULT_HORIZONTAL_MARGIN_DP * density
    val (left, right) = GamePhysics.computeLineEndpoints(line, canvasW, marginPx)
    val wp = GamePhysics.computeWeakPointCenter(Pair(left, right), line.weakPointNormalized)

    if (line.isBroken) {
        // Line break animation: the line splits in two halves rotating and translating outward
        val progress = line.breakProgress.coerceIn(0f, 1f)
        val alpha = (1f - progress).coerceIn(0f, 1f)
        val splitDist = 45f * density * progress
        val splitAngle = 22f * progress

        // Left segment
        rotate(degrees = -splitAngle, pivot = wp) {
            translate(left = -splitDist, top = splitDist * 0.5f) {
                drawLine(
                    color = primaryColor.copy(alpha = alpha),
                    start = left,
                    end = wp,
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Right segment
        rotate(degrees = splitAngle, pivot = wp) {
            translate(left = splitDist, top = splitDist * 0.5f) {
                drawLine(
                    color = primaryColor.copy(alpha = alpha),
                    start = wp,
                    end = right,
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        return
    }

    // Normal incoming line
    // Gap size around the weak point to make the weak point look segmented & distinct
    val wpRadiusPx = line.weakPointRadiusDp * density
    val gapFraction = (wpRadiusPx * 1.25f) / (right.x - left.x)
    val t = line.weakPointNormalized

    val gapLeftT = (t - gapFraction).coerceAtLeast(0f)
    val gapRightT = (t + gapFraction).coerceAtMost(1f)

    val segment1End = GamePhysics.computeWeakPointCenter(Pair(left, right), gapLeftT)
    val segment2Start = GamePhysics.computeWeakPointCenter(Pair(left, right), gapRightT)

    // Draw Left segment
    drawLine(
        color = lineColor,
        start = left,
        end = segment1End,
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Draw Right segment
    drawLine(
        color = lineColor,
        start = segment2Start,
        end = right,
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Weak Point Indicator: Diamond + Outer pulsing ring + bright center
    val outerRadius = wpRadiusPx * pulseScale

    // Pulsing outer ring
    drawCircle(
        color = primaryColor.copy(alpha = 0.35f),
        radius = outerRadius,
        center = wp,
        style = Stroke(width = 2.dp.toPx())
    )

    // Diamond marker
    val diamondSize = wpRadiusPx * 0.85f
    val diamondPath = Path().apply {
        moveTo(wp.x, wp.y - diamondSize)
        lineTo(wp.x + diamondSize, wp.y)
        lineTo(wp.x, wp.y + diamondSize)
        lineTo(wp.x - diamondSize, wp.y)
        close()
    }
    drawPath(
        path = diamondPath,
        color = primaryColor.copy(alpha = 0.85f)
    )

    // Inner bright core
    drawCircle(
        color = Color.White,
        radius = wpRadiusPx * 0.35f,
        center = wp
    )
}

@Composable
private fun HUDOverlay(
    gameState: com.example.linebreak.model.GameState,
    onPauseClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val strokeColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 36.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pause Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(surfaceColor.copy(alpha = 0.85f))
                    .border(1.dp, strokeColor, CircleShape)
                    .clickable { onPauseClick() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(14.dp)) {
                    // Double pause bars
                    val barWidth = 3.5.dp.toPx()
                    val barHeight = size.height
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(size.width - barWidth, 0f),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )
                }
            }

            // Central Score Display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${gameState.score}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${gameState.linesBroken} LINES",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Streak Badge
            val streakMultiplier = GamePhysics.getStreakMultiplier(gameState.streak)
            val badgeColor = if (gameState.streak >= 10) MaterialTheme.colorScheme.tertiary else primaryColor

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor.copy(alpha = 0.85f))
                    .border(1.dp, badgeColor.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${streakMultiplier}x",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        ),
                        color = badgeColor
                    )
                    Text(
                        text = "STREAK ${gameState.streak}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PAUSED",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "RESUME",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "RESTART",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onQuit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "QUIT TO HOME",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                )
            }
        }
    }
}
