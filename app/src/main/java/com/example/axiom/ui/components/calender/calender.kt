package com.example.axiom.ui.components.calender

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth


@Composable
fun ToggleableCalendar(
    selectedDate: LocalDate,
    isWeekMode: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onMonthSettled: (YearMonth) -> Unit,
    scrollToDate: LocalDate?
) {
    val today = remember { LocalDate.now() }

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
            monthState.isScrollInProgress to monthState.firstVisibleMonth.yearMonth
        }
            .filter { (scrolling, _) -> !scrolling }
            .map { (_, month) -> month }

            .distinctUntilChanged()
            .collect { settledMonth ->
                onMonthChanged(settledMonth)
                onMonthSettled(settledMonth) // ⬅️ important
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
                            today = today,
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
                            today = today,
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
    today: LocalDate,
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
        if (date == today) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-6).dp)
                    .background(Color.Red, CircleShape)
            )
        }
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
