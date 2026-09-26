package com.studyoffline.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ColorContrastUtil {
    private fun sRgbToLinear(channel: Float): Double {
        return if (channel <= 0.04045f) {
            channel / 12.92
        } else {
            ((channel + 0.055) / 1.055).pow(2.4)
        }
    }

    fun calculateLuminance(color: Color): Double {
        val r = sRgbToLinear(color.red)
        val g = sRgbToLinear(color.green)
        val b = sRgbToLinear(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    fun contrastRatio(color1: Color, color2: Color): Double {
        val lum1 = calculateLuminance(color1)
        val lum2 = calculateLuminance(color2)
        val lighter = max(lum1, lum2)
        val darker = min(lum1, lum2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /**
     * Checks if contrast meets WCAG AA (4.5:1)
     */
    fun meetsWcagAa(foreground: Color, background: Color): Boolean {
        return contrastRatio(foreground, background) >= 4.5
    }

    /**
     * Determines optimal on-accent color (White or Black/Near-black)
     */
    fun determineOnAccent(accent: Color, darkTheme: Boolean): Color {
        val lightCandidate = Color(0xFFFFFFFF)
        val darkCandidate = if (darkTheme) Color(0xFF1C1B19) else Color(0xFF2B2A28)
        val contrastWithLight = contrastRatio(lightCandidate, accent)
        val contrastWithDark = contrastRatio(darkCandidate, accent)
        return if (contrastWithLight >= contrastWithDark) lightCandidate else darkCandidate
    }

    /**
     * Derives surfaceSelected by blending 20% accent over surface
     */
    fun deriveSurfaceSelected(accent: Color, surface: Color): Color {
        val alpha = 0.20f
        val r = surface.red * (1f - alpha) + accent.red * alpha
        val g = surface.green * (1f - alpha) + accent.green * alpha
        val b = surface.blue * (1f - alpha) + accent.blue * alpha
        return Color(r, g, b, 1f)
    }

    /**
     * Lightens color for dark theme if needed
     */
    fun lightenForDarkTheme(color: Color): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        // Soften saturation slightly and bump brightness for dark backgrounds
        hsv[1] = (hsv[1] * 0.85f).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * 1.18f).coerceIn(0.6f, 0.95f)
        return Color(android.graphics.Color.HSVToColor(hsv))
    }

    fun parseHexColor(hex: String, default: Color = Color(0xFF7C9A82)): Color {
        return try {
            val clean = hex.removePrefix("#")
            if (clean.length == 6) {
                Color(android.graphics.Color.parseColor("#$clean"))
            } else if (clean.length == 8) {
                Color(android.graphics.Color.parseColor("#$clean"))
            } else {
                default
            }
        } catch (e: Exception) {
            default
        }
    }

    fun colorToHex(color: Color): String {
        val argb = color.toArgb()
        return String.format("#%06X", 0xFFFFFF and argb)
    }
}
