package com.pathgeniusacademy.lifemate.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
        bottomBar = {
            if (currentRoute in bottom.map { it.route }) {
                NavigationBar(tonalElevation = 8.dp) {
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
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
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
    val titles = listOf("Your day, finally in one place", "Offline-first by design", "Make it yours")
    val bodies = listOf(
        "Tasks, notes, habits, reminders, planning and an AI copilot — without juggling five different apps.",
        "Your core organizer works without internet. AI is optional and only turns on after you connect a secure backend.",
        "Tell LifeMate what to call you. You can change this anytime in Settings."
    )
    val icons = listOf(Icons.Default.Bolt, Icons.Default.Shield, Icons.Default.Person)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF5B5FEF), Color(0xFF7B61FF), MaterialTheme.colorScheme.background)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(Modifier.height(48.dp))
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icons[step], null, tint = Color.White, modifier = Modifier.size(38.dp))
                }
                Spacer(Modifier.height(26.dp))
                Text(titles[step], color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 38.sp)
                Spacer(Modifier.height(14.dp))
                Text(bodies[step], color = Color.White.copy(alpha = 0.9f), fontSize = 18.sp, lineHeight = 26.sp)
                if (step == 2) {
                    Spacer(Modifier.height(28.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(30) },
                        label = { Text("Your name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color(0xFF1C1C26),
                            unfocusedTextColor = Color(0xFF1C1C26),
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                            focusedLabelColor = Color(0xFF5B5FEF),
                            unfocusedLabelColor = Color(0xFF5B5FEF)
                        )
                    )
                }
            }
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { i ->
                        Box(
                            Modifier
                                .height(7.dp)
                                .width(if (i == step) 30.dp else 9.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = if (i == step) 1f else 0.35f))
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        if (step < 2) step++ else onFinish(name.ifBlank { "Friend" })
                    },
                    enabled = step < 2 || name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF4C4FD4))
                ) {
                    Text(if (step == 2) "Start my day" else "Continue", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null)
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun HomeScreen(viewModel: AppViewModel, nav: NavHostController) {
    val now = remember { LocalDate.now() }
    val todayTasks = viewModel.tasks.filter { !it.completed && isToday(it.dueAt) }.sortedBy { it.dueAt }
    val openTasks = viewModel.tasks.count { !it.completed }
    val doneHabits = viewModel.habits.count { it.lastCompletedDate == LocalDate.now().toString() }
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF5B5FEF), Color(0xFF796CFF), Color(0xFF00A7A5)))
                    )
                    .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 28.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(greeting, color = Color.White.copy(alpha = 0.86f), fontSize = 15.sp)
                            Text(
                                viewModel.settings.displayName.ifBlank { "Friend" },
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        FilledIconButton(
                            onClick = { nav.navigate(Routes.SETTINGS) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = 0.16f))
                        ) { Icon(Icons.Default.Settings, "Settings", tint = Color.White) }
                    }
                    Spacer(Modifier.height(24.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Today, null, tint = Color.White) }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(now.format(DateTimeFormatter.ofPattern("EEEE, d MMM")), color = Color.White, fontWeight = FontWeight.Bold)
                                Text(
                                    if (todayTasks.isEmpty()) "No timed tasks today" else "${todayTasks.size} timed task${if (todayTasks.size == 1) "" else "s"} today",
                                    color = Color.White.copy(alpha = 0.84f)
                                )
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(20.dp))
                Text("Your snapshot", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Open", "$openTasks", Icons.Default.TaskAlt, Modifier.weight(1f))
                    MetricCard("Habits", "$doneHabits/${viewModel.habits.size}", Icons.Default.LocalFireDepartment, Modifier.weight(1f))
                    MetricCard("Notes", "${viewModel.notes.size}", Icons.Default.EditNote, Modifier.weight(1f))
                }
                Spacer(Modifier.height(22.dp))
                Text("Quick actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { QuickAction("Ask AI", Icons.Default.AutoAwesome) { nav.navigate(Routes.AI) } }
                    item { QuickAction("Plan day", Icons.Default.CalendarMonth) { nav.navigate(Routes.PLANNER) } }
                    item { QuickAction("Tasks", Icons.Default.AddTask) { nav.navigate(Routes.TASKS) } }
                    item { QuickAction("Notes", Icons.Default.StickyNote2) { nav.navigate(Routes.NOTES) } }
                }
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Today", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { nav.navigate(Routes.PLANNER) }) { Text("Full planner") }
                }
            }
        }
        if (todayTasks.isEmpty()) {
            item {
                EmptyCard(
                    icon = Icons.Default.WbSunny,
                    title = "Your schedule has breathing room",
                    body = "Add a task or ask the assistant to plan something for you.",
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        } else {
            items(todayTasks.take(5), key = { it.id }) { task ->
                TaskRow(task, onToggle = { viewModel.toggleTask(task) }, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
            }
        }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(22.dp))
                Text("Habit streaks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(10.dp))
            }
        }
        items(viewModel.habits.take(3), key = { it.id }) { habit ->
            HabitCompactRow(habit, onToggle = { viewModel.toggleHabit(habit) }, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
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
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var dueChoice by remember { mutableStateOf("TODAY") }
    var reminder by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("What needs to be done?") }, singleLine = true)
                OutlinedTextField(notes, { notes = it }, label = { Text("Note (optional)") }, minLines = 2, maxLines = 3)
                Text("Priority", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                        FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Text("Due", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("TODAY", "TOMORROW", "NONE").forEach { d ->
                        FilterChip(selected = dueChoice == d, onClick = { dueChoice = d }, label = { Text(d.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                AnimatedVisibility(dueChoice != "NONE") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Local reminder", modifier = Modifier.weight(1f))
                        Switch(checked = reminder, onCheckedChange = { reminder = it })
                    }
                }
                Text(
                    "Tip: for an exact custom time, tell the assistant: “Remind me to call Sam tomorrow at 6 pm”.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val due = when (dueChoice) {
                    "TODAY" -> LocalDate.now().atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    "TOMORROW" -> LocalDate.now().plusDays(1).atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    else -> null
                }
                onAdd(TaskItem(title = title.trim(), notes = notes.trim(), dueAt = due, priority = priority, reminderEnabled = reminder && due != null))
            }, enabled = title.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AssistantScreen(viewModel: AppViewModel, onSettings: () -> Unit) {
    var input by remember { mutableStateOf("") }
    val connected = viewModel.settings.aiBackendUrl.isNotBlank()
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("LifeMate AI", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text(if (connected) "AI connected • local actions enabled" else "Offline assistant • AI not connected", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalIconButton(onClick = onSettings) { Icon(Icons.Default.Tune, "AI settings") }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Plan my day", "What do I have today?", "Habit progress").forEach { suggestion ->
                item { SuggestionChip(onClick = { viewModel.sendMessage(suggestion) }, label = { Text(suggestion) }) }
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.chat, key = { it.id }) { msg -> ChatBubble(msg) }
            if (viewModel.assistantBusy) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 40.dp)) }
            }
        }
        Surface(tonalElevation = 4.dp) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp).imePadding(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask, plan, or create a reminder…") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4
                )
                FilledIconButton(
                    onClick = { val text = input; input = ""; viewModel.sendMessage(text) },
                    enabled = input.isNotBlank() && !viewModel.assistantBusy
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
    val open = viewModel.tasks.filter { !it.completed }
    val morning = open.filter { hourOf(it.dueAt) in 0..11 }.sortedBy { it.dueAt }
    val afternoon = open.filter { hourOf(it.dueAt) in 12..16 }.sortedBy { it.dueAt }
    val evening = open.filter { hourOf(it.dueAt) in 17..23 }.sortedBy { it.dueAt }
    val unscheduled = open.filter { it.dueAt == null }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp, 10.dp, 18.dp, 30.dp)) {
        item { BackHeader("Daily planner", onBack); Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(18.dp)) }
        item { PlannerSection("Morning", "☀️", morning, viewModel); Spacer(Modifier.height(14.dp)) }
        item { PlannerSection("Afternoon", "🌤️", afternoon, viewModel); Spacer(Modifier.height(14.dp)) }
        item { PlannerSection("Evening", "🌙", evening, viewModel); Spacer(Modifier.height(14.dp)) }
        item { PlannerSection("Unscheduled", "📥", unscheduled, viewModel) }
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
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.updateSettings(viewModel.settings.copy(displayName = name.ifBlank { "Friend" }, aiBackendUrl = backend.trimEnd('/'), themeMode = theme, notificationsEnabled = notifications))
                    saved = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Save settings") }
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
    Text(title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
