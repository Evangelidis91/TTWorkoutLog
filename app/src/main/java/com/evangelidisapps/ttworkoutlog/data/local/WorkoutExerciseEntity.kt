package com.evangelidisapps.ttworkoutlog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_exercises")
data class WorkoutExerciseEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val exerciseId: String,
    val position: Int,
    val note: String? = null,
    val isSuperset: Boolean = false
)
