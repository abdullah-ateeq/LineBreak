package com.example.linebreak.engine

import androidx.compose.ui.geometry.Offset
import com.example.linebreak.model.DifficultyConfig
import com.example.linebreak.model.HitResult
import com.example.linebreak.model.LineItem
import com.example.linebreak.model.PrecisionTier
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.tan
import kotlin.random.Random

object GamePhysics {

    const val BASE_SCORE = 100
    const val DEFAULT_HORIZONTAL_MARGIN_DP = 28f

    fun computeLineEndpoints(
        line: LineItem,
        canvasWidth: Float,
        horizontalMarginPx: Float
    ): Pair<Offset, Offset> {
        val leftX = horizontalMarginPx.coerceAtMost(canvasWidth * 0.25f)
        val rightX = (canvasWidth - horizontalMarginPx).coerceAtLeast(canvasWidth * 0.75f)
        val midX = (leftX + rightX) / 2f
        val rad = Math.toRadians(line.angleDegrees.toDouble()).toFloat()
        val tanAngle = tan(rad)
        val halfSpan = (rightX - leftX) / 2f
        val leftY = line.y - halfSpan * tanAngle
        val rightY = line.y + halfSpan * tanAngle
        return Pair(Offset(leftX, leftY), Offset(rightX, rightY))
    }

    fun computeWeakPointCenter(endpoints: Pair<Offset, Offset>, t: Float): Offset {
        val (start, end) = endpoints
        val x = start.x + (end.x - start.x) * t
        val y = start.y + (end.y - start.y) * t
        return Offset(x, y)
    }

    fun getDifficultyConfig(score: Int): DifficultyConfig {
        return when {
            score < 500 -> DifficultyConfig(
                speedMin = 200f,
                speedMax = 270f,
                weakPointRadiusDp = 18f,
                maxAngleDegrees = 12f,
                spawnIntervalSeconds = 2.4f,
                tierName = "EASY"
            )
            score < 1500 -> DifficultyConfig(
                speedMin = 280f,
                speedMax = 390f,
                weakPointRadiusDp = 15f,
                maxAngleDegrees = 22f,
                spawnIntervalSeconds = 1.7f,
                tierName = "MEDIUM"
            )
            else -> DifficultyConfig(
                speedMin = 400f,
                speedMax = 540f,
                weakPointRadiusDp = 12.5f,
                maxAngleDegrees = 32f,
                spawnIntervalSeconds = 1.25f,
                tierName = "HARD"
            )
        }
    }

    fun getStreakMultiplier(streak: Int): Float {
        return when {
            streak >= 15 -> 2.5f
            streak >= 10 -> 2.0f
            streak >= 6 -> 1.5f
            streak >= 3 -> 1.25f
            else -> 1.0f
        }
    }

    fun computePrecisionTier(distance: Float, hitRadius: Float): PrecisionTier {
        val ratio = (distance / hitRadius).coerceIn(0f, 1f)
        return when {
            ratio <= 0.35f -> PrecisionTier.PERFECT
            ratio <= 0.70f -> PrecisionTier.GREAT
            else -> PrecisionTier.GOOD
        }
    }

    fun computeScoreAwarded(
        precision: PrecisionTier,
        streak: Int,
        lineY: Float,
        dangerLineY: Float
    ): Int {
        val base = BASE_SCORE
        val precisionBonus = precision.bonusPoints
        val progress = if (dangerLineY > 0f) (lineY / dangerLineY).coerceIn(0f, 1f) else 0.5f
        val speedBonus = when {
            progress <= 0.40f -> 50
            progress <= 0.70f -> 25
            else -> 0
        }
        val multiplier = getStreakMultiplier(streak)
        return ((base + precisionBonus + speedBonus) * multiplier).toInt()
    }

    fun evaluateTapHit(
        tap: Offset,
        lines: List<LineItem>,
        canvasWidth: Float,
        density: Float,
        dangerLineY: Float
    ): HitResult {
        val marginPx = DEFAULT_HORIZONTAL_MARGIN_DP * density
        var closestHit: HitResult? = null
        var minDistance = Float.MAX_VALUE

        val activeCandidates = lines.filter { !it.isBroken && it.y > -50f && it.y <= dangerLineY + 60f }
        if (activeCandidates.isEmpty()) {
            return HitResult(isHit = false)
        }

        for (line in activeCandidates) {
            val endpoints = computeLineEndpoints(line, canvasWidth, marginPx)
            val wpCenter = computeWeakPointCenter(endpoints, line.weakPointNormalized)

            val dx = tap.x - wpCenter.x
            val dy = tap.y - wpCenter.y
            val dist = hypot(dx, dy)

            // Forgiving hit radius: at least 46dp, scaled with weak point radius
            val hitRadius = max(46f * density, line.weakPointRadiusDp * density * 2.2f)

            if (dist <= hitRadius && dist < minDistance) {
                minDistance = dist
                val precision = computePrecisionTier(dist, hitRadius)
                closestHit = HitResult(
                    isHit = true,
                    hitLineId = line.id,
                    distance = dist,
                    precision = precision,
                    weakPointCenter = wpCenter
                )
            }
        }

        return closestHit ?: HitResult(isHit = false)
    }

    fun generateLine(
        id: Long,
        difficulty: DifficultyConfig,
        random: Random = Random.Default,
        canvasWidth: Float = 1080f
    ): LineItem {
        val maxAngle = difficulty.maxAngleDegrees
        val angle = (random.nextFloat() * 2f - 1f) * maxAngle
        // Ensure weak point is kept away from the screen borders for comfortable tapping
        val t = 0.20f + random.nextFloat() * 0.60f
        val speed = difficulty.speedMin + random.nextFloat() * (difficulty.speedMax - difficulty.speedMin)

        return LineItem(
            id = id,
            y = -20f,
            angleDegrees = angle,
            weakPointNormalized = t,
            weakPointRadiusDp = difficulty.weakPointRadiusDp,
            speed = speed
        )
    }

    fun checkDangerLineBreach(
        lines: List<LineItem>,
        canvasWidth: Float,
        density: Float,
        dangerLineY: Float
    ): LineItem? {
        val marginPx = DEFAULT_HORIZONTAL_MARGIN_DP * density
        return lines.firstOrNull { line ->
            if (line.isBroken) return@firstOrNull false
            val (start, end) = computeLineEndpoints(line, canvasWidth, marginPx)
            val lowestY = max(start.y, end.y)
            lowestY >= dangerLineY
        }
    }
}
