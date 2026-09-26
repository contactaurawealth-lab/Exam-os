package com.studyoffline.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

object StudyOfflineTheme {
    val colors: StudyOfflineColors
        @Composable
        @ReadOnlyComposable
        get() = LocalStudyOfflineColors.current

    val typography: StudyOfflineTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalStudyOfflineTypography.current
}

@Composable
fun StudyOfflineTheme(
    themeMode: String = "SYSTEM",
    accentColorHex: String = "#7C9A82",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    val rawAccent = ColorContrastUtil.parseHexColor(
        accentColorHex,
        if (isDark) DarkAccentDefault else LightAccentDefault
    )

    val effectiveAccent = if (isDark) {
        ColorContrastUtil.lightenForDarkTheme(rawAccent)
    } else {
        rawAccent
    }

    val background = if (isDark) DarkBackground else LightBackground
    val surface = if (isDark) DarkSurface else LightSurface
    val surfaceMuted = if (isDark) DarkSurfaceMuted else LightSurfaceMuted
    val divider = if (isDark) DarkDivider else LightDivider
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val textDisabled = if (isDark) DarkTextDisabled else LightTextDisabled
    val success = if (isDark) DarkSuccess else LightSuccess
    val warning = if (isDark) DarkWarning else LightWarning
    val error = if (isDark) DarkError else LightError
    val onError = if (isDark) DarkOnError else LightOnError

    val onAccent = ColorContrastUtil.determineOnAccent(effectiveAccent, isDark)
    val surfaceSelected = ColorContrastUtil.deriveSurfaceSelected(effectiveAccent, surface)

    val customColors = StudyOfflineColors(
        background = background,
        surface = surface,
        surfaceMuted = surfaceMuted,
        surfaceSelected = surfaceSelected,
        divider = divider,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textDisabled = textDisabled,
        accent = effectiveAccent,
        onAccent = onAccent,
        success = success,
        warning = warning,
        error = error,
        onError = onError,
        isDark = isDark
    )

    val m3ColorScheme = if (isDark) {
        darkColorScheme(
            primary = effectiveAccent,
            onPrimary = onAccent,
            primaryContainer = surfaceSelected,
            onPrimaryContainer = effectiveAccent,
            secondary = effectiveAccent,
            onSecondary = onAccent,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceMuted,
            onSurfaceVariant = textSecondary,
            outline = divider,
            error = error,
            onError = onError
        )
    } else {
        lightColorScheme(
            primary = effectiveAccent,
            onPrimary = onAccent,
            primaryContainer = surfaceSelected,
            onPrimaryContainer = effectiveAccent,
            secondary = effectiveAccent,
            onSecondary = onAccent,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceMuted,
            onSurfaceVariant = textSecondary,
            outline = divider,
            error = error,
            onError = onError
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = background.toArgb()
                window.navigationBarColor = background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalStudyOfflineColors provides customColors,
        LocalStudyOfflineTypography provides StudyOfflineTypography()
    ) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            content = content
        )
    }
}
