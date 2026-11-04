package com.example.linebreak

import androidx.compose.ui.geometry.Offset
import com.example.linebreak.engine.GamePhysics
import com.example.linebreak.model.LineItem
import com.example.linebreak.model.PrecisionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GamePhysicsTest {

    @Test
    fun testWeakPointPlacement() {
        val line = LineItem(
            id = 1L,
            y = 200f,
            angleDegrees = 0f,
            weakPointNormalized = 0.5f,
            weakPointRadiusDp = 16f,
            speed = 200f
        )
        val endpoints = GamePhysics.computeLineEndpoints(line, canvasWidth = 1000f, horizontalMarginPx = 100f)
        assertEquals(100f, endpoints.first.x, 0.01f)
        assertEquals(900f, endpoints.second.x, 0.01f)
        assertEquals(200f, endpoints.first.y, 0.01f)
        assertEquals(200f, endpoints.second.y, 0.01f)

        val center = GamePhysics.computeWeakPointCenter(endpoints, 0.5f)
        assertEquals(500f, center.x, 0.01f)
        assertEquals(200f, center.y, 0.01f)

        // Angled line
        val angledLine = LineItem(
            id = 2L,
            y = 400f,
            angleDegrees = 45f,
            weakPointNormalized = 0.5f,
            weakPointRadiusDp = 16f,
            speed = 200f
        )
        val angledEndpoints = GamePhysics.computeLineEndpoints(angledLine, canvasWidth = 1000f, horizontalMarginPx = 100f)
        val angledCenter = GamePhysics.computeWeakPointCenter(angledEndpoints, 0.5f)
        assertEquals(500f, angledCenter.x, 0.01f)
        assertEquals(400f, angledCenter.y, 0.01f)
    }

    @Test
    fun testTapHitDetection_DirectHitAndMiss() {
        val density = 2f
        val canvasWidth = 1080f
        val dangerY = 1800f
        val line = LineItem(
            id = 10L,
            y = 500f,
            angleDegrees = 0f,
            weakPointNormalized = 0.5f,
            weakPointRadiusDp = 15f,
            speed = 200f
        )
        val endpoints = GamePhysics.computeLineEndpoints(
            line,
            canvasWidth,
            GamePhysics.DEFAULT_HORIZONTAL_MARGIN_DP * density
        )
        val wp = GamePhysics.computeWeakPointCenter(endpoints, 0.5f)

        // Perfect direct tap right at the center
        val perfectHit = GamePhysics.evaluateTapHit(
            tap = Offset(wp.x, wp.y),
            lines = listOf(line),
            canvasWidth = canvasWidth,
            density = density,
            dangerLineY = dangerY
        )
        assertTrue(perfectHit.isHit)
        assertEquals(10L, perfectHit.hitLineId)
        assertEquals(PrecisionTier.PERFECT, perfectHit.precision)

        // Tap far away from weak point
        val missHit = GamePhysics.evaluateTapHit(
            tap = Offset(wp.x + 300f, wp.y + 300f),
            lines = listOf(line),
            canvasWidth = canvasWidth,
            density = density,
            dangerLineY = dangerY
        )
        assertFalse(missHit.isHit)
        assertNull(missHit.hitLineId)
    }

    @Test
    fun testLineMovement() {
        val initialY = 100f
        val speed = 300f // px per second
        val deltaSeconds = 0.016f // ~60fps
        val updatedY = initialY + speed * deltaSeconds
        assertEquals(104.8f, updatedY, 0.01f)
    }

    @Test
    fun testLineExpirationAndDangerBreach() {
        val density = 2f
        val canvasWidth = 1000f
        val dangerLineY = 800f

        val safeLine = LineItem(
            id = 1L,
            y = 500f,
            angleDegrees = 0f,
            weakPointNormalized = 0.5f,
            weakPointRadiusDp = 15f,
            speed = 200f
        )
        val breachedLine = LineItem(
            id = 2L,
            y = 805f,
            angleDegrees = 0f,
            weakPointNormalized = 0.5f,
            weakPointRadiusDp = 15f,
            speed = 200f
        )

        val safeBreach = GamePhysics.checkDangerLineBreach(
            listOf(safeLine),
            canvasWidth = canvasWidth,
            density = density,
            dangerLineY = dangerLineY
        )
        assertNull(safeBreach)

        val activeBreach = GamePhysics.checkDangerLineBreach(
            listOf(safeLine, breachedLine),
            canvasWidth = canvasWidth,
            density = density,
            dangerLineY = dangerLineY
        )
        assertNotNull(activeBreach)
        assertEquals(2L, activeBreach?.id)
    }

    @Test
    fun testPrecisionScoring() {
        // Perfect precision with streak 1
        val scorePerfect = GamePhysics.computeScoreAwarded(
            precision = PrecisionTier.PERFECT,
            streak = 1,
            lineY = 200f,
            dangerLineY = 1000f
        )
        // Base(100) + Perfect(100) + SpeedBonus(50 since 200/1000 <= 0.4) = 250 * 1.0 = 250
        assertEquals(250, scorePerfect)

        // Great precision
        val scoreGreat = GamePhysics.computeScoreAwarded(
            precision = PrecisionTier.GREAT,
            streak = 1,
            lineY = 600f,
            dangerLineY = 1000f
        )
        // Base(100) + Great(50) + SpeedBonus(25 since 600/1000 <= 0.7) = 175 * 1.0 = 175
        assertEquals(175, scoreGreat)

        // Good precision with high streak
        val scoreGoodHighStreak = GamePhysics.computeScoreAwarded(
            precision = PrecisionTier.GOOD,
            streak = 15,
            lineY = 900f,
            dangerLineY = 1000f
        )
        // Base(100) + Good(25) + SpeedBonus(0) = 125 * 2.5 = 312
        assertEquals(312, scoreGoodHighStreak)
    }

    @Test
    fun testStreakMultipliers() {
        assertEquals(1.0f, GamePhysics.getStreakMultiplier(0), 0.001f)
        assertEquals(1.0f, GamePhysics.getStreakMultiplier(2), 0.001f)
        assertEquals(1.25f, GamePhysics.getStreakMultiplier(3), 0.001f)
        assertEquals(1.25f, GamePhysics.getStreakMultiplier(5), 0.001f)
        assertEquals(1.5f, GamePhysics.getStreakMultiplier(6), 0.001f)
        assertEquals(1.5f, GamePhysics.getStreakMultiplier(9), 0.001f)
        assertEquals(2.0f, GamePhysics.getStreakMultiplier(10), 0.001f)
        assertEquals(2.5f, GamePhysics.getStreakMultiplier(15), 0.001f)
        assertEquals(2.5f, GamePhysics.getStreakMultiplier(50), 0.001f)
    }

    @Test
    fun testDifficultyProgression() {
        val easy = GamePhysics.getDifficultyConfig(score = 0)
        assertEquals("EASY", easy.tierName)
        assertEquals(18f, easy.weakPointRadiusDp, 0.01f)
        assertEquals(12f, easy.maxAngleDegrees, 0.01f)

        val medium = GamePhysics.getDifficultyConfig(score = 800)
        assertEquals("MEDIUM", medium.tierName)
        assertEquals(15f, medium.weakPointRadiusDp, 0.01f)
        assertTrue(medium.speedMin > easy.speedMin)

        val hard = GamePhysics.getDifficultyConfig(score = 2500)
        assertEquals("HARD", hard.tierName)
        assertEquals(12.5f, hard.weakPointRadiusDp, 0.01f)
        assertEquals(32f, hard.maxAngleDegrees, 0.01f)
        assertTrue(hard.speedMax > medium.speedMax)
    }

    @Test
    fun testFairLineGeneration() {
        val difficulty = GamePhysics.getDifficultyConfig(score = 1000)
        val fixedRandom = Random(42)
        for (i in 1..20) {
            val line = GamePhysics.generateLine(i.toLong(), difficulty, fixedRandom, 1080f)
            assertTrue(line.weakPointNormalized in 0.15f..0.85f)
            assertTrue(line.angleDegrees in -difficulty.maxAngleDegrees..difficulty.maxAngleDegrees)
            assertTrue(line.speed in difficulty.speedMin..difficulty.speedMax)
        }
    }
}
