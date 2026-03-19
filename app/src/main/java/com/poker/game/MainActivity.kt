package com.poker.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.poker.game.game.GameController
import com.poker.game.ui.screens.GameScreen
import com.poker.game.ui.screens.MenuScreen
import com.poker.game.ui.theme.PokerGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokerGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PokerGameApp()
                }
            }
        }
    }
}

sealed class Screen {
    object Menu : Screen()
    data class Game(val playerCount: Int) : Screen()
}

@Composable
fun PokerGameApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    val controller = remember { GameController() }
    
    when (val screen = currentScreen) {
        is Screen.Menu -> {
            MenuScreen(
                onStartGame = { playerCount ->
                    controller.initGame(playerCount)
                    currentScreen = Screen.Game(playerCount)
                },
                onExit = { /* 退出应用 */ }
            )
        }
        is Screen.Game -> {
            GameScreen(
                controller = controller,
                onBackToMenu = {
                    currentScreen = Screen.Menu
                }
            )
        }
    }
}
