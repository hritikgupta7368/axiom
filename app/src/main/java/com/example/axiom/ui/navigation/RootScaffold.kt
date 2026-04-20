package com.example.axiom.ui.navigation


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.axiom.ui.screens.calendar.CalendarScreen
import com.example.axiom.ui.screens.finances.Invoice.CreateInvoiceScreen
import com.example.axiom.ui.screens.finances.Invoice.InvoicePreviewScreen
import com.example.axiom.ui.screens.finances.Invoice.InvoicesScreen
import com.example.axiom.ui.screens.finances.MainScreen
import com.example.axiom.ui.screens.finances.analytics.GSTAnalyticsScreen
import com.example.axiom.ui.screens.finances.analytics.GSTSummaryScreen
import com.example.axiom.ui.screens.finances.challan.ChallansScreen
import com.example.axiom.ui.screens.finances.customer.CustomerScreen
import com.example.axiom.ui.screens.finances.product.ProductScreen
import com.example.axiom.ui.screens.finances.purchase.CreatePurchaseScreen
import com.example.axiom.ui.screens.finances.purchase.PurchasePreviewScreen
import com.example.axiom.ui.screens.finances.purchase.PurchaseScreen
import com.example.axiom.ui.screens.finances.quotation.CreateQuotationScreen
import com.example.axiom.ui.screens.finances.quotation.QuotationPreviewScreen
import com.example.axiom.ui.screens.finances.quotation.QuotationRoute
import com.example.axiom.ui.screens.finances.suppliers.SupplierScreen
import com.example.axiom.ui.screens.home.HomeScreen
import com.example.axiom.ui.screens.notes.NoteEditorScreen
import com.example.axiom.ui.screens.notes.NotesScreen
import com.example.axiom.ui.screens.profile.ProfileScreen
import com.example.axiom.ui.screens.settings.BackupScreen
import com.example.axiom.ui.screens.settings.RestoreScreen
import com.example.axiom.ui.screens.settings.SettingsScreen
import com.example.axiom.ui.screens.space.WorkspaceScreen
import com.example.axiom.ui.screens.vault.VaultScreen
import com.example.axiom.ui.theme.AxiomTheme
import kotlin.math.roundToInt

sealed class InvoiceFormMode {
    object Create : InvoiceFormMode()
    data class Edit(val invoiceId: String) : InvoiceFormMode()
}

sealed class PurchaseFormMode {
    object Create : PurchaseFormMode()
    data class Edit(val purchaseId: String) : PurchaseFormMode()
}

sealed class QuotationFormMode {
    object Create : QuotationFormMode()
    data class Edit(val quotationId: String) : QuotationFormMode()
}


sealed class Route(open val route: String) {

    // Graphs
    data object Tabs : Route("tabs")

    // Tabs
    data object Home : Route("home")
    data object Bills : Route("bills")
    data object Calendar : Route("calendar")
    data object Workspace : Route("space")
    data object Settings : Route("settings")

    // Stack-only (no bottom bar)
    data object Invoices : Route("invoices")
    data object CreateInvoice : Route("invoice_form/create")

    data object EditInvoice : Route("invoice_form/edit/{invoiceId}") {
        fun createRoute(invoiceId: String) =
            "invoice_form/edit/$invoiceId"
    }

    data object EditPurchase : Route("purchase_form/edit/{purchaseId}") {
        fun createRoute(purchaseId: String) =
            "purchase_form/edit/$purchaseId"
    }

    data object InvoicePreview : Route("invoice_preview/{invoiceId}") {
        fun createRoute(invoiceId: String): String {
            return "invoice_preview/$invoiceId"
        }
    }


    data object Customers : Route("customers")
    data object Products : Route("products")
    data object Purchases : Route("purchases")
    data object CreatePurchase : Route("create_purchase")
    data object PurchasePreview : Route("purchase_preview")

    data object Suppliers : Route("suppliers")


    data object Profile : Route("profile")

    data object GSTAnalytics : Route("gst_analytics")

    data object GSTSummary : Route("gst_summary")

    data object Vault : Route("vault")

    data object Notes : Route("notes")

    data object NoteEditor : Route("note/{noteId}") {
        fun createRoute(noteId: Long) = "note/$noteId"
    }

