package com.example.axiom.ui.screens.calendar


import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.temp.CalendarViewModel
import com.example.axiom.data.temp.CalendarViewModelFactory
import com.example.axiom.data.temp.EventEntity
import com.example.axiom.data.temp.Priority
import com.example.axiom.data.temp.TaskEntity
import com.example.axiom.data.temp.TaskStatus
import com.example.axiom.ui.components.calender.ToggleableCalendar
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons
import com.kizitonwose.calendar.core.yearMonth
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId


sealed interface ActionItem {
    data class Task(val value: TaskEntity) : ActionItem
    data class Event(val value: EventEntity) : ActionItem
}

sealed interface DetailItem {
    data class Task(val value: TaskEntity) : DetailItem
    data class Event(val value: EventEntity) : DetailItem
}

private fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()

private fun formatTime(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .withSecond(0)
        .withNano(0)
        .toString()

private fun formatDateTime(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .withNano(0)
        .toString()


private fun formatTimeTask(task: TaskEntity): String {
    if (task.allDay) return "All day"

    val zone = ZoneId.of(task.timeZone)
    val start = Instant.ofEpochMilli(task.startTime).atZone(zone).toLocalTime()
    val end = Instant.ofEpochMilli(task.endTime).atZone(zone).toLocalTime()
    return "${start} – ${end}"
}

@Composable
private fun priorityColor(priority: Priority, completed: Boolean): Color {
    if (completed) return MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)

    return when (priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
        Priority.LOW -> Color(0xFF49454F)
        Priority.CRITICAL -> MaterialTheme.colorScheme.error

    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(context)
    )


    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var visibleMonth by remember { mutableStateOf(selectedDate.yearMonth) }
    var isWeekMode by remember { mutableStateOf(false) }

    val tasks by viewModel.tasks.collectAsState()
    val events by viewModel.events.collectAsState()


    var actionItem by remember { mutableStateOf<ActionItem?>(null) }

    var scrollTarget by remember { mutableStateOf<LocalDate?>(null) }
    var detailItem by remember { mutableStateOf<DetailItem?>(null) }

    fun selectDate(date: LocalDate) {
        viewModel.selectDay(
            date.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
    }

    LaunchedEffect(Unit) {
        selectDate(selectedDate)
    }

    var fabExpanded by remember { mutableStateOf(false) }
    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var showCreateEventSheet by remember { mutableStateOf(false) }
    val eventCount = events.size
    val taskCount = tasks.size


    var searchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }



    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            fabExpanded = false
                            showCreateTaskSheet = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Create Task")
                    }
                }

                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            fabExpanded = false
                            showCreateEventSheet = true
                        },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Create Event")
                    }
                }

                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded }
                ) {
                    Icon(
                        imageVector = if (fabExpanded)
                            Icons.Default.Close
                        else
                            Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr),
                    top = 10.dp
                )
        ) {

            /* ---------- HEADER ---------- */


            AnimatedContent(
                targetState = searchMode,
                transitionSpec = {
                    fadeIn() + slideInVertically { -it / 2 } togetherWith
                            fadeOut() + slideOutVertically { it / 2 }
                },
                label = "header-root"
            ) { isSearch ->

                if (isSearch) {

                    // SEARCH HEADER (FULL OVERRIDE)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search tasks & events") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(Modifier.width(12.dp))

                        TextButton(
                            onClick = {
                                searchQuery = ""
                                searchMode = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }

                } else {

                    // ORIGINAL HEADER — UNTOUCHED LOGIC
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            AnimatedContent(
                                targetState = actionItem != null,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "header-left"
                            ) { actionMode ->
                                if (actionMode) {
                                    AppIconButton(
                                        icon = AppIcons.Close,
                                        contentDescription = null,
                                        onClick = { actionItem = null }
                                    )
                                } else {
                                    AnimatedContent(
                                        targetState = Pair(visibleMonth, isWeekMode),
                                        transitionSpec = {
                                            (slideInVertically { it / 2 } + fadeIn())
                                                .togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                                        },
                                        label = "month-switch"
                                    ) { (month, week) ->
                                        Column {
                                            if (!week) {
                                                Text(
                                                    month.month.name.lowercase()
                                                        .replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.displaySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    month.year.toString(),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            } else {
                                                Text(
                                                    selectedDate.dayOfWeek.name.lowercase()
                                                        .replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.displaySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    "${selectedDate.dayOfMonth} ${
                                                        selectedDate.month.name.lowercase()
                                                            .replaceFirstChar { it.uppercase() }
                                                    }",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedContent(
                            targetState = actionItem != null,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "header-right"
                        ) { actionMode ->
                            Row {
                                if (actionMode) {
                                    AppIconButton(
                                        icon = AppIcons.Edit,
                                        contentDescription = null,
                                        onClick = {}
                                    )
                                    AppIconButton(
                                        icon = AppIcons.Delete,
                                        contentDescription = null,
                                        onClick = {
                                            when (val item = actionItem) {
                                                is ActionItem.Task -> viewModel.deleteTask(item.value)
                                                is ActionItem.Event -> viewModel.deleteEvent(item.value)
                                                null -> {}
                                            }
                                            actionItem = null
                                        }
                                    )
                                } else {
                                    AppIconButton(
                                        icon = AppIcons.Search,
                                        contentDescription = null,
                                        onClick = { searchMode = true }
                                    )
                                    IconButton(onClick = { isWeekMode = !isWeekMode }) {
                                        Icon(
                                            if (isWeekMode) Icons.Default.Menu
                                            else Icons.Default.DateRange,
                                            null
                                        )
                                    }
                                    AppIconButton(
                                        icon = AppIcons.ArrowForward,
                                        contentDescription = null,
                                        onClick = {
                                            selectedDate = LocalDate.now()
                                            selectDate(selectedDate)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }


            // actual body starts here
            ToggleableCalendar(
                selectedDate = selectedDate,
                isWeekMode = isWeekMode,
                onDateSelected = {
                    selectedDate = it
                    selectDate(it)
                },
                onMonthChanged = { visibleMonth = it },
                scrollToDate = scrollTarget
            )

            Spacer(Modifier.height(12.dp))

            /* ---------- LIST ---------- */

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 1.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "$eventCount Events, $taskCount Tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }


                if (tasks.isEmpty() && events.isEmpty()) {
                    item {
                        Text(
                            "Nothing scheduled",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                /* ---------- EVENTS ---------- */

                items(events, key = { "event-${it.id}" }) { event ->

                    val bgColor =
                        Color(event.color).copy(alpha = 0.12f)

                    val accentColor =
                        Color(event.color)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .combinedClickable(
                                onClick = {
                                    detailItem = DetailItem.Event(event)
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    actionItem = ActionItem.Event(event)
                                }
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Left accent bar
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(42.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(accentColor)
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = event.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text =
                                    if (event.allDay) {
                                        "All day"
                                    } else {
                                        val zone = ZoneId.systemDefault()
                                        val start =
                                            Instant.ofEpochMilli(event.startTime)
                                                .atZone(zone)
                                                .toLocalTime()
                                        val end =
                                            Instant.ofEpochMilli(event.endTime)
                                                .atZone(zone)
                                                .toLocalTime()
                                        "$start – $end"
                                    },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }


                /* ---------- TASKS ---------- */


                items(tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onClick = { detailItem = DetailItem.Task(task) },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            actionItem = ActionItem.Task(task)
                        },
                        onToggleComplete = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.completeTask(task.id)
                        }
                    )
                }


            }

            /* ---------- CREATE TASK ---------- */

            AppBottomSheet(
                showSheet = showCreateTaskSheet,
                onDismiss = { showCreateTaskSheet = false }
            ) {
                CreateTaskSheet(
                    selectedDate = selectedDate,
                    onCreate = { task ->
                        viewModel.addTask(
                            title = task.title,
                            note = task.note,
                            date = task.date,
                            start = task.startTime,
                            end = task.endTime,
                            priority = task.priority,
                            color = task.color,
                            recurrenceRule = task.recurrenceRule,
                            sortIndex = task.sortIndex,
                            timeZone = task.timeZone,
                            allDay = task.allDay
                        )
                        showCreateTaskSheet = false
                    }
                )
            }
            /* ---------- CREATE EVENT ---------- */

            AppBottomSheet(
                showSheet = showCreateEventSheet,
                onDismiss = { showCreateEventSheet = false }
            ) {
                CreateEventSheet(
                    selectedDate = selectedDate,
                    onCreate = { event ->
                        viewModel.addEvent(
                            title = event.title,
                            description = event.description,
                            date = event.date,
                            start = event.startTime,
                            end = event.endTime,
                            importance = event.importance,
                            pinned = event.pinned,
                            color = event.color,
                            timeZone = event.timeZone
                        )
                        showCreateEventSheet = false
                    }
                )
            }
            /* ---------- Task / Event Details ---------- */
            AppBottomSheet(
                showSheet = detailItem != null,
                onDismiss = { detailItem = null }
            ) {
                when (val item = detailItem) {
                    is DetailItem.Task -> TaskDetailSheet(item.value)
                    is DetailItem.Event -> EventDetailSheet(item.value)
                    null -> {}
                }
            }


        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    task: TaskEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val completed = task.status == TaskStatus.COMPLETED
    val alpha by animateFloatAsState(if (completed) 0.5f else 1f, label = "")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 0.dp, vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {

        // Rounded checkbox
        Surface(
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
                .clickable { onToggleComplete() },
            shape = CircleShape,
            color = if (completed)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            border = if (!completed)
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            else null
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // Content column with bottom divider
        Column(
            modifier = Modifier
                .weight(1f)

        ) {

            Text(
                text = task.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                color = priorityColor(task.priority, completed),
                textDecoration = if (completed)
                    TextDecoration.LineThrough
                else TextDecoration.None
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = if (task.allDay) "ALL DAY" else formatTimeTask(task),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskSheet(
    selectedDate: LocalDate,
    onCreate: (TaskEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var allDay by remember { mutableStateOf(true) }

    val zone = ZoneId.systemDefault()
    val dayStartMillis =
        selectedDate.atStartOfDay(zone).toInstant().toEpochMilli()

    val dayEndMillis =
        selectedDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

    var startTime by remember {
        mutableStateOf(LocalTime.of(9, 0))
    }

    var endTime by remember {
        mutableStateOf(LocalTime.of(10, 0))
    }


    val primaryColor =
        MaterialTheme.colorScheme.primary.toArgb()

    val context = LocalContext.current

    fun showTimePicker(
        initial: LocalTime,
        onTimeSelected: (LocalTime) -> Unit
    ) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                onTimeSelected(LocalTime.of(hour, minute))
            },
            initial.hour,
            initial.minute,
            false // false = 12-hour, true = 24-hour
        ).show()
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Create Task", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.values().forEach {
                FilterChip(
                    selected = it == priority,
                    onClick = { priority = it },
                    label = { Text(it.name) }
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("All day")
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = allDay,
                onCheckedChange = { allDay = it }
            )
        }

        // Time selection ONLY when not all-day
        if (!allDay) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        showTimePicker(startTime) {
                            startTime = it
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text(
                        text = startTime.toString(),
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        showTimePicker(endTime) {
                            endTime = it
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text(
                        text = endTime.toString(),
                        maxLines = 1
                    )
                }
            }
        }


        Button(
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                val startMillis =
                    if (allDay) {
                        dayStartMillis
                    } else {
                        selectedDate
                            .atTime(startTime)
                            .atZone(zone)
                            .toInstant()
                            .toEpochMilli()
                    }

                val endMillis =
                    if (allDay) {
                        dayEndMillis
                    } else {
                        selectedDate
                            .atTime(endTime)
                            .atZone(zone)
                            .toInstant()
                            .toEpochMilli()
                    }

                onCreate(
                    TaskEntity(
                        title = title,
                        note = note.ifBlank { null },
                        date = dayStartMillis,
                        startTime = startMillis,
                        endTime = endMillis,
                        allDay = allDay,
                        priority = priority,
                        color = primaryColor,
                        recurrenceRule = null,
                        timeZone = zone.id
                    )
                )
            }
        ) {
            Text("Create Task")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventSheet(
    selectedDate: LocalDate,
    onCreate: (EventEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var importance by remember { mutableStateOf(3) }
    var pinned by remember { mutableStateOf(false) }
    var allDay by remember { mutableStateOf(false) }

    val zone = ZoneId.systemDefault()
    val dateMillis =
        selectedDate.atStartOfDay(zone).toInstant().toEpochMilli()

    val startMillis =
        selectedDate.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
    val endMillis =
        selectedDate.atTime(13, 0).atZone(zone).toInstant().toEpochMilli()
    val primaryColor =
        MaterialTheme.colorScheme.primary.toArgb()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Create Event", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Importance")
        Slider(
            value = importance.toFloat(),
            onValueChange = { importance = it.toInt() },
            valueRange = 1f..5f,
            steps = 3
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pinned")
            Spacer(Modifier.width(12.dp))
            Switch(checked = pinned, onCheckedChange = { pinned = it })
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("All day")
            Spacer(Modifier.width(12.dp))
            Switch(checked = allDay, onCheckedChange = { allDay = it })
        }

        Button(
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onCreate(
                    EventEntity(
                        title = title,
                        description = description.ifBlank { null },
                        date = dateMillis,
                        startTime = startMillis,
                        endTime = endMillis,
                        allDay = allDay,
                        importance = importance,
                        pinned = pinned,
                        color = primaryColor,
                        timeZone = zone.id
                    )
                )
            }
        ) {
            Text("Create Event")
        }
    }
}


@Composable
fun TaskDetailSheet(task: TaskEntity) {

    val priorityColor = when (task.priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.error
        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
        Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
        Priority.CRITICAL -> MaterialTheme.colorScheme.errorContainer
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // TITLE
        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        // STATUS + PRIORITY
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = task.status.name.lowercase().replaceFirstChar { it.uppercase() }
                    )
                }
            )

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = task.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = priorityColor
                    )
                }
            )
        }

        // TIME BLOCK
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text(
                text = formatDate(task.date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text =
                    if (task.allDay) {
                        "All day"
                    } else {
                        "${formatTime(task.startTime)} – ${formatTime(task.endTime)}"
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // NOTE
        task.note?.takeIf { it.isNotBlank() }?.let {
            Divider()
            Text(
                text = "Notes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }
}


@Composable
fun EventDetailSheet(event: EventEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Title
        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Time
        Text(
            text = buildString {
                append(formatDate(event.date))
                append(" • ")
                append(formatTime(event.startTime))
                append(" – ")
                append(formatTime(event.endTime))
            }
        )

        // All day
        if (event.allDay) {
            Text(
                text = "All day",
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Importance
        Text(
            text = "Importance: ${event.importance}/5"
        )

        // Pinned
        if (event.pinned) {
            Text(
                text = "Pinned",
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Description
        event.description?.takeIf { it.isNotBlank() }?.let {
            Divider()
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Divider()

        // Meta
        Text(
            text = "Created: ${formatDateTime(event.createdAt)}",
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = "Updated: ${formatDateTime(event.updatedAt)}",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
