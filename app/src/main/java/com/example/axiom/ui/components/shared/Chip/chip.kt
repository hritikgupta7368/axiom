package com.example.axiom.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.ui.theme.AxiomTheme

enum class ChipColor {
    ORANGE, // Added Orange
    GREEN,
    BLUE,
    RED,
    GRAY
}

data class GradientChipStyle(
    val border: Color,
    val backgroundGradient: List<Color>,
    val textGradient: List<Color>
)

@Composable
fun getGradientChipStyle(type: ChipColor): GradientChipStyle {
    return when (type) {
        ChipColor.ORANGE -> GradientChipStyle(
            border = Color(0xFFEAA968),
            backgroundGradient = listOf(Color(0xFFFDE2B4), Color(0xFFF4C27F)),
            textGradient = listOf(
                Color(0xFFA44405),
                Color(0xFFA44405)
            ) // Solid text for orange to match image
        )

        ChipColor.GREEN -> GradientChipStyle(
            border = Color(0xFF7BEE8D),
            backgroundGradient = listOf(Color(0xFFD4FDE1), Color(0xFF9EF7BA)),
            textGradient = listOf(Color(0xFF1B8524), Color(0xFF0B4A11))
        )

        ChipColor.BLUE -> GradientChipStyle(
            border = Color(0xFF7BB2EE),
            backgroundGradient = listOf(Color(0xFFB3E1FD), Color(0xFF8EC8F7)),
            textGradient = listOf(Color(0xFF1550B4), Color(0xFF002C7E))
        )

        ChipColor.RED -> GradientChipStyle(
            border = Color(0xFFEE7B7B),
            backgroundGradient = listOf(Color(0xFFFDB3B3), Color(0xFFF78E8E)),
            textGradient = listOf(Color(0xFFB41515), Color(0xFF7E0000))
        )

        ChipColor.GRAY -> GradientChipStyle(
            border = Color(0xFFB2B2B2),
            backgroundGradient = listOf(Color(0xFFEAEAEA), Color(0xFFC8C8C8)),
            textGradient = listOf(Color(0xFF505050), Color(0xFF2C2C2C))
        )
    }
}

@Composable
fun Chip(
    text: String,
    color: ChipColor,
    modifier: Modifier = Modifier
) {
    val chipsTheme = AxiomTheme.components.chips
    val style = when (color) {
        ChipColor.ORANGE -> chipsTheme.orange
        ChipColor.GREEN -> chipsTheme.green
        ChipColor.BLUE -> chipsTheme.blue
        ChipColor.RED -> chipsTheme.red
        ChipColor.GRAY -> chipsTheme.gray
    }

    Box(
        modifier = modifier
            .border(width = 0.8.dp, color = style.border, shape = CircleShape) // Slimmer border
            .background(
                brush = Brush.verticalGradient(style.backgroundGradient),
                shape = CircleShape
            )
            // Reduced padding from 10/4 to 8/2 for a tighter look
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 10.sp, // Reduced from 11.sp
            fontWeight = FontWeight.ExtraBold, // Increased weight to maintain legibility at small size
            letterSpacing = 0.4.sp, // Slightly tighter spacing
            style = LocalTextStyle.current.copy(
                brush = Brush.verticalGradient(style.textGradient)
            )
        )
    }
}


// --- PREVIEW ---
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Gradient Chips (Small)")
@Composable
fun ChipPreview() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Tighter spacing for smaller chips
    ) {
        Chip(text = "TIP: $21.38", color = ChipColor.ORANGE)
        Chip(text = "TIP: $21.38", color = ChipColor.GREEN)
        Chip(text = "TIP: $21.38", color = ChipColor.BLUE)
        Chip(text = "TIP: $21.38", color = ChipColor.RED)
        Chip(text = "TIP: $21.38", color = ChipColor.GRAY)
    }
}