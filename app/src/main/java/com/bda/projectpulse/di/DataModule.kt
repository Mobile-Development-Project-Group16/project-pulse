package com.bda.projectpulse.di

import com.bda.projectpulse.data.*
import com.google.firebase.firestore.FirebaseFirestore
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

    @Provides
    @Singleton
    fun provideApiKeyDataSource(firestore: FirebaseFirestore): ApiKeyDataSource {
        return ApiKeyDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideChatHistoryDataSource(firestore: FirebaseFirestore): ChatHistoryDataSource {
        return ChatHistoryDataSource(firestore)
    }
} 