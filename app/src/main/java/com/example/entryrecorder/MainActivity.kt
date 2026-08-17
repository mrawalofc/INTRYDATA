package com.example.entryrecorder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.entryrecorder.ui.EntryViewModel
import com.example.entryrecorder.ui.screens.AdminPanelScreen
import com.example.entryrecorder.ui.screens.DashboardScreen
import com.example.entryrecorder.ui.screens.LoginScreen
import com.example.entryrecorder.ui.theme.EntryRecorderTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: EntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EntryRecorderTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collectLatest { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    when (currentScreen) {
                        "admin" -> AdminPanelScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        "dashboard" -> DashboardScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        else -> LoginScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
