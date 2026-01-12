package com.example.axiom.ui.components.calender

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.IconButton
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme


@Composable
fun ToggleableCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
)
 {

    /* -------------------- CORE STATE -------------------- */

     val today = LocalDate.now()
     var isWeekMode by remember { mutableStateOf(false) }

//    val currentMonth = remember(today) { today.yearMonth }
     val currentMonth = remember(selectedDate) { selectedDate.yearMonth }
     val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(24) }

    val daysOfWeek = remember { daysOfWeek(firstDayOfWeekFromLocale()) }
    val scope = rememberCoroutineScope()




    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first(),

        )

    val weekState = rememberWeekCalendarState(
        startDate = startMonth.atStartOfMonth(),
        endDate = endMonth.atEndOfMonth(),
        firstVisibleWeekDate = today,
        firstDayOfWeek = daysOfWeek.first(),
    )

    val visibleMonth = rememberFirstVisibleMonthAfterScroll(monthState)
    val monthTitle = visibleMonth.yearMonth

    /* -------------------- ANIMATION -------------------- */

    val monthAlpha by animateFloatAsState(if (isWeekMode) 0f else 1f)
    val weekAlpha by animateFloatAsState(if (isWeekMode) 1f else 0f)

    val density = LocalDensity.current
    var weekCalendarSize by remember { mutableStateOf(DpSize.Zero) }


    val weeksInMonth = visibleMonth.weekDays.count()

    val fallbackWeekHeight = 56.dp * 7 // one week row height guess
    val resolvedWeekHeight =
        if (weekCalendarSize.height > 0.dp)
            weekCalendarSize.height
        else
            fallbackWeekHeight

    val calendarHeight by animateDpAsState(
        targetValue =
            if (isWeekMode) {
                resolvedWeekHeight
            } else {
                resolvedWeekHeight * weeksInMonth
            },
        label = "calendarHeight"
    )






    /* -------------------- UI -------------------- */

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        /* ---------- HEADER ---------- */

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${monthTitle.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${monthTitle.year}"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            if (!isWeekMode) {
                                weekState.scrollToWeek(selectedDate)
                            } else {
                                monthState.scrollToMonth(selectedDate.yearMonth)
                            }
                            isWeekMode = !isWeekMode
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isWeekMode) Icons.Filled.Menu else Icons.Filled.DateRange,
                        contentDescription = "Toggle calendar view"
                    )
                }



                IconButton(
                    onClick = {
                        scope.launch {
                            onDateSelected(today)
                            if (isWeekMode) {
                                weekState.animateScrollToWeek(today)
                            } else {
                                monthState.animateScrollToMonth(today.yearMonth)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Go to today"
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        DaysOfWeekRow(daysOfWeek)

        Spacer(Modifier.height(4.dp))

        /* ---------- CALENDAR STACK ---------- */

        Box(
            modifier = Modifier.height(calendarHeight)
        ) {
            HorizontalCalendar(
                modifier = Modifier
                    .alpha(monthAlpha)
                    .zIndex(if (isWeekMode) 0f else 1f),
                state = monthState,
                userScrollEnabled = !isWeekMode,
                dayContent = { day ->
                    val enabled = day.position == DayPosition.MonthDate
                    DayCell(
                        date = day.date,
                        selected = day.date == selectedDate,
                        enabled = day.position == DayPosition.MonthDate,
                        isInCurrentMonth = day.position == DayPosition.MonthDate

                    ) {
                        onDateSelected(it)
                    }
                }
            )

            WeekCalendar(
                modifier = Modifier
                    .onSizeChanged {
                        val size = density.run {
                            DpSize(it.width.toDp(), it.height.toDp())
                        }
                        if (weekCalendarSize != size) {
                            weekCalendarSize = size
                        }
                    }
                    .alpha(weekAlpha)
                    .zIndex(if (isWeekMode) 1f else 0f),
                state = weekState,
                userScrollEnabled = isWeekMode,
                dayContent = { day ->
                    val enabled = day.position == WeekDayPosition.RangeDate
                    DayCell(
                        date = day.date,
                        selected = day.date == selectedDate,
                        enabled = enabled,
                        isInCurrentMonth = true
                    ) {
                        onDateSelected(it)
                    }
                }
            )
        }


    }
}

/* -------------------- DAY CELL -------------------- */

@Composable
private fun DayCell(
    date: LocalDate,
    selected: Boolean,
    enabled: Boolean,
    isInCurrentMonth: Boolean,
    onClick: (LocalDate) -> Unit
) {
    val textColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary //black
        isInCurrentMonth -> MaterialTheme.colorScheme.onSurface //white
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .background(
                color = if (selected) Color.Black else Color.Transparent,
                shape = CircleShape
            )
            .clickable(enabled = enabled) { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor
        )
    }
}

/* -------------------- DAYS HEADER -------------------- */

@Composable
private fun DaysOfWeekRow(daysOfWeek: List<java.time.DayOfWeek>) {
    Row(Modifier.fillMaxWidth()) {
        daysOfWeek.forEach {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                text = it.name.take(3)
            )
        }
    }
}

@Composable
fun rememberFirstVisibleMonthAfterScroll(state: CalendarState): CalendarMonth {
    val visibleMonth = remember(state) { mutableStateOf(state.firstVisibleMonth) }
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }
            .filter { scrolling -> !scrolling }
            .collect { visibleMonth.value = state.firstVisibleMonth }
    }
    return visibleMonth.value
}