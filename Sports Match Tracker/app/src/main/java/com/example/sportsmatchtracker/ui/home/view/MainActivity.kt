package com.example.sportsmatchtracker.ui.home.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsmatchtracker.ui.home.view_model.HomeViewModel
import com.example.sportsmatchtracker.repository.ClientRepository
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme



class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportsMatchTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Connect(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun Connect(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {
    val uiState = viewModel.uiState.collectAsState()
    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Sports match tracker",
            fontSize = 24.sp
        )
        
        Button(onClick = { viewModel.connectToServer() }) {
            Text(text = "Connect to server")
        }
        
        Text(
            text = uiState.value.connectionStatus,
        )
        if(uiState.value.connected) {
            Text(
                text = uiState.value.responseFromServer ?: "No response"
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConnectPreview() {
    SportsMatchTrackerTheme {
        Connect(viewModel = HomeViewModel(ClientRepository()))
    }
}