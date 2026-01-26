package com.example.axiom.ui.screens.finances

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.SellerFirm
import com.example.axiom.data.finances.SellerFirmViewModel
import com.example.axiom.data.finances.SellerFirmViewModelFactory
import com.example.axiom.data.finances.dataStore.FinancePreferences
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.navigation.BillsActions
import kotlinx.coroutines.launch


data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navActions: BillsActions) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val viewModel: SellerFirmViewModel = viewModel(
        factory = SellerFirmViewModelFactory(context)
    )

    val sellerFirms by viewModel.sellers.collectAsStateWithLifecycle()

    // DataStore Integration
    val financePreferences = remember { FinancePreferences(context) }
    val savedSellerName by financePreferences.selectedSellerFirmName.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }
    var selectedOrg by remember { mutableStateOf("Select Organization") }
    var isCreatingNewFirm by remember { mutableStateOf(false) }

    // Update UI when saved preference changes
    LaunchedEffect(savedSellerName) {
        if (savedSellerName != null) {
            selectedOrg = savedSellerName!!
        }
    }


    val actions = listOf(
        QuickAction("Invoices", Icons.Default.List, Color(0xFF3B82F6)),
        QuickAction("Customers", Icons.Default.Person, Color(0xFF10B981)),
        QuickAction("Products", Icons.Default.ShoppingCart, Color(0xFFF97316)),
        QuickAction("Analytics", Icons.Default.AccountBox, Color(0xFFA855F7)),
        QuickAction("Purchase", Icons.Default.Add, Color(0xFFEC4899)),
        QuickAction("Summary", Icons.Default.Person, Color(0xFF22D3EE)),
        QuickAction("Challans", Icons.Default.Add, Color(0xFF6366F1)),
        QuickAction("Quotations", Icons.Default.Add, Color(0xFF3B82F6)),

        )

    val DarkBackgroundGradient = Brush.radialGradient(
        0f to Color(0x662563EB),   // electric blue core
        0.4f to Color(0x4D7C3AED), // indigo–violet mid
        0.75f to Color(0x3323123F),// deep violet shadow
        1f to Color(0xFF05010A),   // near-black edge
        center = Offset(0f, 0f),
        radius = 1500f
    )

    val LightBackgroundGradient = Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFEFF6FF), // very light blue core
            0.4f to Color(0xFFF5F3FF), // soft violet wash
            0.7f to Color(0xFFF8FAFC), // near-white
            1.0f to Color.White        // absolute white edge
        ),
        center = Offset.Unspecified,  // IMPORTANT: auto-centers
        radius = Float.POSITIVE_INFINITY
    )


    val isDark = isSystemInDarkTheme()



    Box(
        modifier = Modifier
            .fillMaxSize()
//            .background(
//                if (isSystemInDarkTheme())
//                    DarkBackgroundGradient
//                else
//                    SolidColor(MaterialTheme.colorScheme.background)
////                    LightBackgroundGradient
//            )
    )


    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column() {
                        Text(
                            text = "ORGANIZATION",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                showSheet = true
                            }

                        ) {
                            Text(
                                text = selectedOrg,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0x706366F1),
                                        Color(0x70A855F7)
                                    )
                                )
                            )
                            .blur(8.dp)
                    ) {}

                }
            }

            // space
            Spacer(modifier = Modifier.height(32.dp))

            // Active Projects
            ActiveProjectsSection()

            Spacer(modifier = Modifier.height(40.dp))

            // Quick Actions
            QuickActionsSection(
                actions = actions,
                onNavigate = { label ->
                    navActions.onNavigate(label)
                }
            )


            Spacer(modifier = Modifier.height(32.dp))

            // Overview Section
            OverviewSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Cards
            StatsCardsSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Performance Chart
            PerformanceChartSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Timeline
            TimelineSection()

            AppBottomSheet(
                showSheet = showSheet,
                onDismiss = {
                    showSheet = false

                }
            ) {
                OrganizationBottomSheetContent(
                    sellerFirms = sellerFirms,
                    onFirmSelected = { seller ->
                        scope.launch {
                            financePreferences.saveSelectedSellerFirm(
                                seller.id,
                                seller.name,
                                seller.stateCode
                            )
                        }
                        showSheet = false // Close after selection
                    },
                    onAddFirm = { firm ->
                        viewModel.insert(firm)
                        // Optionally auto-select the new firm
                        scope.launch {
                            financePreferences.saveSelectedSellerFirm(
                                firm.id,
                                firm.name,
                                firm.stateCode
                            )
                        }
                        showSheet = false // Close after creation
                    }
                )
            }
        }
    }
}


