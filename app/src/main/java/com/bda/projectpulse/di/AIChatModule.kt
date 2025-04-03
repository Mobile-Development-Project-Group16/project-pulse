package com.bda.projectpulse.di

import com.bda.projectpulse.data.api.OpenRouterApi
import com.bda.projectpulse.data.repository.AIChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIChatModule {

    @Provides
    @Singleton
    fun provideAIChatRepository(api: OpenRouterApi): AIChatRepository {
        return AIChatRepository(api)
    }
} 