// ui/icons/AppIcons.kt
package com.example.axiom.ui.components.shared.button

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppIcons {
    val Send: ImageVector = Icons.Filled.Send
    val Back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val ArrowForward: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
    val Close: ImageVector = Icons.Filled.Close
    val Add: ImageVector = Icons.Filled.Add
    val Delete: ImageVector = Icons.Outlined.Delete
    val Settings: ImageVector = Icons.Outlined.Settings
    val Search: ImageVector = Icons.Outlined.Search
    val Edit : ImageVector = Icons.Outlined.Edit
    val notificationBell : ImageVector = Icons.Outlined.Notifications
}


@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp //32 for large
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}