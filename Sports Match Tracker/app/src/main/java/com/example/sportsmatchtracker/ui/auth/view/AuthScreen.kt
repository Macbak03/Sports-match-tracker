package com.example.sportsmatchtracker.ui.auth.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import com.example.sportsmatchtracker.ui.auth.view_model.AuthViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel
) {
    val uiState = viewModel.uiState.collectAsState()

    MaterialTheme {
        Column(
            modifier = modifier
                .padding(all = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stadium,
                contentDescription = "App icon",
                modifier = Modifier
                    .padding(top = 60.dp)
                    .size(50.dp)
            )
            //Title
            Text(
                text = "Sports Match Tracker",
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            //Sign in/Sign up
            AnimatedContent(
                targetState = uiState.value.isInSignUpState,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith  fadeOut(tween(300))
                }
            ) { signUp ->
                if (signUp) {
                    SignUpColumn(fontSize = 18.sp, onSignInClick = {
                        viewModel.setSignUpState(false)
                    })
                } else {
                    SignInColumn(fontSize = 18.sp, onSignUpClick = {
                        viewModel.setSignUpState(true)
                    })
                }
            }
            //e-mail and password
            Column (
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ){
                StyledTextField(
                    text = uiState.value.email,
                    label = "Email",
                    onValueChange = viewModel::onEmailChange,
                    isError = uiState.value.showEmailError,
                    errorMessage = uiState.value.emailErrorMessage
                )

                AnimatedContent(
                    modifier = Modifier.fillMaxWidth(),
                    targetState = uiState.value.isInSignUpState,
                    transitionSpec = {
                        expandVertically(tween(300)) togetherWith  shrinkVertically(tween(300))
                    }
                ) { signUp ->
                    if (signUp) {
                        StyledTextField(
                            text = uiState.value.nick,
                            label = "Nick",
                            onValueChange = viewModel::onNickChange,
                            isError = uiState.value.showNickError,
                            errorMessage = uiState.value.nickErrorMessage
                        )
                    }
                }

                SecuredTextField(
                    state = rememberTextFieldState(),
                    label = "Password",
                    onValueChange = viewModel::onPasswordChange,
                    isError = uiState.value.showPasswordError,
                    errorMessage = uiState.value.passwordErrorMessage
                )

                AnimatedContent(
                    modifier = Modifier.fillMaxWidth(),
                    targetState = uiState.value.isInSignUpState,
                    transitionSpec = {
                        expandVertically(tween(300)) togetherWith  shrinkVertically(tween(300))
                    }
                ) { signUp ->
                    if (signUp) {
                        SecuredTextField(
                            state = rememberTextFieldState(),
                            label = "Repeat password",
                            onValueChange = viewModel::onRepeatPasswordChange,
                            isError = uiState.value.showRepeatPasswordError,
                            errorMessage = uiState.value.repeatPasswordErrorMessage
                        )
                    }
                }
            }

            ContinueButton(
                onClick = {
                    if (uiState.value.isInSignUpState) {
                        viewModel.register()
                    } else {
                        viewModel.login()
                    }
                }
            )
        }
    }
}

@Composable
private fun SignInColumn(
    fontSize: TextUnit,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign in to your account", fontSize = fontSize)
        Row {
            Text("or ", fontSize = fontSize)
            Text(
                "sign up",
                modifier = Modifier.clickable { onSignUpClick() },
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
            Text(" for SMT", fontSize = fontSize)
        }
    }
}

@Composable
private fun SignUpColumn(
    fontSize: TextUnit,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign up for SMT", fontSize = fontSize)
        Row {
            Text("or ", fontSize = fontSize)
            Text(
                "sign in",
                modifier = Modifier.clickable { onSignInClick() },
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
            Text(" to your account", fontSize = fontSize)
        }
    }
}

@Composable
private fun StyledTextField(
    text: String,
    label: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String
) {
    val roundness = 12.dp
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(roundness))
                .padding(top = 5.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.LightGray,
                errorLabelColor = Color.Red,
            ),
            shape = RoundedCornerShape(roundness),
            singleLine = true,
            isError = isError,
        )

        if (isError && errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun SecuredTextField(
    state: TextFieldState,
    label: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String
) {
    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    val roundness = 12.dp

    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .distinctUntilChanged()
            .collect { onValueChange(it) }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedSecureTextField(
            state = state,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(roundness)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.LightGray,
                errorTextColor = Color.Red,
            ),
            textObfuscationMode =
                if (passwordHidden) TextObfuscationMode.RevealLastTyped
                else TextObfuscationMode.Visible,
            trailingIcon = {
                IconButton(onClick = { passwordHidden = !passwordHidden }) {
                    val visibilityIcon =
                        if (passwordHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    val description = if (passwordHidden) "Show password" else "Hide password"
                    Icon(imageVector = visibilityIcon, contentDescription = description)
                }
            },
            shape = RoundedCornerShape(roundness),
            isError = isError,
        )

        if (isError && errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun ContinueButton(onClick: () -> Unit){
    val roundness = 12.dp
    val text = "Continue"
    Button(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(roundness))
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(roundness),

        ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConnectPreview() {
    SportsMatchTrackerTheme {
        AuthScreen(viewModel = AuthViewModel(AuthRepository()))
    }
}
