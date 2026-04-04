package com.evangelidisapps.ttworkoutlog.data.model

data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: String? = null,
    val notes: String? = null
)
