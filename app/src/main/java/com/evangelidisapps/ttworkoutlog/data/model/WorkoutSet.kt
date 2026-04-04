package com.evangelidisapps.ttworkoutlog.data.model

data class WorkoutSet(
    val id: String,
    val workoutExerciseId: String,
    val setNumber: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    val durationSec: Int? = null,
    val distanceMeters: Int? = null,
    val calories: Int? = null,
    val isWarmup: Boolean = false,
    val note: String? = null,
    val setType: String = "strength" // strength, time, distance, complex
)
