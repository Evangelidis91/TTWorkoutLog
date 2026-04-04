package com.evangelidisapps.ttworkoutlog.data.model

data class BodyMeasurement(
    val id: String,
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
