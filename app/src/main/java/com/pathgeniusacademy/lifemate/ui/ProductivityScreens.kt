package com.pathgeniusacademy.lifemate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pathgeniusacademy.lifemate.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun FocusScreen(vm: AppViewModel, onBack: () -> Unit) {
    var confirmReset by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(22.dp, 8.dp, 22.dp, 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp)) {
        item { ProductivityHeader("Focus studio", onBack) }
        item {
            Text("LESS NOISE. MORE YOU.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
            Spacer(Modifier.height(10.dp))
            Text("Make this moment count.", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Pick one thing. Give it your attention.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        item {
            Box(Modifier.sizeIn(maxWidth = 280.dp, maxHeight = 280.dp).fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { (1f - vm.focusRemaining.toFloat() / (vm.focusMinutes * 60)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxSize(), strokeWidth = 9.dp, color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (vm.focusCompleted) Icons.Default.CheckCircle else Icons.Default.Spa, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("%02d:%02d".format(vm.focusRemaining / 60, vm.focusRemaining % 60), fontSize = 55.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                    Text(when { vm.focusCompleted -> "Session complete"; vm.focusRunning -> "Stay with it"; vm.focusRemaining < vm.focusMinutes * 60 -> "Paused · take a breath"; else -> "Your time starts here" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(15, 25, 50).forEach { minutes ->
                    FilterChip(selected = vm.focusMinutes == minutes, onClick = { vm.setFocusDuration(minutes) }, enabled = !vm.focusRunning && (vm.focusRemaining == vm.focusMinutes * 60 || vm.focusRemaining == 0), label = { Text("$minutes min") })
                }
            }
        }
        item {
            Button(onClick = vm::toggleFocus, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp)) {
                Icon(if (vm.focusRunning) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text(if (vm.focusRunning) "Pause session" else if (vm.focusRemaining in 1 until vm.focusMinutes * 60) "Resume session" else "Begin focus")
            }
            TextButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth(), enabled = vm.focusRunning || vm.focusRemaining != vm.focusMinutes * 60) { Text("Reset session") }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WbSunny, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("${vm.focusedMinutes(LocalDate.now())} focused minutes today", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("Completed sessions add up here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("You can leave this screen while the timer runs. Progress is restored when you return. No background alarm is played.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
    if (confirmReset) AlertDialog(onDismissRequest = { confirmReset = false }, title = { Text("Reset this session?") }, text = { Text("This unfinished session won't be added to your focus minutes.") }, confirmButton = { TextButton(onClick = { vm.resetFocus(); confirmReset = false }) { Text("Reset") } }, dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Keep going") } })
}

@Composable
internal fun InsightsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val today = LocalDate.now()
    val days = (6L downTo 0L).map { today.minusDays(it) }
    val minutes = days.map { vm.focusedMinutes(it) }
    val maxMinutes = (minutes.maxOrNull() ?: 0).coerceAtLeast(1)
    val checkIns = days.sumOf { d -> vm.habits.count { d.toString() in it.completionDates } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 30.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { ProductivityHeader("Your progress", onBack) }
        item {
            Text("Consistency over perfection.", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("${days.first().format(DateTimeFormatter.ofPattern("d MMM"))} – ${today.format(DateTimeFormatter.ofPattern("d MMM"))} · Last 7 days", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightMetric("${minutes.sum()}", "Focus minutes", Modifier.weight(1f))
                InsightMetric("$checkIns", "Habit check-ins", Modifier.weight(1f))
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text("A little more focused", style = MaterialTheme.typography.titleLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                        days.forEachIndexed { i, day ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${minutes[i]}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                Box(Modifier.height(115.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                                    Box(Modifier.fillMaxWidth(.65f).height((if (minutes[i] == 0) 3f else 115f * minutes[i] / maxMinutes).dp).clip(RoundedCornerShape(7.dp)).background(if (day == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer))
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(day.format(DateTimeFormatter.ofPattern("EEEEE")), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Text(if (minutes.sum() == 0) "Finish a focus session to start your chart." else "Minutes from completed sessions, grouped by finish date.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Text("Your habit rhythm", style = MaterialTheme.typography.titleLarge) }
        if (vm.habits.isEmpty()) item { Text("Add a habit to start building your rhythm.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(vm.habits, key = { it.id }) { habit ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("${habit.emoji}  ${habit.name}", fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        days.forEach { date ->
                            val done = date.toString() in habit.completionDates
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(date.format(DateTimeFormatter.ofPattern("EEEEE")), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                Box(Modifier.size(30.dp).clip(CircleShape).background(if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    if (done) Icon(Icons.Default.Check, "Completed on $date", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                                    else Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    Text("${vm.habitStreak(habit)} day current streak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item { Text("Only recorded check-ins appear. Older versions kept the last check-in and streak, so earlier daily history may be unavailable.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            val completed = vm.tasks.count { it.completed }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your task collection", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("$completed completed · ${vm.tasks.size - completed} open", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("Current saved tasks; repeating tasks roll forward after completion.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable
private fun InsightMetric(value: String, label: String, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(18.dp)) {
            Text(value, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun ProductivityHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}
