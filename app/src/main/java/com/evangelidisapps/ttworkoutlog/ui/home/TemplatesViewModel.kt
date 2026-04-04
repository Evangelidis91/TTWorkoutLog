package com.evangelidisapps.ttworkoutlog.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutWithDetails
import com.evangelidisapps.ttworkoutlog.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplatesViewModel(
    app: Application,
    private val repository: WorkoutRepository
) : AndroidViewModel(app) {

    val templates: StateFlow<List<WorkoutWithDetails>> = repository.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(templateId: String) {
        viewModelScope.launch { repository.deleteTemplate(templateId) }
    }

    companion object {
        fun provideFactory(app: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return TemplatesViewModel(app, WorkoutRepository.create(app)) as T
                }
            }
    }
}
