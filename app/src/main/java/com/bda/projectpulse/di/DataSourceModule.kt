package com.bda.projectpulse.di

import com.bda.projectpulse.data.source.TaskDataSource
import com.bda.projectpulse.data.source.firebase.FirebaseTaskDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun provideTaskDataSource(
        firebaseTaskDataSource: FirebaseTaskDataSource
    ): TaskDataSource
} 