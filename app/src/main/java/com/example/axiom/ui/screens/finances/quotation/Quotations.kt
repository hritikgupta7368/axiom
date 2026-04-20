package com.example.axiom.ui.screens.finances.quotation


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.EmptyState.EmptyScreen
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.quotation.components.QuotationCardDto
import com.example.axiom.ui.screens.finances.quotation.components.QuotationViewModel
import com.example.axiom.ui.screens.finances.quotation.components.QuotationViewModelFactory
import com.example.axiom.ui.theme.AxiomTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationRoute(
    onBack: () -> Unit,
    onCreateQuotation: () -> Unit,
    onQuotationPreview: (String) -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: QuotationViewModel = viewModel(
        factory = QuotationViewModelFactory(context)
    )

    val quotations by viewModel.quotationCards.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val isSearching = searchQuery.isNotEmpty()
    val isEmpty = quotations.isEmpty()

    AnimatedHeaderScrollView(
        largeTitle = "Quotation",
        onAddClick = { onCreateQuotation() },
        onBack = onBack,
        query = searchQuery,
        updateQuery = viewModel::updateSearchQuery,
    ) {

        if (isEmpty) {
            if (isSearching) {
                // CASE A: Searching, but no results
                item(key = "no_results_state") {
                    Column(
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AxiomTheme.components.card.mutedText.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AxiomTheme.components.card.title
                        )
                        Text(
                            text = "Try adjusting your search for \"$searchQuery\"",
                            fontSize = 14.sp,
                            color = AxiomTheme.components.card.subtitle
                        )
                    }
                }
            } else {
                // CASE B: Database is completely empty (No search active)
                // We show the empty state INSIDE the scroll view so the header stays!
                item(key = "database_empty_state") {

                    EmptyScreen(
                        title = "No invoices are there",
                        description = "No invoices found",
                        buttonText = "Add Invoice",
                        onAdd = { onCreateQuotation() },
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                    )
                }
            }
        } else {
            items(
                items = quotations,
                key = { it.id }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    QuotationCard(
                        quotation = it,
                        onClick = { onQuotationPreview(it.id) }
                    )
                }
            }
        }
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuotationCard(
    quotation: QuotationCardDto,
    onClick: () -> Unit,
) {
    // Interaction state for click bounce effect
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "card_scale"
    )


    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AxiomTheme.components.card.background),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
    ) {
        Column(
            Modifier
                .padding(14.dp)
                .clickable { onClick() }) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${quotation.quotationNumber}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = quotation.quotationDate.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = quotation.grandTotal.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(6.dp))


        }
    }
}