    data object Backup : Route("backup")
    data object Restore : Route("restore")

    data object Quotations : Route("quotations")
    data object CreateQuotation : Route("create_quotation")
    data object EditQuotation : Route("edit_quotation/{quotationId}") {
        fun createRoute(quotationId: String) = "edit_quotation/$quotationId"
    }

    data object QuotationPreview : Route("quotation_preview/{quotationId}") {
        fun createRoute(quotationId: String) = "quotation_preview/$quotationId"
    }


    data object Challans : Route("challans")
    data object CreateChallan : Route("create_challan")


}

data class BillsActions(
    val onNavigatePreview: () -> Unit,
    val onOpenProfile: () -> Unit,
    val onNavigate: (String) -> Unit,
)

data object InvoicePreview : Route("invoice_preview/{invoiceId}") {
    fun createRoute(invoiceId: String) = "invoice_preview/$invoiceId"
}

// added for animtaion

sealed class BottomTab(override val route: String, val label: String) : Route(route) {
    data object Home : BottomTab("home", "Home")
    data object Bills : BottomTab("bills", "Bills")
    data object Calendar : BottomTab("calendar", "Plan")
    data object Workspace : BottomTab("space", "Space")
    data object Settings : BottomTab("settings", "Prefs")
}


@Composable
fun FloatingDockNavigationBar(
    tabRoutes: List<BottomTab>,
    currentRoute: String?,
    onNavigate: (BottomTab) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // High Contrast Theme Colors
    val dockBgColor = if (isDark) Color(0xFF18181A) else Color(0xFFFFFFFF)
    val dockBorderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF3F4F6)

    val activePillColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF09090B)
    val activeContentColor = if (isDark) Color(0xFF09090B) else Color(0xFFFFFFFF)
    val inactiveContentColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Prevents overlap with system Android gestures
            .padding(horizontal = 5.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = dockBgColor,
            shadowElevation = if (isDark) 0.dp else 16.dp,
            modifier = Modifier.border(1.dp, dockBorderColor, RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabRoutes.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    FloatingDockItem(
                        tab = tab,
                        isSelected = isSelected,
                        activePillColor = activePillColor,
                        activeContentColor = activeContentColor,
                        inactiveContentColor = inactiveContentColor,
                        onClick = { onNavigate(tab) }
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingDockItem(
    tab: BottomTab,
    isSelected: Boolean,
    activePillColor: Color,
    activeContentColor: Color,
    inactiveContentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Smooth color transitions
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) activeContentColor else inactiveContentColor,
        animationSpec = tween(300),
        label = "color"
    )

    // Spring animation for the active background pill pop effect
    val pillScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.5f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Remove ripple for custom premium feel
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // The Active Background Pill
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pillScale
                        scaleY = pillScale
                    }
                    .background(activePillColor, RoundedCornerShape(16.dp))
            )
        }

        // Icon & Label Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Note: Update painterResource logic here to match your custom drawables
            Icon(
                imageVector = when (tab) {
                    BottomTab.Home -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                    BottomTab.Calendar -> if (isSelected) Icons.Filled.DateRange else Icons.Outlined.DateRange
                    BottomTab.Settings -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
                    else -> Icons.Outlined.Home // Fallback, replace with your R.drawable code
                },
                contentDescription = tab.label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = tab.label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}


