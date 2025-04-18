package com.bda.projectpulse.di

import com.bda.projectpulse.services.AuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    @Provides
    @Singleton
    fun provideAuthService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthService {
        return AuthService(auth, firestore)
    }
} 