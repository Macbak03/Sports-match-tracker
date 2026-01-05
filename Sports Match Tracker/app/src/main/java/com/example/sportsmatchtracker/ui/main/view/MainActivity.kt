package com.example.sportsmatchtracker.ui.main.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsmatchtracker.ui.auth.view.AuthActivity
import com.example.sportsmatchtracker.ui.main.view_model.MainViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportsMatchTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ConnectionScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel,
                        onConnected = {
                            // Navigate to AuthActivity
                            val intent = Intent(this, AuthActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onConnected: () -> Unit = {}
) {
    val clientState by viewModel.clientRepository.clientState.collectAsState()
    
    // Navigate when connected
    LaunchedEffect(clientState.connected) {
        if (clientState.connected) {
            onConnected()
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            clientState.isLoading -> {
                // Loading state - show circular progress indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Connecting to server...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            !clientState.connected && !clientState.isLoading -> {
                // Error state - show error message and retry button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Failed to connect to server",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Text(
                        text = clientState.connectionStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = { viewModel.connectToServer() }
                    ) {
                        Text(text = "Try Again")
                    }
                }
            }
        }
    }
}
