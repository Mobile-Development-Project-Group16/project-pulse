package com.bda.projectpulse.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.models.Attachment
import com.bda.projectpulse.models.AttachmentType
import com.bda.projectpulse.models.ChatMessage
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
    var selectedAttachments by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedAttachments = uris
    }

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
                    ChatMessageItem(
                        message = message,
                        onAttachmentClick = { attachment ->
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(attachment.url)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // If no app can handle the file, try to download it
                                val downloadIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(attachment.url)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(downloadIntent)
                            }
                        }
                    )
                }
            }

            // Show selected attachments
            if (selectedAttachments.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attachments: ${selectedAttachments.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedAttachments = emptyList() }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear attachments")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach file")
                }
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank() || selectedAttachments.isNotEmpty()) {
                            viewModel.sendMessage(projectId, messageText, selectedAttachments)
                            messageText = ""
                            selectedAttachments = emptyList()
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onAttachmentClick: (Attachment) -> Unit
) {
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
                if (message.attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    message.attachments.forEach { attachment ->
                        Card(
                            onClick = { onAttachmentClick(attachment) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (attachment.type) {
                                        AttachmentType.IMAGE -> Icons.Default.Image
                                        AttachmentType.PDF -> Icons.Default.PictureAsPdf
                                        AttachmentType.DOCUMENT -> Icons.Default.Description
                                        AttachmentType.EXCEL -> Icons.Default.TableChart
                                        else -> Icons.Default.AttachFile
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = attachment.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = formatFileSize(attachment.size),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var sizeInBytes = size.toDouble()
    var unitIndex = 0
    
    while (sizeInBytes >= 1024 && unitIndex < units.size - 1) {
        sizeInBytes /= 1024
        unitIndex++
    }
    
    return String.format("%.1f %s", sizeInBytes, units[unitIndex])
} 