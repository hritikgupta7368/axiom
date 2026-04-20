package com.example.axiom.ui.components.shared.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ButtonVariant { Gray, Red, White }

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Gray,
    icon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 14.dp, horizontal = 16.dp),
    enabled: Boolean = true
) {


    // Map variants to specific background, text, and border colors based on the theme
    val (bgColor, textColor, borderColor) = when (variant) {
        ButtonVariant.Gray -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.outline
        ) // Dark Mode
        ButtonVariant.Red -> Triple(Color(0xFFFD5252), Color.White, Color.Transparent)
        ButtonVariant.White -> Triple(
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.outline
        )
    }

    // Animation states
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), // Snappy spring animation
        label = "ButtonScaleAnimation"
    )

    Row(
        modifier = modifier
            .scale(scale) // Applies the scale down effect
            .alpha(if (enabled) 1f else 0.5f)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(19.dp)
            )

            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
fun ButtonPreview() {
    Column(modifier = Modifier.fillMaxSize()) {

        // Upper half — Light mode
        MaterialTheme(colorScheme = lightColorScheme()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ButtonRowLight()
            }
        }

        // Lower half — Dark mode
        MaterialTheme(colorScheme = darkColorScheme()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF000000))

                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ButtonRowLight()
            }
        }
    }
}

@Composable
private fun ButtonRowLight() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            text = "Cancel",
            onClick = {},
            variant = ButtonVariant.Gray,
            modifier = Modifier.weight(1f)
        )

        Button(
            text = "Cancel",
            onClick = {},
            variant = ButtonVariant.White,
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            text = "Cancel",
            onClick = {},
            variant = ButtonVariant.Red,
            modifier = Modifier.weight(1f)
        )
    }
}