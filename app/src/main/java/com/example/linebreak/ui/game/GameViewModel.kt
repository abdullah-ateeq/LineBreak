package com.example.linebreak.ui.game

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.linebreak.data.GamePreferences
import com.example.linebreak.data.ThemeMode
import com.example.linebreak.data.UserPreferences
import com.example.linebreak.engine.GamePhysics
import com.example.linebreak.model.BreakResult
import com.example.linebreak.model.DifficultyConfig
import com.example.linebreak.model.GameOverReason
import com.example.linebreak.model.GameState
import com.example.linebreak.model.LineItem
import com.example.linebreak.model.Particle
import com.example.linebreak.model.PrecisionTier
import com.example.linebreak.model.ScorePop
import com.example.linebreak.model.TapMissRipple
import com.example.linebreak.sound.HapticManager
import com.example.linebreak.sound.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = GamePreferences(application)
    val soundManager = SoundManager()
    val hapticManager = HapticManager(application)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private var gameLoopJob: Job? = null
    private var nextLineId = 1L
    private var nextParticleId = 1L
    private var nextScorePopId = 1L
    private var timeSinceLastSpawn = 0f
    private var currentDensity = 2.5f

    init {
        viewModelScope.launch {
            preferences.userPreferencesFlow.collectLatest { prefs ->
                _userPreferences.value = prefs
                soundManager.soundEnabled = prefs.soundEnabled
                hapticManager.hapticsEnabled = prefs.hapticsEnabled
            }
        }
    }

    fun setPlayfieldDimensions(width: Float, height: Float, density: Float) {
        currentDensity = density
        _gameState.update {
            it.copy(playfieldWidth = width, playfieldHeight = height)
        }
    }

    fun startGame() {
        gameLoopJob?.cancel()
        nextLineId = 1L
        timeSinceLastSpawn = 0f

        val initialDifficulty = GamePhysics.getDifficultyConfig(0)
        val firstLine = GamePhysics.generateLine(
            id = nextLineId++,
            difficulty = initialDifficulty,
            canvasWidth = _gameState.value.playfieldWidth.takeIf { it > 0 } ?: 1080f
        ).copy(y = 40f * currentDensity) // Start comfortably in view near top

        _gameState.value = GameState(
            isPlaying = true,
            isPaused = false,
            isGameOver = false,
            gameOverReason = null,
            score = 0,
            streak = 0,
            highestStreak = 0,
            linesBroken = 0,
            perfectBreaks = 0,
            activeLines = listOf(firstLine),
            particles = emptyList(),
            scorePops = emptyList(),
            missRipples = emptyList(),
            playfieldWidth = _gameState.value.playfieldWidth,
            playfieldHeight = _gameState.value.playfieldHeight
        )

        startGameLoop()
    }

    fun pauseGame() {
        if (_gameState.value.isPlaying && !_gameState.value.isGameOver) {
            _gameState.update { it.copy(isPaused = true) }
        }
    }

    fun resumeGame() {
        if (_gameState.value.isPlaying && !_gameState.value.isGameOver) {
            _gameState.update { it.copy(isPaused = false) }
        }
    }

    fun onTapPlayfield(tapX: Float, tapY: Float) {
        val state = _gameState.value
        if (!state.isPlaying || state.isPaused || state.isGameOver) return

        val dangerLineY = getDangerLineY(state.playfieldHeight)
        val activeLines = state.activeLines

        // Only evaluate if lines are active on screen
        val linesOnScreen = activeLines.filter { !it.isBroken && it.y >= 0f }
        if (linesOnScreen.isEmpty()) return

        val hitResult = GamePhysics.evaluateTapHit(
            tap = Offset(tapX, tapY),
            lines = activeLines,
            canvasWidth = state.playfieldWidth,
            density = currentDensity,
            dangerLineY = dangerLineY
        )

        if (hitResult.isHit && hitResult.hitLineId != null) {
            val brokenLine = activeLines.find { it.id == hitResult.hitLineId } ?: return
            val precision = hitResult.precision ?: PrecisionTier.GOOD
            val newStreak = state.streak + 1
            val newHighestStreak = maxOf(state.highestStreak, newStreak)
            val scoreAwarded = GamePhysics.computeScoreAwarded(
                precision = precision,
                streak = newStreak,
                lineY = brokenLine.y,
                dangerLineY = dangerLineY
            )
            val newScore = state.score + scoreAwarded
            val newPerfectCount = if (precision == PrecisionTier.PERFECT) state.perfectBreaks + 1 else state.perfectBreaks

            // Sound and haptic feedback
            if (precision == PrecisionTier.PERFECT) {
                soundManager.playPerfect()
            } else {
                soundManager.playHit(newStreak)
            }
            hapticManager.playBreakPulse()

            // Spawn particle burst
            val burstParticles = createBurstParticles(hitResult.weakPointCenter)

            // Spawn score pop
            val scorePop = ScorePop(
                id = nextScorePopId++,
                text = "+$scoreAwarded",
                subText = if (precision == PrecisionTier.PERFECT) "PERFECT!" else if (precision == PrecisionTier.GREAT) "GREAT" else null,
                x = hitResult.weakPointCenter.x,
                y = hitResult.weakPointCenter.y - 20f * currentDensity,
                color = when (precision) {
                    PrecisionTier.PERFECT -> Color(0xFF00F5D4)
                    PrecisionTier.GREAT -> Color(0xFF70D6FF)
                    PrecisionTier.GOOD -> Color(0xFFFFD166)
                }
            )

            // Update line state to broken with split animation
            val updatedLines = activeLines.map { line ->
                if (line.id == brokenLine.id) {
                    line.copy(
                        isBroken = true,
                        breakProgress = 0f,
                        breakTapPoint = Offset(tapX, tapY),
                        breakPrecision = precision
                    )
                } else {
                    line
                }
            }

            _gameState.update {
                it.copy(
                    score = newScore,
                    streak = newStreak,
                    highestStreak = newHighestStreak,
                    linesBroken = it.linesBroken + 1,
                    perfectBreaks = newPerfectCount,
                    activeLines = updatedLines,
                    particles = it.particles + burstParticles,
                    scorePops = it.scorePops + scorePop,
                    lastBreakResult = BreakResult(
                        scoreAwarded = scoreAwarded,
                        precision = precision,
                        streak = newStreak,
                        streakMultiplier = GamePhysics.getStreakMultiplier(newStreak),
                        tapPoint = hitResult.weakPointCenter
                    )
                )
            }

            // If broken line was the only one, spawn next smoothly
            val remainingUnbroken = updatedLines.count { !it.isBroken }
            if (remainingUnbroken == 0) {
                val difficulty = GamePhysics.getDifficultyConfig(newScore)
                timeSinceLastSpawn = difficulty.spawnIntervalSeconds * 0.75f
            }
        } else {
            // Wrong tap! Missed the weak point -> Game Over
            triggerGameOver(GameOverReason.MISSED_WEAK_POINT, Offset(tapX, tapY))
        }
    }

    private fun triggerGameOver(reason: GameOverReason, tapMissOffset: Offset? = null) {
        val state = _gameState.value
        if (state.isGameOver) return

        if (reason == GameOverReason.MISSED_WEAK_POINT) {
            soundManager.playWrongTap()
            hapticManager.playHeavyWarning()
        } else {
            soundManager.playDangerLineBreach()
            hapticManager.playHeavyWarning()
        }

        val missRipples = if (tapMissOffset != null) {
            listOf(
                TapMissRipple(
                    x = tapMissOffset.x,
                    y = tapMissOffset.y,
                    maxRadius = 70f * currentDensity
                )
            )
        } else emptyList()

        _gameState.update {
            it.copy(
                isGameOver = true,
                gameOverReason = reason,
                shakeIntensity = 20f,
                dangerLineFlash = if (reason == GameOverReason.DANGER_ZONE_BREACH) 1f else 0.4f,
                missRipples = it.missRipples + missRipples
            )
        }

        // Persist game result locally
        viewModelScope.launch {
            val isNewBest = preferences.saveGameResult(
                score = state.score,
                streak = state.highestStreak,
                brokenLines = state.linesBroken
            )
            _gameState.update { it.copy(isNewBestScore = isNewBest) }
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                delay(16L) // ~60fps fixed step
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                updateGame(dt)
            }
        }
    }

    private fun updateGame(dt: Float) {
        val state = _gameState.value
        val dangerY = getDangerLineY(state.playfieldHeight)

        if (state.isGameOver || state.isPaused || !state.isPlaying) {
            // Only update decaying visual effects (particles, score pops, miss ripples)
            updateDecayingEffectsOnly(dt)
            return
        }

        // Check if danger line breached by any unbroken line
        val breachedLine = GamePhysics.checkDangerLineBreach(
            lines = state.activeLines,
            canvasWidth = state.playfieldWidth,
            density = currentDensity,
            dangerLineY = dangerY
        )

        if (breachedLine != null) {
            triggerGameOver(GameOverReason.DANGER_ZONE_BREACH)
            return
        }

        // Advance lines
        val updatedLines = state.activeLines.mapNotNull { line ->
            if (line.isBroken) {
                val newProgress = line.breakProgress + dt * 3.5f
                if (newProgress >= 1f) null // animation finished, remove line
                else line.copy(breakProgress = newProgress)
            } else {
                line.copy(y = line.y + line.speed * dt)
            }
        }

        // Spawning logic
        val difficulty = GamePhysics.getDifficultyConfig(state.score)
        timeSinceLastSpawn += dt
        val linesToRetain = updatedLines.toMutableList()

        if (timeSinceLastSpawn >= difficulty.spawnIntervalSeconds) {
            timeSinceLastSpawn = 0f
            val newLine = GamePhysics.generateLine(
                id = nextLineId++,
                difficulty = difficulty,
                canvasWidth = state.playfieldWidth.takeIf { it > 0 } ?: 1080f
            )
            linesToRetain.add(newLine)
        }

        // Update particles
        val updatedParticles = state.particles.mapNotNull { p ->
            val newLife = p.life + dt
            if (newLife >= p.maxLife) null
            else {
                val ratio = newLife / p.maxLife
                p.copy(
                    x = p.x + p.vx * dt,
                    y = p.y + p.vy * dt,
                    life = newLife,
                    alpha = 1f - ratio
                )
            }
        }

        // Update score pops
        val updatedScorePops = state.scorePops.mapNotNull { sp ->
            val newLife = sp.life + dt
            if (newLife >= sp.maxLife) null
            else {
                val ratio = newLife / sp.maxLife
                sp.copy(
                    y = sp.y - 45f * currentDensity * dt,
                    life = newLife,
                    alpha = 1f - ratio,
                    scale = 1f + 0.3f * (1f - ratio)
                )
            }
        }

        // Update miss ripples
        val updatedMissRipples = state.missRipples.mapNotNull { r ->
            val newLife = r.life + dt
            if (newLife >= r.maxLife) null
            else {
                val ratio = newLife / r.maxLife
                r.copy(
                    radius = r.maxRadius * ratio,
                    life = newLife,
                    alpha = 1f - ratio
                )
            }
        }

        // Decay screen shake & flash
        val newShake = maxOf(0f, state.shakeIntensity - 40f * dt)
        val newFlash = maxOf(0f, state.dangerLineFlash - 3f * dt)

        _gameState.update {
            it.copy(
                activeLines = linesToRetain,
                particles = updatedParticles,
                scorePops = updatedScorePops,
                missRipples = updatedMissRipples,
                shakeIntensity = newShake,
                dangerLineFlash = newFlash
            )
        }
    }

    private fun updateDecayingEffectsOnly(dt: Float) {
        _gameState.update { state ->
            val updatedParticles = state.particles.mapNotNull { p ->
                val newLife = p.life + dt
                if (newLife >= p.maxLife) null
                else p.copy(
                    x = p.x + p.vx * dt,
                    y = p.y + p.vy * dt,
                    life = newLife,
                    alpha = 1f - (newLife / p.maxLife)
                )
            }
            val updatedScorePops = state.scorePops.mapNotNull { sp ->
                val newLife = sp.life + dt
                if (newLife >= sp.maxLife) null
                else sp.copy(
                    y = sp.y - 30f * currentDensity * dt,
                    life = newLife,
                    alpha = 1f - (newLife / sp.maxLife)
                )
            }
            val updatedMissRipples = state.missRipples.mapNotNull { r ->
                val newLife = r.life + dt
                if (newLife >= r.maxLife) null
                else r.copy(
                    radius = r.maxRadius * (newLife / r.maxLife),
                    life = newLife,
                    alpha = 1f - (newLife / r.maxLife)
                )
            }
            val newShake = maxOf(0f, state.shakeIntensity - 35f * dt)
            val newFlash = maxOf(0f, state.dangerLineFlash - 2.5f * dt)

            state.copy(
                particles = updatedParticles,
                scorePops = updatedScorePops,
                missRipples = updatedMissRipples,
                shakeIntensity = newShake,
                dangerLineFlash = newFlash
            )
        }
    }

    private fun createBurstParticles(center: Offset): List<Particle> {
        val particles = mutableListOf<Particle>()
        val count = 16
        val palette = listOf(
            Color(0xFF00F5D4), // Cyan
            Color(0xFF70D6FF), // Sky
            Color(0xFFFFD166), // Gold
            Color(0xFFFFFFFF)  // White
        )

        for (i in 0 until count) {
            val angle = (i.toFloat() / count) * 2f * Math.PI.toFloat() + (Random.nextFloat() * 0.4f - 0.2f)
            val speed = (180f + Random.nextFloat() * 260f) * currentDensity
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed
            val color = palette[Random.nextInt(palette.size)]
            val size = (3f + Random.nextFloat() * 3.5f) * currentDensity

            particles.add(
                Particle(
                    id = nextParticleId++,
                    x = center.x,
                    y = center.y,
                    vx = vx,
                    vy = vy,
                    color = color,
                    size = size,
                    maxLife = 0.35f + Random.nextFloat() * 0.2f
                )
            )
        }
        return particles
    }

    fun getDangerLineY(height: Float): Float {
        // Danger line is located near bottom (e.g. 110dp above bottom)
        return if (height > 0) height - (110f * currentDensity) else 1600f
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setSoundEnabled(enabled)
        }
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setHapticsEnabled(enabled)
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            preferences.resetStats()
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
