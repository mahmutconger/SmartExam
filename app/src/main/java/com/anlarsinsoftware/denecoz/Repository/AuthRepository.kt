package com.anlarsinsoftware.denecoz.Repository

interface AuthRepository {
    suspend fun registerUser(email: String, pass: String): Result<Unit>
    suspend fun loginUser(email: String, pass: String): Result<Unit>
}