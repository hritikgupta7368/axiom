package com.example.axiom.ui.components.shared.EmptyState

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons
import com.example.axiom.ui.components.shared.button.Button
import com.example.axiom.ui.components.shared.button.ButtonVariant
import com.example.axiom.ui.theme.AxiomTheme


@Composable
fun EmptyScreen(
    title: String,
    description: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String = "Add Item",

    ) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {


        AppIconButton(
            icon = AppIcons.Add,
            contentDescription = "Empty icon",
            tint = AxiomTheme.colors.textPrimary,
            onClick = {},
            iconSize = 40.dp
        )


        Spacer(modifier = Modifier.height(32.dp))

        // --- Title ---
        Text(
            text = title,
            color = AxiomTheme.colors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- Description ---
        Text(
            text = description,
            color = AxiomTheme.colors.textSecondary,
            fontSize = 14.5.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Action Button ---
        Button(
            text = buttonText,
            onClick = onAdd,
            variant = ButtonVariant.Gray,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        )

    }
}


