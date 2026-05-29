package com.anatomia.app.db

import com.anatomia.app.network.StudentData

object SessionRepository {

    private val queries get() = DatabaseProvider.database.sessionQueries

    fun save(student: StudentData) {
        queries.upsertSession(
            id         = student.id,
            name       = student.name,
            email      = student.email,
            class_id   = student.classId.toLong(),
            class_name = student.className,
            class_code = student.classCode,
            created_at = student.createdAt
        )
    }

    fun load(): StudentData? {
        return queries.loadSession().executeAsOneOrNull()?.let { row ->
            StudentData(
                id        = row.id,
                name      = row.name,
                email     = row.email,
                classId   = row.class_id.toInt(),
                className = row.class_name,
                classCode = row.class_code,
                createdAt = row.created_at
            )
        }
    }

    fun clear() = queries.clearSession()

    fun isLoggedIn(): Boolean = queries.hasSession().executeAsOne() > 0
}
