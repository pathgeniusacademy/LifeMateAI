package com.pathgeniusacademy.lifemate.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pathgeniusacademy.lifemate.AppViewModel
import com.pathgeniusacademy.lifemate.model.*
import com.pathgeniusacademy.lifemate.ui.theme.LifeMateTheme
import java.time.*
import java.time.format.DateTimeFormatter

private object Routes {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val AI = "assistant"
    const val NOTES = "notes"
    const val HABITS = "habits"
    const val PLANNER = "planner"
    const val SETTINGS = "settings"
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun LifeMateApp(viewModel: AppViewModel) {
    LifeMateTheme(viewModel.settings.themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (!viewModel.settings.onboarded) {
                OnboardingScreen(onFinish = viewModel::finishOnboarding)
            } else {
                MainShell(viewModel)
            }
        }
    }
}

@Composable
private fun MainShell(viewModel: AppViewModel) {
    val nav = rememberNavController()
    val bottom = listOf(
        BottomItem(Routes.HOME, "Home", Icons.Default.Home),
        BottomItem(Routes.TASKS, "Tasks", Icons.Default.CheckCircle),
        BottomItem(Routes.AI, "AI", Icons.Default.AutoAwesome),
        BottomItem(Routes.NOTES, "Notes", Icons.Default.EditNote),
        BottomItem(Routes.HABITS, "Habits", Icons.Default.LocalFireDepartment)
    )
    val current by nav.currentBackStackEntryAsState()
    val currentRoute = current?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in bottom.map { it.route }) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 10.dp,
                    shadowElevation = 14.dp,
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(78.dp)
                    ) {
                        bottom.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = {
                                    nav.navigate(item.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(if (currentRoute == item.route) 24.dp else 22.dp)
                                    )
                                },
                                label = { Text(item.label, fontWeight = if (currentRoute == item.route) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) { HomeScreen(viewModel, nav) }
            composable(Routes.TASKS) { TasksScreen(viewModel) }
            composable(Routes.AI) { AssistantScreen(viewModel, onSettings = { nav.navigate(Routes.SETTINGS) }) }
            composable(Routes.NOTES) { NotesScreen(viewModel) }
            composable(Routes.HABITS) { HabitsScreen(viewModel) }
            composable(Routes.PLANNER) { PlannerScreen(viewModel, onBack = { nav.popBackStack() }) }
            composable(Routes.SETTINGS) { SettingsScreen(viewModel, onBack = { nav.popBackStack() }) }
        }
    }
}

