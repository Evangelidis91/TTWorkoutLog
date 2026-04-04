package com.evangelidisapps.ttworkoutlog.data.remote

import com.evangelidisapps.ttworkoutlog.data.model.Exercise
import com.evangelidisapps.ttworkoutlog.data.model.Workout
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutExercise
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutSet
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutWithDetails
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val auth get() = runCatching { Firebase.auth }.getOrNull()
    private val db get() = runCatching { Firebase.firestore }.getOrNull()

    private val uid get() = auth?.currentUser?.uid

    private fun userWorkoutsCollection(): com.google.firebase.firestore.CollectionReference? {
        val currentUid = uid ?: return null
        return db?.collection("users")?.document(currentUid)?.collection("workouts")
    }

    suspend fun saveWorkout(details: WorkoutWithDetails) {
        val col = userWorkoutsCollection() ?: return
        val doc = details.toMap()
        col.document(details.workout.id).set(doc).await()
    }

    suspend fun deleteWorkout(workoutId: String) {
        userWorkoutsCollection()?.document(workoutId)?.delete()?.await()
    }

    suspend fun fetchAllWorkouts(): List<WorkoutWithDetails> {
        val col = userWorkoutsCollection() ?: return emptyList()
        return runCatching {
            col.get().await().documents.mapNotNull { doc ->
                runCatching { doc.data?.toWorkoutWithDetails() }.getOrNull()
            }
        }.getOrElse { emptyList() }
    }

    // ── Serialization ────────────────────────────────────────────────────────

    private fun WorkoutWithDetails.toMap(): Map<String, Any?> = mapOf(
        "id" to workout.id,
        "title" to workout.title,
        "dateLabel" to workout.dateLabel,
        "note" to workout.note,
        "durationSec" to workout.durationSec,
        "calories" to workout.calories,
        "style" to workout.style,
        "createdAt" to workout.createdAt,
        "exercises" to exercises.map { ex ->
            mapOf(
                "id" to ex.id,
                "exerciseId" to ex.exercise.id,
                "exerciseName" to ex.exercise.name,
                "muscleGroup" to ex.exercise.muscleGroup,
                "position" to ex.position,
                "note" to ex.note,
                "isSuperset" to ex.isSuperset,
                "sets" to ex.sets.map { set ->
                    mapOf(
                        "id" to set.id,
                        "setNumber" to set.setNumber,
                        "reps" to set.reps,
                        "weight" to set.weight,
                        "durationSec" to set.durationSec,
                        "distanceMeters" to set.distanceMeters,
                        "calories" to set.calories,
                        "isWarmup" to set.isWarmup,
                        "note" to set.note,
                        "setType" to set.setType
                    )
                }
            )
        }
    )

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any?>.toWorkoutWithDetails(): WorkoutWithDetails {
        val workoutId = this["id"] as String
        val workout = Workout(
            id = workoutId,
            title = this["title"] as? String ?: "",
            dateLabel = this["dateLabel"] as? String ?: "",
            note = this["note"] as? String,
            durationSec = (this["durationSec"] as? Long)?.toInt(),
            calories = (this["calories"] as? Long)?.toInt(),
            style = this["style"] as? String,
            createdAt = (this["createdAt"] as? Long) ?: System.currentTimeMillis()
        )
        val exerciseMaps = this["exercises"] as? List<Map<String, Any?>> ?: emptyList()
        val exercises = exerciseMaps.map { ex ->
            val exerciseId = ex["exerciseId"] as? String ?: ""
            val workoutExerciseId = ex["id"] as? String ?: ""
            val setMaps = ex["sets"] as? List<Map<String, Any?>> ?: emptyList()
            WorkoutExercise(
                id = workoutExerciseId,
                workoutId = workoutId,
                exercise = Exercise(
                    id = exerciseId,
                    name = ex["exerciseName"] as? String ?: "",
                    muscleGroup = ex["muscleGroup"] as? String,
                    notes = null
                ),
                position = (ex["position"] as? Long)?.toInt() ?: 0,
                note = ex["note"] as? String,
                isSuperset = ex["isSuperset"] as? Boolean ?: false,
                sets = setMaps.map { s ->
                    WorkoutSet(
                        id = s["id"] as? String ?: "",
                        workoutExerciseId = workoutExerciseId,
                        setNumber = (s["setNumber"] as? Long)?.toInt() ?: 0,
                        reps = (s["reps"] as? Long)?.toInt(),
                        weight = s["weight"] as? Double,
                        durationSec = (s["durationSec"] as? Long)?.toInt(),
                        distanceMeters = (s["distanceMeters"] as? Long)?.toInt(),
                        calories = (s["calories"] as? Long)?.toInt(),
                        isWarmup = s["isWarmup"] as? Boolean ?: false,
                        note = s["note"] as? String,
                        setType = s["setType"] as? String ?: "strength"
                    )
                }
            )
        }
        return WorkoutWithDetails(workout = workout, exercises = exercises)
    }
}
