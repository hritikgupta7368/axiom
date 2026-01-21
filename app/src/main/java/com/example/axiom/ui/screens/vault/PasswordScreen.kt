package com.example.axiom.ui.screens.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.vault.VaultEntryEntity
import com.example.axiom.data.vault.VaultViewModel
import com.example.axiom.data.vault.VaultViewModelFactory
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Data class for saved logins
data class SavedLogin(
    val name: String,
    val email: String,
    val icon: ImageVector,
    val password: String = "••••••••••"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    var showCreateTaskSheet by remember { mutableStateOf(false) }

    val vaultViewModel: VaultViewModel = viewModel(
        factory = VaultViewModelFactory(context)
    )
    // get data from db
    val entries by vaultViewModel.entries.collectAsState(
        initial = emptyList()
    )
    var deleteTarget by remember { mutableStateOf<VaultEntryEntity?>(null) }


    // Password generator state
    var generatedPassword by remember { mutableStateOf(generateSecurePassword()) }
    var copiedMessage by remember { mutableStateOf(false) }


    var visiblePasswords by remember { mutableStateOf(setOf<Int>()) }

    // Animation for pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(copiedMessage) {
        if (copiedMessage) {
            kotlinx.coroutines.delay(2000)
            copiedMessage = false
        }
    }

    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTaskSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    )
    { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Ambient violet glow background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF271F36),
                                Color.Black
                            ),
                            center = androidx.compose.ui.geometry.Offset(0.5f, 0f),
                            radius = 1000f
                        )
                    )
            )
            // page layout starts here
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF14B3AB),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "VAULT",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = { /* Handle notifications */ },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Generator Section Header
                Text(
                    text = "GENERATE PASSWORD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9CA3AF),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Password Generator Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Color.White.copy(alpha = 0.03f)
                        )
                        .padding(24.dp)
                ) {
                    // Subtle glow effect
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .align(Alignment.TopEnd)
                            .blur(50.dp)
                            .background(Color(0xFF14B3AB).copy(alpha = 0.1f), CircleShape)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Password Display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = generatedPassword,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
//                                clipboardManager.setText(AnnotatedString(generatedPassword))

                                    copiedMessage = true
                                }
                            ) {
                                Icon(
                                    imageVector = if (copiedMessage) Icons.Outlined.Check else Icons.Outlined.Notifications,
                                    contentDescription = "Copy",
                                    tint = if (copiedMessage) Color(0xFF14B3AB) else Color(
                                        0xFF9CA3AF
                                    )
                                )
                            }
                        }

                        // Regenerate Button
                        Button(
                            onClick = {
                                generatedPassword = generateSecurePassword()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF14B3AB)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Regenerate",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        // Security Meter
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "WEAK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF9CA3AF),
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "HIGH SECURITY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF00FF00),
                                    letterSpacing = 1.5.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Low strength bar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF14B3AB).copy(alpha = 0.2f))
                                )
                                // Medium strength bar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF14B3AB).copy(alpha = 0.2f))
                                )
                                // High strength bar with pulse
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF00FF00))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.White.copy(alpha = pulseAlpha))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Saved Logins Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Logins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    TextButton(onClick = { /* View all */ }) {
                        Text(
                            text = "View All",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14B3AB)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                entries.forEachIndexed { index, entry ->
                    LoginItem(
                        login = SavedLogin(
                            name = entry.serviceName,
                            email = entry.username,
                            icon = Icons.Outlined.AccountCircle,
                            password = entry.password
                        ),
                        isVisible = visiblePasswords.contains(entry.id.toInt()),
                        onVisibilityToggle = {
                            visiblePasswords =
                                if (visiblePasswords.contains(entry.id.toInt()))
                                    visiblePasswords - entry.id.toInt()
                                else
                                    visiblePasswords + entry.id.toInt()
                        },
                        onLongPress = {
                            deleteTarget = entry
                        },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }


                Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom nav
            }
        }


        // outer components
        AppBottomSheet(
            showSheet = showCreateTaskSheet,
            onDismiss = { showCreateTaskSheet = false }
        ) {
            CreateVaultEntryContent(
                onDone = { service, user, password, note, icon, expiry ->
                    vaultViewModel.addEntry(
                        service = service,
                        user = user,
                        password = password,
                        expiry = expiry,
                        note = note
                    )
                    showCreateTaskSheet = false
                }
            )
        }
        // model to delete
        deleteTarget?.let { entry ->
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("Delete entry?") },
                text = {
                    Text("Delete ${entry.serviceName} (${entry.username})?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vaultViewModel.deleteEntry(entry)
                            deleteTarget = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTarget = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVaultEntryContent(
    onDone: (
        serviceName: String,
        username: String,
        password: String,
        note: String?,
        serviceIcon: String?,
        expiryDate: Long?
    ) -> Unit
) {
    var serviceName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var serviceIcon by remember { mutableStateOf("") }

    // Expiry
    var hasExpiry by remember { mutableStateOf(false) }
    var expiryDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = "New Vault Entry",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        // Service name
        OutlinedTextField(
            value = serviceName,
            onValueChange = { serviceName = it },
            label = { Text("Service") },
            placeholder = { Text("Google, Netflix…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username / Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Notes
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Service icon (optional url / key)
        OutlinedTextField(
            value = serviceIcon,
            onValueChange = { serviceIcon = it },
            label = { Text("Service Icon (optional)") },
            placeholder = { Text("icon url or key") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Expiry toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Expiry date", style = MaterialTheme.typography.labelLarge)
            Switch(
                checked = hasExpiry,
                onCheckedChange = {
                    hasExpiry = it
                    if (!it) expiryDate = null
                }
            )
        }

        // Expiry picker
        AnimatedVisibility(visible = hasExpiry) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        expiryDate?.toString() ?: "Select expiry date"
                    )
                }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()

                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let {
                                        expiryDate =
                                            Instant.ofEpochMilli(it)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save
        Button(
            onClick = {
                if (serviceName.isNotBlank() &&
                    username.isNotBlank() &&
                    password.isNotBlank()
                ) {
                    onDone(
                        serviceName.trim(),
                        username.trim(),
                        password,
                        note.takeIf { it.isNotBlank() },
                        serviceIcon.takeIf { it.isNotBlank() },
                        expiryDate?.atStartOfDay(ZoneId.systemDefault())
                            ?.toInstant()
                            ?.toEpochMilli()
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = serviceName.isNotBlank()
                    && username.isNotBlank()
                    && password.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save to Vault", modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}


@Composable
fun LoginItem(
    login: SavedLogin,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            ),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = login.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Name and email
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = login.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = login.email,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Password dots
                if (!isVisible) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4B5563))
                            )
                        }
                    }
                } else {
                    Text(
                        text = login.password,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                // Visibility toggle
                IconButton(
                    onClick = onVisibilityToggle,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isVisible) Icons.Outlined.Favorite else Icons.Filled.Favorite,
                        contentDescription = if (isVisible) "Hide" else "Show",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Password generator function
fun generateSecurePassword(length: Int = 12): String {
    val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lowercase = "abcdefghijklmnopqrstuvwxyz"
    val numbers = "0123456789"
    val special = "@#$%&*!?"
    val allChars = uppercase + lowercase + numbers + special

    return buildString {
        // Ensure at least one of each type
        append(uppercase.random())
        append(lowercase.random())
        append(numbers.random())
        append(special.random())

        // Fill the rest randomly
        repeat(length - 4) {
            append(allChars.random())
        }
    }.toList().shuffled().joinToString("")
}