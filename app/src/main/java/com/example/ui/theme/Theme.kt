package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class AppThemeSetting(val title: String) {
    DARK("Karanlık Mod"),
    LIGHT("Aydınlık Mod"),
    SYSTEM("Sistem Teması")
}

enum class ViewModeSetting(val title: String) {
    EPG("EPG Rehber"),
    GRID("Küçük Resim"),
    LIST("Kompakt Liste")
}

private val StreamFlowDarkColorScheme = darkColorScheme(
  primary = StreamFlowPrimary,
  onPrimary = StreamFlowOnPrimary,
  primaryContainer = StreamFlowPrimaryContainer,
  onPrimaryContainer = StreamFlowOnPrimaryContainer,
  secondary = StreamFlowSecondary,
  onSecondary = StreamFlowOnSecondary,
  secondaryContainer = StreamFlowSecondaryContainer,
  onSecondaryContainer = StreamFlowOnSecondaryContainer,
  tertiary = StreamFlowTertiary,
  onTertiary = StreamFlowOnTertiary,
  tertiaryContainer = StreamFlowTertiaryContainer,
  error = StreamFlowError,
  onError = StreamFlowOnError,
  errorContainer = StreamFlowErrorContainer,
  onErrorContainer = StreamFlowOnErrorContainer,
  background = StreamFlowBackground,
  onBackground = StreamFlowOnBackground,
  surface = StreamFlowSurface,
  onSurface = StreamFlowOnSurface,
  surfaceVariant = StreamFlowSurfaceVariant,
  onSurfaceVariant = StreamFlowOnSurfaceVariant,
  surfaceContainer = StreamFlowSurfaceContainer,
  surfaceContainerHigh = StreamFlowSurfaceContainerHigh,
  surfaceContainerHighest = StreamFlowSurfaceContainerHighest,
  surfaceContainerLow = StreamFlowSurfaceContainerLow,
  surfaceContainerLowest = StreamFlowSurfaceContainerLowest,
  surfaceBright = StreamFlowSurfaceBright,
  surfaceDim = StreamFlowSurfaceDim,
  outline = StreamFlowOutline,
  outlineVariant = StreamFlowOutlineVariant,
)

private val StreamFlowLightColorScheme = lightColorScheme(
  primary = StreamFlowLightPrimary,
  onPrimary = StreamFlowLightOnPrimary,
  primaryContainer = StreamFlowLightPrimaryContainer,
  onPrimaryContainer = StreamFlowLightOnPrimaryContainer,
  secondary = StreamFlowLightSecondary,
  onSecondary = StreamFlowLightOnSecondary,
  secondaryContainer = StreamFlowLightSecondaryContainer,
  onSecondaryContainer = StreamFlowLightOnSecondaryContainer,
  tertiary = StreamFlowTertiary,
  onTertiary = StreamFlowOnTertiary,
  tertiaryContainer = StreamFlowTertiaryContainer,
  error = StreamFlowError,
  onError = StreamFlowOnError,
  errorContainer = StreamFlowErrorContainer,
  onErrorContainer = StreamFlowOnErrorContainer,
  background = StreamFlowLightBackground,
  onBackground = StreamFlowLightOnBackground,
  surface = StreamFlowLightSurface,
  onSurface = StreamFlowLightOnSurface,
  surfaceVariant = StreamFlowLightSurfaceVariant,
  onSurfaceVariant = StreamFlowLightOnSurfaceVariant,
  surfaceContainer = StreamFlowLightSurfaceContainer,
  surfaceContainerHigh = StreamFlowLightSurfaceContainerHigh,
  surfaceContainerHighest = StreamFlowLightSurfaceContainerHighest,
  surfaceContainerLow = StreamFlowLightSurfaceContainerLow,
  surfaceContainerLowest = StreamFlowLightSurfaceContainerLowest,
  surfaceBright = StreamFlowLightSurfaceBright,
  surfaceDim = StreamFlowLightSurfaceDim,
  outline = StreamFlowLightOutline,
  outlineVariant = StreamFlowLightOutlineVariant,
)

@Composable
fun MyApplicationTheme(
  themeSetting: AppThemeSetting = AppThemeSetting.DARK,
  content: @Composable () -> Unit,
) {
  val isDark = when (themeSetting) {
    AppThemeSetting.DARK -> true
    AppThemeSetting.LIGHT -> false
    AppThemeSetting.SYSTEM -> isSystemInDarkTheme()
  }

  val colorScheme = if (isDark) StreamFlowDarkColorScheme else StreamFlowLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}


