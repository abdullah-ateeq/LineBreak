package com.example.linebreak.ui.about

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linebreak.sound.SoundManager

@Composable
fun AboutScreen(
    soundManager: SoundManager,
    onBackClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val strokeColor = MaterialTheme.colorScheme.outline

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
                        Canvas(modifier = Modifier.size(16.dp)) {
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
                        text = "ABOUT",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Geometric Logo Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor, RoundedCornerShape(20.dp))
                        .border(1.dp, strokeColor, RoundedCornerShape(20.dp))
                        .padding(vertical = 32.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(width = 140.dp, height = 40.dp)) {
                            val cy = size.height / 2f
                            drawLine(
                                color = primaryColor.copy(alpha = 0.5f),
                                start = Offset(0f, cy),
                                end = Offset(size.width, cy),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.3f),
                                radius = 16.dp.toPx(),
                                center = Offset(size.width / 2f, cy),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 6.dp.toPx(),
                                center = Offset(size.width / 2f, cy)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "LINEBREAK",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 6.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "VERSION 1.0.0",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 11.sp,
                                letterSpacing = 2.sp
                            ),
                            color = primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor, RoundedCornerShape(18.dp))
                        .border(1.dp, strokeColor, RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "CONCEPT",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "LineBreak is a pure precision and reflex arcade game. Incoming lines travel downward with concealed weak points. Tap the exact weak point to fracture the line and build your combo streak before it breaches the danger threshold.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Credits Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor, RoundedCornerShape(18.dp))
                        .border(1.dp, strokeColor, RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "ENGINE & ARCHITECTURE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Built with Kotlin, Jetpack Compose, Material 3, and Compose Canvas. Completely offline with zero trackers, ads, or external dependencies.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PRECISION • TIMING • REFLEX",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
