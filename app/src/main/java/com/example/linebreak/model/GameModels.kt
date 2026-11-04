package com.example.linebreak.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class PrecisionTier(val label: String, val bonusPoints: Int) {
    PERFECT("PERFECT!", 100),
    GREAT("GREAT", 50),
    GOOD("GOOD", 25)
}

enum class GameOverReason(val displayMessage: String) {
    MISSED_WEAK_POINT("Missed the weak point!"),
    DANGER_ZONE_BREACH("Line reached the danger line!")
}

data class LineItem(
    val id: Long,
    val y: Float, // Current Y center of the line
    val angleDegrees: Float, // Tilt in degrees (-35 to +35)
    val weakPointNormalized: Float, // Position along line segment [0.15, 0.85]
    val weakPointRadiusDp: Float, // Size of weak point
    val speed: Float, // Pixels per second
    val isBroken: Boolean = false,
    val breakProgress: Float = 0f, // 0f to 1f for split animation
    val breakTapPoint: Offset? = null,
    val breakPrecision: PrecisionTier? = null
)

data class Particle(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val alpha: Float = 1f,
    val size: Float = 4f,
    val life: Float = 0f,
    val maxLife: Float = 0.45f
)

data class ScorePop(
    val id: Long,
    val text: String,
    val subText: String? = null,
    val x: Float,
    val y: Float,
    val alpha: Float = 1f,
    val scale: Float = 1f,
    val color: Color,
    val life: Float = 0f,
    val maxLife: Float = 0.8f
)

data class TapMissRipple(
    val x: Float,
    val y: Float,
    val radius: Float = 0f,
    val maxRadius: Float = 60f,
    val alpha: Float = 1f,
    val life: Float = 0f,
    val maxLife: Float = 0.35f
)

data class DifficultyConfig(
    val speedMin: Float,
    val speedMax: Float,
    val weakPointRadiusDp: Float,
    val maxAngleDegrees: Float,
    val spawnIntervalSeconds: Float,
    val tierName: String
)

data class HitResult(
    val isHit: Boolean,
    val hitLineId: Long? = null,
    val distance: Float = 0f,
    val precision: PrecisionTier? = null,
    val weakPointCenter: Offset = Offset.Zero
)

data class BreakResult(
    val scoreAwarded: Int,
    val precision: PrecisionTier,
    val streak: Int,
    val streakMultiplier: Float,
    val tapPoint: Offset
)

data class GameState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val gameOverReason: GameOverReason? = null,
    val score: Int = 0,
    val streak: Int = 0,
    val highestStreak: Int = 0,
    val linesBroken: Int = 0,
    val perfectBreaks: Int = 0,
    val activeLines: List<LineItem> = emptyList(),
    val particles: List<Particle> = emptyList(),
    val scorePops: List<ScorePop> = emptyList(),
    val missRipples: List<TapMissRipple> = emptyList(),
    val shakeIntensity: Float = 0f,
    val dangerLineFlash: Float = 0f, // 0f to 1f
    val lastBreakResult: BreakResult? = null,
    val playfieldWidth: Float = 0f,
    val playfieldHeight: Float = 0f,
    val isNewBestScore: Boolean = false
)
