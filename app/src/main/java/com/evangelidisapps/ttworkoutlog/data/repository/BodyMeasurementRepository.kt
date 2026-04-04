package com.evangelidisapps.ttworkoutlog.data.repository

import android.content.Context
import com.evangelidisapps.ttworkoutlog.data.local.BodyMeasurementEntity
import com.evangelidisapps.ttworkoutlog.data.local.WorkoutDatabase
import com.evangelidisapps.ttworkoutlog.data.model.BodyMeasurement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BodyMeasurementRepository(context: Context) {

    private val dao = WorkoutDatabase.get(context).workoutDao()

    fun observeAll(): Flow<List<BodyMeasurement>> =
        dao.observeBodyMeasurements().map { list -> list.map { it.toDomain() } }

    suspend fun upsert(measurement: BodyMeasurement) =
        dao.upsertBodyMeasurement(measurement.toEntity())

    suspend fun delete(id: String) =
        dao.deleteBodyMeasurement(id)

    private fun BodyMeasurementEntity.toDomain() = BodyMeasurement(
        id = id,
        date = date,
        weightKg = weightKg,
        bodyFatPercent = bodyFatPercent,
        muscleMassKg = muscleMassKg,
        bmi = bmi,
        waistCm = waistCm,
        hipCm = hipCm,
        chestCm = chestCm,
        armCm = armCm,
        thighCm = thighCm,
        note = note,
        createdAt = createdAt
    )

    private fun BodyMeasurement.toEntity() = BodyMeasurementEntity(
        id = id,
        date = date,
        weightKg = weightKg,
        bodyFatPercent = bodyFatPercent,
        muscleMassKg = muscleMassKg,
        bmi = bmi,
        waistCm = waistCm,
        hipCm = hipCm,
        chestCm = chestCm,
        armCm = armCm,
        thighCm = thighCm,
        note = note,
        createdAt = createdAt
    )
}
