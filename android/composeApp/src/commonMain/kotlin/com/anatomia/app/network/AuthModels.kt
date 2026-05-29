package com.anatomia.app.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email:    String,
    val password: String
)

@Serializable
data class StudentData(
    val id:        String = "",  // C.I. — antes era Int
    val name:      String,
    val email:     String,
    val classId:   Int    = 0,
    val className: String = "",
    val classCode: String = "",
    val createdAt: String = ""
)

@Serializable
data class LoginResponseData(
    val usuarioId:   String,
    val nombre:      String,
    val apellido:    String,
    val email:       String,
    val claseId:     String,
    val claseNombre: String,
    val grado:       String,
    val nivelXp:     Int
)

@Serializable
data class ApiResponse(
    val ok:    Boolean,
    val data:  LoginResponseData? = null,
    val error: String?            = null
)
