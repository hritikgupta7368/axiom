// ui/icons/AppIcons.kt
package com.example.axiom.ui.components.shared.button

//import androidx.compose.foundation.layout.size
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.automirrored.filled.ArrowForward
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.*
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import com.example.axiom.R
//
//object AppIcons {
//    val Send: ImageVector = Icons.Filled.Send
//    val Back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
//    val ArrowForward: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
//    val Close: ImageVector = Icons.Filled.Close
//    val Add: ImageVector = Icons.Filled.Add
//    val Delete: ImageVector = Icons.Outlined.Delete
//    val Settings: ImageVector = Icons.Outlined.Settings
//    val Search: ImageVector = Icons.Outlined.Search
//    val Edit : ImageVector = Icons.Outlined.Edit
//    val notificationBell : ImageVector = Icons.Outlined.Notifications
//val Sun : ImageVector = Icon(painter = painterResource(id = R.drawable.workspace)
//}
//
//
//@Composable
//fun AppIconButton(
//    icon: ImageVector,
//    contentDescription: String?,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    tint: Color = MaterialTheme.colorScheme.onSurface,
//    iconSize: androidx.compose.ui.unit.Dp = 24.dp //32 for large
//) {
//    IconButton(
//        onClick = onClick,
//        modifier = modifier
//    ) {
//        Icon(
//            imageVector = icon,
//            contentDescription = contentDescription,
//            tint = tint,
//            modifier = Modifier.size(iconSize)
//        )
//    }
//}

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.axiom.R

sealed interface AppIcon {
    data class Vector(val imageVector: ImageVector) : AppIcon
    data class PainterRes(val resId: Int) : AppIcon
}


object AppIcons {

    val Send = AppIcon.Vector(Icons.Filled.Send)
    val Back = AppIcon.Vector(Icons.AutoMirrored.Filled.ArrowBack)
    val ArrowForward = AppIcon.Vector(Icons.AutoMirrored.Filled.ArrowForward)
    val Close = AppIcon.Vector(Icons.Filled.Close)
    val Add = AppIcon.Vector(Icons.Filled.Add)
    val Delete = AppIcon.Vector(Icons.Outlined.Delete)
    val Settings = AppIcon.Vector(Icons.Outlined.Settings)
    val Search = AppIcon.Vector(Icons.Outlined.Search)
    val Edit = AppIcon.Vector(Icons.Outlined.Edit)
    val NotificationBell = AppIcon.Vector(Icons.Outlined.Notifications)
    val Sun = AppIcon.PainterRes(R.drawable.sun)
    val Moon = AppIcon.PainterRes(R.drawable.moon)
    val copy = AppIcon.PainterRes(R.drawable.copy)
    val shield = AppIcon.PainterRes(R.drawable.shield)
    val key = AppIcon.PainterRes(R.drawable.key)
    val visibilityOn = AppIcon.PainterRes(R.drawable.visibilityon)
    val visibilityOff = AppIcon.PainterRes(R.drawable.visibilityoff)

}


@Composable
fun AppIconButton(
    icon: AppIcon,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: Dp = 24.dp
) {
    IconButton(onClick = onClick, modifier = modifier) {
        when (icon) {
            is AppIcon.Vector -> Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )

            is AppIcon.PainterRes -> Icon(
                painter = painterResource(icon.resId),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}



