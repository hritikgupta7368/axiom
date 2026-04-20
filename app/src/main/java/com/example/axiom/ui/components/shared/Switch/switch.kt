package com.example.axiom.ui.components.shared.Switch

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==========================================
// SIZES & COLORS
// ==========================================
enum class SwitchSize(val width: Dp, val height: Dp, val thumbInset: Dp) {
    SM(width = 44.dp, height = 24.dp, thumbInset = 2.dp),
    MD(width = 56.dp, height = 32.dp, thumbInset = 2.dp)
}

val DefaultOnColor = Color(0xFF4CD964)
val DefaultOffColor = Color(0xFFE9E9EA)
val DefaultThumbColor = Color(0xFFFFFFFF)

@Composable
fun AnimatedSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: SwitchSize = SwitchSize.MD,
    enabled: Boolean = true,
    iconOn: ImageVector? = null,
    iconOff: ImageVector? = null,
    onColor: Color = DefaultOnColor,
    offColor: Color = DefaultOffColor,
    thumbColor: Color = DefaultThumbColor
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Calculate thumb dimensions based on the size variant and inset
    val thumbSize = size.height - (size.thumbInset * 2)
    val minOffset = size.thumbInset
    val maxOffset = size.width - thumbSize - size.thumbInset

    // === Animations ===
    // Spring physics matching your RN config (damping/stiffness)
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) maxOffset else minOffset,
        animationSpec = spring(
            dampingRatio = 0.7f, // Maps closely to your RN damping of 15
            stiffness = Spring.StiffnessMediumLow // Maps closely to RN stiffness 120
        ),
        label = "thumbOffset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) onColor else offColor,
        animationSpec = tween(durationMillis = 200),
        label = "trackColor"
    )

    val iconOpacityOn by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "iconOpacityOn"
    )

    val iconOpacityOff by animateFloatAsState(
        targetValue = if (checked) 0f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "iconOpacityOff"
    )

    // === UI ===
    Box(
        modifier = modifier
            .width(size.width)
            .height(size.height)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Removes the standard ripple so it feels like iOS/Custom
                enabled = enabled,
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) }
            )
            .alpha(if (enabled) 1f else 0.7f),
        contentAlignment = Alignment.CenterStart
    ) {
        // Thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(thumbColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Optional Thumb Icons overlapping each other with crossfade logic
            if (iconOn != null) {
                Icon(
                    imageVector = iconOn,
                    contentDescription = null,
                    tint = onColor,
                    modifier = Modifier
                        .size(thumbSize * 0.6f)
                        .alpha(iconOpacityOn)
                )
            }

            if (iconOff != null) {
                Icon(
                    imageVector = iconOff,
                    contentDescription = null,
                    tint = Color.Gray, // A neutral color for the off state icon
                    modifier = Modifier
                        .size(thumbSize * 0.6f)
                        .alpha(iconOpacityOff)
                )
            }
        }
    }
}


