package com.example.axiom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


/* ───────────────────────────────────────────────
   DARK COLOR SCHEME
   Used when darkTheme == true
   True black base for OLED
   ─────────────────────────────────────────────── */

private val DarkColorScheme = darkColorScheme(

    /**
     * PRIMARY
     * Used for:
     * - Primary buttons
     * - FloatingActionButton
     * - Selected states
     * - Active icons
     */
    primary = BrandPrimary,

    /**
     * SECONDARY
     * Used for:
     * - Secondary actions
     * - Accent UI elements
     */
    secondary = BrandSecondary,

    /**
     * TERTIARY
     * Used for:
     * - Decorative accents
     * - Optional highlights
     */
    tertiary = BrandTertiary,

    /**
     * BACKGROUND
     * Used for:
     * - Entire screen background
     * - Scaffold background
     */
    background = DarkBackground,

    /**
     * SURFACE
     * Used for:
     * - Cards
     * - Dialogs
     * - Bottom sheets
     * - App bars
     */
    surface = DarkSurface,

    /**
     * SURFACE VARIANT
     * Used for:
     * - Elevated containers
     * - Input field backgrounds
     * - Grouped UI sections
     */
    surfaceVariant = DarkSurfaceVariant,

    /**
     * TEXT / ICON COLOR ON PRIMARY
     * Text shown on buttons or FABs
     */
    onPrimary = Color.Black,

    /**
     * TEXT / ICON COLOR ON SECONDARY
     */
    onSecondary = Color.Black,

    /**
     * TEXT / ICON COLOR ON TERTIARY
     */
    onTertiary = Color.Black,

    /**
     * TEXT COLOR ON BACKGROUND
     * Used for:
     * - Main body text
     * - Titles
     */
    onBackground = DarkTextPrimary,

    /**
     * TEXT COLOR ON SURFACE
     * Used for:
     * - Text inside cards / sheets
     */
    onSurface = DarkTextPrimary,

    /**
     * SECONDARY TEXT COLOR
     * Used for:
     * - Subtitles
     * - Timestamps
     * - Helper text
     */
    onSurfaceVariant = DarkTextSecondary,

    /**
     * OUTLINE
     * Used for:
     * - Borders
     * - Dividers
     * - Strokes
     */
    outline = DarkOutline,

    /**
     * ERROR COLOR
     * Used for:
     * - Error messages
     * - Destructive actions
     */
    error = Error
)


/* ───────────────────────────────────────────────
   LIGHT COLOR SCHEME
   Used when darkTheme == false
   ─────────────────────────────────────────────── */

private val LightColorScheme = lightColorScheme(

    // Same semantic meaning as dark mode
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = BrandTertiary,

    // Screen background
    background = LightBackground,

    // Cards / sheets / dialogs
    surface = LightSurface,

    // Elevated UI containers
    surfaceVariant = LightSurfaceVariant,

    // Text on primary buttons
    onPrimary = Color.White,

    // Text on secondary / tertiary
    onSecondary = Color.Black,
    onTertiary = Color.Black,

    // Main text
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,

    // Secondary / helper text
    onSurfaceVariant = LightTextSecondary,

    // Borders / dividers
    outline = LightOutline,

    // Error states
    error = Error
)

@Composable
fun AxiomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // DISABLED — breaks brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
