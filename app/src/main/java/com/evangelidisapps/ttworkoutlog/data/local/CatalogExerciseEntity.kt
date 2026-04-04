package com.evangelidisapps.ttworkoutlog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_exercises")
data class CatalogExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val level: String,
    val equipment: String?,
    val force: String?,
    /** Pipe-separated list, e.g. "chest|triceps" */
    val primaryMuscles: String,
    /** Pipe-separated list */
    val secondaryMuscles: String,
    /** Newline-separated instructions */
    val instructions: String
)
