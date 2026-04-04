package com.evangelidisapps.ttworkoutlog.data.repository

import android.app.Application
import com.evangelidisapps.ttworkoutlog.data.local.ExerciseEntity
import com.evangelidisapps.ttworkoutlog.data.local.WorkoutDao
import com.evangelidisapps.ttworkoutlog.data.local.WorkoutDatabase
import com.evangelidisapps.ttworkoutlog.data.remote.FirestoreRepository
import com.evangelidisapps.ttworkoutlog.data.local.WorkoutEntity
import com.evangelidisapps.ttworkoutlog.data.local.WorkoutExerciseEntity
import com.evangelidisapps.ttworkoutlog.data.local.WorkoutSetEntity
import com.evangelidisapps.ttworkoutlog.data.local.WorkoutWithDetailsEntity
import com.evangelidisapps.ttworkoutlog.data.model.Exercise
import com.evangelidisapps.ttworkoutlog.data.model.Workout
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutExercise
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutSet
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutWithDetails
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val firestore: FirestoreRepository = FirestoreRepository()
) {

    companion object {
        fun create(app: Application) = WorkoutRepository(
            workoutDao = WorkoutDatabase.get(app).workoutDao()
        )
    }

    fun observeWorkouts(): Flow<List<Workout>> =
        workoutDao.observeWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }

    fun observeWorkoutWithDetails(id: String): Flow<WorkoutWithDetails?> =
        workoutDao.observeWorkoutWithDetails(id).map { it?.toDomain() }

    fun observeWorkoutsWithDetails(): Flow<List<WorkoutWithDetails>> =
        workoutDao.observeWorkoutsWithDetails().map { list -> list.map { it.toDomain() } }

    suspend fun upsert(workout: Workout) {
        workoutDao.upsert(workout.toEntity())
    }

    /** Fetch all workouts from Firestore and upsert into Room. Returns count synced. */
    suspend fun syncFromFirestore(): Int {
        val workouts = firestore.fetchAllWorkouts()
        workouts.forEach { saveToRoomOnly(it) }
        return workouts.size
    }

    /** Delete all local workout data from Room. */
    suspend fun clearAllLocalWorkouts() {
        workoutDao.deleteAllWorkoutSets()
        workoutDao.deleteAllWorkoutExercises()
        workoutDao.deleteAllWorkouts()
        workoutDao.deleteAllExercises()
    }

    /** Upload every local Room workout to Firestore (guest→account migration). */
    suspend fun uploadLocalToFirestore() {
        workoutDao.observeWorkoutsWithDetails().first()
            .map { it.toDomain() }
            .forEach { firestore.saveWorkout(it) }
    }

    suspend fun upsertWithDetails(details: WorkoutWithDetails) {
        saveToRoomOnly(details)
        firestore.saveWorkout(details)
    }

    suspend fun delete(workout: Workout) {
        workoutDao.delete(workout.toEntity())
        firestore.deleteWorkout(workout.id)
    }

    private suspend fun saveToRoomOnly(details: WorkoutWithDetails) {
        val workoutEntity = details.workout.toEntity()
        val exerciseEntities = details.exercises.map { it.exercise.toEntity() }
        val workoutExercises = details.exercises.map { item ->
            WorkoutExerciseEntity(
                id = item.id.ifBlank { UUID.randomUUID().toString() },
                workoutId = details.workout.id,
                exerciseId = item.exercise.id,
                position = item.position,
                note = item.note,
                isSuperset = item.isSuperset
            )
        }
        val sets = details.exercises.flatMap { item ->
            item.sets.map { set -> set.toEntity(item.id) }
        }
        workoutDao.upsertWorkoutWithDetails(
            workout = workoutEntity,
            exercises = exerciseEntities,
            workoutExercises = workoutExercises,
            sets = sets
        )
    }

private fun WorkoutEntity.toDomain(): Workout =
        Workout(
            id = id,
            title = title,
            dateLabel = dateLabel,
            note = note,
            durationSec = durationSec,
            calories = calories,
            style = style,
            createdAt = createdAt
        )

    private fun Workout.toEntity(): WorkoutEntity =
        WorkoutEntity(
            id = id,
            title = title,
            dateLabel = dateLabel,
            note = note,
            durationSec = durationSec,
            calories = calories,
            style = style,
            createdAt = createdAt
        )

    private fun Exercise.toEntity(): ExerciseEntity =
        ExerciseEntity(
            id = id,
            name = name,
            muscleGroup = muscleGroup,
            notes = notes
        )

    private fun WorkoutSet.toEntity(workoutExerciseId: String): WorkoutSetEntity =
        WorkoutSetEntity(
            id = id,
            workoutExerciseId = workoutExerciseId,
            setNumber = setNumber,
            reps = reps,
            weight = weight,
            durationSec = durationSec,
            distanceMeters = distanceMeters,
            calories = calories,
            isWarmup = isWarmup,
            note = note,
            setType = setType
        )

    private fun WorkoutWithDetailsEntity.toDomain(): WorkoutWithDetails =
        WorkoutWithDetails(
            workout = workout.toDomain(), // carries createdAt via WorkoutEntity.toDomain()
            exercises = exercises.map { relation ->
                WorkoutExercise(
                    id = relation.workoutExercise.id,
                    workoutId = workout.id,
                    exercise = relation.exercise.toDomain(),
                    position = relation.workoutExercise.position,
                    note = relation.workoutExercise.note,
                    isSuperset = relation.workoutExercise.isSuperset,
                    sets = relation.sets.map { set ->
                        WorkoutSet(
                            id = set.id,
                            workoutExerciseId = set.workoutExerciseId,
                            setNumber = set.setNumber,
                            reps = set.reps,
                            weight = set.weight,
                            durationSec = set.durationSec,
                            distanceMeters = set.distanceMeters,
                            calories = set.calories,
                            isWarmup = set.isWarmup,
                            note = set.note,
                            setType = set.setType
                        )
                    }
                )
            }.sortedBy { it.position }
        )

    private fun ExerciseEntity.toDomain(): Exercise =
        Exercise(
            id = id,
            name = name,
            muscleGroup = muscleGroup,
            notes = notes
        )
}