@Composable
fun ActiveProjectsSection() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Projects",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,

                )

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x1A6366F1)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "See all",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ProjectCard(
                title = "Project Alpha",
                category = "Digital Platform",
                status = "LIVE",
                progress = 0.75f,
                statusColor = Color(0xFF3B82F6),
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCC-aHyFZoD_kWlHXoWKoyKr9n5jwjjK4HIvq8rsXOuhy4DcJcK4k39y99VXcaxn2nwPgstQ7H770ShbIHUcrS33oAdm_fvjbSXnEDkVTy9Xchqf7g17V55PMKfyWFsIlir_n4PHyt_gZ6XJc3Lg9kiM84FlLKyEmRvJHG4cy3_Jd39y-s6W9ehiaRJIGvjAs8ZbFvrzC_NlCn4oGkUzw814vEZqEpMbdw5SeYSQ88wHxTnVbDrWCZl-PExsvabB1zrn5FPh313iwsW"
            )

            ProjectCard(
                title = "Q3 Strategy",
                category = "Finance",
                status = "OK",
                progress = null,
                statusColor = Color(0xFF10B981),
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAe5mE_c1MmJA5SrH-fqYrABc7Y8AnqE2tX0eosSPOfMUOTQNa30_3VHu1F2l0_dEZf4SvvmcXNJ7gNo0lNGG-0QlXHj1o7HlbbMAmrcumtVy098mMuZxBbJUP0bTHzBg5Vz0yTBQJ1ZmGelefPCMi9pwjyre8WSbFwpiuN0SZ7YBjvSDJntirhJ5p8VcL6HugSA-oVQjxZmshLy16f2Exsfqlz1zpztK3Vnt7YzcVZ4pGBy_90NXg1ef129YSB5a5MXm2C_Wu4VNKn",
                showTeam = true
            )
        }
    }
}

@Composable
fun ProjectCard(
    title: String,
    category: String,
    status: String,
    progress: Float?,
    statusColor: Color,
    imageUrl: String,
    showTeam: Boolean = false
) {
    val isDark = isSystemInDarkTheme()

    val glassBackground =
        if (isDark)
            Color(0x14FFFFFF)   // subtle white glass on dark
        else
            Color(0x99FFFFFF)   // soft white card on light

    val glassBorder =
        if (isDark)
            Color(0x26FFFFFF)
        else
            Color(0x1A000000)

    Box(
        modifier = Modifier
            .width(300.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(40.dp))
    ) {
        // Background Image


        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(glassBackground)
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,

                        )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
                ) {
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,

                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (progress != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Completion",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0x99FFFFFF)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,

                        )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x1AFFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF2563EB),
                                        Color(0xFF06B6D4)
                                    )
                                )
                            )
                    )
                }
            }

            if (showTeam) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262626))
                                .border(2.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF404040))
                                .border(2.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "MK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "+4 members",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0x66FFFFFF)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    actions: List<QuickAction>,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,

            )

        Spacer(modifier = Modifier.height(24.dp))



        actions.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { action ->
                    QuickActionItem(
                        label = action.label,
                        icon = action.icon,
                        color = action.color,
                        onClick = { onNavigate(action.label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }


    }
}

@Composable
fun QuickActionItem(
    label: String,
    icon: ImageVector,
    color: Color,
    isMore: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(color.copy(alpha = 0.1f))
                .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isMore) Color(0x66FFFFFF) else color,
                modifier = Modifier.size(30.dp)
            )
        }

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
//            color = if (isMore) Color(0x66FFFFFF) else Color(0xB3FFFFFF),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CreateSellerFirmForm(
    onCancel: () -> Unit,
    onCreate: (SellerFirm) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var stateCode by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Create New Organization",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = gstin,
            onValueChange = { gstin = it },
            label = { Text("GSTIN") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = stateCode,
            onValueChange = { stateCode = it },
            label = { Text("State Code") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contactNumber,
            onValueChange = { contactNumber = it },
            label = { Text("Contact Number") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (name.isNotBlank()) {
                    val firm = SellerFirm(
                        name = name,
                        gstin = gstin,
                        address = address,
                        stateCode = stateCode,
                        contactNumber = contactNumber,
                        email = email.ifBlank { null }
                    )
                    onCreate(firm)
                }
            }) {
                Text("Create")
            }
        }
    }
}

