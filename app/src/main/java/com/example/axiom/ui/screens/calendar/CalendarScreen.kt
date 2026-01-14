package com.example.axiom.ui.screens.calendar

import android.graphics.Color.parseColor
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.axiom.data.calendar.domain.*
import com.example.axiom.ui.components.calender.ToggleableCalendar
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import com.kizitonwose.calendar.core.yearMonth

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
    val today = remember { LocalDate.now() }
    /* ---------------- SCREEN STATE ---------------- */

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isWeekMode by remember { mutableStateOf(false) }
    var visibleMonth by remember { mutableStateOf(selectedDate.yearMonth) }

    val tasks by viewModel.tasksForSelectedDate.collectAsState()

    var showCreateTaskSheet by remember { mutableStateOf(false) }

    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    var scrollTarget by remember { mutableStateOf<LocalDate?>(null) }

    var actionTask by remember { mutableStateOf<Task?>(null) }



    LaunchedEffect(scrollTarget) {
        scrollTarget?.let { scrollTarget = null }
    }



    /* ---------------- UI ---------------- */

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr),
//                    bottom = padding.calculateBottomPadding(),
                    top = 10.dp
                )
        ) {

            /* ---------- HEADER (CONTROL LIVES HERE) ---------- */


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {

                /* ---------- LEFT SLOT (ALWAYS PRESENT) ---------- */
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {

                    AnimatedContent(
                        targetState = actionTask != null,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "header-left-switch"
                    ) { isActionMode ->

                        if (isActionMode) {
                            // CANCEL ICON (REPLACES TITLE)
                            AppIconButton(
                                icon = AppIcons.Close,
                                contentDescription = "Cancel",
                                onClick = { actionTask = null }
                            )
                        } else {
                            // EXISTING TITLE ANIMATION — UNTOUCHED
                            AnimatedContent(
                                targetState = Pair(visibleMonth, isWeekMode),
                                transitionSpec = {
                                    (slideInVertically { it / 2 } + fadeIn())
                                        .togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                                },
                                label = "calendar-header"
                            ) { (month, weekMode) ->

                                Column(horizontalAlignment = Alignment.Start) {
                                    if (!weekMode) {
                                        Text(
                                            text = month.month.name.lowercase()
                                                .replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.displaySmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = month.year.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Text(
                                            text = today.dayOfWeek.name.lowercase()
                                                .replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.displaySmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${today.dayOfMonth} ${
                                                today.month.name.lowercase()
                                                    .replaceFirstChar { it.uppercase() }
                                            }",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                /* ---------- RIGHT ACTIONS (NEVER MOVE) ---------- */
                AnimatedContent(
                    targetState = actionTask != null,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "header-actions-switch"
                ) { isActionMode ->

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (isActionMode) {
                            AppIconButton(
                                icon = AppIcons.Edit,
                                contentDescription = "Edit",
                                onClick = {}
                            )
                            AppIconButton(
                                icon = AppIcons.Delete,
                                contentDescription = "Delete",
                                onClick = {
                                    viewModel.deleteTask(actionTask!!)
                                    actionTask = null
                                }
                            )
                        } else {
                            AppIconButton(
                                icon = AppIcons.Search,
                                contentDescription = "Search",
                                onClick = {}
                            )
                            IconButton(onClick = { isWeekMode = !isWeekMode }) {
                                Icon(
                                    imageVector = if (isWeekMode)
                                        Icons.Filled.Menu
                                    else
                                        Icons.Filled.DateRange,
                                    contentDescription = null
                                )
                            }
                            AppIconButton(
                                icon = AppIcons.ArrowForward,
                                contentDescription = "Today",
                                onClick = {
                                    val today = LocalDate.now()
                                    selectedDate = today
                                    viewModel.setDate(today)
                                    scrollTarget = today
                                }
                            )
                        }
                    }
                }

            }


            Spacer(Modifier.height(12.dp))


            /* ---------- CALENDAR (RENDER ONLY) ---------- */

            ToggleableCalendar(
                selectedDate = selectedDate,
                isWeekMode = isWeekMode,
                onDateSelected = {
                    selectedDate = it
                    viewModel.setDate(it)
                },
                onMonthChanged = {
                    visibleMonth = it
                },
                scrollToDate = scrollTarget
            )

            Spacer(Modifier.height(12.dp))

            /* ---------- TASK LIST ---------- */


            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "3 Events, 2 Tasks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (tasks.isEmpty()) {
                    item {
                        Text(
                            text = "No tasks for this day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                } else {
                    items(
                        items = tasks,
                        key = { it.id }
                    ) { task ->
                        SimpleTaskRow(
                            task = task,
                            onToggle = { viewModel.toggleTask(it) },
                            onLongPress = { actionTask = it }
                        )

                    }
                }
            }

            AppBottomSheet(
                showSheet = showCreateTaskSheet,
                onDismiss = { showCreateTaskSheet = false }
            ) {
                CreateTaskContent(
                    date = selectedDate,
                    onDone = { title, notes, priority, colorHex, recurrence ->
                        val task = Task(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            notes = if (notes.isNotBlank()) TaskNotes(notes) else null,
                            createdAt = Instant.now(),
                            updatedAt = Instant.now(),
                            scheduledDate = selectedDate,
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
fun SimpleTaskRow(
    task: Task,
    onToggle: (Task) -> Unit,
    onLongPress: (Task) -> Unit
) {
    val completed = task.status == TaskStatus.COMPLETED
    val haptic = LocalHapticFeedback.current

    val alpha by animateFloatAsState(
        targetValue = if (completed) 0.45f else 1f,
        label = "task-alpha"
    )

    val priorityColor = when (task.priority) {
        TaskPriority.CRITICAL -> MaterialTheme.colorScheme.error
        TaskPriority.HIGH -> Color(0xFFFF9800)
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.primary
        TaskPriority.LOW -> MaterialTheme.colorScheme.secondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle(task)
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress(task)
                }
            ),
        verticalAlignment = Alignment.Top
    ) {

        // DOT INDICATOR (changes when completed)
        Box(
            modifier = Modifier
                .size(8.dp)
                .offset(y = 6.dp)
                .background(
                    color = if (completed)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    else
                        priorityColor,
                    shape = CircleShape
                )
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {

            // TITLE
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (completed)
                    TextDecoration.LineThrough
                else
                    TextDecoration.None,
                color = if (completed)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // TIME (optional)

                Text(
                    text = "9:00 AM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

        }
    }
}


private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
