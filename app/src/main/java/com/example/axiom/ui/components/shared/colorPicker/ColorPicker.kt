package com.example.axiom.ui.components.shared.colorPicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single source of truth:
 * background color -> readable text color
 */
data class PickerColor(
    val background: Color,
    val text: Color
)

private val DefaultPalette = listOf(
    PickerColor(Color(0xFF3369ff), Color.White), // BabyBlue
    PickerColor(Color(0xFFFF80AB), Color.Black), // RedPink
    PickerColor(Color(0xFFA7FFEB), Color.White), // LightGreen
    PickerColor(Color(0xFFFF9E80), Color.Black), // RedOrange
    PickerColor(Color(0xFFEA80FC), Color.White), // Violet
    PickerColor(Color(0xFF0EA5E9), Color.White),  // Blue
    PickerColor(Color.Gray, Color.White),  // Gray
    PickerColor(Color(0xFFFFFFFF), Color.Black),  // White
    PickerColor(Color.Magenta, Color.White),  // Black

)

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    selectedBackground: Int?,
    onColorPicked: (background: Int?, textColor: Int) -> Unit,
    colors: List<PickerColor> = DefaultPalette,
    itemSize: Dp = 35.dp,
    shape: Shape = CircleShape,
    showDefaultOption: Boolean = true,
    defaultBackground: Color = MaterialTheme.colorScheme.background,
    defaultTextColor: Color = MaterialTheme.colorScheme.onBackground
) {
    val borderColor = MaterialTheme.colorScheme.onBackground

    val resolvedColors =
        if (showDefaultOption)
            listOf(PickerColor(defaultBackground, defaultTextColor)) + colors
        else
            colors

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(resolvedColors) { pickerColor ->

            val bgInt = pickerColor.background.toArgb()

            val isSelected =
                if (showDefaultOption && pickerColor.background == defaultBackground)
                    selectedBackground == null
                else
                    selectedBackground == bgInt

            Box(
                modifier = Modifier
                    .size(itemSize)
                    .clip(shape)
                    .background(pickerColor.background)
                    .clickable {
                        onColorPicked(
                            if (showDefaultOption && pickerColor.background == defaultBackground)
                                null
                            else
                                bgInt,
                            pickerColor.text.toArgb()
                        )
                    }
                    .border(
                        width = 2.dp,
                        color = if (isSelected) borderColor else Color.Transparent,
                        shape = shape
                    )
                    .padding(all = 4.dp),

                ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = borderColor
                    )
                }
            }
        }
    }
}
