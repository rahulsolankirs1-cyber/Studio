package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AmberGold500,
    onPrimary = Color.Black,
    primaryContainer = StudioNavy800,
    onPrimaryContainer = AmberGold300,
    secondary = ApertureCyanLight,
    onSecondary = Color.Black,
    secondaryContainer = StudioNavy700,
    onSecondaryContainer = Color.White,
    tertiary = AmberGold300,
    background = StudioNavy900,
    onBackground = TextPrimaryDark,
    surface = StudioNavy900,
    onSurface = TextPrimaryDark,
    surfaceVariant = StudioNavy800,
    onSurfaceVariant = TextSecondaryDark,
    error = DangerRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AmberGold600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = ApertureCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFFD97706),
    background = SurfaceLight,
    onBackground = StudioNavy900,
    surface = Color.White,
    onSurface = StudioNavy900,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = StudioNavy700,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent high-end studio branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
