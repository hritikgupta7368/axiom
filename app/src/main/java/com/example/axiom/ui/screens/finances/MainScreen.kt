package com.example.axiom.ui.screens.finances


import ActionItem
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.dataStore.FinancePreferences
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.cards.ActivityItem
import com.example.axiom.ui.components.shared.cards.HeaderCard
import com.example.axiom.ui.components.shared.cards.MetricCard
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.navigation.BillsActions
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceViewModel
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceViewModelFactory
import com.example.axiom.ui.screens.finances.seller.components.SellerListSheetWrapper
import com.example.axiom.ui.theme.AxiomTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navActions: BillsActions) {
    val context = LocalContext.current

    // DataStore Integration
    val financePreferences = remember { FinancePreferences(context) }
    val savedSellerName by financePreferences.selectedSellerFirmName.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }
    var selectedOrg by remember { mutableStateOf("Select Organization") }


    // Update UI when saved preference changes
    LaunchedEffect(savedSellerName) {
        if (savedSellerName != null) {
            selectedOrg = savedSellerName!!
        }
    }

    val viewModel: InvoiceViewModel = viewModel(
        factory = InvoiceViewModelFactory(context)
    )

    val metricState by viewModel.metricState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMetricData()
    }

    val totalProfit by viewModel.totalBusinessProfit.collectAsState()


//    val analyticsViewModel: BusinessAnalyticsViewModel = viewModel(
//        factory = BusinessAnalyticsViewModelFactory(context)
//    )
//    val totalSales by analyticsViewModel.totalSales.collectAsState()
//    val totalPurchases by analyticsViewModel.totalPurchases.collectAsState()


    val isDark = true


    val greenBgColor = if (isDark) Color(0xFF22C55E).copy(alpha = 0.1f) else Color(0xFFF0FDF4)
    val greenTextColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)

    val chartBarColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE5E7EB)
    val chartActiveBarColor = if (isDark) Color.White else Color(0xFF111827)

    AnimatedHeaderScrollView(
        largeTitle = "Welcome",
        subtitle = selectedOrg,
        isParentRoute = false,
        onHeaderClick = { showSheet = true }
    ) {
        item {
            // Main Content Area
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                HeaderCard(
                    surfaceColor = AxiomTheme.components.card.background,
                    borderColor = AxiomTheme.components.card.border,
                    textColor = AxiomTheme.components.card.title,
                    textMutedColor = AxiomTheme.components.card.subtitle,
                    badgeBgColor = AxiomTheme.components.card.BadgeBgColor,
                    badgeTextColor = AxiomTheme.components.card.BadgeTextColor
                )
            }
        }


        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Grid: Revenue & Active Users
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Revenue Card
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Sales",
                    valueMonthly = metricState.valueMonthly,
                    valueAnnually = metricState.valueAnnually, // Will auto-scale perfectly!
                    barHeightsMonthly = metricState.barHeightsMonthly,
                    barHeightsAnnually = metricState.barHeightsAnnually,
                    greenBgColor = AxiomTheme.colors.accentGreenBg,
                    greenTextColor = AxiomTheme.colors.accentGreen,
                    chartBarColor = AxiomTheme.components.card.chartBar,
                    chartActiveColor = AxiomTheme.components.card.chartActiveBar
                )

                // Active Users Card
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Purchases",
                    valueMonthly = "₹ 48.2k",
                    valueAnnually = "₹ 10,50,40,000", // Will auto-scale perfectly!
                    barHeightsMonthly = listOf(0.4f, 0.3f, 0.6f, 0.45f, 0.7f, 0.55f, 1.0f),
                    barHeightsAnnually = listOf(0.2f, 0.5f, 0.3f, 0.8f, 0.4f, 0.9f, 0.6f),
                    greenBgColor = greenBgColor,
                    greenTextColor = greenTextColor,
                    chartBarColor = chartBarColor,
                    chartActiveColor = chartActiveBarColor
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionItem(
                    "Invoice",
                    Icons.Outlined.Add,
                    onClick = { navActions.onNavigate("Invoices") }
                )
                ActionItem(
                    "Customer",
                    Icons.Outlined.Send,
                    onClick = { navActions.onNavigate("Customers") }
                )
                ActionItem(
                    "Product",
                    Icons.Outlined.Email,
                    onClick = { navActions.onNavigate("Products") }
                )
                ActionItem(
                    "Analytics",
                    Icons.Outlined.DateRange,
                    onClick = { navActions.onNavigate("Analytics") }
                )
            }
        }

        // Action Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionItem(
                    "Purchase",
                    Icons.Outlined.Add,
                    onClick = { navActions.onNavigate("Purchase") }
                )
                ActionItem(
                    "Supplier",
                    Icons.Outlined.Send,
                    onClick = { navActions.onNavigate("Suppliers") }
                )
                ActionItem(
                    "Quotation",
                    Icons.Outlined.Email,
                    onClick = { navActions.onNavigate("Quotations") }
                )
                ActionItem(
                    "Challan",
                    Icons.Outlined.DateRange,
                    onClick = { navActions.onNavigate("Challans") }
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }


        // Recent Activity Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AxiomTheme.colors.textPrimary
                )
                Text(
                    text = "View all",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AxiomTheme.colors.textSecondary
                )
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ActivityItem(
                    title = "New component added",
                    subtitle = "Button group with variants",
                    time = "2h ago",
                    icon = Icons.Outlined.Info,
                    surfaceColor = AxiomTheme.components.card.background,
                    borderColor = AxiomTheme.components.card.border,
                    textColor = AxiomTheme.components.card.title,
                    textMutedColor = AxiomTheme.components.card.subtitle,
                    iconBgColor = AxiomTheme.components.card.BadgeBgColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                ActivityItem(
                    title = "Theme updated",
                    subtitle = "Dark mode improvements",
                    time = "5h ago",
                    icon = Icons.Outlined.Info,
                    surfaceColor = AxiomTheme.components.card.background,
                    borderColor = AxiomTheme.components.card.border,
                    textColor = AxiomTheme.components.card.title,
                    textMutedColor = AxiomTheme.components.card.subtitle,
                    iconBgColor = AxiomTheme.components.card.BadgeBgColor
                )
            }
        }


        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                PerformanceChartSection()
            }
        }
    }
    AppBottomSheet(
        showSheet = showSheet,
        onDismiss = {
            showSheet = false

        }
    ) {
        SellerListSheetWrapper(
            onConfirmSelection = { selected ->
                scope.launch {
                    financePreferences.saveSelectedSellerFirm(
                        selected.id,
                        selected.businessName,
                        selected.stateCode.toString()
                    )
                }
                showSheet = false
            },
            onBack = {
                showSheet = false
            }
        )

    }
}


