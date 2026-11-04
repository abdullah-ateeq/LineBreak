package com.example.linebreak.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "linebreak_settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

data class UserPreferences(
    val bestScore: Int = 0,
    val highestStreak: Int = 0,
    val totalLinesBroken: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

class GamePreferences(private val context: Context) {

    private object PreferencesKeys {
        val BEST_SCORE = intPreferencesKey("best_score")
        val HIGHEST_STREAK = intPreferencesKey("highest_streak")
        val TOTAL_LINES_BROKEN = intPreferencesKey("total_lines_broken")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.gameDataStore.data.map { preferences ->
        val bestScore = preferences[PreferencesKeys.BEST_SCORE] ?: 0
        val highestStreak = preferences[PreferencesKeys.HIGHEST_STREAK] ?: 0
        val totalLinesBroken = preferences[PreferencesKeys.TOTAL_LINES_BROKEN] ?: 0
        val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        val soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true
        val hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true

        UserPreferences(
            bestScore = bestScore,
            highestStreak = highestStreak,
            totalLinesBroken = totalLinesBroken,
            themeMode = themeMode,
            soundEnabled = soundEnabled,
            hapticsEnabled = hapticsEnabled
        )
    }

    suspend fun saveGameResult(score: Int, streak: Int, brokenLines: Int): Boolean {
        var isNewBest = false
        context.gameDataStore.edit { preferences ->
            val currentBest = preferences[PreferencesKeys.BEST_SCORE] ?: 0
            if (score > currentBest) {
                preferences[PreferencesKeys.BEST_SCORE] = score
                isNewBest = true
            }

            val currentHighestStreak = preferences[PreferencesKeys.HIGHEST_STREAK] ?: 0
            if (streak > currentHighestStreak) {
                preferences[PreferencesKeys.HIGHEST_STREAK] = streak
            }

            val totalBroken = preferences[PreferencesKeys.TOTAL_LINES_BROKEN] ?: 0
            preferences[PreferencesKeys.TOTAL_LINES_BROKEN] = totalBroken + brokenLines
        }
        return isNewBest
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.gameDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.gameDataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.gameDataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun resetStats() {
        context.gameDataStore.edit { preferences ->
            preferences[PreferencesKeys.BEST_SCORE] = 0
            preferences[PreferencesKeys.HIGHEST_STREAK] = 0
            preferences[PreferencesKeys.TOTAL_LINES_BROKEN] = 0
        }
    }
}
