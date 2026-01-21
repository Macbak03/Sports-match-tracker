package com.example.sportsmatchtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.sportsmatchtracker.model.league.League

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeamDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (String, String, League, (String) -> Unit) -> Unit,
    availableLeagues: List<League>
) {
    var teamName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedLeague by remember { mutableStateOf<League?>(null) }
    var leagueExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Add New Team", style = MaterialTheme.typography.headlineSmall)

                // Team Name
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { 
                        teamName = it 
                        errorMessage = null
                    },
                    label = { Text("Team Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )

                // City
                OutlinedTextField(
                    value = city,
                    onValueChange = { 
                        city = it 
                        errorMessage = null
                    },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth()
                )

                // League
                ExposedDropdownMenuBox(
                    expanded = leagueExpanded,
                    onExpandedChange = { leagueExpanded = !leagueExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLeague?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select League") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = leagueExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("League") }
                    )
                    ExposedDropdownMenu(
                        expanded = leagueExpanded,
                        onDismissRequest = { leagueExpanded = false }
                    ) {
                        availableLeagues.forEach { league ->
                            DropdownMenuItem(
                                text = { Text("${league.name} (${league.country})") },
                                onClick = {
                                    selectedLeague = league
                                    leagueExpanded = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onDismissRequest() }
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            if (teamName.isNotBlank() && city.isNotBlank() && selectedLeague != null) {
                                onConfirmation(teamName, city, selectedLeague!!) { error ->
                                    errorMessage = error
                                }
                            }
                        },
                        enabled = teamName.isNotBlank() && city.isNotBlank() && selectedLeague != null
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
