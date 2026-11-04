package com.example.linebreak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.linebreak.ui.about.AboutScreen
import com.example.linebreak.ui.game.GameScreen
import com.example.linebreak.ui.game.GameViewModel
import com.example.linebreak.ui.home.HomeScreen
import com.example.linebreak.ui.navigation.Screen
import com.example.linebreak.ui.result.ResultScreen
import com.example.linebreak.ui.settings.SettingsScreen
import com.example.linebreak.ui.splash.SplashScreen
import com.example.linebreak.ui.theme.LineBreakTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gameViewModel: GameViewModel = viewModel()
            val preferences by gameViewModel.userPreferences.collectAsState()
            val gameState by gameViewModel.gameState.collectAsState()

            LineBreakTheme(themeMode = preferences.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                onSplashFinished = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(
                                preferences = preferences,
                                soundManager = gameViewModel.soundManager,
                                onPlayClick = {
                                    navController.navigate(Screen.Game.route)
                                },
                                onSettingsClick = {
                                    navController.navigate(Screen.Settings.route)
                                },
                                onAboutClick = {
                                    navController.navigate(Screen.About.route)
                                }
                            )
                        }

                        composable(Screen.Game.route) {
                            GameScreen(
                                viewModel = gameViewModel,
                                onGameOver = {
                                    navController.navigate(Screen.Result.route) {
                                        popUpTo(Screen.Game.route) { inclusive = true }
                                    }
                                },
                                onQuitToHome = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Result.route) {
                            ResultScreen(
                                gameState = gameState,
                                bestScore = preferences.bestScore,
                                soundManager = gameViewModel.soundManager,
                                onPlayAgain = {
                                    navController.navigate(Screen.Game.route) {
                                        popUpTo(Screen.Result.route) { inclusive = true }
                                    }
                                },
                                onHome = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                preferences = preferences,
                                soundManager = gameViewModel.soundManager,
                                onThemeChange = { mode ->
                                    gameViewModel.updateThemeMode(mode)
                                },
                                onSoundChange = { enabled ->
                                    gameViewModel.updateSoundEnabled(enabled)
                                },
                                onHapticsChange = { enabled ->
                                    gameViewModel.updateHapticsEnabled(enabled)
                                },
                                onResetStats = {
                                    gameViewModel.resetStats()
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Screen.About.route) {
                            AboutScreen(
                                soundManager = gameViewModel.soundManager,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
