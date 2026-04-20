package com.example.axiom.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.BuildConfig
import com.example.axiom.ui.components.shared.ThemeToggle
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.theme.AxiomTheme


private val TextGray = Color(0xFF666666)
private val IconBgColor = Color(0x260A84FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenBackup: () -> Unit,
    onOpenRestore: () -> Unit
) {
    AnimatedHeaderScrollView(
        largeTitle = "Settings",
        subtitle = "Manage your preferences",
        isParentRoute = false
    ) {

        // --- APPEARANCE SECTION ---
        item {
            SettingsSectionTitle("APPEARANCE")
        }
        item {
            SettingsCardContainer {
                SettingsRow(
                    icon = Icons.Default.Star, // Replace with a moon/sun icon
                    title = "Dark Mode",
                    subtitle = "Toggle application theme",
                    isLast = true,
                    trailingContent = {
                        ThemeToggle(showText = false) // <--- Just the switch!
                    }
                )
            }
        }


        // --- ACCOUNT SECTION ---
        item {
            SettingsSectionTitle("ACCOUNTS")
        }

        item {
            SettingsCardContainer {
                SettingsRow(
                    icon = Icons.Default.Person,
                    title = "Profile",
                    subtitle = "Manage your account",
                    isLast = false
                )
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = "Security",
                    subtitle = "Password, biometrics",
                    isLast = true
                )
            }
        }


        // --- DATA SECTION ---
        item {
            SettingsSectionTitle("DATA")
        }
        item {
            SettingsCardContainer {
                SettingsRow(
                    icon = Icons.Default.Check,
                    title = "Backup",
                    subtitle = "Export vault data to file",
                    onClick = onOpenBackup,
                    isLast = false
                )
                SettingsRow(
                    icon = Icons.Default.Build,
                    title = "Restore",
                    subtitle = "Import vault data from file",
                    onClick = onOpenRestore,
                    isLast = true
                )
            }
        }


        // --- ABOUT SECTION ---
        item {
            SettingsSectionTitle("ABOUT")
        }
        item {
            SettingsCardContainer {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = BuildConfig.VERSION_NAME,
                    isLast = false
                )
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "DB Version",
                    subtitle = BuildConfig.DB_VERSION.toString(),
                    isLast = false
                )
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "Terms & Privacy",
                    subtitle = "Legal information",
                    isLast = true
                )
            }
        }

    }
}


@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = TextGray,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 10.dp)
    )
}

@Composable
private fun SettingsCardContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AxiomTheme.components.card.background)
    ) {
        content()
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    isLast: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = {
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = Color(0xFF444444),
            modifier = Modifier.size(20.dp)
        )
    }
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Leading Icon Box
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(IconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF0A84FF), // iOS Blue tint
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title and Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = AxiomTheme.components.card.title,
                    fontSize = 17.sp,
                    letterSpacing = (-0.4).sp
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = AxiomTheme.components.card.subtitle,
                        fontSize = 13.sp
                    )
                }
            }

            // Trailing Content (Chevron by default)
            if (trailingContent != null) {
                trailingContent()
            }
        }

        // Bottom Divider
        if (!isLast) {
            HorizontalDivider(
                color = AxiomTheme.components.card.mutedText,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 68.dp) // Aligns with text start
            )
        }
    }
}



