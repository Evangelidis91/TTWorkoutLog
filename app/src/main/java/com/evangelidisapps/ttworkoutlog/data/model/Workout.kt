package com.evangelidisapps.ttworkoutlog.data.model

data class Workout(
    val id: String,
    val title: String,
    val dateLabel: String,
    val note: String? = null,
    val durationSec: Int? = null,
    val calories: Int? = null,
    val style: String? = null, // e.g., "Standard", "WOD", "Complex", "For Time"
    val createdAt: Long = System.currentTimeMillis()
)
