package com.anatomia.app.db

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object ProgressRepository {

    private val queries get() = DatabaseProvider.database.progressQueries

    fun getByOrgan(organId: String): Map<String, Boolean> =
        queries.getByOrgan(organId).executeAsList().associate {
            it.question_id to (it.was_correct == 1L)
        }

    fun getAll(): Map<String, Map<String, Boolean>> =
        queries.getAll().executeAsList()
            .groupBy { it.organ_id }
            .mapValues { (_, rows) ->
                rows.associate { it.question_id to (it.was_correct == 1L) }
            }

    fun recordAnswer(organId: String, questionId: String, wasCorrect: Boolean) {
        queries.upsertAnswer(
            organ_id    = organId,
            question_id = questionId,
            was_correct = if (wasCorrect) 1L else 0L,
            answered_at = Clock.System.now().toString()
        )
    }

    fun getAllEntries() = queries.getAll().executeAsList()

    fun getFirstAnsweredAt(): Long? {
        return try {
            val minIso = queries.getAll()
                .executeAsList()
                .map { it.answered_at }
                .minOrNull()
                ?: return null
            Instant.parse(minIso).toEpochMilliseconds()
        } catch (e: Exception) {
            null
        }
    }

    fun clearByOrgan(organId: String) = queries.clearByOrgan(organId)

    fun clearAll() = queries.clearAll()
}
