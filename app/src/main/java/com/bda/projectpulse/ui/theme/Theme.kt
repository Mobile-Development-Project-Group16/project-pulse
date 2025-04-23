package com.bda.projectpulse.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = OnPrimary,
    secondary = Success,
    onSecondary = OnPrimary,
    secondaryContainer = Success,
    onSecondaryContainer = OnPrimary,
    tertiary = Warning,
    onTertiary = OnPrimary,
    tertiaryContainer = Warning,
    onTertiaryContainer = OnPrimary,
    error = Error,
    onError = OnPrimary,
    errorContainer = Error,
    onErrorContainer = OnPrimary,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimary,
    primaryContainer = Primary,
    onPrimaryContainer = OnPrimary,
    secondary = Success,
    onSecondary = OnPrimary,
    secondaryContainer = Success,
    onSecondaryContainer = OnPrimary,
    tertiary = Warning,
    onTertiary = OnPrimary,
    tertiaryContainer = Warning,
    onTertiaryContainer = OnPrimary,
    error = Error,
    onError = OnPrimary,
    errorContainer = Error,
    onErrorContainer = OnPrimary,
    background = Color(0xFF1A1C1E),
    onBackground = OnPrimary,
    surface = Color(0xFF1A1C1E),
    onSurface = OnPrimary,
    surfaceVariant = Color(0xFF2F3133),
    onSurfaceVariant = Color(0xFFE2E2E2),
)

@Composable
fun ProjectPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled dynamic colors to use our custom theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}