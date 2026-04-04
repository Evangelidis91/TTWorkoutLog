package com.evangelidisapps.ttworkoutlog.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val IS_GUEST = booleanPreferencesKey("is_guest")
        val DEFAULT_STYLE = stringPreferencesKey("default_style")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "system" | "dark" | "light"
        val DATE_FORMAT = stringPreferencesKey("date_format") // e.g. "dd/MM/yyyy"
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit") // "kg" | "lbs"
    }

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
    }

    val isGuest: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_GUEST] ?: false
    }

    val defaultStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_STYLE] ?: "Standard"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    val dateFormat: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DATE_FORMAT] ?: "dd/MM/yyyy"
    }

    suspend fun setDateFormat(format: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATE_FORMAT] = format
        }
    }

    val weightUnit: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WEIGHT_UNIT] ?: "kg"
    }

    suspend fun setWeightUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit
        }
    }

    suspend fun setDefaultStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_STYLE] = style
        }
    }

    suspend fun setOnboardingCompleted(isGuest: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = true
            preferences[PreferencesKeys.IS_GUEST] = isGuest
        }
    }

    suspend fun clearOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = false
            preferences[PreferencesKeys.IS_GUEST] = false
        }
    }
}
