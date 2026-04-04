package com.evangelidisapps.ttworkoutlog.data.model

data class WorkoutExercise(
    val id: String,
    val workoutId: String,
    val exercise: Exercise,
    val position: Int,
    val note: String? = null,
    val isSuperset: Boolean = false,
    val sets: List<WorkoutSet> = emptyList()
)
