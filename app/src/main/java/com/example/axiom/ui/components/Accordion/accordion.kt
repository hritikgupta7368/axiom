package com.example.axiom.ui.components.Accordion

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.ui.theme.AxiomTheme


@Composable
fun Accordion(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false, // Allows setting default state, but manages itself
    content: @Composable () -> Unit
) {


    val borderColor = AxiomTheme.components.card.border
    val surfaceColor = AxiomTheme.components.card.background
    val textMainColor = AxiomTheme.components.card.title
    val textMutedColor = AxiomTheme.components.card.subtitle

    // State is handled internally so the parent doesn't need to track it
    // rememberSaveable ensures it survives screen rotations
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    // Hardware-accelerated rotation for the icon
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "icon_rotation",
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            // This is the Compose equivalent of the CSS Grid trick.
            // It automatically measures and animates height changes smoothly.
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textMainColor
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = textMutedColor,
                modifier = Modifier.rotate(rotation)
            )
        }

        // Content divider & body
        if (expanded) {
            HorizontalDivider(color = borderColor)
            Column( // CHANGED from Box to Column
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp) // Automatically spaces your rows!
            ) {
                content()
            }
        }
    }
}