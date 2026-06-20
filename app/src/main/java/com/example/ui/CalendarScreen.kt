package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.EventType
import com.example.data.MasoomDetails
import com.example.data.MasoomeenData

@Composable
fun CalendarScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    var displayedCalendar by remember { mutableStateOf(java.util.Calendar.getInstance()) }
    var selectedDayNum by remember { mutableStateOf<Int?>(null) }

    val allEvents = remember { MasoomeenData.generalNotableDays + MasoomeenData.list.flatMap { it.events } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button & Tab Headers
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Calendar & History", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Events List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                item {
                    val year = displayedCalendar.get(java.util.Calendar.YEAR)
                    val month = displayedCalendar.get(java.util.Calendar.MONTH)
                    val maxDays = displayedCalendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    
                    val firstDayCal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.YEAR, year)
                        set(java.util.Calendar.MONTH, month)
                        set(java.util.Calendar.DAY_OF_MONTH, 1)
                    }
                    val firstDayOfWeek = firstDayCal.get(java.util.Calendar.DAY_OF_WEEK) // 1 = Sunday, ..., 7 = Saturday
                    
                    val monthName = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(displayedCalendar.time)

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Calendar header with navigation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    val nextCal = displayedCalendar.clone() as java.util.Calendar
                                    nextCal.add(java.util.Calendar.MONTH, -1)
                                    displayedCalendar = nextCal
                                    selectedDayNum = null
                                }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = monthName.uppercase(),
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Interactive Gregorian & Hijri Grid",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(onClick = {
                                    val nextCal = displayedCalendar.clone() as java.util.Calendar
                                    nextCal.add(java.util.Calendar.MONTH, 1)
                                    displayedCalendar = nextCal
                                    selectedDayNum = null
                                }) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Day of week headers
                            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                daysOfWeek.forEach { dayName ->
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.width(36.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Days Grid
                            val totalCells = (firstDayOfWeek - 1) + maxDays
                            val rows = (totalCells + 6) / 7

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (r in 0 until rows) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        for (c in 0..6) {
                                            val cellIndex = r * 7 + c
                                            val dayNumber = cellIndex - (firstDayOfWeek - 2)
                                            
                                            if (dayNumber in 1..maxDays) {
                                                // Convert to Hijri:
                                                val hijriForDay = com.example.utils.HijriCalendarHelper.convertGregorianToHijri(year, month + 1, dayNumber)
                                                // Find events:
                                                val dayEvents = allEvents.filter { matchEvent(hijriForDay.day, hijriForDay.monthName, it) }
                                                
                                                val hasWiladat = dayEvents.any { it.eventType == com.example.data.EventType.WILADAT }
                                                val hasShahadat = dayEvents.any { it.eventType == com.example.data.EventType.SHAHADAT }

                                                val isSelected = selectedDayNum == dayNumber

                                                val cellBg = when {
                                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                    hasShahadat -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.50f)
                                                    hasWiladat -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f)
                                                    else -> MaterialTheme.colorScheme.surface
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(cellBg)
                                                        .clickable {
                                                            selectedDayNum = if (isSelected) null else dayNumber
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Text(
                                                            text = dayNumber.toString(),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = hijriForDay.day.toString(),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.size(38.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // Selected day events details
                            selectedDayNum?.let { dayNum ->
                                val hijriForSelected = com.example.utils.HijriCalendarHelper.convertGregorianToHijri(year, month + 1, dayNum)
                                val dayEvents = allEvents.filter { matchEvent(hijriForSelected.day, hijriForSelected.monthName, it) }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "SELECTED: ${dayNum} ${monthName.split(" ")[0]} (${hijriForSelected.day} ${hijriForSelected.monthName} ${hijriForSelected.year} AH)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                if (dayEvents.isEmpty()) {
                                    Text(
                                        text = "No major Shia historical events logged for this date.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                } else {
                                    dayEvents.forEach { ev ->
                                        val isWiladat = ev.eventType == com.example.data.EventType.WILADAT
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isWiladat) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isWiladat) Icons.Default.Star else Icons.Default.Info,
                                                contentDescription = null,
                                                tint = if (isWiladat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = ev.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isWiladat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                )
                                                Text(
                                                    text = ev.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "HISTORICAL HIJRI CALENDAR EVENTS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(allEvents) { event ->
                    val isWiladat = event.eventType == EventType.WILADAT
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWiladat) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isWiladat) Icons.Default.Star else Icons.Default.Info,
                                contentDescription = if (isWiladat) "Joyous Celebration" else "Solemn Commemoration",
                                tint = if (isWiladat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = event.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isWiladat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = event.dateStringHijri,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        textAlign = TextAlign.End
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

private fun matchEvent(day: Int, monthName: String, event: com.example.data.MasoomEvent): Boolean {
    val targetDate = "$day ${monthName.trim()}".lowercase()
    val eventDates = event.dateStringHijri.split("/").map { it.trim().lowercase() }
    return eventDates.any { it == targetDate }
}
