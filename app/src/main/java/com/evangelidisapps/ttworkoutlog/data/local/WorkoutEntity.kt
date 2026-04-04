package com.evangelidisapps.ttworkoutlog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dateLabel: String,
    val note: String?,
    val durationSec: Int? = null,
    val calories: Int? = null,
    val style: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
