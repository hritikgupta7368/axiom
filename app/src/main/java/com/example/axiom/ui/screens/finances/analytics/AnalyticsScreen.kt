package com.example.axiom.ui.screens.finances.analytics


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.BusinessAnalyticsViewModel
import com.example.axiom.data.finances.BusinessAnalyticsViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale

// -----------------------------------------------------------------------------
// 1. DATA MODELS
// -----------------------------------------------------------------------------

data class GstSummary(
    val outputTax: Double, // Liability (Sales)
    val inputTax: Double,  // ITC (Purchases)
    val pendingItc: Double // Stuck ITC (Vendor hasn't filed)
)

data class TaxSlab(
    val percentage: String,
    val amount: Double,
    val color: Color
)

data class FilingStatus(
    val title: String,
    val dueDate: String,
    val status: String,
    val isCompleted: Boolean,
    val estimatedPay: Double? = null
)

data class DashboardUiState(
    val currentMonth: String = "October 2023",
    val complianceStatus: String = "Action Required",
    val summary: GstSummary = GstSummary(0.0, 0.0, 0.0),
    val slabs: List<TaxSlab> = emptyList(),
    val filings: List<FilingStatus> = emptyList()
)

// -----------------------------------------------------------------------------
// 2. VIEWMODEL
// -----------------------------------------------------------------------------

class GSTAnalyticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Simulating network delay for animation effect
        _uiState.value = DashboardUiState()
        loadDummyData()
    }

    private fun loadDummyData() {
        _uiState.value = DashboardUiState(
            currentMonth = "October 2023",
            complianceStatus = "Action Required",
            summary = GstSummary(
                outputTax = 125000.00,
                inputTax = 85000.00,
                pendingItc = 15000.00
            ),
            slabs = listOf(
                TaxSlab("5%", 5000.0, Color(0xFF4ADE80)),
                TaxSlab("12%", 12000.0, Color(0xFFFACC15)),
                TaxSlab("18%", 95000.0, Color(0xFF3B82F6)),
                TaxSlab("28%", 13000.0, Color(0xFFEF4444))
            ),
            filings = listOf(
                FilingStatus("GSTR-1", "11th Oct", "Data Ready", true),
                FilingStatus("GSTR-3B", "20th Oct", "Estimated Pay", false, 40000.00)
            )
        )
    }
}

// -----------------------------------------------------------------------------
// 3. MAIN SCREEN
// -----------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GSTAnalyticsScreen(
    onBack: () -> Unit, // Back navigation callback

) {

    val context = LocalContext.current
    val analyticsViewModel: BusinessAnalyticsViewModel = viewModel(
        factory = BusinessAnalyticsViewModelFactory(context)
    )
    val totalSales by analyticsViewModel.totalSales.collectAsState()
    val totalPurchases by analyticsViewModel.totalPurchases.collectAsState()

    val outputTax = totalSales * 0.18
    val inputTax = totalPurchases * 0.18
    val pendingItc = inputTax * 0.15

    val summary = GstSummary(
        outputTax = outputTax,
        inputTax = inputTax,
        pendingItc = pendingItc
    )

    val slabs = listOf(
        TaxSlab("5%", totalSales * 0.05, Color(0xFF4ADE80)),
        TaxSlab("12%", totalSales * 0.12, Color(0xFFFACC15)),
        TaxSlab("18%", totalSales * 0.18, Color(0xFF3B82F6))
    )

    val filings = listOf(
        FilingStatus(
            title = "GSTR-1",
            dueDate = "11th",
            status = "Data Ready",
            isCompleted = true
        ),
        FilingStatus(
            title = "GSTR-3B",
            dueDate = "20th",
            status = "Estimated Pay",
            isCompleted = false,
            estimatedPay = outputTax - inputTax
        )
    )

    val complianceStatus =
        if (outputTax > inputTax) "Action Required" else "Safe"


    // Animation Trigger State
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Sticky Header ---
            stickyHeader {
                GSTHeader(
                    month = "Current Month",
                    status = complianceStatus,
                    onBack = onBack
                )
            }

            // --- Section 1: Cash Flow Trio ---
            item {
                StaggeredEntry(visible = isVisible, index = 0) {
                    CashFlowSection(summary = summary)
                }
            }

            // --- Section 2: ITC Visualizer ---
            item {
                StaggeredEntry(visible = isVisible, index = 1) {
                    ITCVisualizerSection(summary = summary)

                }
            }

            // --- Section 3: Slab Distribution ---
            item {
                StaggeredEntry(visible = isVisible, index = 2) {
                    SlabDistributionSection(slabs = slabs)

                }
            }

            // --- Section 4: Filing Timeline ---
            item {
                StaggeredEntry(visible = isVisible, index = 3) {
                    FilingTimelineSection(filings = filings)

                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. ANIMATION HELPERS
// -----------------------------------------------------------------------------

@Composable
fun StaggeredEntry(
    visible: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { 50 * (index + 1) }, // Slight offset for slide effect
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = index * 100,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(durationMillis = 500, delayMillis = index * 100)
        )
    ) {
        content()
    }
}

@Composable
fun AnimatedCounter(
    targetValue: Double,
    style: TextStyle,
    color: Color = MaterialTheme.colorScheme.onSurface,
    prefix: String = ""
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Text(
        text = formatCurrency(animatedValue.value.toDouble()),
        style = style,
        color = color
    )
}

// -----------------------------------------------------------------------------
// 5. COMPONENTS
// -----------------------------------------------------------------------------

@Composable
fun GSTHeader(month: String, status: String, onBack: () -> Unit) {
    val isSafe = status == "Safe"
    val badgeColor =
        if (isSafe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val badgeBg = badgeColor.copy(alpha = 0.1f)

    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp // Slight shadow for sticky effect
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp) // Adjusted padding for back button
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "GST Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { /* Date Picker Logic */ }
                            .padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = month,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Month",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Compliance Badge
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(100),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = badgeColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CashFlowSection(summary: GstSummary) {
    val netPayable = summary.outputTax - summary.inputTax
    val isPayable = netPayable > 0

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Cash Flow")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Liability
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Output Tax",
                amount = summary.outputTax,
                color = MaterialTheme.colorScheme.onSurface
            )

            // ITC
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Input Credit",
                amount = summary.inputTax,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Net Payable Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (isPayable) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
            borderColor = if (isPayable) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Net Payable",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isPayable) "Liability" else "Carry Forward",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPayable) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                AnimatedCounter(
                    targetValue = Math.abs(netPayable),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isPayable) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier = Modifier, title: String, amount: Double, color: Color) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedCounter(
                targetValue = amount,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = color
            )
        }
    }
}

