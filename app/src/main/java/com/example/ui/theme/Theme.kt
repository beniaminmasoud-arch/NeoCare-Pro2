package com.example.ui.theme

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

private val LightColorScheme = lightColorScheme(
  primary = NeoTealPrimary,
  onPrimary = Color.White,
  primaryContainer = NeoTealContainerLight,
  onPrimaryContainer = NeoTealOnContainerLight,
  secondary = Color(0xFF169873),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFD6F5EC),
  onSecondaryContainer = Color(0xFF033928),
  tertiary = Color(0xFF2C6E8F),
  onTertiary = Color.White,
  background = NeoBackgroundLight,
  onBackground = NeoInkLight,
  surface = NeoCardLight,
  onSurface = NeoInkLight,
  surfaceVariant = Color(0xFFE9F1F0),
  onSurfaceVariant = NeoMuteLight,
  outline = NeoLineLight,
  error = NeoRedAlert,
  onError = Color.White,
  errorContainer = NeoRedAlertSoft,
  onErrorContainer = NeoRedAlert
)

private val DarkColorScheme = darkColorScheme(
  primary = NeoTealDark,
  onPrimary = Color(0xFF003734),
  primaryContainer = NeoTealContainerDark,
  onPrimaryContainer = NeoTealOnContainerDark,
  secondary = Color(0xFF7BD69A),
  onSecondary = Color(0xFF003822),
  secondaryContainer = Color(0xFF184E34),
  onSecondaryContainer = Color(0xFF99F3B6),
  tertiary = Color(0xFF8DCDEF),
  onTertiary = Color(0xFF00344B),
  background = NeoBackgroundDark,
  onBackground = NeoInkDark,
  surface = NeoCardDark,
  onSurface = NeoInkDark,
  surfaceVariant = Color(0xFF1C3130),
  onSurfaceVariant = NeoMuteDark,
  outline = NeoLineDark,
  error = NeoRedAlertDark,
  onError = Color(0xFF601410),
  errorContainer = NeoRedAlertSoftDark,
  onErrorContainer = NeoRedAlertDark
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to maintain the strict professional clinical identity
  dynamicColor: Boolean = false,
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

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.surface.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = NeoTypography,
    content = content
  )
}
