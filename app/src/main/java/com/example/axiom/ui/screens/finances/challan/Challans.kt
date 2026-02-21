package com.example.axiom.ui.screens.finances.challan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.ui.components.shared.AnimatedHeaderScrollView

@Composable
fun ChallansScreen() {
    AnimatedHeaderScrollView(
        largeTitle = "Today",
        subtitle = "Wednesday, Jan 22",
//        onBackClick = {
//            // Handle your back navigation here
//            println("Back button clicked!")
//        }
    ) {
        // --- Schedule Section ---
        SectionTitle("Schedule")
        CardBlock {
            ScheduleItem(title = "Morning Routine", time = "6:00 AM", icon = Icons.Default.Add)
            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
            ScheduleItem(title = "Workout", time = "7:30 AM", icon = Icons.Default.DateRange)
            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
            ScheduleItem(title = "Team Standup", time = "9:00 AM", icon = Icons.Default.CheckCircle)
        }

        // --- Tasks Section ---
        SectionTitle("Tasks")
        CardBlock {
            TaskItem(title = "Review designs", initialDone = true)
            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
            TaskItem(title = "Update documentation", initialDone = false)
            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
            TaskItem(title = "Send weekly report", initialDone = false)
            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
            TaskItem(title = "Schedule meeting", initialDone = true)
        }

        SectionTitle("Tasks again")
        CardBlock {
            TaskItem(title = "Review designs", initialDone = true)
            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
            TaskItem(title = "Update documentation", initialDone = false)
            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
            TaskItem(title = "Send weekly report", initialDone = false)
            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
            TaskItem(title = "Schedule meeting", initialDone = true)
        }

        // --- Quick Actions Section ---
        SectionTitle("Quick Actions")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "New Task",
                icon = Icons.Default.Add,
                color = Color(0xFF30D158)
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Schedule",
                icon = Icons.Default.DateRange,
                color = Color(0xFF0A84FF)
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Saved",
                icon = Icons.Default.Email,
                color = Color(0xFFFF9F0A)
            )
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