@Composable
fun RootScaffold(navController: NavHostController) {


    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val tabRoutes = listOf(
        BottomTab.Home,
        BottomTab.Bills,
        BottomTab.Calendar,
        BottomTab.Workspace,
        BottomTab.Settings
    )

    // FIX 1: Foolproof check. Instead of checking hierarchy, we directly check if the current route is one of our bottom tabs.
    val currentRoute = currentDestination?.route
    val showBottomBar = tabRoutes.any { it.route == currentRoute }

    // --- Auto-Hide Scroll Logic Setup ---
    val bottomBarHeightPx = remember { mutableFloatStateOf(0f) }
    val bottomBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx.floatValue - delta

                // Coerce between 0 (fully visible) and the EXACT measured height of the bar (fully hidden)
                bottomBarOffsetHeightPx.floatValue =
                    newOffset.coerceIn(0f, bottomBarHeightPx.floatValue)

                return Offset.Zero // Do not consume scroll, let the list scroll naturally
            }
        }
    }

    // Reset the bar to visible if we navigate to a new tab
    LaunchedEffect(currentRoute) {
        bottomBarOffsetHeightPx.floatValue = 0f
    }



    Scaffold(
        // Apply the scroll connection to the root so it listens to everything
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        containerColor = AxiomTheme.colors.background,

        // FIX 2: We leave bottomBar empty to completely remove the rigid "black box" space
    ) { innerPadding ->

        // Wrap NavHost and NavBar in a Box so the NavBar floats OVER the content
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Apply ONLY top padding so top app bars work, but bottom goes to the edge
                .padding(top = innerPadding.calculateTopPadding())
        ) {

            // 1. The Main Content (Draws fully behind the transparent nav bar)
            NavHost(
                navController = navController,
                startDestination = Route.Tabs.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    fadeIn(tween(250)) + scaleIn(initialScale = 0.98f, animationSpec = tween(250))
                },
                exitTransition = {
                    fadeOut(tween(250)) + scaleOut(targetScale = 1.02f, animationSpec = tween(250))
                },
                popEnterTransition = {
                    fadeIn(tween(250)) + scaleIn(initialScale = 1.02f, animationSpec = tween(250))
                },
                popExitTransition = {
                    fadeOut(tween(250)) + scaleOut(targetScale = 0.98f, animationSpec = tween(250))
                }
            ) {

                navigation(
                    startDestination = Route.Home.route,
                    route = Route.Tabs.route,

                    ) {
                    // 3. Pass state to HomeScreen
                    composable(Route.Home.route) {
                        HomeScreen()
                    }
                    composable(Route.Calendar.route) { CalendarScreen() }
                    composable(Route.Workspace.route) {
                        WorkspaceScreen(
                            onVaultPreview = {
                                navController.navigate(Route.Vault.route)
                            },
                            onNotesPreview = {
                                navController.navigate(Route.Notes.route)
                            }
                        )
                    }
                    composable(Route.Settings.route) {
                        SettingsScreen(
                            onOpenBackup = { navController.navigate(Route.Backup.route) },
                            onOpenRestore = { navController.navigate(Route.Restore.route) }
                        )
                    }
                    composable(Route.Bills.route) {
                        val actions = remember(navController) {
                            BillsActions(
                                onNavigatePreview = { navController.navigate(Route.InvoicePreview.route) },
                                onOpenProfile = { navController.navigate(Route.Profile.route) },
                                onNavigate = { label ->
                                    when (label) {
                                        "Invoices" -> navController.navigate(Route.Invoices.route)
                                        "Customers" -> navController.navigate(Route.Customers.route)
                                        "Products" -> navController.navigate(Route.Products.route)
                                        "Purchase" -> navController.navigate(Route.Purchases.route)
                                        "Analytics" -> navController.navigate(Route.GSTAnalytics.route)
                                        "Summary" -> navController.navigate(Route.GSTSummary.route)
                                        "Suppliers" -> navController.navigate(Route.Suppliers.route)
                                        "Quotations" -> navController.navigate(Route.Quotations.route)
                                        "Challans" -> navController.navigate(Route.Challans.route)
                                        else -> println("No route defined for $label")
                                    }
                                },
                            )
                        }
                        MainScreen(navActions = actions)
                    }
                }

                composable(Route.Backup.route) {
                    BackupScreen(onBack = { navController.popBackStack() })
                }
                composable(Route.Restore.route) {
                    RestoreScreen(onBack = { navController.popBackStack() })
                }
                composable(Route.Vault.route) {
                    VaultScreen(onBack = { navController.popBackStack() })
                }
                composable(Route.Quotations.route) {
                    QuotationRoute(
                        onBack = { navController.popBackStack() },
                        onCreateQuotation = { navController.navigate(Route.CreateQuotation.route) },
                        onQuotationPreview = { id ->
                            navController.navigate(Route.QuotationPreview.createRoute(id))

                        },

                        )
                }
                composable(Route.CreateQuotation.route) {
                    CreateQuotationScreen(
                        mode = QuotationFormMode.Create,
                        onBack = { navController.popBackStack() },
                        onInvoicePreview = { id ->
                            navController.navigate(Route.QuotationPreview.createRoute(id))
                        }
                    )
                }

                composable(
                    route = Route.QuotationPreview.route,
                    arguments = listOf(
                        navArgument("quotationId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val quotationId = backStackEntry.arguments?.getString("quotationId")

                    QuotationPreviewScreen(
                        quotationId = quotationId,
                        onBack = { navController.popBackStack() },
                        onEditQuotation = { id ->
                            navController.navigate(Route.EditQuotation.createRoute(id))
                        },


                        )


                }
                composable(Route.Notes.route) {
                    NotesScreen(
                        onBack = { navController.popBackStack() },
                        onOpenNote = { noteId ->
                            navController.navigate(Route.NoteEditor.createRoute(noteId))

                        }

                    )
                }
                composable(
                    route = Route.NoteEditor.route,
                    arguments = listOf(
                        navArgument("noteId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments!!.getLong("noteId")

                    NoteEditorScreen(
                        noteId = noteId,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Routes without bottom nav bar
                composable(Route.Profile.route) { ProfileScreen(onBack = { navController.popBackStack() }) }
                composable(Route.Challans.route) { ChallansScreen() }
                composable(Route.Invoices.route) {
                    InvoicesScreen(
                        onBack = { navController.popBackStack() },
                        onCreateInvoice = { navController.navigate(Route.CreateInvoice.route) },
                        onInvoiceClick = { invoiceId ->
                            navController.navigate(Route.InvoicePreview.createRoute(invoiceId))
                        }
                    )
                }

                composable(
                    route = Route.InvoicePreview.route,
                    arguments = listOf(
                        navArgument("invoiceId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val invoiceId = backStackEntry.arguments?.getString("invoiceId")

                    InvoicePreviewScreen(
                        invoiceId = invoiceId,
                        onBack = { navController.popBackStack() },
                        onEditInvoice = { id ->
                            navController.navigate(Route.EditInvoice.createRoute(id))
                        },
                    )

                }


                // CREATE
                composable(Route.CreateInvoice.route) {
                    CreateInvoiceScreen(
                        mode = InvoiceFormMode.Create,
                        onBack = { navController.popBackStack() },
                        onInvoicePreview = { invoiceId ->
                            navController.navigate(Route.InvoicePreview.createRoute(invoiceId))
                        }
                    )
                }

// EDIT
                composable(
                    route = Route.EditInvoice.route,
                    arguments = listOf(
                        navArgument("invoiceId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val invoiceId = backStackEntry.arguments!!.getString("invoiceId")!!

                    CreateInvoiceScreen(
                        mode = InvoiceFormMode.Edit(invoiceId),
                        onBack = { navController.popBackStack() },
                        onInvoicePreview = { id ->
                            navController.navigate(Route.InvoicePreview.createRoute(id))
                        }
                    )
                }

                composable(Route.Customers.route) {
                    CustomerScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Route.Products.route) {
                    ProductScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Route.Purchases.route) {
                    PurchaseScreen(
                        onBack = { navController.popBackStack() },
                        onCreatePurchase = { navController.navigate(Route.CreatePurchase.route) },
                        onEditPurchase = { id ->
                            navController.navigate(Route.EditPurchase.createRoute(id))
                        },
                    )
                }
                composable(Route.Suppliers.route) {
                    SupplierScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Route.CreatePurchase.route) {
                    CreatePurchaseScreen(
                        mode = PurchaseFormMode.Create,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Route.PurchasePreview.route) {
                    PurchasePreviewScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Route.GSTAnalytics.route) {
                    GSTAnalyticsScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Route.GSTSummary.route) {
                    GSTSummaryScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter) // Align to bottom of the Box
            ) {
                // Wrap the Bar in another Box just to handle the dynamic scroll Offset & Measurement
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            // Dynamically update the height limit so it knows exactly how far to hide
                            bottomBarHeightPx.floatValue = coordinates.size.height.toFloat()
                        }
                        .offset {
                            IntOffset(
                                x = 0,
                                y = bottomBarOffsetHeightPx.floatValue.roundToInt()
                            )
                        }
                ) {
                    FloatingDockNavigationBar(
                        tabRoutes = tabRoutes,
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route.route) {
                                popUpTo(Route.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }

}