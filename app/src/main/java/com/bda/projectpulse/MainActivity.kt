package com.bda.projectpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.bda.projectpulse.navigation.AppNavigation
import com.bda.projectpulse.ui.auth.AuthViewModel
import com.bda.projectpulse.ui.projects.ProjectViewModel
import com.bda.projectpulse.ui.tasks.TaskViewModel
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
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val projectViewModel: ProjectViewModel = viewModel()
                    val taskViewModel: TaskViewModel = viewModel()
                    
                    AppNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        projectViewModel = projectViewModel,
                        taskViewModel = taskViewModel
                    )
                }
            }
        }
    }
}