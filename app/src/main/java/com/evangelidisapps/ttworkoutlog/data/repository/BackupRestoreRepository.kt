package com.evangelidisapps.ttworkoutlog.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutWithDetails
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.Instant

private data class BackupFile(
    val version: Int = 1,
    val exportedAt: String = Instant.now().toString(),
    val workouts: List<WorkoutWithDetails>
)

class BackupRestoreRepository(
    private val workoutRepository: WorkoutRepository,
    private val contentResolver: ContentResolver
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /** Serialises all local workouts to JSON and writes to [uri]. Returns workout count. */
    suspend fun exportToJson(uri: Uri): Int {
        val workouts = workoutRepository.observeWorkoutsWithDetails().first()
        val backup = BackupFile(workouts = workouts)
        val json = gson.toJson(backup)
        contentResolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream, Charsets.UTF_8).use { it.write(json) }
        } ?: error("Cannot open output stream for $uri")
        return workouts.size
    }

    /**
     * Reads a JSON file from [uri], deserialises it, and upserts every workout into
     * Room (and Firestore if the user is signed in). Returns the number of workouts restored.
     */
    suspend fun importFromJson(uri: Uri): Int {
        val type = object : TypeToken<BackupFile>() {}.type
        val backup: BackupFile = contentResolver.openInputStream(uri)?.use { stream ->
            InputStreamReader(stream, Charsets.UTF_8).use { gson.fromJson(it, type) }
        } ?: error("Cannot open input stream for $uri")

        backup.workouts.forEach { workoutRepository.upsertWithDetails(it) }
        return backup.workouts.size
    }
}
