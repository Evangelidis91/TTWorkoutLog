package com.evangelidisapps.ttworkoutlog

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.evangelidisapps.ttworkoutlog.data.repository.UserPreferencesRepository
import com.evangelidisapps.ttworkoutlog.navigation.WorkoutNavGraph
import com.evangelidisapps.ttworkoutlog.ui.theme.TTWorkoutLogTheme

@Composable
fun WorkoutApp() {
    val context = LocalContext.current
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val themeMode by prefsRepo.themeMode.collectAsState(initial = "system")
    val systemIsDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemIsDark
    }
    TTWorkoutLogTheme(darkTheme = isDark) {
        WorkoutNavGraph()
    }
}
