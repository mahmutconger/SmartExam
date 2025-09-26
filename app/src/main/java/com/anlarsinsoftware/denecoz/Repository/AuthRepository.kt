package com.anlarsinsoftware.denecoz.Repository

import com.anlarsinsoftware.denecoz.Model.UserRole
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun registerUser(email: String, pass: String, role: UserRole): FirebaseUser
    suspend fun loginUser(email: String, pass: String,role: UserRole): Result<Unit>
}