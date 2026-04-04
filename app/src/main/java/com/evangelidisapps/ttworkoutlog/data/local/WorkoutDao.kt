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
}
