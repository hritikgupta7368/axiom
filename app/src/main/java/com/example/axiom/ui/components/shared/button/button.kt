package com.example.axiom.ui.components.shared.button

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A custom, reusable button component with support for loading and icons.
 *
 * @param text The text to display on the button.
 * @param onClick The callback to be invoked when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param icon The optional icon to display to the left of the text.
 * @param enabled Whether the button is enabled and can be interacted with.
 * @param loading Whether the button is in a loading state.
 * @param buttonColor The background color of the button when enabled. Defaults to MaterialTheme's primary color.
 * @param contentColor The color of the text and icon when enabled. Defaults to MaterialTheme's onPrimary color.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    buttonColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "button-scale"
    )

    val showIconSlot = loading || icon != null
    val isEffectivelyEnabled = enabled && !loading

    // --- START: Updated Color Logic ---
    val finalButtonColor = if (isEffectivelyEnabled) {
        buttonColor
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    val finalContentColor = if (isEffectivelyEnabled) {
        contentColor
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }
    // --- END: Updated Color Logic ---

    val rippleColor = contentColor.copy(alpha = 0.25f)
    val buttonShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isEffectivelyEnabled) 8.dp else 0.dp,
                shape = buttonShape,
                ambientColor = buttonColor,
                spotColor = buttonColor
            )
            .background(
                color = finalButtonColor, // Use the new variable
                shape = buttonShape
            )
            .clip(buttonShape)
            .clickable(
                enabled = isEffectivelyEnabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = rippleColor),
                onClick = onClick
            )
            .padding(horizontal = 24.dp)
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showIconSlot) {
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = finalContentColor, // Use the new variable
                            strokeWidth = 2.dp
                        )
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = finalContentColor, // Use the new variable
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Text(
                text = text,
                color = finalContentColor, // Use the new variable
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
                maxLines = 1
            )
        }
    }
}
