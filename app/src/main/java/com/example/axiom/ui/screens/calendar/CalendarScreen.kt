package com.example.axiom.ui.screens.calendar

import android.graphics.Color.parseColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.axiom.data.calendar.domain.*
import com.example.axiom.ui.components.calender.ToggleableCalendar
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

// ... Helper functions ...
fun String.toComposeColor(): Color {
    return try {
        Color(parseColor(this))
    } catch (e: Exception) {
        Color.Gray // Fallback
    }
}

fun Color.toHex(): String {
    return String.format("#%06X", (0xFFFFFF and this.toArgb()))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var taskDate by remember { mutableStateOf(LocalDate.now()) }

    // State for Deletion Dialog
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTaskSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Apply the Scaffold's padding here
        ) {

            ToggleableCalendar(
                selectedDate = taskDate,
                onDateSelected = { date ->
                    taskDate = date
                    viewModel.setDate(date)
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${tasks.size} total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            LazyColumn(modifier = Modifier.weight(1f)) {

                item {
                    TaskList(
                        tasks = tasks,
                        onToggle = { task -> viewModel.toggleTask(task) },
                        onDelete = { task -> taskToDelete = task }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            // --- Create Task Sheet ---
            AppBottomSheet(
                showSheet = showCreateTaskSheet,
                onDismiss = { showCreateTaskSheet = false }
            ) {
                CreateTaskContent(
                    date = taskDate,
                    onDone = { title, notes, priority, colorHex, recurrence ->
                        val task = Task(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            notes = if (notes.isNotBlank()) TaskNotes(notes) else null,
                            createdAt = Instant.now(),
                            updatedAt = Instant.now(),
                            scheduledDate = taskDate,
                            priority = priority,
                            color = if (colorHex != null) TaskColor(colorHex) else null,
                            status = TaskStatus.PENDING,
                            recurrence = recurrence // Pass the recurrence object
                        )
                        viewModel.addTask(task)
                        showCreateTaskSheet = false
                    }
                )
            }

            // --- Deletion Confirmation Dialog ---
            if (taskToDelete != null) {
                AlertDialog(
                    onDismissRequest = { taskToDelete = null },
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    title = { Text(text = "Delete Task?") },
                    text = {
                        Text("Are you sure you want to delete '${taskToDelete?.title}'? This cannot be undone.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                taskToDelete?.let { viewModel.deleteTask(it) }
                                taskToDelete = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { taskToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskContent(
    date: LocalDate,
    onDone: (String, String, TaskPriority, String?, TaskRecurrence?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedColor by remember { mutableStateOf<Color?>(null) }

    // Recurrence State
    var recurrenceType by remember { mutableStateOf(RecurrenceType.NONE) }
    var recurrenceInterval by remember { mutableStateOf("1") }
    var isRecurrenceExpanded by remember { mutableStateOf(false) }

    val predefinedColors = listOf(
        Color(0xFFEF5350), Color(0xFFFFA726), Color(0xFFFFEE58),
        Color(0xFF66BB6A), Color(0xFF42A5F5), Color(0xFFAB47BC)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("New Task", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("What needs to be done?") },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = { Text("Add details...") },
            label = { Text("Notes") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Priority
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Priority", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskPriority.entries.forEach { priority ->
                    FilterChip(
                        selected = priority == selectedPriority,
                        onClick = { selectedPriority = priority },
                        label = { Text(priority.name.lowercase().capitalize()) },
                        leadingIcon = if (priority == selectedPriority) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null
                    )
                }
            }
        }

        // Recurrence (Repeating)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Repeat", style = MaterialTheme.typography.labelLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dropdown for Type
                ExposedDropdownMenuBox(
                    expanded = isRecurrenceExpanded,
                    onExpandedChange = { isRecurrenceExpanded = !isRecurrenceExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = recurrenceType.name.lowercase().capitalize().replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRecurrenceExpanded) },
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isRecurrenceExpanded,
                        onDismissRequest = { isRecurrenceExpanded = false }
                    ) {
                        RecurrenceType.entries
                            .filter { it != RecurrenceType.CUSTOM_RANGE } // Hide custom for simple UI
                            .forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name.lowercase().capitalize()) },
                                    onClick = {
                                        recurrenceType = type
                                        isRecurrenceExpanded = false
                                    }
                                )
                            }
                    }
                }

                // Interval Input (Only show if repeating)
                AnimatedVisibility(
                    visible = recurrenceType != RecurrenceType.NONE,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    OutlinedTextField(
                        value = recurrenceInterval,
                        onValueChange = { if (it.all { char -> char.isDigit() }) recurrenceInterval = it },
                        label = { Text("Every...") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        suffix = {
                            Text(when(recurrenceType) {
                                RecurrenceType.DAILY -> "Days"
                                RecurrenceType.WEEKLY -> "Wks"
                                RecurrenceType.MONTHLY -> "Mos"
                                else -> ""
                            })
                        }
                    )
                }
            }
        }

        // Color
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Color Tag", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(predefinedColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                selectedColor = if (selectedColor == color) null else color
                            }
                            .border(
                                if (selectedColor == color) 2.dp else 0.dp,
                                MaterialTheme.colorScheme.onSurface,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == color) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    // Construct Recurrence Object
                    val recurrence = if (recurrenceType != RecurrenceType.NONE) {
                        val interval = recurrenceInterval.toIntOrNull() ?: 1
                        TaskRecurrence(
                            type = recurrenceType,
                            interval = interval,
                            startDate = date,
                            endDate = null // Infinite recurrence by default
                        )
                    } else null

                    onDone(title.trim(), notes.trim(), selectedPriority, selectedColor?.toHex(), recurrence)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = title.isNotBlank()
        ) {
            Text("Save Task", modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
fun TaskList(
    tasks: List<Task>,
    onToggle: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tasks for this day",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tasks.forEach { task ->
                TaskRow(
                    task = task,
                    onToggle = { onToggle(task) },
                    onDelete = { onDelete(task) }
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val completed = task.status == TaskStatus.COMPLETED
    val haptic = LocalHapticFeedback.current

    val priorityColor = when (task.priority) {
        TaskPriority.CRITICAL -> MaterialTheme.colorScheme.error
        TaskPriority.HIGH -> Color(0xFFFF9800)
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.primary
        TaskPriority.LOW -> MaterialTheme.colorScheme.secondary
    }

    val taskColor = task.color?.hex?.toComposeColor() ?: Color.Transparent

    val alpha by animateFloatAsState(targetValue = if (completed) 0.5f else 1f, label = "alpha")
    val cardBgColor by animateColorAsState(
        targetValue = if (completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "bgColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { onToggle() },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if(completed) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = completed,
                onCheckedChange = { onToggle() },
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.priority != TaskPriority.MEDIUM && task.priority != TaskPriority.LOW) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Priority",
                            tint = priorityColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                if (task.notes != null && task.notes.raw.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.notes.raw,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (taskColor != Color.Transparent) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(taskColor)
                )
            }
        }
    }
}

private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
