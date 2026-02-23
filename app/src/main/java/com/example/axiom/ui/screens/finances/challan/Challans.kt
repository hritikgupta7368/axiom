package com.example.axiom.ui.screens.finances.challan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.ui.components.shared.AnimatedHeaderScrollView

//@Composable
//fun ChallansScreen() {
//    AnimatedHeaderScrollView(
//        largeTitle = "Today",
//        subtitle = "Wednesday, Jan 22",
////        onBackClick = {
////            // Handle your back navigation here
////            println("Back button clicked!")
////        }
//    ) {
//        // --- Schedule Section ---
//        SectionTitle("Schedule")
//        CardBlock {
//            ScheduleItem(title = "Morning Routine", time = "6:00 AM", icon = Icons.Default.Add)
//            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
//            ScheduleItem(title = "Workout", time = "7:30 AM", icon = Icons.Default.DateRange)
//            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
//            ScheduleItem(title = "Team Standup", time = "9:00 AM", icon = Icons.Default.CheckCircle)
//        }
//
//        // --- Tasks Section ---
//        SectionTitle("Tasks")
//        CardBlock {
//            TaskItem(title = "Review designs", initialDone = true)
//            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
//            TaskItem(title = "Update documentation", initialDone = false)
//            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
//            TaskItem(title = "Send weekly report", initialDone = false)
//            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
//            TaskItem(title = "Schedule meeting", initialDone = true)
//        }
//
//        SectionTitle("Tasks again")
//        CardBlock {
//            TaskItem(title = "Review designs", initialDone = true)
//            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
//            TaskItem(title = "Update documentation", initialDone = false)
//            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
//            TaskItem(title = "Send weekly report", initialDone = false)
//            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
//            TaskItem(title = "Schedule meeting", initialDone = true)
//        }
//
//        // --- Quick Actions Section ---
//        SectionTitle("Quick Actions")
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            QuickActionButton(
//                modifier = Modifier.weight(1f),
//                title = "New Task",
//                icon = Icons.Default.Add,
//                color = Color(0xFF30D158)
//            )
//            QuickActionButton(
//                modifier = Modifier.weight(1f),
//                title = "Schedule",
//                icon = Icons.Default.DateRange,
//                color = Color(0xFF0A84FF)
//            )
//            QuickActionButton(
//                modifier = Modifier.weight(1f),
//                title = "Saved",
//                icon = Icons.Default.Email,
//                color = Color(0xFFFF9F0A)
//            )
//        }
//    }
//}

// for use with serach
@Composable
fun ChallansScreen() {
    var selectedItemIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedItemIds.isNotEmpty()

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val iosBlue = Color(0xFF0A84FF)
    val iosYellow = Color(0xFFFFCC00) // Matched to your screenshot
    val iosGray = Color(0xFF8E8E93)
    val iosSearchBg = Color(0xFF1C1C1E)

    AnimatedHeaderScrollView(
        largeTitle = "Notes",
        subtitle = "14 Items",

        // --- 1. SEARCH BAR OVERLAY (Slides right-to-left) ---
//        searchBarContent = {
//            AnimatedVisibility(
//                visible = isSearchActive,
//                enter = slideInHorizontally(
//                    initialOffsetX = { fullWidth -> fullWidth },
//                    animationSpec = tween(300)
//                ) + fadeIn(tween(300)),
//                exit = slideOutHorizontally(
//                    targetOffsetX = { fullWidth -> fullWidth },
//                    animationSpec = tween(300)
//                ) + fadeOut(tween(300))
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp)
//                        .padding(bottom = 6.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Gray Input Field
//                    Row(
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(36.dp)
//                            .clip(RoundedCornerShape(10.dp))
//                            .background(iosSearchBg)
//                            .padding(horizontal = 8.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            Icons.Rounded.Search,
//                            contentDescription = null,
//                            tint = iosGray,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        BasicTextField(
//                            value = searchQuery,
//                            onValueChange = { searchQuery = it },
//                            textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
//                            cursorBrush = SolidColor(iosYellow),
//                            modifier = Modifier
//                                .weight(1f)
//                                .padding(horizontal = 8.dp),
//                            decorationBox = { innerTextField ->
//                                if (searchQuery.isEmpty()) {
//                                    Text("Search", color = iosGray, fontSize = 17.sp)
//                                }
//                                innerTextField()
//                            }
//                        )
//                        if (searchQuery.isNotEmpty()) {
//                            Icon(
//                                imageVector = Icons.Rounded.Close,
//                                contentDescription = "Clear",
//                                tint = Color.Black,
//                                modifier = Modifier
//                                    .size(16.dp)
//                                    .clip(CircleShape)
//                                    .background(iosGray)
//                                    .clickable { searchQuery = "" }
//                            )
//                        }
//                    }
//
//                    // Cancel Button
//                    Text(
//                        text = "Cancel",
//                        color = iosYellow,
//                        fontSize = 17.sp,
//                        modifier = Modifier
//                            .clickable {
//                                isSearchActive = false
//                                searchQuery = ""
//                            }
//                            .padding(start = 16.dp)
//                    )
//                }
//            }
//        },

        // --- 2. BACK BUTTON ---
//        leadingContent = {
//            AnimatedVisibility(visible = !isSearchActive, enter = fadeIn(), exit = fadeOut()) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(8.dp))
//                        .clickable { /* Handle Back */ }
//                        .padding(horizontal = 8.dp, vertical = 8.dp)
//                        .padding(bottom = 2.dp)
//                ) {
//                    AppIconButton(
//                        icon = AppIcons.Back_IOS,
//                        contentDescription = "Search",
//                        onClick = {  },
//                        tint = iosYellow
//                    )
//                    Text(
//                        text = "Folders",
//                        color = iosYellow,
//                        fontSize = 17.sp,
//                        modifier = Modifier.padding(start = 2.dp)
//                    )
//                }
////            }
//        },

//        leadingContent = {
//            Box(modifier = Modifier.wrapContentSize()) {
//                // --- Content (12 padding left) ---
//                Row(
//                    modifier = Modifier
//                        .padding(start = 12.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // --- Icon and Text (Gap 4) ---
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(4.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        // 1. Icon Component
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Standard back icon
//                            contentDescription = "Back",
//                            modifier = Modifier.size(24.dp),
//                            tint = iosYellow
//
//                        )
//                        // 2. Text Component
//                        Text(
//                            text = "Back",
//                            style = MaterialTheme.typography.bodyMedium
//                                .copy(color = iosYellow),
//
//                        )
//                    }
//                }
//            }
//        },


        // --- 3. ICONS (Search, Add -> Edit, Delete) ---
//        trailingContent = {
//            AnimatedVisibility(visible = !isSearchActive, enter = fadeIn(), exit = fadeOut()) {
//                AnimatedContent(
//                    targetState = isSelectionMode,
//                    transitionSpec = {
//                        if (targetState) {
//                            (slideInVertically(tween(300)) { it } + fadeIn(tween(300))) togetherWith (slideOutVertically(
//                                tween(300)
//                            ) { -it } + fadeOut(tween(300)))
//                        } else {
//                            (slideInVertically(tween(300)) { -it } + fadeIn(tween(300))) togetherWith (slideOutVertically(
//                                tween(300)
//                            ) { it } + fadeOut(tween(300)))
//                        }
//                    },
//                    label = "Actions"
//                ) { selectionActive ->
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(bottom = 2.dp)
//                    ) {
//                        if (selectionActive) {
//                            IconButton(onClick = { /* Edit */ }) {
//                                Icon(
//                                    Icons.Rounded.Edit,
//                                    contentDescription = "Edit",
//                                    tint = iosYellow,
//                                    modifier = Modifier.size(24.dp)
//                                )
//                            }
//                            IconButton(onClick = {
//                                selectedItemIds = emptySet()
//                            }) {
//                                Icon(
//                                    Icons.Rounded.Delete,
//                                    contentDescription = "Delete",
//                                    tint = Color(0xFFFF453A),
//                                    modifier = Modifier.size(24.dp)
//                                )
//                            }
//                        } else {
//                            IconButton(onClick = {
//                                isSearchActive = true
//                            }) {
//                                Icon(
//                                    Icons.Rounded.Search,
//                                    contentDescription = "Search",
//                                    tint = iosYellow,
//                                    modifier = Modifier.size(26.dp)
//                                )
//                            }
//                            IconButton(onClick = { /* Add */ }) {
//                                Icon(
//                                    Icons.Rounded.Add,
//                                    contentDescription = "Add",
//                                    tint = iosYellow,
//                                    modifier = Modifier.size(28.dp)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
    ) {
        // --- LIST CONTENT ---
        val dummyItems = (1..15).toList()

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E))
        ) {
            dummyItems.forEach { itemId ->
                val isSelected = selectedItemIds.contains(itemId)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedItemIds =
                                if (isSelected) selectedItemIds - itemId else selectedItemIds + itemId
                        }
                        .padding(16.dp)
                ) {
                    AnimatedVisibility(visible = isSelectionMode) {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(22.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(if (isSelected) iosYellow else Color.Transparent)
                                .border(
                                    1.5.dp,
                                    if (isSelected) iosYellow else Color.DarkGray,
                                    RoundedCornerShape(11.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "Item $itemId",
                        color = Color.White,
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (itemId != dummyItems.last()) Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Color(0xFF2C2C2E))
                )
            }
        }
    }
}


//preview
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=360dp,height=800dp,dpi=420"
)
@Composable
fun ChallansScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            ChallansScreen()
        }
    }
}
// --- Reusable UI Elements ---

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 13.sp,
        color = Color.Gray,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp, start = 4.dp)
    )
}

@Composable
fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1C1E))
    ) {
        content()
    }
}

@Composable
fun ScheduleItem(title: String, time: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0A84FF).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0A84FF),
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(text = title, fontSize = 16.sp, color = Color.White)
            Text(text = time, fontSize = 13.sp, color = Color.Gray)
        }
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color.DarkGray
        )
    }
}

@Composable
fun TaskItem(title: String, initialDone: Boolean) {
    var isDone by remember { mutableStateOf(initialDone) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isDone = !isDone }
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(2.dp, if (isDone) Color(0xFF30D158) else Color(0xFF444444), CircleShape)
                .background(if (isDone) Color(0xFF30D158) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Text(
            text = title,
            fontSize = 16.sp,
            color = if (isDone) Color.Gray else Color.White,
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun QuickActionButton(modifier: Modifier, title: String, icon: ImageVector, color: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1C1C1E))
            .clickable { /* action */ }
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, fontSize = 13.sp, color = Color.White)
    }
}