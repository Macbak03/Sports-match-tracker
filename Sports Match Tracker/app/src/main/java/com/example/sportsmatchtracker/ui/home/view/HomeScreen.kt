package com.example.sportsmatchtracker.ui.home.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.user.User
import com.example.sportsmatchtracker.ui.home.view_model.HomeViewModel

@Composable
fun HomeScreen(
    user: User,
    viewModel: HomeViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Welcome, ${user.nick}!",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Email: ${user.email}",
            style = MaterialTheme.typography.bodyLarge
        )

        Button(onClick = { viewModel.logout() }) {
            Text("Sign out")
        }
    }
}