@Composable
fun PerformanceChartSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0x08FFFFFF))
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(40.dp))
            .padding(32.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "WEEKLY ACTIVITY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = AxiomTheme.components.card.subtitle,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Performance",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AxiomTheme.components.card.title
                    )
                }

                Surface(
                    color = Color(0x0DFFFFFF),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1).copy(alpha = alpha))
                        )
                        Text(
                            text = "Real-time",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AxiomTheme.components.card.subtitle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Simple chart representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw gradient area
                    val path = Path().apply {
                        moveTo(0f, height * 0.67f)
                        cubicTo(
                            width * 0.25f, height * 0.53f,
                            width * 0.25f, height * 0.73f,
                            width * 0.5f, height * 0.4f
                        )
                        cubicTo(
                            width * 0.75f, height * 0.4f,
                            width * 0.75f, height * 0.6f,
                            width, height * 0.27f
                        )
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }

                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x806366F1),
                                Color(0x006366F1)
                            )
                        )
                    )

                    // Draw line
                    val linePath = Path().apply {
                        moveTo(0f, height * 0.67f)
                        cubicTo(
                            width * 0.25f, height * 0.53f,
                            width * 0.25f, height * 0.73f,
                            width * 0.5f, height * 0.4f
                        )
                        cubicTo(
                            width * 0.75f, height * 0.4f,
                            width * 0.75f, height * 0.6f,
                            width, height * 0.27f
                        )
                    }

                    drawPath(
                        path = linePath,
                        color = Color(0xFF6366F1),
                        style = Stroke(width = 4f)
                    )

                    // Draw points
                    drawCircle(
                        color = Color(0xFF6366F1),
                        radius = 6f,
                        center = Offset(width * 0.25f, height * 0.73f),
                        style = Fill
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 4f,
                        center = Offset(width * 0.25f, height * 0.73f),
                        style = Fill
                    )

                    drawCircle(
                        color = Color(0xFF6366F1),
                        radius = 6f,
                        center = Offset(width * 0.5f, height * 0.4f),
                        style = Fill
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 4f,
                        center = Offset(width * 0.5f, height * 0.4f),
                        style = Fill
                    )

                    drawCircle(
                        color = Color(0xFF6366F1),
                        radius = 6f,
                        center = Offset(width * 0.75f, height * 0.6f),
                        style = Fill
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 4f,
                        center = Offset(width * 0.75f, height * 0.6f),
                        style = Fill
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("MOn", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = AxiomTheme.components.card.subtitle
                    )
                }
            }
        }
    }
}


