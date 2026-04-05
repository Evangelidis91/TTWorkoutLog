package com.evangelidisapps.ttworkoutlog.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.evangelidisapps.ttworkoutlog.data.repository.UserPreferencesRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    app: Application,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val analytics = FirebaseAnalytics.getInstance(app)

    val defaultStyle: StateFlow<String> = prefsRepo.defaultStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Standard")

    val themeMode: StateFlow<String> = prefsRepo.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    val dateFormat: StateFlow<String> = prefsRepo.dateFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "dd/MM/yyyy")

    val weightUnit: StateFlow<String> = prefsRepo.weightUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "kg")

    val distanceUnit: StateFlow<String> = prefsRepo.distanceUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "km")

    fun setDefaultStyle(style: String) {
        logSetting("default_style", style)
        viewModelScope.launch { prefsRepo.setDefaultStyle(style) }
    }

    fun setThemeMode(mode: String) {
        logSetting("theme_mode", mode)
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setDateFormat(format: String) {
        logSetting("date_format", format)
        viewModelScope.launch { prefsRepo.setDateFormat(format) }
    }

    fun setWeightUnit(unit: String) {
        logSetting("weight_unit", unit)
        viewModelScope.launch { prefsRepo.setWeightUnit(unit) }
    }

    fun setDistanceUnit(unit: String) {
        logSetting("distance_unit", unit)
        viewModelScope.launch { prefsRepo.setDistanceUnit(unit) }
    }

    private fun logSetting(setting: String, value: String) {
        analytics.logEvent("setting_changed") {
            param("setting", setting)
            param("value", value)
        }
    }

    companion object {
        fun provideFactory(app: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    @Suppress("UNCHECKED_CAST")
                    return SettingsViewModel(app, UserPreferencesRepository(app)) as T
                }
            }
    }
}
