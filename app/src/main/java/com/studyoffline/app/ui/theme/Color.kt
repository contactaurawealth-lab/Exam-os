package com.studyoffline.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LightBackground = Color(0xFFFAF7F2)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceMuted = Color(0xFFF1ECE3)
val LightSurfaceSelectedDefault = Color(0xFFE4EDE6)
val LightDivider = Color(0xFFE7E1D6)
val LightTextPrimary = Color(0xFF2B2A28)
val LightTextSecondary = Color(0xFF7A756C)
val LightTextDisabled = Color(0xFFC9C2B4)
val LightAccentDefault = Color(0xFF7C9A82)
val LightSuccess = Color(0xFF7C9A82)
val LightWarning = Color(0xFFD9A25B)
val LightError = Color(0xFFC77B6B)
val LightOnAccentDefault = Color(0xFFFFFFFF)
val LightOnError = Color(0xFFFFFFFF)

val DarkBackground = Color(0xFF1C1B19)
val DarkSurface = Color(0xFF252420)
val DarkSurfaceMuted = Color(0xFF2E2C27)
val DarkSurfaceSelectedDefault = Color(0xFF2E3B31)
val DarkDivider = Color(0xFF3A3833)
val DarkTextPrimary = Color(0xFFEDE9E2)
val DarkTextSecondary = Color(0xFFA39D91)
val DarkTextDisabled = Color(0xFF5C584F)
val DarkAccentDefault = Color(0xFF8FB396)
val DarkSuccess = Color(0xFF8FB396)
val DarkWarning = Color(0xFFE0B378)
val DarkError = Color(0xFFD6917F)
val DarkOnAccentDefault = Color(0xFF1C1B19)
val DarkOnError = Color(0xFF1C1B19)

val SubjectTagPalette = listOf(
    "#B8C9E1", // Blue
    "#D9C6E8", // Lavender
    "#E8D0B0", // Tan
    "#C7DFC2", // Mint
    "#E9C3C3", // Rose
    "#C9E1DC", // Teal
    "#E3D6A8", // Ochre
    "#D3CBE3"  // Violet
)

val PresetAccents = listOf(
    "#7C9A82", // Sage (Default)
    "#D9A25B", // Ochre
    "#6897BB", // Slate Blue
    "#B07C9A", // Plum
    "#C48F70"  // Terracotta
)

data class StudyOfflineColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val surfaceSelected: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val accent: Color,
    val onAccent: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val onError: Color,
    val isDark: Boolean
)

val LocalStudyOfflineColors = staticCompositionLocalOf<StudyOfflineColors> {
    error("No StudyOfflineColors provided")
}
