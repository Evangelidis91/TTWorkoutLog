package com.evangelidisapps.ttworkoutlog.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.evangelidisapps.ttworkoutlog.data.model.Workout
import com.evangelidisapps.ttworkoutlog.ui.theme.TTWorkoutLogTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String?,
    isSignedIn: Boolean,
    isGuest: Boolean,
    workouts: List<Workout>,
    dateFormat: String = "dd/MM/yyyy",
    onAddWorkout: () -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    onSettings: () -> Unit,
    onBodyMeasurements: () -> Unit,
    onDeleteWorkout: (Workout) -> Unit,
    onUndoDelete: (Workout) -> Unit,
    onProfile: () -> Unit,
    onBackupRestore: () -> Unit,
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
                dateFormat = dateFormat,
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
    dateFormat: String,
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

        if (workouts.isEmpty()) {
            item {
                Text(
                    text = "No workouts yet. Add your first session.",
                    style = MaterialTheme.typography.bodyMedium
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
            workout.note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
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
    onBackupRestore: () -> Unit
) {
    val items = listOf(
        DrawerItem(label = "Profile", action = onProfile),
        DrawerItem(
            label = if (isSignedIn && !isGuest) "Logout" else "Login",
            action = if (isSignedIn && !isGuest) onLogout else onLogin
        ),
        DrawerItem(label = "Body measurements", action = onBodyMeasurements),
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

private data class DrawerItem(
    val label: String,
    val action: () -> Unit
)

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
                Workout(id = "1", title = "Push Day", dateLabel = "2024-10-01", note = "Bench / OHP / Dips"),
                Workout(id = "2", title = "Legs", dateLabel = "2024-09-30", note = "Squat / RDL / Lunges")
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
