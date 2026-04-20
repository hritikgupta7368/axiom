package com.example.axiom.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AxiomColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val background: Color,
    val cardBackground: Color,
    val border: Color,
    val accentBlue: Color,
    val accentBlueBg: Color,
    val accentGreen: Color,
    val accentGreenBg: Color,
    val error: Color,
    val isLight: Boolean,
    val red: Color,
    val mutedRed: Color,
    val green: Color,
    val mutedGreen: Color
)

@Immutable
data class CardStyle(
    val background: Color,
    val border: Color,
    val selectedBorder: Color,
    val selectedBackground: Color,
    val title: Color,
    val subtitle: Color,
    val mutedText: Color, // more darker than subtitle
    val badgeBackground: Color,
    val badgeText: Color,
    val chartBar: Color,
    val chartActiveBar: Color,
    val BadgeBgColor: Color,
    val BadgeTextColor: Color

)

@Immutable
data class TextInputStyle(
    val textColor: Color,
    val unfocusedBorder: Color,
    val focusedBorder: Color,
    val unfocusedLabel: Color,
    val focusedLabel: Color,
    val iconColorResting: Color,
    val errorColor: Color,
    val unfocusedBg: Color,
    val focusedBg: Color,
    val errorBg: Color,
)


@Immutable
data class AvatarStyle(
    val background: Color,
    val border: Color,
    val tickBorder: Color,
    val iconColor: Color
)

@Immutable
data class ChipStyle(
    val border: Color,
    val backgroundGradient: List<Color>,
    val textGradient: List<Color>
)

@Immutable
data class ChipVariants(
    val orange: ChipStyle, // Added Orange
    val green: ChipStyle,
    val blue: ChipStyle,
    val red: ChipStyle,
    val gray: ChipStyle
)

@Immutable
data class AxiomComponents(
    val card: CardStyle,
    val chips: ChipVariants,
    val avatar: AvatarStyle,
    val textInput: TextInputStyle
)


// -----------------------------------------------------------Light Colors
val White = Color.White // cards bg , title in dark
val Gray100 = Color(0xFFF3F4F6)// cards border
val Gray900 = Color(0xFF111827) // for card title

// Green Light
val Green50 = Color(0xFFF0FDF4)
val Green600 = Color(0xFF16A34A)
val Green100 = Color(0xFFDCFCE7)
val Green500_10 = Color(0x1A22C55E)
val Green400 = Color(0xFF4ADE80)
val Green500_20 = Color(0x3322C55E)

//Red
val Red400 = Color(0xFFDC2626) //opening balance
val Red600 = Color(0xFFDC2626) //opening balance
val Red500_70 = Color(0xB3EF4444) // for dr / cr

// -------------------------------------------------------------Dark Colors
val Dark_Neutral = Color(0xFF18181B) // for cards Bg
val White_5 = Color(0x0DFFFFFF) // cards border


fun lightComponents(colors: AxiomColors) = AxiomComponents(
    card = CardStyle(
        background = Color(0xFFFFFFFF),
        border = Color(0xFFE5E7EB),
        selectedBorder = Color(0xFF3B82F6),
        selectedBackground = Color(0xFFEFF6FF),
        title = Color(0xFF111827),
        subtitle = Color(0xFF6B7280),
        mutedText = Color(0xFF9CA3AF),
        badgeBackground = Color(0xFFF3F4F6),
        badgeText = Color(0xFF4B5563),
        chartBar = Color(0xFFE5E7EB),
        chartActiveBar = Color(0xFF111827),
        BadgeBgColor = Color(0xFFF3F4F6),
        BadgeTextColor = Color(0xFF4B5563)

    ),
    chips = ChipVariants(
        orange = ChipStyle(
            border = Color(0xFFEEB27B),
            backgroundGradient = listOf(Color(0xFFFDE1B3), Color(0xFFF7C88E)),
            textGradient = listOf(Color(0xFFB45015), Color(0xFF7E2C00))
        ),
        blue = ChipStyle(
            border = Color(0xFF7BB2EE),
            backgroundGradient = listOf(Color(0xFFB3E1FD), Color(0xFF8EC8F7)),
            textGradient = listOf(Color(0xFF1550B4), Color(0xFF002C7E))
        ),
        gray = ChipStyle(
            border = Color(0xFFB2B2B2),
            backgroundGradient = listOf(Color(0xFFEAEAEA), Color(0xFFC8C8C8)),
            textGradient = listOf(Color(0xFF505050), Color(0xFF2C2C2C))
        ),
        green = ChipStyle(
            border = Color(0xFF7BEE8D),
            backgroundGradient = listOf(Color(0xFFD4FDE1), Color(0xFF9EF7BA)),
            textGradient = listOf(Color(0xFF1B8524), Color(0xFF0B4A11))
        ),
        red = ChipStyle(
            border = Color(0xFFEE7B7B),
            backgroundGradient = listOf(Color(0xFFFDB3B3), Color(0xFFF78E8E)),
            textGradient = listOf(Color(0xFFB41515), Color(0xFF7E0000))
        )
    ),
    avatar = AvatarStyle(
        background = Color(0xFFFFFFFF),
        border = Color(0xFFE5E7EB),
        tickBorder = Color(0xFFFFFFFF),
        iconColor = Color(0xFF9CA3AF)
    ),
    textInput = TextInputStyle(
        textColor = Color(0xFF111827),
        unfocusedBorder = Color(0xFFD1D5DB),
        focusedBorder = Color(0xFF3B82F6),
        unfocusedLabel = Color(0xFF6B7280),
        focusedLabel = Color(0xFF3B82F6),
        iconColorResting = Color(0xFF9CA3AF),
        errorColor = Color(0xFFEF4444),
        unfocusedBg = Color(0x05000000),
        focusedBg = Color.Transparent,
        errorBg = Color(0x0DEF4444)
    )
)


