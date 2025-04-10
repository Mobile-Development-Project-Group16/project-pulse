package com.bda.projectpulse.di

import com.bda.projectpulse.repositories.NotificationRepository
import com.bda.projectpulse.repositories.NotificationRepositoryImpl
import com.bda.projectpulse.repositories.ProjectRepository
import com.bda.projectpulse.repositories.TaskRepository
import com.bda.projectpulse.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        messaging: FirebaseMessaging
    ): NotificationRepository {
        return NotificationRepositoryImpl(firestore, auth, messaging)
    }
    
    // Note: Other repositories are already provided elsewhere.
    // This module focuses on adding the new NotificationRepository
} 