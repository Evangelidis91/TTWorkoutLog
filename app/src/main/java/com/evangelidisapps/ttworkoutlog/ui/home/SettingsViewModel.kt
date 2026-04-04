package com.evangelidisapps.ttworkoutlog.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.evangelidisapps.ttworkoutlog.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    val defaultStyle: StateFlow<String> = prefsRepo.defaultStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Standard")

    val themeMode: StateFlow<String> = prefsRepo.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    val dateFormat: StateFlow<String> = prefsRepo.dateFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "dd/MM/yyyy")

    fun setDefaultStyle(style: String) {
        viewModelScope.launch { prefsRepo.setDefaultStyle(style) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setDateFormat(format: String) {
        viewModelScope.launch { prefsRepo.setDateFormat(format) }
    }

    companion object {
        fun provideFactory(app: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    @Suppress("UNCHECKED_CAST")
                    return SettingsViewModel(UserPreferencesRepository(app)) as T
                }
            }
    }
}
