package com.slpolice.reporting.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PoliceColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = NavyMuted,
    onPrimaryContainer = Color.White,
    secondary = BraidGold,
    onSecondary = NavyDeep,
    secondaryContainer = BraidGoldSoft,
    onSecondaryContainer = NavyDeep,
    background = Docket,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Docket,
    onSurfaceVariant = InkSoft,
    outline = Hairline,
    outlineVariant = Hairline,
    error = StatusRejected,
    onError = Color.White,
    errorContainer = DangerSurface,
    onErrorContainer = StatusRejected
)

/**
 * The department brief calls for one predictable presentation, so the palette deliberately
 * ignores the system dark setting and Android 12 dynamic colour.
 */
@Composable
fun SLPoliceTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Navy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = PoliceColors,
        typography = AppTypography,
        content = content
    )
}
