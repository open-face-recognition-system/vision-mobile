package com.mariofronza.face_collection_app.api

data class User(val id: Int, val name: String, val email: String, val role: String)

data class Session(val user: User, val token: String, val refreshToken: String)

data class SessionRequest(val email: String, val password: String)

data class RefreshTokenRequest(val refreshToken: String)

data class SignUpRequest(val name: String, val email: String, val password: String, val enrollment: String)

data class ProfileRequest(
    val name: String,
    val email: String,
    val oldPassword: String,
    val password: String,
    val passwordConfirmation: String,
)

data class Student(val id: Int, val enrollment: String, val user: User)

data class Photo(val id: Int?, val photoType: String, val updatedAt: String?, val url: String?)

data class Subject(val id: Int, val name: String)

data class Lecture(val id: Int, val startHour: String, val endHour: String, val date: String, val subject: Subject)

data class Page<T>(val total: Int, val data: List<T>)

data class Recognition(val student: Student, val confidence: Double, val attendanceId: Int)

data class PresenceRequest(val classId: Int, val studentId: Int, val isPresent: Boolean)
