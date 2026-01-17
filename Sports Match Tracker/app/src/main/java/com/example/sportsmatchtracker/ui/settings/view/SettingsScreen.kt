package com.example.sportsmatchtracker.ui.settings.view


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.user.User
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import com.example.sportsmatchtracker.repository.settings.SettingsRepository
import com.example.sportsmatchtracker.ui.auth.view.AuthScreen
import com.example.sportsmatchtracker.ui.auth.view_model.AuthViewModel
import com.example.sportsmatchtracker.ui.settings.view_model.SettingsViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

@Composable
fun SettingsScreen(
    user: User,
    viewModel: SettingsViewModel
) {

    val user by viewModel.user.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    var newNick by remember { mutableStateOf("") }

    val maxLength = 15

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Nick: ${user?.nick ?: "N/A"}",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
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
                        .padding(end = 4.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    if (newNick.isNotBlank()) {
                        viewModel.updateNick(newNick)
                        newNick = ""
                    }
                },
                modifier = Modifier.size(width = 85.dp, height = 56.dp)
            ) {
                Text("Apply")
            }
        }

        if (updateStatus != null) {
            Text(
                text = updateStatus ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = if (updateStatus?.contains("Error") == true)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        }

        Button(
            modifier = Modifier.size(width = 250.dp, height = 40.dp),
            onClick = { viewModel.logout() }
        ) {
            Text("Sign out")
        }
    }
}
