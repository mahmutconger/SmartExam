package com.anlarsinsoftware.denecoz.Repository

import android.net.Uri
import com.anlarsinsoftware.denecoz.Model.Province
import com.anlarsinsoftware.denecoz.Model.State.Student.UserProfile
import com.anlarsinsoftware.denecoz.Model.UserRole
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun registerStudent(email: String, pass: String, name: String): Result<FirebaseUser>
    suspend fun registerPublisher(
        email: String,
        pass: String,
        publisherName: String
    ): Result<FirebaseUser>
    suspend fun loginStudent(email: String, pass: String): Result<Unit>
    suspend fun loginPublisher(email: String, pass: String): Result<Unit>
    suspend fun getUserProfile(studentId: String): Result<UserProfile>
    suspend fun updateUserProfile(
        studentId: String,
        name: String,
        educationLevel: String,
        city: String,
        district: String,
        school: String,
        newProfilePhotoUri: Uri?
    ): Result<Unit>
    suspend fun updatePublisherProfile(publisherId: String, name: String, newLogoUri: Uri?): Result<Unit>
    suspend fun getProvincesAndDistricts(): Result<List<Province>>
}