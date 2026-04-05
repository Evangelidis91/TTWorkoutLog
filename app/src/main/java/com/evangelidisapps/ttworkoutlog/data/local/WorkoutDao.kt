package com.evangelidisapps.ttworkoutlog.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY createdAt DESC")
    fun observeWorkouts(): Flow<List<WorkoutEntity>>

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY createdAt DESC")
    fun observeWorkoutsWithDetails(): Flow<List<WorkoutWithDetailsEntity>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId LIMIT 1")
    fun observeWorkoutWithDetails(workoutId: String): Flow<WorkoutWithDetailsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkoutExercises(workoutExercises: List<WorkoutExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSets(sets: List<WorkoutSetEntity>)

    @Transaction
    suspend fun upsertWorkoutWithDetails(
        workout: WorkoutEntity,
        exercises: List<ExerciseEntity>,
        workoutExercises: List<WorkoutExerciseEntity>,
        sets: List<WorkoutSetEntity>
    ) {
        upsert(workout)
        if (exercises.isNotEmpty()) {
            upsertExercises(exercises)
        }
        if (workoutExercises.isNotEmpty()) {
            upsertWorkoutExercises(workoutExercises)
        }
        if (sets.isNotEmpty()) {
            upsertSets(sets)
        }
    }

    @Delete
    suspend fun delete(workout: WorkoutEntity)

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()

    @Query("DELETE FROM workout_exercises")
    suspend fun deleteAllWorkoutExercises()

    @Query("DELETE FROM workout_sets")
    suspend fun deleteAllWorkoutSets()

    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

    @Query("SELECT COUNT(*) FROM workouts")
    fun observeTotalWorkouts(): Flow<Int>

    @Query(
        "SELECT COALESCE(SUM(weight * reps), 0.0) FROM workout_sets " +
        "WHERE isWarmup = 0 AND weight IS NOT NULL AND reps IS NOT NULL"
    )
    fun observeTotalVolumeKg(): Flow<Double>

    // ── Exercise catalog ──────────────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM catalog_exercises")
    suspend fun getCatalogCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCatalogExercises(exercises: List<CatalogExerciseEntity>)

    @Query("""
        SELECT * FROM catalog_exercises
        WHERE (:query = '' OR name LIKE '%' || :query || '%')
        AND (:equipment IS NULL OR equipment = :equipment)
        AND (:muscle IS NULL OR primaryMuscles LIKE '%' || :muscle || '%')
        AND (:category IS NULL OR category = :category)
        ORDER BY name ASC
        LIMIT 100
    """)
    fun searchCatalog(
        query: String,
        equipment: String?,
        muscle: String?,
        category: String?
    ): Flow<List<CatalogExerciseEntity>>

    @Query("SELECT DISTINCT equipment FROM catalog_exercises WHERE equipment IS NOT NULL ORDER BY equipment ASC")
    suspend fun getDistinctEquipment(): List<String>

    @Query("SELECT DISTINCT category FROM catalog_exercises ORDER BY category ASC")
    suspend fun getDistinctCategories(): List<String>

    // ── Body measurements ─────────────────────────────────────────────────────

    @Query("SELECT * FROM body_measurements ORDER BY date DESC, createdAt DESC")
    fun observeBodyMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBodyMeasurement(measurement: BodyMeasurementEntity)

    @Query("DELETE FROM body_measurements WHERE id = :id")
    suspend fun deleteBodyMeasurement(id: String)
}
