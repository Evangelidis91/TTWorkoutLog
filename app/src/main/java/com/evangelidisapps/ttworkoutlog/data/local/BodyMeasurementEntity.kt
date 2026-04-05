package com.evangelidisapps.ttworkoutlog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey val id: String,
    val date: String,           // ISO-8601 (yyyy-MM-dd)
    val weightKg: Double?,
    val bodyFatPercent: Double?,
    val muscleMassKg: Double?,
    val bmi: Double?,
    val waistCm: Double?,
    val hipCm: Double?,
    val chestCm: Double?,
    val armCm: Double?,
    val thighCm: Double?,
    val note: String?,
    val createdAt: Long
)
