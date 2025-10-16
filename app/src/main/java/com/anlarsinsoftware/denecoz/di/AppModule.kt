package com.anlarsinsoftware.denecoz.di

import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepositoryImpl
import com.anlarsinsoftware.denecoz.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage // Bu import'u ekle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestore, storage)
    }

    @Provides
    @Singleton
    fun provideExamRepository(impl: ExamRepositoryImpl): ExamRepository = impl
}