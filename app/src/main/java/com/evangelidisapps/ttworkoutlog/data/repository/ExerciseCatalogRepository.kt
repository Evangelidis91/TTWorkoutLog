package com.evangelidisapps.ttworkoutlog.data.repository

import android.content.Context
import com.evangelidisapps.ttworkoutlog.data.local.CatalogExerciseEntity
import com.evangelidisapps.ttworkoutlog.data.local.WorkoutDatabase
import com.evangelidisapps.ttworkoutlog.data.model.CatalogExercise
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.URL

class ExerciseCatalogRepository(context: Context) {

    private val dao = WorkoutDatabase.get(context).workoutDao()
    private val gson = Gson()

    /** Fetches exercises from the free-exercise-db CDN and inserts them into Room.
     *  No-op if catalog is already populated. */
    suspend fun loadIfNeeded() {
        if (dao.getCatalogCount() > 0) return
        withContext(Dispatchers.IO) {
            runCatching {
                val json = URL(CDN_URL).readText()
                val raw = gson.fromJson(json, Array<ExerciseJson>::class.java)
                val entities = raw.map { it.toEntity() }
                dao.insertCatalogExercises(entities)
            }
        }
    }

    fun search(
        query: String,
        equipment: String?,
        muscle: String?,
        category: String?
    ): Flow<List<CatalogExercise>> =
        dao.searchCatalog(
            query = query.trim(),
            equipment = equipment,
            muscle = muscle,
            category = category
        ).map { list -> list.map { it.toDomain() } }

    suspend fun getDistinctEquipment(): List<String> = dao.getDistinctEquipment()
    suspend fun getDistinctCategories(): List<String> = dao.getDistinctCategories()

    // ── JSON → Entity mapping ─────────────────────────────────────────────────

    private data class ExerciseJson(
        val id: String = "",
        val name: String = "",
        val category: String = "",
        val level: String = "",
        val equipment: String? = null,
        val force: String? = null,
        val primaryMuscles: List<String> = emptyList(),
        val secondaryMuscles: List<String> = emptyList(),
        val instructions: List<String> = emptyList()
    )

    private fun ExerciseJson.toEntity() = CatalogExerciseEntity(
        id = id,
        name = name,
        category = category,
        level = level,
        equipment = equipment,
        force = force,
        primaryMuscles = primaryMuscles.joinToString("|"),
        secondaryMuscles = secondaryMuscles.joinToString("|"),
        instructions = instructions.joinToString("\n")
    )

    private fun CatalogExerciseEntity.toDomain() = CatalogExercise(
        id = id,
        name = name,
        category = category,
        level = level,
        equipment = equipment,
        force = force,
        primaryMuscles = primaryMuscles.split("|").filter { it.isNotBlank() },
        secondaryMuscles = secondaryMuscles.split("|").filter { it.isNotBlank() },
        instructions = instructions.split("\n").filter { it.isNotBlank() }
    )

    companion object {
        private const val CDN_URL =
            "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json"
    }
}
