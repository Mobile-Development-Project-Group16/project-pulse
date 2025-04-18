package com.bda.projectpulse.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.bda.projectpulse.models.UserRole
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val availableRoles by viewModel.availableRoles.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (formState.isRegistering) "Register" else "Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Display error message if exists
        formState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        OutlinedTextField(
            value = formState.email,
            onValueChange = viewModel::updateEmail,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.error != null,
            supportingText = if (formState.error != null) {
                { Text(formState.error!!) }
            } else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = formState.password,
            onValueChange = viewModel::updatePassword,
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.error != null,
            supportingText = if (formState.error != null) {
                { Text(formState.error!!) }
            } else null
        )

        if (formState.isRegistering) {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = formState.displayName,
                onValueChange = viewModel::updateDisplayName,
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = formState.error != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Role Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                availableRoles.forEach { role ->
                    FilterChip(
                        selected = formState.role == role,
                        onClick = { viewModel.updateRole(role) },
                        label = { Text(role.name) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { 
                if (formState.isRegistering) {
                    viewModel.register(
                        formState.email,
                        formState.password,
                        formState.displayName,
                        onAuthSuccess
                    )
                } else {
                    viewModel.login(
                        formState.email,
                        formState.password,
                        onAuthSuccess
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading
        ) {
            if (formState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (formState.isRegistering) "Register" else "Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { viewModel.toggleAuthMode() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (formState.isRegistering) "Already have an account? Login" else "Don't have an account? Register")
        }
    }
} 