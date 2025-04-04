package com.bda.projectpulse.data.models

data class ModelConfig(
    val id: String,
    val name: String,
    val description: String,
    val isFree: Boolean = true
)

object AvailableModels {
    val DEEPSEEK_CHAT = ModelConfig(
        id = "deepseek/deepseek-chat-v3-0324",
        name = "DeepSeek Chat v3",
        description = "High-performance chat model with strong reasoning capabilities"
    )

    val DEEPSEEK_BASE = ModelConfig(
        id = "deepseek/deepseek-v3-base",
        name = "DeepSeek Base v3",
        description = "Base model for general text generation and analysis"
    )

    val GEMINI = ModelConfig(
        id = "google/gemini-2.5-pro-exp-03-25",
        name = "Gemini 2.5 Pro",
        description = "Google's advanced language model with strong capabilities"
    )

    val MISTRAL = ModelConfig(
        id = "mistralai/mistral-small-3.1-24b-instruct",
        name = "Mistral Small",
        description = "Efficient and fast model for general tasks"
    )

    val ALL_MODELS = listOf(
        DEEPSEEK_CHAT,
        DEEPSEEK_BASE,
        GEMINI,
        MISTRAL
    )
} 