package com.example.sportsmatchtracker.ui.settings.view


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import com.example.sportsmatchtracker.repository.leagues.LeaguesRepository
import com.example.sportsmatchtracker.repository.seasons.SeasonsRepository
import com.example.sportsmatchtracker.repository.settings.SettingsRepository
import com.example.sportsmatchtracker.repository.tables.TablesRepository
import com.example.sportsmatchtracker.ui.settings.view_model.SettingsViewModel
import com.example.sportsmatchtracker.ui.tables.view.TablesScreen
import com.example.sportsmatchtracker.ui.tables.view_model.TablesViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {

    val user by viewModel.user.collectAsState()
    val nickUpdateStatus by viewModel.nickStatus.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newNick by remember { mutableStateOf("") }

    val maxLength = 30
    
    // Automatically close dialog on success
    LaunchedEffect(nickUpdateStatus) {
        if (nickUpdateStatus?.error == false) {
            showDialog = false
            newNick = ""
            viewModel.clearNickStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // User info section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "User info",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Email: ${user?.email ?: "N/A"}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nick: ${user?.nick ?: "N/A"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    IconButton(
                        onClick = { showDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit nick"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                HorizontalDivider()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign out button
            TextButton(
                onClick = { viewModel.logout() },
            ) {
                Text(
                    text = "Sign out",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    // Edit nick dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                newNick = ""
                viewModel.clearNickStatus()
            },
            title = { Text("Change Nick") },
            text = {
                Column {
                    TextField(
                        value = newNick,
                        onValueChange = { input ->
                            newNick = input
                                .filterNot { it.isWhitespace() }
                                .take(maxLength)
                        },
                        label = { Text("New nick") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "${newNick.length}/$maxLength",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, end = 4.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (nickUpdateStatus?.error == true && nickUpdateStatus != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = nickUpdateStatus!!.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newNick.isNotBlank()) {
                            viewModel.updateNick(newNick)
                        }
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDialog = false
                        newNick = ""
                        viewModel.clearNickStatus()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsPreview() {
    SportsMatchTrackerTheme {
        SettingsScreen(viewModel = SettingsViewModel(
            SettingsRepository(),
            AuthRepository()
        ), onBack = {})
    }
}
