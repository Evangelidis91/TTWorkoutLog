package com.evangelidisapps.ttworkoutlog.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.evangelidisapps.ttworkoutlog.data.model.Workout
import com.evangelidisapps.ttworkoutlog.ui.theme.TTWorkoutLogTheme
import kotlinx.coroutines.launch

private val WORKOUT_STYLES = listOf("Standard", "WOD", "For Time", "AMRAP", "Complex", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String?,
    isSignedIn: Boolean,
    isGuest: Boolean,
    workouts: List<Workout>,
    dateFormat: String = "dd/MM/yyyy",
    filter: WorkoutFilter = WorkoutFilter(),
    onSearchQueryChange: (String) -> Unit = {},
    onStyleFilterChange: (String?) -> Unit = {},
    onStartDateChange: (LocalDate?) -> Unit = {},
    onEndDateChange: (LocalDate?) -> Unit = {},
    onClearFilters: () -> Unit = {},
    streakData: StreakData = StreakData(),
    onAddWorkout: () -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    onSettings: () -> Unit,
    onBodyMeasurements: () -> Unit,
    onDeleteWorkout: (Workout) -> Unit,
    onUndoDelete: (Workout) -> Unit,
    onProfile: () -> Unit,
    onBackupRestore: () -> Unit,
    onTemplates: () -> Unit = {},
    onWorkoutClick: (Workout) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun handleSwipeDelete(workout: Workout) {
        onDeleteWorkout(workout)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "\"${workout.title}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndoDelete(workout)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    isSignedIn = isSignedIn,
                    isGuest = isGuest,
                    onLogin = {
                        scope.launch { drawerState.close() }
                        onLogin()
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    onSettings = {
                        scope.launch { drawerState.close() }
                        onSettings()
                    },
                    onBodyMeasurements = {
                        scope.launch { drawerState.close() }
                        onBodyMeasurements()
                    },
                    onProfile = {
                        scope.launch { drawerState.close() }
                        onProfile()
                    },
                    onBackupRestore = {
                        scope.launch { drawerState.close() }
                        onBackupRestore()
                    },
                    onTemplates = {
                        scope.launch { drawerState.close() }
                        onTemplates()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Workout Log",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        TextButton(onClick = onLogout) {
                            Text(if (isSignedIn && !isGuest) "Logout" else "Login")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddWorkout) {
                    Text("+")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            HomeContent(
                paddingValues = innerPadding,
                workouts = workouts,
                userName = userName,
                streakData = streakData,
                dateFormat = dateFormat,
                filter = filter,
                onSearchQueryChange = onSearchQueryChange,
                onStyleFilterChange = onStyleFilterChange,
                onStartDateChange = onStartDateChange,
                onEndDateChange = onEndDateChange,
                onClearFilters = onClearFilters,
                onWorkoutClick = onWorkoutClick,
                onSwipeDelete = ::handleSwipeDelete
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    paddingValues: PaddingValues,
    workouts: List<Workout>,
    userName: String?,
    streakData: StreakData,
    dateFormat: String,
    filter: WorkoutFilter,
    onSearchQueryChange: (String) -> Unit,
    onStyleFilterChange: (String?) -> Unit,
    onStartDateChange: (LocalDate?) -> Unit,
    onEndDateChange: (LocalDate?) -> Unit,
    onClearFilters: () -> Unit,
    onWorkoutClick: (Workout) -> Unit,
    onSwipeDelete: (Workout) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Hi ${userName ?: "Athlete"}!",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Here are your recent workouts:",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            ActivityHeatmapCard(streakData = streakData)
        }

        item {
            SearchFilterSection(
                filter = filter,
                dateFormat = dateFormat,
                onSearchQueryChange = onSearchQueryChange,
                onStyleFilterChange = onStyleFilterChange,
                onStartDateChange = onStartDateChange,
                onEndDateChange = onEndDateChange,
                onClearFilters = onClearFilters
            )
        }

        if (workouts.isEmpty()) {
            item {
                Text(
                    text = if (filter.isActive) "No workouts match the current filters."
                           else "No workouts yet. Add your first session.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(workouts, key = { it.id }) { workout ->
                SwipeToDeleteWorkoutCard(
                    workout = workout,
                    dateFormat = dateFormat,
                    onWorkoutClick = onWorkoutClick,
                    onDelete = { onSwipeDelete(workout) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteWorkoutCard(
    workout: Workout,
    dateFormat: String,
    onWorkoutClick: (Workout) -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it != SwipeToDismissBoxValue.Settled },
        positionalThreshold = { totalDistance -> totalDistance * 0.35f }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val targetColor by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.errorContainer
                },
                label = "swipe_bg_color"
            )
            val alignment = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.Settled -> Alignment.Center
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(targetColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete workout",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        WorkoutCard(workout = workout, dateFormat = dateFormat, onWorkoutClick = onWorkoutClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchFilterSection(
    filter: WorkoutFilter,
    dateFormat: String,
    onSearchQueryChange: (String) -> Unit,
    onStyleFilterChange: (String?) -> Unit,
    onStartDateChange: (LocalDate?) -> Unit,
    onEndDateChange: (LocalDate?) -> Unit,
    onClearFilters: () -> Unit
) {
    var filtersExpanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startPickerState = rememberDatePickerState(
        initialSelectedDateMillis = filter.startDate
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )
    val endPickerState = rememberDatePickerState(
        initialSelectedDateMillis = filter.endDate
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = filter.query,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search workouts…") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (filter.query.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { filtersExpanded = !filtersExpanded }) {
                Icon(
                    imageVector = if (filtersExpanded) Icons.Default.KeyboardArrowUp
                                  else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
                Text(
                    text = if (filtersExpanded) "Hide filters" else "Filters",
                    modifier = Modifier.padding(start = 4.dp)
                )
                if (filter.isActive) {
                    Text(text = " •", color = MaterialTheme.colorScheme.primary)
                }
            }
            if (filter.isActive) {
                TextButton(onClick = onClearFilters) {
                    Text("Clear all", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (filtersExpanded) {
            HorizontalDivider()

            Text(
                text = "Style",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter.style == null,
                    onClick = { onStyleFilterChange(null) },
                    label = { Text("All") }
                )
                WORKOUT_STYLES.forEach { style ->
                    FilterChip(
                        selected = filter.style == style,
                        onClick = {
                            onStyleFilterChange(if (filter.style == style) null else style)
                        },
                        label = { Text(style) }
                    )
                }
            }

            Text(
                text = "Date range",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = filter.startDate?.format(DateTimeFormatter.ofPattern(dateFormat)) ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("From") },
                    trailingIcon = {
                        if (filter.startDate != null) {
                            IconButton(onClick = { onStartDateChange(null) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear start date")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartDatePicker = true }
                )
                OutlinedTextField(
                    value = filter.endDate?.format(DateTimeFormatter.ofPattern(dateFormat)) ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("To") },
                    trailingIcon = {
                        if (filter.endDate != null) {
                            IconButton(onClick = { onEndDateChange(null) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear end date")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndDatePicker = true }
                )
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startPickerState.selectedDateMillis?.let { millis ->
                        onStartDateChange(
                            Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
                        )
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = startPickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endPickerState.selectedDateMillis?.let { millis ->
                        onEndDateChange(
                            Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
                        )
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = endPickerState)
        }
    }
}

@Composable
private fun WorkoutCard(workout: Workout, dateFormat: String, onWorkoutClick: (Workout) -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWorkoutClick(workout) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = workout.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatDateDisplay(workout.dateLabel, dateFormat),
                style = MaterialTheme.typography.bodySmall
            )
            workout.style?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            workout.note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DrawerContent(
    isSignedIn: Boolean,
    isGuest: Boolean,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit,
    onBodyMeasurements: () -> Unit,
    onProfile: () -> Unit,
    onBackupRestore: () -> Unit,
    onTemplates: () -> Unit
) {
    val items = listOf(
        DrawerItem(label = "Profile", action = onProfile),
        DrawerItem(
            label = if (isSignedIn && !isGuest) "Logout" else "Login",
            action = if (isSignedIn && !isGuest) onLogout else onLogin
        ),
        DrawerItem(label = "Body measurements", action = onBodyMeasurements),
        DrawerItem(label = "Templates", action = onTemplates),
        DrawerItem(label = "Backup & Restore", action = onBackupRestore),
        DrawerItem(label = "Settings", action = onSettings)
    )
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Menu",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = false,
                onClick = item.action,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

private data class DrawerItem(val label: String, val action: () -> Unit)

internal fun formatDateDisplay(isoDate: String, pattern: String): String =
    runCatching {
        LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE)
            .format(DateTimeFormatter.ofPattern(pattern))
    }.getOrElse { isoDate }

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TTWorkoutLogTheme {
        HomeScreen(
            userName = "Preview User",
            isSignedIn = true,
            isGuest = false,
            workouts = listOf(
                Workout(id = "1", title = "Push Day", dateLabel = "2024-10-01", style = "Standard", note = "Bench / OHP / Dips"),
                Workout(id = "2", title = "Legs", dateLabel = "2024-09-30", style = "WOD", note = "Squat / RDL / Lunges")
            ),
            onAddWorkout = {},
            onLogout = {},
            onLogin = {},
            onSettings = {},
            onBodyMeasurements = {},
            onProfile = {},
            onBackupRestore = {},
            onDeleteWorkout = {},
            onUndoDelete = {},
            onWorkoutClick = {}
        )
    }
}
