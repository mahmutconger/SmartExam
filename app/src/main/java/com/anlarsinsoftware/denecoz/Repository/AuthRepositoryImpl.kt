package com.anlarsinsoftware.denecoz.data.repository

import com.anlarsinsoftware.denecoz.Model.UserRole
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun registerUser(email: String, pass: String, role: UserRole): FirebaseUser {
        val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
        val firebaseUser = authResult.user ?: throw Exception("Kullanıcı oluşturulamadı.")

        val userMap = hashMapOf(
            "uid" to firebaseUser.uid,
            "email" to email,
            "role" to role.name,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users").document(firebaseUser.uid).set(userMap).await()

        return firebaseUser
    }

    override suspend fun loginUser(email: String, pass: String, role: UserRole): Result<Unit> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw Exception("Kullanıcı kimliği alınamadı.")

            val userDoc = firestore.collection("users").document(user.uid).get().await()

            if (userDoc.exists()) {
                val storedRole = userDoc.getString("role")

                if (storedRole == role.name) {
                    Result.success(Unit)
                } else {
                    auth.signOut()
                    throw Exception("Seçilen rol ile kayıtlı rol uyuşmuyor.")
                }
            } else {
                auth.signOut()
                throw Exception("Kullanıcı verisi bulunamadı. Lütfen tekrar kayıt olun.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}