package com.poker.game.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GreenFelt = Color(0xFF35654d)
private val GreenFeltDark = Color(0xFF2a503d)
private val CardWhite = Color(0xFFF5F5F5)
private val CardBack = Color(0xFF1a4785)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4a90d9),
    secondary = Color(0xFF6c757d),
    tertiary = Color(0xFFffd700),
    background = GreenFelt,
    surface = GreenFeltDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4a90d9),
    secondary = Color(0xFF6c757d),
    tertiary = Color(0xFFffd700),
    background = CardWhite,
    surface = CardWhite,
)

@Composable
fun PokerGameTheme(
    darkTheme: Boolean = true, // 扑克游戏通常用深色
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GreenFeltDark.toArgb()
            window.navigationBarColor = GreenFeltDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
