package com.bda.projectpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.bda.projectpulse.ui.auth.AuthScreen
import com.bda.projectpulse.ui.auth.AuthState
import com.bda.projectpulse.ui.auth.AuthViewModel
import com.bda.projectpulse.ui.theme.ProjectPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = remember { AuthViewModel() }
                    val authState by viewModel.authState.collectAsState()

                    when (authState) {
                        is AuthState.Loading -> {
                            // Show loading screen
                        }
                        is AuthState.Unauthenticated -> {
                            AuthScreen(
                                viewModel = viewModel,
                                onAuthSuccess = {
                                    // Navigate to main app content
                                }
                            )
                        }
                        is AuthState.Authenticated -> {
                            // Show main app content
                        }
                        is AuthState.Error -> {
                            // Show error screen
                        }
                    }
                }
            }
        }
    }
}