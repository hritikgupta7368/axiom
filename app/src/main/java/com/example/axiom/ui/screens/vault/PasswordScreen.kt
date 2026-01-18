package com.example.axiom.ui.screens.vault

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// Data class for saved logins
data class SavedLogin(
    val name: String,
    val email: String,
    val icon: ImageVector,
    val password: String = "••••••••••"
)

@Composable
fun VaultScreen(
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Password generator state
    var generatedPassword by remember { mutableStateOf(generateSecurePassword()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var copiedMessage by remember { mutableStateOf(false) }

    // Dummy data for saved logins
    val savedLogins = remember {
        listOf(
            SavedLogin("Google", "user@gmail.com", Icons.Outlined.Email),
            SavedLogin("Netflix", "movie.fan@mail.com", Icons.Outlined.PlayArrow),
            SavedLogin("Chase Bank", "finance_guy88", Icons.Outlined.AccountCircle),
            SavedLogin("X / Twitter", "@design_guru", Icons.Outlined.AccountBox),
            SavedLogin("Amazon", "prime_member_01", Icons.Outlined.ShoppingCart)
        )
    }

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
                                clipboardManager.setText(AnnotatedString(generatedPassword))
                                copiedMessage = true
                            }
                        ) {
                            Icon(
                                imageVector = if (copiedMessage) Icons.Outlined.Check else Icons.Outlined.Notifications,
                                contentDescription = "Copy",
                                tint = if (copiedMessage) Color(0xFF14B3AB) else Color(0xFF9CA3AF)
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

            // Login Items
            savedLogins.forEachIndexed { index, login ->
                LoginItem(
                    login = login,
                    isVisible = visiblePasswords.contains(index),
                    onVisibilityToggle = {
                        visiblePasswords = if (visiblePasswords.contains(index)) {
                            visiblePasswords - index
                        } else {
                            visiblePasswords + index
                        }
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom nav
        }
    }
}

@Composable
fun LoginItem(
    login: SavedLogin,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { /* Handle item click */ },
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