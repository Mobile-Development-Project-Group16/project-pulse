package com.bda.projectpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.bda.projectpulse.navigation.AppNavigation
import com.bda.projectpulse.navigation.Screen
import com.bda.projectpulse.ui.theme.ProjectPulseTheme
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val auth = FirebaseAuth.getInstance()
                    
                    // Check if we should navigate somewhere specific
                    val navigationTarget = intent.getStringExtra("navigation")
                    val taskId = intent.getStringExtra("taskId")
                    val projectId = intent.getStringExtra("projectId")
                    val notificationType = intent.getStringExtra("type")
                    
                    // Create a key object that combines all the values
                    val navigationKey = listOf(auth.currentUser, navigationTarget, taskId, projectId, notificationType)
                    
                    // Set up navigation based on auth state and intent
                    LaunchedEffect(navigationKey) {
                        if (auth.currentUser != null) {
                            // User is authenticated
                            when (navigationTarget) {
                                "notifications" -> {
                                    // Navigate to notifications screen
                                    navController.navigate(Screen.Notifications.route) {
                                        popUpTo(Screen.Projects.route)
                                    }
                                    
                                    // Then, if we have a taskId, navigate to task details
                                    if (!taskId.isNullOrEmpty()) {
                                        navController.navigate(Screen.TaskDetails.createRoute(taskId))
                                    }
                                    
                                    // Or if we have a projectId, navigate to project chat
                                    else if (!projectId.isNullOrEmpty() && notificationType == "CHAT_MESSAGE") {
                                        navController.navigate(Screen.ProjectChat.createRoute(projectId))
                                    }
                                }
                                else -> {
                                    navController.navigate(Screen.Projects.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        } else {
                            // User is not authenticated, go to auth screen
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                    
                    AppNavigation(
                        navController = navController,
                        startDestination = Screen.Auth.route
                    )
                }
            }
        }
    }
}