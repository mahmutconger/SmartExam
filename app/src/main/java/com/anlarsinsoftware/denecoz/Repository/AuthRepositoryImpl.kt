package com.anlarsinsoftware.denecoz.data.repository

import android.net.Uri
import com.anlarsinsoftware.denecoz.Model.Province
import com.anlarsinsoftware.denecoz.Model.State.Student.UserProfile
import com.anlarsinsoftware.denecoz.Model.UserRole
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import com.anlarsinsoftware.denecoz.Services.ApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val apiService: ApiService
) : AuthRepository {

    override suspend fun getProvincesAndDistricts(): Result<List<Province>> {
        return try {
            val response = apiService.getAllProvinces()
            if (response.status == "OK") {
                Result.success(response.data)
            } else {
                Result.failure(Exception("API hatası: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerStudent(email: String, pass: String, name: String /*, profilePhotoUri: Uri? */): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user!!

            // TODO: Eğer profilePhotoUri varsa, önce Firebase Storage'a yükleyip URL'ini almalıyız.

            val userMap = hashMapOf(
                "uid" to user.uid,
                "email" to email,
                "name" to name,
                "profileImageUrl" to null,
                "bestScores" to emptyMap<String, Any>(),
                "city" to null,
                "district" to null,
                "school" to null,
                "educationLevel" to null,
                "isProfileComplete" to false
            )
            firestore.collection("students").document(user.uid).set(userMap).await()
            Result.success(user)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Yeni 'publishers' koleksiyonuna yazıyor
    override suspend fun registerPublisher(email: String, pass: String, publisherName: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user!!
            val publisherMap = hashMapOf(
                "uid" to user.uid,
                "email" to email,
                "name" to publisherName, // Yayınevi adı
                "verificationStatus" to "PENDING" // DOĞRULAMA ADIMI
            )
            firestore.collection("publishers").document(user.uid).set(publisherMap).await()
            Result.success(user)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Artık 'students' koleksiyonunu kontrol ediyor
    override suspend fun loginStudent(email: String, pass: String): Result<Unit> {
        return try {
            val user = auth.signInWithEmailAndPassword(email, pass).await().user!!
            val userDoc = firestore.collection("students").document(user.uid).get().await()
            if (userDoc.exists()) Result.success(Unit)
            else {
                auth.signOut()
                throw Exception("Öğrenci hesabı bulunamadı.")
            }
        } catch (e: Exception) { Result.failure(Exception("Giriş bilgileri hatalı.")) }
    }

    override suspend fun loginPublisher(email: String, pass: String): Result<Unit> {
        return try {
            val user = auth.signInWithEmailAndPassword(email, pass).await().user!!
            val userDoc = firestore.collection("publishers").document(user.uid).get().await()
            if (userDoc.exists()) Result.success(Unit)
            else {
                auth.signOut()
                throw Exception("Öğrenci hesabı bulunamadı.")
            }
        } catch (e: Exception) { Result.failure(Exception("Giriş bilgileri hatalı.")) }
    }
    override suspend fun getUserProfile(studentId: String): Result<UserProfile> {
        return try {
            val documentSnapshot = firestore.collection("students").document(studentId).get().await()
            if (documentSnapshot.exists()) {
                // Firestore'un toObject metodu, dökümanı doğrudan data class'ımıza çevirir.
                // Alan isimlerinin eşleşmesi yeterlidir.
                val userProfile = documentSnapshot.toObject(UserProfile::class.java)
                    ?: throw IllegalStateException("Kullanıcı profili verisi dönüştürülemedi.")

                Result.success(userProfile)
            } else {
                Result.failure(Exception("Kullanıcı profili bulunamadı."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(
        studentId: String,
        name: String,
        educationLevel: String,
        city: String,
        district: String,
        school: String,
        newProfilePhotoUri: Uri?
    ): Result<Unit> {
        return try {
            var newImageUrl: String? = null
            if (newProfilePhotoUri != null) {
                val storageRef = storage.reference.child("profile_images/$studentId/profile.jpg")
                storageRef.putFile(newProfilePhotoUri).await()
                newImageUrl = storageRef.downloadUrl.await().toString()
            }

            val updates = mutableMapOf<String, Any?>(
                "name" to name,
                "educationLevel" to educationLevel,
                "city" to city,
                "district" to district,
                "school" to school,
                "isProfileComplete" to true
            )
            if (newImageUrl != null) {
                updates["profileImageUrl"] = newImageUrl
            }

            firestore.collection("students").document(studentId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun updatePublisherProfile(publisherId: String, name: String, newLogoUri: Uri?): Result<Unit> {
        return try {
            var newLogoUrl: String? = null
            if (newLogoUri != null) {
                val storageRef = storage.reference.child("publisher_logos/$publisherId/logo.jpg")
                storageRef.putFile(newLogoUri).await()
                newLogoUrl = storageRef.downloadUrl.await().toString()
            }

            val updates = mutableMapOf<String, Any>("name" to name)
            if (newLogoUrl != null) {
                updates["logoUrl"] = newLogoUrl
            }

            firestore.collection("publishers").document(publisherId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}