package com.bda.projectpulse.data.api

import com.bda.projectpulse.data.models.AIChatRequest
import com.bda.projectpulse.data.models.AIChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenRouterApi {
    @Headers(
        "Content-Type: application/json",
        "X-ASSISTANT-API-KEY: cursor-v1"
    )
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") apiKey: String,
        @Header("HTTP-Referer") referer: String? = null,
        @Header("X-Title") title: String? = null,
        @Body request: AIChatRequest
    ): AIChatResponse
} 