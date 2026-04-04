package com.evangelidisapps.ttworkoutlog.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.evangelidisapps.ttworkoutlog.ui.auth.AuthScreen
import com.evangelidisapps.ttworkoutlog.ui.auth.AuthViewModel
import com.evangelidisapps.ttworkoutlog.ui.home.HomeScreen
import com.evangelidisapps.ttworkoutlog.ui.home.HomeViewModel
import com.evangelidisapps.ttworkoutlog.ui.home.WorkoutFilter
import com.evangelidisapps.ttworkoutlog.ui.home.WorkoutDetailScreen
import com.evangelidisapps.ttworkoutlog.ui.home.WorkoutEditScreen
import com.evangelidisapps.ttworkoutlog.ui.home.WorkoutEditViewModel
import com.evangelidisapps.ttworkoutlog.ui.home.SettingsScreen
import com.evangelidisapps.ttworkoutlog.ui.home.BodyMeasurementsScreen
import com.evangelidisapps.ttworkoutlog.ui.home.ProfileScreen
import com.evangelidisapps.ttworkoutlog.ui.home.ProfileViewModel
import com.evangelidisapps.ttworkoutlog.ui.home.BackupRestoreScreen
import com.evangelidisapps.ttworkoutlog.ui.home.BackupRestoreViewModel

private const val ROUTE_AUTH = "auth"
private const val ROUTE_HOME = "home"
private const val ROUTE_WORKOUT_DETAIL = "workout"
private const val ARG_WORKOUT_ID = "workoutId"
private const val ROUTE_WORKOUT_EDIT = "workout_edit"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_BODY = "body"
private const val ROUTE_PROFILE = "profile"
private const val ROUTE_BACKUP = "backup_restore"

@Composable
fun WorkoutNavGraph(
    homeViewModel: HomeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as Application
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(app)
    )
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val workouts by homeViewModel.workouts.collectAsStateWithLifecycle()
    val filteredWorkouts by homeViewModel.filteredWorkouts.collectAsStateWithLifecycle()
    val filter by homeViewModel.filter.collectAsStateWithLifecycle()
    val workoutsWithDetails by homeViewModel.workoutsWithDetails.collectAsStateWithLifecycle()
    val dateFormat by homeViewModel.dateFormat.collectAsStateWithLifecycle()
    val weightUnit by homeViewModel.weightUnit.collectAsStateWithLifecycle()
    val distanceUnit by homeViewModel.distanceUnit.collectAsStateWithLifecycle()

    // Show nothing while loading auth state
    if (authState.isLoading) {
        return
    }

    LaunchedEffect(authState.isSignedIn) {
        val target = if (authState.isSignedIn) ROUTE_HOME else ROUTE_AUTH
        navController.navigate(target) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val startDest = if (authState.isSignedIn || authState.isGuest) ROUTE_HOME else ROUTE_AUTH

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(ROUTE_AUTH) {
            AuthScreen(
                uiState = authState,
                onSignInEmail = { email, password -> authViewModel.signInWithEmail(email, password) },
                onRegisterEmail = { email, password, name -> authViewModel.registerWithEmail(email, password, name) },
                onGoogleSignIn = { idToken -> authViewModel.signInWithGoogle(idToken) },
                onFacebookSignIn = { token -> authViewModel.signInWithFacebook(token) },
                onContinueAsGuest = { authViewModel.continueAsGuest() }
            )
        }
        composable(ROUTE_HOME) {
            HomeScreen(
                userName = authState.displayName,
                isSignedIn = authState.isSignedIn,
                isGuest = authState.isGuest,
                workouts = filteredWorkouts,
                dateFormat = dateFormat,
                filter = filter,
                onSearchQueryChange = homeViewModel::setSearchQuery,
                onStyleFilterChange = homeViewModel::setStyleFilter,
                onStartDateChange = homeViewModel::setStartDate,
                onEndDateChange = homeViewModel::setEndDate,
                onClearFilters = homeViewModel::clearFilters,
                onAddWorkout = {
                    navController.navigate(ROUTE_WORKOUT_EDIT)
                },
                onWorkoutClick = { workout ->
                    navController.navigate("$ROUTE_WORKOUT_DETAIL/${workout.id}")
                },
                onLogin = {
                    navController.navigate(ROUTE_AUTH) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(ROUTE_AUTH) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onSettings = { navController.navigate(ROUTE_SETTINGS) },
                onBodyMeasurements = { navController.navigate(ROUTE_BODY) },
                onDeleteWorkout = { homeViewModel.deleteWorkout(it) },
                onUndoDelete = { homeViewModel.undoDelete(it) },
                onProfile = { navController.navigate(ROUTE_PROFILE) },
                onBackupRestore = { navController.navigate(ROUTE_BACKUP) }
            )
        }
        composable(
            route = "$ROUTE_WORKOUT_DETAIL/{$ARG_WORKOUT_ID}",
            arguments = listOf(
                navArgument(ARG_WORKOUT_ID) { type = NavType.StringType }
            )
        ) { entry ->
            val workoutId = entry.arguments?.getString(ARG_WORKOUT_ID)
            val workoutWithDetails = workoutsWithDetails.firstOrNull { it.workout.id == workoutId }
            WorkoutDetailScreen(
                workoutWithDetails = workoutWithDetails,
                dateFormat = dateFormat,
                weightUnit = weightUnit,
                distanceUnit = distanceUnit,
                onBack = { navController.popBackStack() },
                onEdit = {
                    navController.navigate("$ROUTE_WORKOUT_EDIT?workoutId=$workoutId")
                }
            )
        }
        composable(
            route = "$ROUTE_WORKOUT_EDIT?workoutId={$ARG_WORKOUT_ID}",
            arguments = listOf(
                navArgument(ARG_WORKOUT_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { entry ->
            val workoutId = entry.arguments?.getString(ARG_WORKOUT_ID).orEmpty().ifBlank { null }
            val app = LocalContext.current.applicationContext as Application
            val editViewModel: WorkoutEditViewModel = viewModel(
                factory = WorkoutEditViewModel.provideFactory(app, workoutId)
            )
            WorkoutEditScreen(
                viewModel = editViewModel,
                onSaved = { savedId ->
                    navController.popBackStack()
                    navController.navigate("$ROUTE_WORKOUT_DETAIL/$savedId")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(ROUTE_SETTINGS) {
            val settingsViewModel: com.evangelidisapps.ttworkoutlog.ui.home.SettingsViewModel = viewModel(
                factory = com.evangelidisapps.ttworkoutlog.ui.home.SettingsViewModel.provideFactory(app)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(ROUTE_BODY) {
            BodyMeasurementsScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_PROFILE) {
            val profileViewModel: ProfileViewModel = viewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(ROUTE_BACKUP) {
            val backupViewModel: BackupRestoreViewModel = viewModel()
            BackupRestoreScreen(
                viewModel = backupViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
