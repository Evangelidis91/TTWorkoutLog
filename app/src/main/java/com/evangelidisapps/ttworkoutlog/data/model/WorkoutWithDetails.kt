package com.evangelidisapps.ttworkoutlog.data.model

data class WorkoutWithDetails(
    val workout: Workout,
    val exercises: List<WorkoutExercise>
)