@Composable
private fun OrganizationBottomSheetContent(
    sellerFirms: List<SellerFirm>,
    onFirmSelected: (SellerFirm) -> Unit,
    onAddFirm: (SellerFirm) -> Unit
) {
    var isCreatingNewFirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // Header
        Text(
            text = if (isCreatingNewFirm) "Create Organization" else "Select Organization",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isCreatingNewFirm) {
            CreateSellerFirmForm(
                onCancel = { isCreatingNewFirm = false },
                onCreate = {
                    onAddFirm(it)
                    isCreatingNewFirm = false
                }
            )
            return
        }

        // Create action (secondary but visible)
        OutlinedButton(
            onClick = { isCreatingNewFirm = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("New Organization")
        }

        Spacer(Modifier.height(12.dp))

        Divider(color = Color.DarkGray.copy(alpha = 0.4f))

        Spacer(Modifier.height(8.dp))

        // List / Empty State
        if (sellerFirms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No organizations yet",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                items(sellerFirms, key = { it.id }) { seller ->
                    OrganizationRow(
                        seller = seller,
                        onClick = { onFirmSelected(seller) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrganizationRow(
    seller: SellerFirm,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = seller.name,
                style = MaterialTheme.typography.bodyLarge
            )
            if (!seller.gstin.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = seller.gstin,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}


@Composable
fun OverviewSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Overview",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "Updates for today, Oct 24",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0x66FFFFFF),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun StatsCardsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatsCard(
            title = "Total Revenue",
            value = "$142.8k",
            change = "+18.4%",
            iconColor = Color(0xFF3B82F6),
            modifier = Modifier.weight(1f)
        )

        StatsCard(
            title = "Active Users",
            value = "2,842",
            change = "+5.2%",
            iconColor = Color(0xFFA855F7),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    change: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0x08FFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0x66FFFFFF),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = change,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        }
    }
}

@Composable
fun PerformanceChartSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0x08FFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(40.dp))
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
                        color = Color(0x66FFFFFF),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Performance",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
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
                            color = Color(0xCCFFFFFF)
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
                        color = Color(0x4DFFFFFF)
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Timeline",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 20.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TimelineItem(
                title = "Invoice #9021 sent",
                subtitle = "To Global Tech • 2m ago",
                iconColor = Color(0xFF3B82F6)
            )

            TimelineItem(
                title = "Payment Received",
                subtitle = "From Sarah K. • 45m ago",
                iconColor = Color(0xFF10B981)
            )
        }
    }
}

@Composable
fun TimelineItem(
    title: String,
    subtitle: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x08FFFFFF))
            .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconColor.copy(alpha = 0.2f))
                .border(1.dp, iconColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0x66FFFFFF)
            )
        }

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = Color(0x33FFFFFF),
            modifier = Modifier.size(24.dp)
        )
    }
}

