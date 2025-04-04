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
import com.bda.projectpulse.ui.common.ErrorMessage
import com.bda.projectpulse.utils.SecureStorage
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminSettingsViewModel
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val activeModel by viewModel.activeModel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = viewModel::updateApiKey,
                    label = { Text("OpenRouter API Key") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = viewModel::saveApiKey,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save API Key")
                }
            }

            item {
                Text(
                    text = "Available Models",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(AvailableModels.ALL_MODELS) { model ->
                ModelSelectionItem(
                    model = model,
                    isSelected = activeModel?.id == model.id,
                    onSelect = { viewModel.setActiveModel(model.id) }
                )
            }

            if (error != null) {
                item {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (success != null) {
                item {
                    Text(
                        text = success!!,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelectionItem(
    model: ModelConfig,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = model.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
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