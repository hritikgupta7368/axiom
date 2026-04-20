package com.example.axiom.ui.screens.finances.product.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.axiom.ui.components.shared.bottomSheet.SearchBar
import com.example.axiom.ui.components.shared.button.Button
import com.example.axiom.ui.components.shared.button.ButtonVariant
import com.example.axiom.ui.components.shared.dialog.AppDialog
import com.example.axiom.ui.screens.finances.product.constants.GST_UNITS
import com.example.axiom.ui.theme.AxiomTheme
import kotlinx.coroutines.delay


@Composable
fun UnitSelectionDialog(
    onDismiss: () -> Unit,
    onUnitSelected: (String) -> Unit
) {
    // Internal state to handle the exit animation gracefully
    var isVisible by remember { mutableStateOf(true) }

    // Trigger exit animation, then notify parent to remove the component
    val triggerClose = {
        isVisible = false
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(650) // Wait for AnimatedDialog's exit animation to finish
            onDismiss()
        }
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredUnits = remember(searchQuery) {
        GST_UNITS.filter {
            it.first.contains(searchQuery, ignoreCase = true) ||
                    it.second.contains(searchQuery, ignoreCase = true)
        }
    }

    // Wrapped in your custom AnimatedDialog
    AppDialog(
        visible = isVisible,
        onDismissRequest = triggerClose
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = AxiomTheme.components.card.background,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Unit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AxiomTheme.components.card.title
                )

                Spacer(modifier = Modifier.height(16.dp))

                SearchBar(
                    containerWidth = 350.dp,
                    tint = Color.White,
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search Units",

                    )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredUnits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching units found",
                            color = AxiomTheme.components.card.subtitle
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(filteredUnits) { (code, name) ->
                            ListItem(
                                headlineContent = { Text(name, fontWeight = FontWeight.Medium, color = AxiomTheme.components.card.title) },
                                supportingContent = {
                                    Text(
                                        text = "Code: $code",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AxiomTheme.components.card.subtitle
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = AxiomTheme.components.card.background
                                ),
                                modifier = Modifier
                                    .clickable {
                                        onUnitSelected(code)
                                        triggerClose()
                                    }
                                    .fillMaxWidth()
                            )
                            HorizontalDivider(
                                color = AxiomTheme.components.card.border
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Button(
                    text = "Cancel",
                    onClick = triggerClose,
                    variant = ButtonVariant.White,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}