@Composable
fun ITCVisualizerSection(summary: GstSummary) {
    val totalITC = summary.inputTax + summary.pendingItc
    val matchedPercentage = if (totalITC > 0) (summary.inputTax / totalITC).toFloat() else 0f

    // Animation state
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(matchedPercentage) {
        // Delay slightly so the user sees the chart grow after the page slide-in
        delay(300)
        animatedProgress.animateTo(
            targetValue = matchedPercentage,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("ITC Reconciliation")
        Text(
            text = "Missing Credit: Supplier hasn't uploaded invoices",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(130.dp)
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val warningColor =
                        MaterialTheme.colorScheme.tertiary // Or specific warning color

                    Canvas(modifier = Modifier.size(130.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        val size = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // Background Arc (Pending)
                        drawArc(
                            color = warningColor.copy(alpha = 0.3f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft,
                            size = size
                        )

                        // Foreground Arc (Matched) - Animated
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress.value,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft,
                            size = size
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(animatedProgress.value * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Matched",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = "Matched ITC",
                        value = summary.inputTax
                    )
                    LegendItem(
                        color = MaterialTheme.colorScheme.tertiary,
                        label = "Pending ITC",
                        value = summary.pendingItc
                    )
                }
            }
        }
    }
}

@Composable
fun SlabDistributionSection(slabs: List<TaxSlab>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Tax Slab Distribution")

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                val maxAmount = slabs.maxOfOrNull { it.amount } ?: 1.0

                slabs.forEachIndexed { index, slab ->
                    val fillFraction = (slab.amount / maxAmount).toFloat()

                    // Animate Bar Width
                    val animatedWidth = remember { Animatable(0f) }
                    LaunchedEffect(fillFraction) {
                        delay(500 + (index * 100).toLong()) // Staggered bars
                        animatedWidth.animateTo(
                            targetValue = fillFraction,
                            animationSpec = tween(1000, easing = FastOutSlowInEasing)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Label
                        Text(
                            text = slab.percentage,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(40.dp)
                        )

                        // Bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(100))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedWidth.value)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(100))
                                    .background(slab.color)
                            )
                        }

                        // Value
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = formatCurrency(slab.amount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(80.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilingTimelineSection(filings: List<FilingStatus>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Filing Timeline")

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                filings.forEachIndexed { index, filing ->
                    TimelineItem(
                        filing = filing,
                        isLast = index == filings.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(filing: FilingStatus, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // Line & Dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            val dotColor =
                if (filing.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

            // Dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )

            // Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 24.dp)) {
            Text(
                text = filing.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Due: ${filing.dueDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (filing.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.5f
                    )
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = filing.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (filing.isCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (filing.estimatedPay != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AnimatedCounter(
                        targetValue = filing.estimatedPay,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 5. HELPER UI ELEMENTS
// -----------------------------------------------------------------------------

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.border(
                BorderStroke(1.dp, borderColor),
                RoundedCornerShape(24.dp)
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
fun LegendItem(color: Color, label: String, value: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedCounter(
                targetValue = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}
