package com.evangelidisapps.ttworkoutlog.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutExerciseWithSetsEntity(
    @Embedded val workoutExercise: WorkoutExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutExerciseId"
    )
    val sets: List<WorkoutSetEntity>
)

data class WorkoutWithDetailsEntity(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId",
        entity = WorkoutExerciseEntity::class
    )
    val exercises: List<WorkoutExerciseWithSetsEntity>
)
