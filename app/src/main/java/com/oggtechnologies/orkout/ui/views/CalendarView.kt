package com.oggtechnologies.orkout.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarView(
    dateRingColor: (LocalDate) -> Color? = { null },
) {
    var selectedMonth: LocalDate by remember { mutableStateOf(LocalDate.now()) }
    val firstInMonth = selectedMonth.minusDays(selectedMonth.dayOfMonth - 1L)
    val firstInCalendar = firstInMonth.minusDays(firstInMonth.dayOfWeek.value - 1L)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            val monthButtonSize = 25.sp
            IconButton(
                onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                Text("<", fontSize = monthButtonSize)
            }
            Text(
                text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 20.sp,
                modifier = Modifier.clickable { selectedMonth = LocalDate.now() }
            )
            IconButton(
                onClick = { selectedMonth = selectedMonth.plusMonths(1) }) {
                Text(">", fontSize = monthButtonSize)
            }
        }
        Row {
            for (day in listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(day)
                }
            }
        }
        for (i in 0..5) {
            Row {
                for (j in 0..6) {
                    val date = firstInCalendar.plusDays((i * 7 + j).toLong())
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                            .border(0.2.dp, Color.Gray)
                    ) {
                        DateBoxContents(date, selectedMonth, dateRingColor(date))
                    }
                }
            }
        }
    }
}

@Composable
private fun DateBoxContents(
    date: LocalDate,
    selectedMonth: LocalDate,
    ringColor: Color?
) {
    val alpha = if (date.month == selectedMonth.month) 1f else 0.4f
    Text(
        text = "${date.dayOfMonth}",
        fontSize = 20.sp,
        modifier = Modifier.alpha(alpha)
    )
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {
        if (ringColor != null) {
            val radius = (size.minDimension / 2) * 0.8f
            drawArc(
                color = ringColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true,
                size = Size(radius * 2, radius * 2),
                style = Stroke(20.0f),
                topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius)
            )
        }
    }
}