fun darkComponents(colors: AxiomColors) = AxiomComponents(
    card = CardStyle(

        background = Color(0xFF18181A),
        border = Color(0x0AFFFFFF),
        selectedBorder = Color(0xFF3B82F6),
        selectedBackground = Color(0x1A3B82F6),
        title = Color(0xFFFFFFFF),
        subtitle = Color(0xFFA1A1AA),
        mutedText = Color(0xFF71717A),
        badgeBackground = Color.White.copy(alpha = 0.1f),
        badgeText = Color(0xFFA1A1AA),
        chartBar = Color.White.copy(alpha = 0.2f),
        chartActiveBar = Color.White,
        BadgeBgColor = Color.White.copy(alpha = 0.1f),
        BadgeTextColor = Color(0xFFA1A1AA)

    ),
    chips = ChipVariants(
        orange = ChipStyle(
            border = Color(0xFFA44405),
            backgroundGradient = listOf(Color(0xFF381A05), Color(0xFF200D02)),
            textGradient = listOf(Color(0xFFFDE1B3), Color(0xFFEAA968))
        ),
        blue = ChipStyle(
            border = Color(0xFF1550B4),
            backgroundGradient = listOf(Color(0xFF091C3A), Color(0xFF040D1F)),
            textGradient = listOf(Color(0xFFB3E1FD), Color(0xFF7BB2EE))
        ),
        gray = ChipStyle(
            border = Color(0xFF3A3A3E),
            backgroundGradient = listOf(Color(0xFF2A2A2D), Color(0xFF1E1E21)),
            textGradient = listOf(Color(0xFFEAEAEA), Color(0xFFB2B2B2))
        ),
        green = ChipStyle(
            border = Color(0xFF1B8524),
            backgroundGradient = listOf(Color(0xFF0A2910), Color(0xFF041407)),
            textGradient = listOf(Color(0xFFD4FDE1), Color(0xFF7BEE8D))
        ),
        red = ChipStyle(
            border = Color(0xFFB41515),
            backgroundGradient = listOf(Color(0xFF360B0B), Color(0xFF1F0404)),
            textGradient = listOf(Color(0xFFFDB3B3), Color(0xFFEE7B7B))
        )
    ),
    avatar = AvatarStyle(
        background = Color(0xFF18181A),
        border = Color.White.copy(0.10f),
        tickBorder = Color(0xFF3B82F6),
        iconColor = Color(0xFF6B7280)
    ),
    textInput = TextInputStyle(
        textColor = Color(0xFFFAFAFA),
        unfocusedBorder = Color(0xFF3F3F46),
        focusedBorder = Color(0xFF3B82F6),
        unfocusedLabel = Color(0xFFA1A1AA),
        focusedLabel = Color(0xFF3B82F6),
        iconColorResting = Color(0xFF71717A),
        errorColor = Color(0xFFF87171),
        unfocusedBg = Color(0x08FFFFFF),
        focusedBg = Color.Transparent,
        errorBg = Color(0x14F87171)
    )
)


val LocalAxiomComponents = staticCompositionLocalOf<AxiomComponents> {
    error("No components provided")
}

// The "Link" that allows all screens to see these colors
val LocalAxiomColors = staticCompositionLocalOf<AxiomColors> {
    error("No AxiomColors provided")
}


// Your specific Hex constants
val Zinc950 = Color(0xFF09090B)
val Zinc900 = Color(0xFF18181B)
val Zinc800 = Color(0xFF27272A)
val Zinc400 = Color(0xFFA1A1AA)
val Blue500_10 = Color(0x1A3B82F6)
val Blue400 = Color(0xFF60A5FA)

val AxiomDarkColors = AxiomColors(
    background = Color(0xFF09090B),
    textPrimary = Color.White,
    textSecondary = Color(0xFF4D4D4D),

    cardBackground = Color.Green,
    border = Zinc800,
    accentBlue = Blue400,
    accentBlueBg = Blue500_10,
    accentGreen = Color(0xFF4ADE80),
    accentGreenBg = Color(0x1A22C55E),
    error = Color(0xFFF87171),
    isLight = false,

    red = Red400,
    mutedRed = Red500_70,

    green = Green400,
    mutedGreen = Green500_20,
)

val AxiomLightColors = AxiomColors(
    textPrimary = Zinc950,
    textSecondary = Color(0xFF4D4D4D),
    background = Color(0xFFF3F4F6),
    cardBackground = Color.White,
    border = Color(0xFFE4E4E7),
    accentBlue = Color(0xFF2563EB),
    accentBlueBg = Color(0xFFDBEAFE),
    accentGreen = Green600,
    accentGreenBg = Green50,
    error = Red600,
    isLight = true,

    red = Red600,
    mutedRed = Red500_70,

    green = Green600,
    mutedGreen = Green500_20,
)


object AxiomTheme {
    val colors: AxiomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAxiomColors.current

    val components: AxiomComponents
        @Composable
        @ReadOnlyComposable
        get() = LocalAxiomComponents.current
}

@Composable
fun AxiomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) AxiomDarkColors else AxiomLightColors

    val components =
        if (darkTheme) darkComponents(colors)
        else lightComponents(colors)

    CompositionLocalProvider(
        LocalAxiomColors provides colors,
        LocalAxiomComponents provides components
    ) {
        MaterialTheme(
            typography = Typography,
            content = content
        )
    }
}