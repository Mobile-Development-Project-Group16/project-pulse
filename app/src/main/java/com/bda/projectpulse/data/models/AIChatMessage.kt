package com.bda.projectpulse.data.models

data class AIChatMessage(
    val role: String,
    val content: String
)

data class AIChatRequest(
    val model: String = "deepseek/deepseek-r1:free",
    val messages: List<AIChatMessage>
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