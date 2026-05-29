package com.anatomia.app.db

import com.anatomia.app.network.QuestionsService
import com.anatomia.app.network.createHttpClient

object SyncRepository {

    private val service = QuestionsService(createHttpClient())

    suspend fun syncQuestionsForOrgan(organId: String): Boolean {
        return service.fetchByOrgan(organId).fold(
            onSuccess = { questions ->
                questions.forEach { QuestionRepository.upsert(it) }
                println("[SYNC] ${questions.size} preguntas sincronizadas para $organId")
                true
            },
            onFailure = { error ->
                println("[SYNC] Falló sync para $organId: ${error.message}")
                false
            }
        )
    }

    suspend fun syncAll(): Boolean {
        return service.fetchAll().fold(
            onSuccess = { questions ->
                questions.forEach { QuestionRepository.upsert(it) }
                println("[SYNC] ${questions.size} preguntas totales sincronizadas")
                true
            },
            onFailure = { error ->
                println("[SYNC] Falló sync completo: ${error.message}")
                false
            }
        )
    }
}
