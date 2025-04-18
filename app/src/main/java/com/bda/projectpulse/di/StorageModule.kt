package com.bda.projectpulse.di

import com.bda.projectpulse.services.StorageService
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    
    @Provides
    @Singleton
    fun provideStorageService(storage: FirebaseStorage): StorageService {
        return StorageService(storage)
    }
} 