package com.evangelidisapps.ttworkoutlog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sets")
data class WorkoutSetEntity(
    @PrimaryKey val id: String,
    val workoutExerciseId: String,
    val setNumber: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    val durationSec: Int? = null,
    val distanceMeters: Int? = null,
    val calories: Int? = null,
    val isWarmup: Boolean = false,
    val note: String? = null,
    val setType: String = "strength"
)
