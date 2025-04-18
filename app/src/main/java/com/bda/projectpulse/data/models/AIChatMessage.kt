package com.bda.projectpulse.data.models

data class AIChatMessage(
    val role: String,
    val content: String
)

data class AIChatRequest(
    val model: String,
    val messages: List<AIChatMessage>,
    val stream: Boolean = false,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1000
)

data class AIChatResponse(
    val id: String,
    val choices: List<AIChatChoice>,
    val created: Long
)

data class AIChatChoice(
    val message: AIChatMessage,
    val finish_reason: String
) 