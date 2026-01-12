package com.example.axiom.ui.screens.finances

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.domain.*
import com.example.axiom.data.finances.dataStore.FinancePreferences
import com.example.axiom.ui.navigation.BillsActions
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navActions : BillsActions) {
    val context = LocalContext.current
    val viewModel: FinancesViewModel = viewModel(factory = FinancesViewModelFactory())
    val sellerFirms by viewModel.sellerFirms.collectAsStateWithLifecycle()
    
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

    LaunchedEffect(sellerFirms) {
        if (sellerFirms.isNotEmpty()) {
            Log.d("DataLoaded", "sellerFirms now contains: ${sellerFirms.size} items")
        }
    }


    val actions = listOf(
        "Invoices" to Icons.Default.List,         // Substitute for Receipt
        "Customers" to Icons.Default.Person,     // Substitute for People
        "Products" to Icons.Default.ShoppingCart, // Present in default set
        "GST Analytics" to Icons.Default.AccountBox,
        "Purchase" to Icons.Default.Add,
        "Summary" to Icons.Default.Person
//        "Challans" to Icons.Default.Send,         // Substitute for Shipping
//        "Quotations" to Icons.Default.Search,       // Present in default set
//        "Credit Note" to Icons.Default.Clear,      // Substitute for RemoveCircle
//        "Pro Forma" to Icons.Default.Edit,         // Substitute for Description
//        "Expenses" to Icons.Default.AccountBox,    // Substitute for Wallet
        // Substitute for ShoppingCart (v2)
//        "Delivery" to Icons.Default.Check,         // Substitute for LocalMall
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
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
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = navActions.onOpenProfile) {
                        Icon(Icons.Default.AccountCircle, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        val horizontalPadding = 24.dp
        LazyColumn(
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                )
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
//            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            /** OVERVIEW **/
            item {
                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    Text(
                        text = "Overview",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OverviewCard(
                            title = "Sales",
                            value = "₹12,450",
                            icon = Icons.Default.Person,
                            accent = MaterialTheme.colorScheme.primary
                        )
                        OverviewCard(
                            title = "Users",
                            value = "342",
                            icon = Icons.Default.Person,
                            accent = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            /** QUICK ACTIONS **/
            item {
                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .height(260.dp)
                        .padding(horizontal = horizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(actions.size) { index ->
                        val (label, icon) = actions[index]
                        QuickActionItem(
                            label = label,
                            icon = icon
                        ) {
                            navActions.onNavigate(label)
                        }
                    }
                }
            }

            /** RECENT ACTIVITIES **/
            item {
                Card(
                    modifier = Modifier.padding(horizontal = horizontalPadding), // Apply padding
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recent Activities",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "View All",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        ActivityRow(
                            title = "New Report Generated",
                            subtitle = "Analysis Q3 • 2 mins ago",
                            icon = Icons.Default.Edit
                        )
                        ActivityRow(
                            title = "New Login Detected",
                            subtitle = "MacBook Pro • 1 hr ago",
                            icon = Icons.Default.Check
                        )
                    }
                }
            }
        }
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
                        financePreferences.saveSelectedSellerFirm(seller.id, seller.name, seller.stateCode)
                    }
                    showSheet = false // Close after selection
                },
                onAddFirm = { firm ->
                    viewModel.addSellerFirm(firm)
                    // Optionally auto-select the new firm
                    scope.launch {
                        financePreferences.saveSelectedSellerFirm(firm.id, firm.name, firm.stateCode)
                    }
                    showSheet = false // Close after creation
                }
            )
        }
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
        Text("Create New Organization", style = MaterialTheme.typography.titleMedium, color = Color.Black)

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
private fun OverviewCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accent)
                }
                Spacer(Modifier.width(8.dp))
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
private fun QuickActionItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun ActivityRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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



