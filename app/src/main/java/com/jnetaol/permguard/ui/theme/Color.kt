package com.jnetaol.permguard.ui.theme

import androidx.compose.ui.graphics.Color

val RedNeon = Color(0xFFFF1744)
val RedDark = Color(0xFFB2102F)
val RedLight = Color(0xFFFF5252)
val CrimsonDark = Color(0xFF1A0A0F)
val CrimsonSurface = Color(0xFF1E1215)
val CrimsonSurfaceVariant = Color(0xFF24171A)
val CrimsonBackground = Color(0xFF0A0406)

val DarkBackground = Color(0xFF0A0406)
val DarkSurface = Color(0xFF1A0A0F)
val DarkSurfaceVariant = Color(0xFF24171A)
val DarkOnBackground = Color(0xFFF5F5F5)
val DarkOnSurface = Color(0xFFE0D6D8)
val DarkOnSurfaceVariant = Color(0xFFB0A0A4)
val DarkOutline = Color(0xFF604448)

val DarkPrimary = RedNeon
val DarkOnPrimary = Color(0xFF0A0406)
val DarkPrimaryContainer = RedDark
val DarkOnPrimaryContainer = Color(0xFFFFCDD2)

val DarkSecondary = Color(0xFFE91E63)
val DarkOnSecondary = Color(0xFF0A0406)
val DarkSecondaryContainer = Color(0xFF880E4F)
val DarkOnSecondaryContainer = Color(0xFFF8BBD0)

val DarkTertiary = Color(0xFFFF5722)
val DarkOnTertiary = Color(0xFF0A0406)
val DarkTertiaryContainer = Color(0xFFBF360C)
val DarkOnTertiaryContainer = Color(0xFFFFCCBC)

val DarkError = Color(0xFFFF1744)
val DarkOnError = Color(0xFF0A0406)
val DarkErrorContainer = Color(0xFFB71C1C)
val DarkOnErrorContainer = Color(0xFFFFCDD2)

val ScoreGreen = Color(0xFF00E676)
val ScoreYellow = Color(0xFFFFEA00)
val ScoreOrange = Color(0xFFFF9100)
val ScoreRed = Color(0xFFFF1744)

val GlowRed = Color(0x33FF1744)
val GlowCrimson = Color(0x22D50000)

fun scoreColor(score: Int): Color = when {
    score >= 75 -> ScoreGreen
    score >= 50 -> ScoreYellow
    score >= 25 -> ScoreOrange
    else -> ScoreRed
}

fun scoreBackground(score: Int): Color = when {
    score >= 75 -> Color(0x2200E676)
    score >= 50 -> Color(0x22FFEA00)
    score >= 25 -> Color(0x22FF9100)
    else -> Color(0x22FF1744)
}
