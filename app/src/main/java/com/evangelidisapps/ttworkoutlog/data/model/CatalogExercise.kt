package com.evangelidisapps.ttworkoutlog.data.model

data class CatalogExercise(
    val id: String,
    val name: String,
    val category: String,
    val level: String,
    val equipment: String?,
    val force: String?,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val instructions: List<String>
)
