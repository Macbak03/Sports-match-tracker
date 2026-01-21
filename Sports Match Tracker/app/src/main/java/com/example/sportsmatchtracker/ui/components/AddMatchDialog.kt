package com.example.sportsmatchtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.team.Team
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMatchDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (String, String, LocalDateTime, League, String) -> Unit,
    availableLeagues: List<League>,
    availableBuildings: List<String>
) {
    var selectedLeague by remember { mutableStateOf<League?>(null) }
    var selectedHomeTeam by remember { mutableStateOf<Team?>(null) }
    var selectedAwayTeam by remember { mutableStateOf<Team?>(null) }
    var selectedBuilding by remember { mutableStateOf<String?>(null) }
    
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(18, 0)) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var leagueExpanded by remember { mutableStateOf(false) }
    var homeExpanded by remember { mutableStateOf(false) }
    var awayExpanded by remember { mutableStateOf(false) }
    var buildingExpanded by remember { mutableStateOf(false) }

    val availableTeams = selectedLeague?.teams ?: emptyList()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
             initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                     Text("OK")
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Add New Match", style = MaterialTheme.typography.headlineSmall)

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
                                    selectedHomeTeam = null
                                    selectedAwayTeam = null
                                    leagueExpanded = false
                                }
                            )
                        }
                    }
                }

                // Home Team
                ExposedDropdownMenuBox(
                    expanded = homeExpanded,
                    onExpandedChange = { homeExpanded = !homeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedHomeTeam?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select Home Team") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Home Team") },
                        enabled = selectedLeague != null
                    )
                    ExposedDropdownMenu(
                        expanded = homeExpanded,
                        onDismissRequest = { homeExpanded = false }
                    ) {
                        availableTeams.filter { it != selectedAwayTeam }.forEach { team ->
                            DropdownMenuItem(
                                text = { Text(team.name) },
                                onClick = {
                                    selectedHomeTeam = team
                                    homeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Away Team
                ExposedDropdownMenuBox(
                    expanded = awayExpanded,
                    onExpandedChange = { awayExpanded = !awayExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAwayTeam?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select Away Team") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = awayExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Away Team") },
                         enabled = selectedLeague != null
                    )
                    ExposedDropdownMenu(
                        expanded = awayExpanded,
                        onDismissRequest = { awayExpanded = false }
                    ) {
                        availableTeams.filter { it != selectedHomeTeam }.forEach { team ->
                            DropdownMenuItem(
                                text = { Text(team.name) },
                                onClick = {
                                    selectedAwayTeam = team
                                    awayExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Building
                ExposedDropdownMenuBox(
                    expanded = buildingExpanded,
                    onExpandedChange = { buildingExpanded = !buildingExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBuilding ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select Stadium") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buildingExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Stadium") }
                    )
                    ExposedDropdownMenu(
                        expanded = buildingExpanded,
                        onDismissRequest = { buildingExpanded = false }
                    ) {
                        availableBuildings.forEach { building ->
                            DropdownMenuItem(
                                text = { Text(building) },
                                onClick = {
                                    selectedBuilding = building
                                    buildingExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                             Text("📅")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Time
                OutlinedTextField(
                    value = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Time") },
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                             Text("⏰")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

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
                            if (selectedLeague != null && selectedHomeTeam != null && selectedAwayTeam != null && selectedBuilding != null) {
                                onConfirmation(
                                    selectedHomeTeam!!.name,
                                    selectedAwayTeam!!.name,
                                    LocalDateTime.of(selectedDate, selectedTime),
                                    selectedLeague!!,
                                    selectedBuilding!!
                                )
                            }
                        },
                        enabled = selectedLeague != null && selectedHomeTeam != null && selectedAwayTeam != null && selectedBuilding != null
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
