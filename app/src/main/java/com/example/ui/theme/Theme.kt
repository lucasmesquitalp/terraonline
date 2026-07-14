package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = RpgGold,
    onPrimary = DeepCharcoal,
    secondary = CrimsonRed,
    onSecondary = ParchmentWhite,
    tertiary = RpgGoldMuted,
    background = DeepCharcoal,
    onBackground = ParchmentWhite,
    surface = SlateObsidian,
    onSurface = ParchmentWhite,
    surfaceVariant = LightObsidian,
    onSurfaceVariant = ParchmentWhite,
    outline = DarkBorder
  )

private val LightColorScheme = DarkColorScheme // Default to dark for RPG style

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for Dark Fantasy
  dynamicColor: Boolean = false, // Disable dynamic colors to keep thematic RPG style
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
