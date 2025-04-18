package com.bda.projectpulse.di

import com.bda.projectpulse.data.AIChatRepository
import com.bda.projectpulse.data.ApiKeyDataSource
import com.bda.projectpulse.data.ChatHistoryDataSource
import com.bda.projectpulse.data.ModelConfigDataSource
import com.bda.projectpulse.data.api.OpenRouterApi
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideModelConfigDataSource(firestore: FirebaseFirestore): ModelConfigDataSource {
        return ModelConfigDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideAIChatRepository(
        openRouterApi: OpenRouterApi,
        apiKeyDataSource: ApiKeyDataSource,
        chatHistoryDataSource: ChatHistoryDataSource,
        modelConfigDataSource: ModelConfigDataSource,
        firestore: FirebaseFirestore
    ): AIChatRepository {
        return AIChatRepository(
            openRouterApi = openRouterApi,
            apiKeyDataSource = apiKeyDataSource,
            chatHistoryDataSource = chatHistoryDataSource,
            modelConfigDataSource = modelConfigDataSource,
            firestore = firestore
        )
    }
} 