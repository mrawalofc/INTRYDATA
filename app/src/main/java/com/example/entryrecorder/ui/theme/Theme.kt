package com.example.entryrecorder.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryLightBlue,
    onPrimaryContainer = PrimaryDarkBlue,
    secondary = AdminPurple,
    onSecondary = Color.White,
    secondaryContainer = AdminLightPurple,
    onSecondaryContainer = AdminDarkPurple,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    tertiaryContainer = SuccessLightGreen,
    onTertiaryContainer = SuccessDarkGreen,
    background = Color(0xFFF3F4F6),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate800,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerLightRed,
    onErrorContainer = DangerRed
)

@Composable
fun EntryRecorderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
