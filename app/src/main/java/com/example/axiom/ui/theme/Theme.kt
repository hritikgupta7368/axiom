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


    primary = BrandPrimary,  //purple
    secondary = BrandSecondary, //aqua color : eg fab icon background
    tertiary = BrandTertiary,  //pink like used eg : fab icon background


    background = DarkBackground, // page background
    surface = DarkSurface,       // card background
    surfaceVariant = DarkSurfaceVariant, // for things like on input text background


    onPrimary = Color.Black, // 1st color text in buttons
    onSecondary = Color.Black, // 2nd color text in buttons
    onTertiary = Color.Black, // 3rd color text in buttons
    onBackground = DarkTextPrimary, // text background like title , paragraphs
    onSurface = DarkTextPrimary, // text inside card or dialog
    onSurfaceVariant = DarkTextSecondary, // for subtitiles , placeholders etc


    outline = DarkOutline, // card borders
    error = Error // rec color for error
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
    background = LightBackground,  // almost white

    // Cards / sheets / dialogs
    surface = LightSurface,       //white

    // Elevated UI containers
    surfaceVariant = LightSurfaceVariant, //very light gray

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
