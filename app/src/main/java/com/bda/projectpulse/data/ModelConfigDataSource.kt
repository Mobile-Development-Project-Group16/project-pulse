package com.bda.projectpulse.data

import com.bda.projectpulse.data.models.ModelConfig
import com.bda.projectpulse.data.models.AvailableModels
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelConfigDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val configCollection = firestore.collection("model_config")
    private val _activeModel = MutableStateFlow<ModelConfig>(AvailableModels.DEEPSEEK_CHAT)
    val activeModel: StateFlow<ModelConfig> = _activeModel.asStateFlow()

    init {
        loadActiveModel()
    }

    private fun loadActiveModel() {
        try {
            configCollection.document("active_model")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    
                    val modelId = snapshot?.getString("modelId")
                    val model = if (modelId != null) {
                        AvailableModels.ALL_MODELS.find { it.id == modelId }
                    } else {
                        AvailableModels.DEEPSEEK_CHAT
                    }
                    
                    _activeModel.value = model ?: AvailableModels.DEEPSEEK_CHAT
                }
        } catch (e: Exception) {
            _activeModel.value = AvailableModels.DEEPSEEK_CHAT
        }
    }

    suspend fun setActiveModel(modelId: String) {
        try {
            configCollection.document("active_model")
                .set(mapOf("modelId" to modelId))
                .await()
                
            val model = AvailableModels.ALL_MODELS.find { it.id == modelId }
            if (model != null) {
                _activeModel.value = model
            }
        } catch (e: Exception) {
            // Keep the current model if update fails
        }
    }
} 