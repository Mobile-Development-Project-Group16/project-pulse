package com.bda.projectpulse.di

import com.bda.projectpulse.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideTaskDataSource(firestore: FirebaseFirestore): TaskDataSource {
        return FirebaseTaskDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideUserDataSource(firestore: FirebaseFirestore): UserDataSource {
        return FirebaseUserDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(taskDataSource: TaskDataSource): TaskRepository {
        return TaskRepository(taskDataSource)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDataSource: UserDataSource): UserRepository {
        return UserRepository(userDataSource)
    }
} 