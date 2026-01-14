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
import java.time.YearMonth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map





@Composable
fun ToggleableCalendar(
    selectedDate: LocalDate,
    isWeekMode: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    scrollToDate: LocalDate?
) {
    val currentMonth = remember(selectedDate) { selectedDate.yearMonth }
    val startMonth = currentMonth.minusMonths(24)
    val endMonth = currentMonth.plusMonths(24)

    val daysOfWeek = remember { daysOfWeek(firstDayOfWeekFromLocale()) }

    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    val weekState = rememberWeekCalendarState(
        startDate = startMonth.atStartOfMonth(),
        endDate = endMonth.atEndOfMonth(),
        firstVisibleWeekDate = selectedDate,
        firstDayOfWeek = daysOfWeek.first()
    )

    LaunchedEffect(monthState.firstVisibleMonth) {
        onMonthChanged(monthState.firstVisibleMonth.yearMonth)
    }



    LaunchedEffect(monthState) {
        snapshotFlow {
            monthState.isScrollInProgress to monthState.firstVisibleMonth
        }
            .filter { (scrolling, _) -> !scrolling }
            .map { (_, month) -> month.yearMonth }
            .distinctUntilChanged()
            .collect { yearMonth ->
                onMonthChanged(yearMonth)
            }
    }

    LaunchedEffect(scrollToDate, isWeekMode) {
        scrollToDate ?: return@LaunchedEffect

        if (isWeekMode) {
            weekState.animateScrollToWeek(scrollToDate)
        } else {
            monthState.animateScrollToMonth(scrollToDate.yearMonth)
        }
    }





    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {

        DaysOfWeekRow(daysOfWeek)

        Spacer(Modifier.height(6.dp))

        AnimatedContent(
            targetState = isWeekMode,
            transitionSpec = {
                if (targetState) {
                    (slideInVertically { it } + fadeIn())
                        .togetherWith(slideOutVertically { -it } + fadeOut())
                } else {
                    (slideInVertically { -it } + fadeIn())
                        .togetherWith(slideOutVertically { it } + fadeOut())
                }
            },
            label = "calendar-switch"
        ) { weekMode ->
            if (!weekMode) {
                HorizontalCalendar(
                    state = monthState,
                    dayContent = { day ->
                        DayCell(
                            date = day.date,
                            selected = day.date == selectedDate,
                            enabled = day.position == DayPosition.MonthDate,
                            onClick = onDateSelected,
                            isInCurrentMonth = day.position == DayPosition.MonthDate
                        )
                    }
                )
            } else {
                WeekCalendar(
                    state = weekState,
                    dayContent = { day ->
                        DayCell(
                            date = day.date,
                            selected = day.date == selectedDate,
                            enabled = day.position == WeekDayPosition.RangeDate,
                            onClick = onDateSelected,
                            isInCurrentMonth = true
                        )
                    }
                )
            }
        }



    }
}

/* ---------------- DAY CELL ---------------- */

@Composable
private fun DayCell(
    date: LocalDate,
    selected: Boolean,
    enabled: Boolean,
    onClick: (LocalDate) -> Unit,
    isInCurrentMonth: Boolean,
) {
    val bg = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent


    val textColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        isInCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .minimumInteractiveComponentSize()
            .background(bg, CircleShape)
            .clickable(enabled = enabled) { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium, // ↓ smaller
            color = textColor
        )
    }
}



@Composable
private fun DaysOfWeekRow(daysOfWeek: List<java.time.DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp) // tighter
    ) {
        daysOfWeek.forEach {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = it.name.take(3),
                style = MaterialTheme.typography.labelSmall, // ↓ smaller than day cell
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
