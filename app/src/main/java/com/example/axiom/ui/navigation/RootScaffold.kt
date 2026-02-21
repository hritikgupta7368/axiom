package com.example.axiom.ui.navigation


import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.axiom.R
import com.example.axiom.preferences.theme.ThemeMode
import com.example.axiom.preferences.theme.ThemeViewModel
import com.example.axiom.preferences.theme.ThemeViewModelFactory
import com.example.axiom.ui.screens.calendar.CalendarScreen
import com.example.axiom.ui.screens.finances.Invoice.CreateInvoiceScreen
import com.example.axiom.ui.screens.finances.Invoice.InvoicePreviewScreen
import com.example.axiom.ui.screens.finances.Invoice.InvoicesScreen
import com.example.axiom.ui.screens.finances.Invoice.PdfPreviewScreen
import com.example.axiom.ui.screens.finances.MainScreen
import com.example.axiom.ui.screens.finances.analytics.GSTAnalyticsScreen
import com.example.axiom.ui.screens.finances.analytics.GSTSummaryScreen
import com.example.axiom.ui.screens.finances.challan.ChallansScreen
import com.example.axiom.ui.screens.finances.customer.CustomerScreen
import com.example.axiom.ui.screens.finances.product.ProductScreen
import com.example.axiom.ui.screens.finances.purchase.CreatePurchaseScreen
import com.example.axiom.ui.screens.finances.purchase.PurchasePreviewScreen
import com.example.axiom.ui.screens.finances.purchase.PurchaseScreen
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

sealed class InvoiceFormMode {
    object Create : InvoiceFormMode()
    data class Edit(val invoiceId: String) : InvoiceFormMode()
}

sealed class Route(val route: String) {

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
    data object PdfPreview : Route("pdf_preview")

    data object Vault : Route("vault")

    data object Notes : Route("notes")

    data object NoteEditor : Route("note/{noteId}") {
        fun createRoute(noteId: Long) = "note/$noteId"
    }

    data object Backup : Route("backup")
    data object Restore : Route("restore")

    data object Quotations : Route("quotations")

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

sealed class BottomTab(route: String) : Route(route) {
    data object Home : BottomTab("home")
    data object Bills : BottomTab("bills")
    data object Calendar : BottomTab("calendar")
    data object Workspace : BottomTab("space")
    data object Settings : BottomTab("settings")
}


@Composable
fun RootScaffold(navController: NavHostController) {
    val context = LocalContext.current

    // Theme here
    val themeViewModel: ThemeViewModel = viewModel(
        factory = ThemeViewModelFactory(context)
    )
    val themeMode by themeViewModel.themeMode.collectAsState(
        initial = ThemeMode.DARK
    )


    // 2. Wrap everything in UltraTheme to apply the change dynamically
    AxiomTheme(darkTheme = themeMode == ThemeMode.DARK) {

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination

        val tabRoutes = listOf(
            BottomTab.Home,
            BottomTab.Bills,
            BottomTab.Calendar,
            BottomTab.Workspace,
            BottomTab.Settings
        )


        val showBottomBar = currentDestination
            ?.hierarchy
            ?.any { it.route == Route.Tabs.route } == true

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { it / 2 }
                    ) + fadeOut()
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 8.dp
                    ) {
                        tabRoutes.forEach { route ->
                            NavigationBarItem(
                                selected = currentDestination?.route == route.route,
                                onClick = {
                                    navController.navigate(route.route) {
                                        popUpTo(Route.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    when (route) {
                                        BottomTab.Home -> Icon(Icons.Default.Home, null)
                                        BottomTab.Bills -> Icon(
                                            painterResource(R.drawable.analytics),
                                            null
                                        )

                                        BottomTab.Calendar -> Icon(Icons.Default.DateRange, null)
                                        BottomTab.Workspace -> Icon(
                                            painterResource(R.drawable.workspace),
                                            null
                                        )

                                        BottomTab.Settings -> Icon(Icons.Default.Settings, null)
                                    }

                                },
                                label = { Text(route.route) }
                            )
                        }
                    }
                }
            }

        ) { padding ->

            NavHost(
                navController = navController,
                startDestination = Route.Tabs.route,
                modifier = Modifier.padding(padding)
            ) {

                navigation(
                    startDestination = Route.Home.route,
                    route = Route.Tabs.route
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
                    QuotationRoute(onBack = { navController.popBackStack() })
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
                        onNavigateToPdfViewer = { pdfUri ->
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("pdf_uri", pdfUri)

                            navController.navigate(Route.PdfPreview.route)
                        }
                    )

                }

                composable(Route.PdfPreview.route) { backStackEntry ->
                    // Retrieve the URI from the savedStateHandle
                    val pdfUri = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<Uri>("pdf_uri")

                    if (pdfUri != null) {
                        PdfPreviewScreen(
                            pdfUri = pdfUri,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        // Handle error case where URI is null, maybe just pop back
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
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
                    )
                }
                composable(Route.Suppliers.route) {
                    SupplierScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Route.CreatePurchase.route) {
                    CreatePurchaseScreen(
                        onBack = { navController.popBackStack() },
                        onPurchasePreview = { navController.navigate(Route.PurchasePreview.route) }
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
        }
    }
}
