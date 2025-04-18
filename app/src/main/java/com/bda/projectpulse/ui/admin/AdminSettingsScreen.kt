package com.bda.projectpulse.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bda.projectpulse.data.models.AvailableModels
import com.bda.projectpulse.data.models.ModelConfig
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.ui.components.ErrorMessage
import com.bda.projectpulse.utils.SecureStorage
import com.google.firebase.auth.FirebaseAuth
import com.bda.projectpulse.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    viewModel: AdminSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }

    if (isLoading) {
        LoadingIndicator()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Model Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "AI Model Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            // API Key Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "OpenRouter API Key",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Enter your OpenRouter API key to enable AI chat functionality",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showApiKey) "Hide API Key" else "Show API Key"
                                )
                            }
                        }
                    )
                    
                    Button(
                        onClick = { viewModel.saveApiKey(apiKey) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save API Key")
                    }
                }
            }

            // Model Selection Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select AI Model",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Choose the AI model for chat functionality",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Only show Deepseek chat v3 option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Deepseek chat v3")
                        RadioButton(
                            selected = selectedModel == "deepseek-chat-v3",
                            onClick = { /* No action needed as this is the only option */ }
                        )
                    }
                }
            }

            // Error message
            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInfoCard(
    currentUser: com.bda.projectpulse.models.User?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Admin Information",
                style = MaterialTheme.typography.titleLarge
            )
            currentUser?.let { user ->
                Text(
                    text = "Name: ${user.displayName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Email: ${user.email}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Role: ${user.role.name}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyCard(
    apiKey: String,
    isLoading: Boolean,
    error: String?,
    success: String?,
    showApiKey: Boolean,
    onApiKeyChange: (String) -> Unit,
    onSaveApiKey: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "OpenRouter API Key",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Configure the API key for AI chat functionality",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (success != null) {
                Text(
                    text = success,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { onApiKeyChange(apiKey) }) {
                        Icon(
                            if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Hide API Key" else "Show API Key"
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onSaveApiKey,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save API Key")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "System Information",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Version: 1.0.0",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Build: Debug",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 