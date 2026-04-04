package com.evangelidisapps.ttworkoutlog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val muscleGroup: String? = null,
    val notes: String? = null
)
