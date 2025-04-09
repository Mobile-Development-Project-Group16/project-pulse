package com.bda.projectpulse.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.models.ChatMessage
import com.bda.projectpulse.ui.chat.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectChatScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(projectId) {
        viewModel.loadMessages(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Chat") },
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                items(messages) { message ->
                    ChatMessageItem(message = message)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(projectId, messageText)
                            messageText = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = if (message.isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromCurrentUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
} 