@Composable
private fun OnboardingScreen(onFinish: (String) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    val titles = listOf(
        "One calm place for your whole day",
        "Simple on the surface. Powerful underneath.",
        "Ready when you are"
    )
    val bodies = listOf(
        "Tasks, notes, habits, reminders and your daily plan stay together — so your head doesn't have to.",
        "LifeMate works offline for everyday organization. Connect AI only when you want deeper help.",
        "A clean home screen will show what matters now, not fifty things competing for attention."
    )
    val icons = listOf(Icons.Default.AutoAwesome, Icons.Default.Bolt, Icons.Default.WavingHand)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4146D8), Color(0xFF6D5DFB), Color(0xFFF6F7FF))
                )
            )
            .padding(horizontal = 22.dp)
    ) {
        Box(
            Modifier
                .size(220.dp)
                .offset(x = 210.dp, y = (-55).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(Modifier.height(70.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icons[step], null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("LifeMate", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Your personal day companion", color = Color.White.copy(alpha = .78f), fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(42.dp))
                Text(
                    titles[step],
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 42.sp
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    bodies[step],
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 17.sp,
                    lineHeight = 25.sp
                )
                Spacer(Modifier.height(30.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .14f)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        when (step) {
                            0 -> {
                                OnboardingFeature(Icons.Default.CheckCircle, "Tasks & reminders", "Know exactly what needs attention")
                                OnboardingFeature(Icons.Default.EditNote, "Notes", "Capture ideas in seconds")
                                OnboardingFeature(Icons.Default.LocalFireDepartment, "Habits", "Build routines without clutter")
                            }
                            1 -> {
                                OnboardingFeature(Icons.Default.WifiOff, "Offline core", "Your organizer keeps working without internet")
                                OnboardingFeature(Icons.Default.Shield, "Private by default", "AI stays optional and separate")
                                OnboardingFeature(Icons.Default.AutoAwesome, "Smart when needed", "Ask for plans, summaries and next steps")
                            }
                            else -> {
                                Text("What should I call you?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it.take(30) },
                                    placeholder = { Text("Your name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedTextColor = Color(0xFF181A2C),
                                        unfocusedTextColor = Color(0xFF181A2C),
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = .85f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { i ->
                        Box(
                            Modifier
                                .height(7.dp)
                                .width(if (i == step) 34.dp else 9.dp)
                                .clip(CircleShape)
                                .background(if (i == step) Color(0xFF4E52DF) else Color(0xFFBFC2E8))
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { if (step < 2) step++ else onFinish(name.ifBlank { "Friend" }) },
                    enabled = step < 2 || name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(if (step == 2) "Open my LifeMate" else "Continue", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun OnboardingFeature(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .14f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = .74f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun HomeScreen(viewModel: AppViewModel, nav: NavHostController) {
    val now = remember { LocalDate.now() }
    var quickTask by remember { mutableStateOf(false) }
    var quickNote by remember { mutableStateOf(false) }
    val todayTasks = viewModel.tasks.filter { !it.completed && isToday(it.dueAt) }.sortedBy { it.dueAt }
    val openTasks = viewModel.tasks.count { !it.completed }
    val doneHabits = viewModel.habits.count { it.lastCompletedDate == LocalDate.now().toString() }
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
    val nextTask = todayTasks.firstOrNull()
    val habitProgress = if (viewModel.habits.isEmpty()) 0f else doneHabits.toFloat() / viewModel.habits.size

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(viewModel.settings.displayName.take(1).uppercase(), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("$greeting,", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text(viewModel.settings.displayName, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                }
                FilledTonalIconButton(onClick = { nav.navigate(Routes.SETTINGS) }) {
                    Icon(Icons.Default.Settings, "Settings")
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF4B50DA), Color(0xFF6D5DFB), Color(0xFF6F7CF5))))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), color = Color.White.copy(alpha = .78f), fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (todayTasks.isEmpty()) "Your day is clear" else "${todayTasks.size} things need your attention",
                                    color = Color.White,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 30.sp
                                )
                            }
                            Box(
                                Modifier.size(54.dp).clip(CircleShape).background(Color.White.copy(alpha = .14f)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                        }
                        Spacer(Modifier.height(18.dp))
                        Surface(color = Color.White.copy(alpha = .12f), shape = RoundedCornerShape(20.dp)) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Color.White.copy(alpha = .14f)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(if (nextTask == null) Icons.Default.WbSunny else Icons.Default.Schedule, null, tint = Color.White) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(if (nextTask == null) "Nothing urgent" else "Next up", color = Color.White.copy(alpha = .72f), fontSize = 12.sp)
                                    Text(
                                        nextTask?.title ?: "A good moment to plan ahead",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    nextTask?.dueAt?.let { Text(formatDue(it), color = Color.White.copy(alpha = .72f), fontSize = 12.sp) }
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { nav.navigate(Routes.AI) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF4C50D7)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, Modifier.size(19.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ask LifeMate", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Today at a glance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                TextButton(onClick = { nav.navigate(Routes.PLANNER) }) { Text("Open planner") }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumMetricCard("Open", "$openTasks", Icons.Default.TaskAlt, Modifier.weight(1f)) { nav.navigate(Routes.TASKS) }
                PremiumMetricCard("Habits", "$doneHabits/${viewModel.habits.size}", Icons.Default.LocalFireDepartment, Modifier.weight(1f)) { nav.navigate(Routes.HABITS) }
                PremiumMetricCard("Notes", "${viewModel.notes.size}", Icons.Default.EditNote, Modifier.weight(1f)) { nav.navigate(Routes.NOTES) }
            }
            Spacer(Modifier.height(22.dp))
        }

        item {
            Text("Quick actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionCard("Plan my day", "Build a simple schedule", Icons.Default.CalendarMonth, Modifier.weight(1f)) { nav.navigate(Routes.PLANNER) }
                QuickActionCard("New task", "Capture it before you forget", Icons.Default.AddTask, Modifier.weight(1f)) { quickTask = true }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionCard("Quick note", "Save an idea", Icons.Default.StickyNote2, Modifier.weight(1f)) { quickNote = true }
                QuickActionCard("Habits", "Keep your streak alive", Icons.Default.LocalFireDepartment, Modifier.weight(1f)) { nav.navigate(Routes.HABITS) }
            }
            Spacer(Modifier.height(22.dp))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Today's tasks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                if (todayTasks.isNotEmpty()) AssistChip(onClick = { nav.navigate(Routes.TASKS) }, label = { Text("${todayTasks.size} left") })
            }
            Spacer(Modifier.height(10.dp))
        }
        if (todayTasks.isEmpty()) {
            item {
                EmptyCard(
                    icon = Icons.Default.WbSunny,
                    title = "No timed tasks today",
                    body = "Enjoy the breathing room or add something important."
                )
                Spacer(Modifier.height(20.dp))
            }
        } else {
            items(todayTasks.take(4), key = { it.id }) { task ->
                TaskRow(task, onToggle = { viewModel.toggleTask(task) }, modifier = Modifier.padding(vertical = 4.dp))
            }
            item { Spacer(Modifier.height(18.dp)) }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Daily rhythm", color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .7f), fontSize = 12.sp)
                            Text("Habit progress", color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Text("${(habitProgress * 100).toInt()}%", color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { habitProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = .55f)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        items(viewModel.habits.take(3), key = { it.id }) { habit ->
            HabitCompactRow(habit, onToggle = { viewModel.toggleHabit(habit) }, modifier = Modifier.padding(vertical = 4.dp))
        }
        item { Spacer(Modifier.height(18.dp)) }
    }

    if (quickTask) {
        AddTaskDialog(onDismiss = { quickTask = false }, onAdd = {
            viewModel.addTask(it)
            quickTask = false
        })
    }
    if (quickNote) {
        NoteDialog(
            note = null,
            onDismiss = { quickNote = false },
            onSave = { title, body, _ ->
                viewModel.addNote(title, body)
                quickNote = false
            },
            onDelete = null
        )
    }
}

@Composable
private fun PremiumMetricCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 116.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp)) }
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 132.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(13.dp))
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 2)
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label) }, leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) })
}

@Composable
private fun TasksScreen(viewModel: AppViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("OPEN") }
    val list = when (filter) {
        "TODAY" -> viewModel.tasks.filter { isToday(it.dueAt) }
        "DONE" -> viewModel.tasks.filter { it.completed }
        else -> viewModel.tasks.filter { !it.completed }
    }.sortedWith(compareBy<TaskItem> { it.completed }.thenBy { it.dueAt ?: Long.MAX_VALUE })

    Scaffold(
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { showAdd = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("New task") }) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(18.dp, 18.dp, 18.dp, 110.dp)) {
            item {
                ScreenTitle("Tasks", "Capture it, schedule it, finish it.")
                Spacer(Modifier.height(14.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(filter == "OPEN", { filter = "OPEN" }, { Text("Open") }) }
                    item { FilterChip(filter == "TODAY", { filter = "TODAY" }, { Text("Today") }) }
                    item { FilterChip(filter == "DONE", { filter = "DONE" }, { Text("Done") }) }
                }
                Spacer(Modifier.height(14.dp))
            }
            if (list.isEmpty()) {
                item { EmptyCard(Icons.Default.TaskAlt, "Nothing here", "Your list is clear for this filter.") }
            } else {
                items(list, key = { it.id }) { task ->
                    TaskRow(task, onToggle = { viewModel.toggleTask(task) }, onDelete = { viewModel.deleteTask(task) }, modifier = Modifier.padding(vertical = 5.dp))
                }
            }
        }
    }
    if (showAdd) {
        AddTaskDialog(onDismiss = { showAdd = false }, onAdd = { viewModel.addTask(it); showAdd = false })
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (TaskItem) -> Unit) {
    val context = LocalContext.current
    val initial = remember { LocalDateTime.now().withSecond(0).withNano(0).plusHours(1) }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var selectedDate by remember { mutableStateOf(initial.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(initial.toLocalTime()) }
    var hasDueDate by remember { mutableStateOf(true) }
    var reminder by remember { mutableStateOf(true) }
    var repeat by remember { mutableStateOf("NONE") }

    fun openDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, day -> selectedDate = LocalDate.of(year, month + 1, day) },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).apply { datePicker.minDate = System.currentTimeMillis() - 86_400_000L }.show()
    }

    fun openTimePicker() {
        TimePickerDialog(
            context,
            { _, hour, minute -> selectedTime = LocalTime.of(hour, minute) },
            selectedTime.hour,
            selectedTime.minute,
            false
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Create task", fontWeight = FontWeight.ExtraBold)
                Text("Add it once. Let LifeMate remember it.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(160) },
                    label = { Text("What needs to be done?") },
                    leadingIcon = { Icon(Icons.Default.TaskAlt, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(1000) },
                    label = { Text("Details (optional)") },
                    minLines = 2, maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Text("Priority", fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(listOf("LOW", "MEDIUM", "HIGH")) { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.lowercase().replaceFirstChar { it.uppercase() }) },
                            leadingIcon = if (priority == p) { { Icon(Icons.Default.Check, null, Modifier.size(17.dp)) } } else null
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Schedule", fontWeight = FontWeight.SemiBold)
                        Text("Choose an exact date and time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = hasDueDate, onCheckedChange = { hasDueDate = it; if (!it) reminder = false })
                }

                AnimatedVisibility(hasDueDate) {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { openDatePicker() },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(7.dp))
                                Text(selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                            }
                            OutlinedButton(
                                onClick = { openTimePicker() },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Schedule, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(7.dp))
                                Text(selectedTime.format(DateTimeFormatter.ofPattern("h:mm a")))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Notify me", modifier = Modifier.weight(1f))
                            Switch(checked = reminder, onCheckedChange = { reminder = it })
                        }
                    }
                }

                Text("Repeat", fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(listOf("NONE", "DAILY", "WEEKLY", "MONTHLY")) { r ->
                        FilterChip(
                            selected = repeat == r,
                            onClick = { repeat = r; if (r != "NONE") hasDueDate = true },
                            label = { Text(if (r == "NONE") "Never" else r.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                if (repeat != "NONE") {
                    Text("When you complete it, the next $repeat occurrence is scheduled automatically.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val due = if (hasDueDate) {
                        ZonedDateTime.of(selectedDate, selectedTime, ZoneId.systemDefault()).toInstant().toEpochMilli()
                    } else null
                    onAdd(
                        TaskItem(
                            title = title.trim(),
                            notes = notes.trim(),
                            dueAt = due,
                            priority = priority,
                            reminderEnabled = reminder && due != null,
                            repeat = if (due != null) repeat else "NONE"
                        )
                    )
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.AddTask, null, Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text("Add task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(26.dp)
    )
}

@Composable
private fun AssistantScreen(viewModel: AppViewModel, onSettings: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var voiceStatus by remember { mutableStateOf<String?>(null) }
    val connected = viewModel.settings.aiBackendUrl.isNotBlank()
    val context = LocalContext.current

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                input = spoken
                voiceStatus = "Voice captured"
            }
        } else {
            voiceStatus = "Voice input cancelled"
        }
    }

    fun startVoiceInput() {
        runCatching {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell LifeMate what you need")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
            voiceLauncher.launch(intent)
        }.onFailure { voiceStatus = "Voice input is not available on this device" }
    }

    LaunchedEffect(viewModel.settings.aiBackendUrl) {
        if (connected && viewModel.aiConnectionState == "UNKNOWN") viewModel.testAiConnection()
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(
                    Brush.linearGradient(listOf(Color(0xFF4B50DA), Color(0xFF7A62F5)))
                ),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(27.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("LifeMate AI", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (viewModel.aiConnectionState) {
                        "CONNECTED" -> Color(0xFF20B486)
                        "TESTING" -> Color(0xFF5B7CFA)
                        else -> Color(0xFFFFB74D)
                    }
                    Box(Modifier.size(7.dp).clip(CircleShape).background(statusColor))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        when {
                            !connected -> "Offline assistant"
                            viewModel.aiConnectionState == "CONNECTED" -> "AI connected • actions enabled"
                            viewModel.aiConnectionState == "TESTING" -> "Connecting…"
                            else -> "AI needs attention"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                    )
                }
            }
            IconButton(onClick = { viewModel.clearChat() }) { Icon(Icons.Default.Refresh, "Clear conversation") }
            FilledTonalIconButton(onClick = onSettings) { Icon(Icons.Default.Tune, "AI settings") }
        }

        if (!connected) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = .75f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Offline assistant is ready", fontWeight = FontWeight.ExtraBold)
                        Text("Natural reminders and basic planning work now. Connect AI for writing, prioritization and smarter task creation.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(onClick = onSettings) { Text("Connect") }
                }
            }
        } else if (viewModel.aiConnectionState == "DISCONNECTED") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(10.dp))
                    Text("AI backend is not reachable. Offline commands still work.", modifier = Modifier.weight(1f), fontSize = 13.sp)
                    TextButton(onClick = { viewModel.testAiConnection() }) { Text("Retry") }
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Plan my day",
                "Add a high priority task for tomorrow",
                "What should I do next?",
                "Turn this idea into a note",
                "Help me prioritize"
            ).forEach { suggestion ->
                item { SuggestionChip(onClick = { input = suggestion }, label = { Text(suggestion) }) }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.chat, key = { it.id }) { msg -> ChatBubble(msg) }
            if (viewModel.assistantBusy) {
                item {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Thinking and checking your day…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        voiceStatus?.let { status ->
            Text(status, modifier = Modifier.padding(horizontal = 18.dp, vertical = 3.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp, shadowElevation = 10.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp).imePadding(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalIconButton(
                    onClick = { startVoiceInput() },
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(17.dp)
                ) { Icon(Icons.Default.Mic, "Voice input") }
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    placeholder = { Text(if (connected) "Ask, plan, create…" else "Try: Remind me tomorrow at 6 pm…") },
                    modifier = Modifier.weight(1f), maxLines = 4,
                    shape = RoundedCornerShape(20.dp)
                )
                FilledIconButton(
                    onClick = {
                        val text = input
                        input = ""
                        voiceStatus = null
                        viewModel.sendMessage(text)
                    },
                    enabled = input.isNotBlank() && !viewModel.assistantBusy,
                    modifier = Modifier.size(54.dp), shape = RoundedCornerShape(18.dp)
                ) { Icon(Icons.Default.ArrowUpward, "Send") }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val user = msg.role == "user"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (user) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = if (user) 20.dp else 6.dp,
                bottomEnd = if (user) 6.dp else 20.dp
            )
        ) {
            Text(
                msg.text,
                modifier = Modifier.padding(14.dp),
                color = if (user) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun NotesScreen(viewModel: AppViewModel) {
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<NoteItem?>(null) }
    var creating by remember { mutableStateOf(false) }
    val filtered = viewModel.notes
        .filter { query.isBlank() || it.title.contains(query, true) || it.body.contains(query, true) }
        .sortedWith(compareByDescending<NoteItem> { it.pinned }.thenByDescending { it.updatedAt })

    Scaffold(floatingActionButton = {
        ExtendedFloatingActionButton(onClick = { creating = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("New note") })
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(18.dp, 18.dp, 18.dp, 110.dp)) {
            item {
                ScreenTitle("Notes", "Thoughts, ideas and things worth keeping.")
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    query, { query = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Search notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
            }
            if (filtered.isEmpty()) item { EmptyCard(Icons.Default.EditNote, "No notes yet", "Save an idea before it disappears.") }
            items(filtered, key = { it.id }) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { editing = note },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(note.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                            if (note.pinned) Icon(Icons.Default.PushPin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        if (note.body.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(note.body, maxLines = 3, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    if (creating) {
        NoteDialog(null, onDismiss = { creating = false }, onSave = { title, body, _ -> viewModel.addNote(title, body); creating = false }, onDelete = null)
    }
    editing?.let { note ->
        NoteDialog(note, onDismiss = { editing = null }, onSave = { title, body, pinned -> viewModel.updateNote(note.copy(title = title, body = body, pinned = pinned)); editing = null }, onDelete = { viewModel.deleteNote(note); editing = null })
    }
}

@Composable
private fun NoteDialog(note: NoteItem?, onDismiss: () -> Unit, onSave: (String, String, Boolean) -> Unit, onDelete: (() -> Unit)?) {
    var title by remember(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var body by remember(note?.id) { mutableStateOf(note?.body.orEmpty()) }
    var pinned by remember(note?.id) { mutableStateOf(note?.pinned ?: false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (note == null) "New note" else "Edit note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(body, { body = it }, label = { Text("Note") }, minLines = 5, maxLines = 10)
                if (note != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Pin this note", modifier = Modifier.weight(1f))
                        Switch(pinned, { pinned = it })
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(title.ifBlank { "Untitled note" }, body, pinned) }) { Text("Save") } },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun HabitsScreen(viewModel: AppViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    val today = LocalDate.now().toString()
    val completed = viewModel.habits.count { it.lastCompletedDate == today }
    Scaffold(floatingActionButton = {
        ExtendedFloatingActionButton(onClick = { showAdd = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("New habit") })
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(18.dp, 18.dp, 18.dp, 110.dp)) {
            item {
                ScreenTitle("Habits", "Small repeats become big change.")
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Today", fontWeight = FontWeight.Bold)
                            Text("$completed of ${viewModel.habits.size} complete", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        CircularProgressIndicator(
                            progress = { if (viewModel.habits.isEmpty()) 0f else completed.toFloat() / viewModel.habits.size },
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            items(viewModel.habits, key = { it.id }) { habit ->
                Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(20.dp)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(habit.emoji, fontSize = 30.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(habit.name, fontWeight = FontWeight.Bold)
                            Text("🔥 ${habit.streak} day streak", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        Checkbox(checked = habit.lastCompletedDate == today, onCheckedChange = { viewModel.toggleHabit(habit) })
                        IconButton(onClick = { viewModel.deleteHabit(habit) }) { Icon(Icons.Default.DeleteOutline, "Delete habit") }
                    }
                }
            }
        }
    }
    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var emoji by remember { mutableStateOf("✨") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New habit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Habit") }, singleLine = true)
                    OutlinedTextField(emoji, { emoji = it.take(3) }, label = { Text("Emoji") }, singleLine = true)
                }
            },
            confirmButton = { Button(onClick = { viewModel.addHabit(name, emoji); showAdd = false }, enabled = name.isNotBlank()) { Text("Add") } },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun PlannerScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAdd by remember { mutableStateOf(false) }
    val open = viewModel.tasks.filter { !it.completed && taskDate(it.dueAt) == selectedDate }
    val morning = open.filter { hourOf(it.dueAt) in 0..11 }.sortedBy { it.dueAt }
    val afternoon = open.filter { hourOf(it.dueAt) in 12..16 }.sortedBy { it.dueAt }
    val evening = open.filter { hourOf(it.dueAt) in 17..23 }.sortedBy { it.dueAt }
    val unscheduled = if (selectedDate == LocalDate.now()) viewModel.tasks.filter { !it.completed && it.dueAt == null } else emptyList()
    val week = remember(selectedDate) { (-3L..3L).map { selectedDate.plusDays(it) } }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add task") }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(18.dp, 10.dp, 18.dp, 110.dp)
        ) {
            item {
                BackHeader("Daily planner", onBack)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (open.isEmpty()) "A lighter day" else "${open.size} scheduled task${if (open.size == 1) "" else "s"}", fontWeight = FontWeight.Bold)
                    }
                    if (selectedDate != LocalDate.now()) TextButton(onClick = { selectedDate = LocalDate.now() }) { Text("Today") }
                }
                Spacer(Modifier.height(14.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(week) { day ->
                        val selected = day == selectedDate
                        Card(
                            onClick = { selectedDate = day },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(Modifier.width(58.dp).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(day.format(DateTimeFormatter.ofPattern("EEE")), fontSize = 11.sp, color = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = .8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(day.dayOfMonth.toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            item { PlannerSection("Morning", "☀️", morning, viewModel); Spacer(Modifier.height(14.dp)) }
            item { PlannerSection("Afternoon", "🌤️", afternoon, viewModel); Spacer(Modifier.height(14.dp)) }
            item { PlannerSection("Evening", "🌙", evening, viewModel); Spacer(Modifier.height(14.dp)) }
            if (selectedDate == LocalDate.now()) item { PlannerSection("Unscheduled", "📥", unscheduled, viewModel) }
        }
    }

    if (showAdd) {
        AddTaskDialog(
            onDismiss = { showAdd = false },
            onAdd = { viewModel.addTask(it); showAdd = false }
        )
    }
}

@Composable
private fun PlannerSection(title: String, emoji: String, tasks: List<TaskItem>, viewModel: AppViewModel) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("$emoji  $title", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            if (tasks.isEmpty()) Text("Nothing scheduled", color = MaterialTheme.colorScheme.onSurfaceVariant)
            tasks.forEach { task ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Checkbox(task.completed, { viewModel.toggleTask(task) })
                    Column(Modifier.weight(1f)) {
                        Text(task.title, fontWeight = FontWeight.Medium)
                        task.dueAt?.let { Text(formatTime(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        if (task.repeat != "NONE") Text("Repeats ${task.repeat.lowercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf(viewModel.settings.displayName) }
    var backend by remember { mutableStateOf(viewModel.settings.aiBackendUrl) }
    var theme by remember { mutableStateOf(viewModel.settings.themeMode) }
    var notifications by remember { mutableStateOf(viewModel.settings.notificationsEnabled) }
    var saved by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp, 10.dp, 18.dp, 34.dp)) {
        item {
            BackHeader("Settings", onBack)
            Spacer(Modifier.height(10.dp))
            Text("Profile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(name, { name = it.take(30) }, label = { Text("Your name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
            Text("Appearance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                    FilterChip(theme == mode, { theme = mode }, { Text(mode.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Reminders", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Local notifications", fontWeight = FontWeight.Medium)
                    Text("Tasks can remind you even when the app is closed.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(notifications, { notifications = it })
            }
            Spacer(Modifier.height(20.dp))
            Text("AI connection", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                "Your API key must never be stored in this app. Paste only your HTTPS backend URL here.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                backend, { backend = it.trim() }, label = { Text("Backend URL") },
                placeholder = { Text("https://your-worker.example.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text("Leave blank to keep AI disabled. Tasks, notes, habits and reminders still work offline.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.testAiConnection(backend) },
                enabled = backend.isNotBlank() && viewModel.aiConnectionState != "TESTING",
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (viewModel.aiConnectionState == "TESTING") {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.CloudDone, null, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(if (viewModel.aiConnectionState == "TESTING") "Testing connection…" else "Test AI connection")
            }
            AnimatedVisibility(viewModel.aiConnectionMessage.isNotBlank()) {
                val ok = viewModel.aiConnectionState == "CONNECTED"
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (ok) Icons.Default.CheckCircle else Icons.Default.Info,
                        null,
                        tint = if (ok) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        viewModel.aiConnectionMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (ok) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.updateSettings(viewModel.settings.copy(displayName = name.ifBlank { "Friend" }, aiBackendUrl = backend.trimEnd('/'), themeMode = theme, notificationsEnabled = notifications))
                    saved = true
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(17.dp)
            ) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Save settings", fontWeight = FontWeight.ExtraBold) }
            AnimatedVisibility(saved) { Text("Saved ✓", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 10.dp)) }
            Spacer(Modifier.height(26.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Privacy snapshot", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("• Tasks, notes and habits are stored locally on your device.\n• AI is optional.\n• When AI is connected, only chat content sent to the assistant goes to your backend.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ScreenTitle(title: String, subtitle: String) {
    Text(title, fontSize = 31.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
    Spacer(Modifier.height(3.dp))
    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
}

@Composable
private fun BackHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun TaskRow(task: TaskItem, onToggle: () -> Unit, onDelete: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.completed, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                Text(task.title, fontWeight = FontWeight.Bold, color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    task.dueAt?.let { Text(formatDue(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (task.priority == "HIGH") Text("High", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    if (task.repeat != "NONE") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Repeat, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(task.repeat.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (task.reminderEnabled && !task.completed) Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                }
            }
            if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, "Delete task") }
        }
    }
}

@Composable
private fun HabitCompactRow(habit: HabitItem, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val done = habit.lastCompletedDate == LocalDate.now().toString()
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(habit.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(habit.name, fontWeight = FontWeight.SemiBold)
                Text("${habit.streak}-day streak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Checkbox(done, { onToggle() })
        }
    }
}

@Composable
private fun EmptyCard(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun taskDate(ms: Long?): LocalDate? = ms?.let {
    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
}

private fun isToday(ms: Long?): Boolean {
    if (ms == null) return false
    return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()
}

private fun hourOf(ms: Long?): Int {
    if (ms == null) return -1
    return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).hour
}

private fun formatTime(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("h:mm a"))

private fun formatDue(ms: Long): String {
    val z = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault())
    val date = z.toLocalDate()
    val day = when (date) {
        LocalDate.now() -> "Today"
        LocalDate.now().plusDays(1) -> "Tomorrow"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM"))
    }
    return "$day • ${z.format(DateTimeFormatter.ofPattern("h:mm a"))}"
}
