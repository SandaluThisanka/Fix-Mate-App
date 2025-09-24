package com.fixmate.presentation.auth.registration

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fixmate.R
import com.fixmate.data.models.UserType
import com.fixmate.presentation.auth.components.AuthHeader
import com.fixmate.presentation.auth.components.CustomTextField
import com.fixmate.presentation.components.common.PrimaryButton
import com.fixmate.presentation.components.common.ProgressIndicator
import com.fixmate.ui.theme.S_INPUT_BACKGROUND
import com.fixmate.ui.theme.S_LIGHT_TEXT
import com.fixmate.ui.theme.S_YELLOW
import com.fixmate.ui.theme.SevaLKTheme

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {},
    onNavigateToServiceSelection: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToUserTypeSelection: (String, String) -> Unit = { _, _ -> }
) {
    val uiState = viewModel.uiState.collectAsState().value
    
    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleSignInResult(result.data, onNavigateToUserTypeSelection)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column {
            AuthHeader(
                title = when (uiState.currentStep) {
                    1 -> "Let's Get Started"
                    2 -> "Verify Your Email"
                    3 -> "Almost There"
                    else -> "Registration"
                },
            )
            
            // Show error message if present
            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                ProgressIndicator(
                    currentStep = uiState.currentStep,
                    totalSteps = 3
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                // Show loading indicator when processing
                // if (uiState.isLoading) {
                //     androidx.compose.material3.CircularProgressIndicator(
                //         modifier = Modifier.align(Alignment.CenterHorizontally)
                //     )
                // }
                
                when (uiState.currentStep) {
                    1 -> Step1GetStarted(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onNavigateToLogin = onNavigateToLogin,
                        onGoogleSignIn = {
                            viewModel.initiateGoogleSignIn { signInIntent ->
                                googleSignInLauncher.launch(signInIntent)
                            }
                        }
                    )
                    2 -> Step2VerifyEmail(
                        uiState = uiState,
                        onEvent = viewModel::onEvent
                    )
                    3 -> Step3AlmostThere(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onNavigateToServiceSelection = onNavigateToServiceSelection,
                        onNavigateToHome = onNavigateToHome
                    )
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) { } // Prevent clicks while loading
                    .background(Color.Black.copy(alpha = 0.5f)), // Semi-transparent background
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun Step1GetStarted(
    uiState: RegistrationState,
    onEvent: (RegistrationEvent) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {}
) {
    Column {
        // Full Name TextField
        CustomTextField(
            value = uiState.fullName,
            onValueChange = { onEvent(RegistrationEvent.FullNameChanged(it)) },
            label = "Full Name",
            placeholder = "Enter your full name",
            leadingIcon = painterResource(id = R.drawable.user),
            keyboardType = KeyboardType.Text,
        )

        // Email TextField
        CustomTextField(
            value = uiState.email,
            onValueChange = { onEvent(RegistrationEvent.EmailChanged(it)) },
            label = "Email Address",
            placeholder = "Enter your email",
            leadingIcon = painterResource(id = R.drawable.email),
            keyboardType = KeyboardType.Email,
        )

        Spacer(modifier = Modifier.height(36.dp))

        PrimaryButton(
            text = "Next: Verify Email",
            onClick = {
                onEvent(RegistrationEvent.NextStep)
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Or continue with divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray
            )
            Text(
                text = "Or sign up using",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = S_INPUT_BACKGROUND
            ),
            contentPadding = PaddingValues(0.dp),
            enabled = !uiState.isLoading
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google Sign-In",
                modifier = Modifier.size(34.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.height(28.dp))


        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account? ",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Login",
                color = Color(0xFF1D4EDA),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}


@Composable
fun Step2VerifyEmail(
    uiState: RegistrationState,
    onEvent: (RegistrationEvent) -> Unit
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val clipboardManager = LocalClipboardManager.current
    
    Column {
        Text(
            text = "We've sent a verification code to ${uiState.email}. Please check your inbox (and spam folder) and click the verification link or enter the code below.",
            textAlign = TextAlign.Justify,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Verification Code",
            color = S_LIGHT_TEXT,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..5) {
                TextField(
                    value = uiState.verificationCode[i],
                    onValueChange = { newValue ->
                        // Handle paste operation
                        if (newValue.length > 1) {
                            val digits = newValue.filter { it.isDigit() }.take(6)
                            for (j in digits.indices) {
                                if (i + j < 6) {
                                    onEvent(RegistrationEvent.VerificationCodeChanged(i + j, digits[j].toString()))
                                }
                            }
                            // Focus the next empty field or the last field
                            val nextFocusIndex = (i + digits.length).coerceAtMost(5)
                            focusRequesters[nextFocusIndex].requestFocus()
                        } else {
                            // Handle single character input
                            val filteredValue = newValue.filter { it.isDigit() }.take(1)
                            onEvent(RegistrationEvent.VerificationCodeChanged(i, filteredValue))
                            
                            // Auto-focus next field if a digit was entered
                            if (filteredValue.isNotEmpty() && i < 5) {
                                focusRequesters[i + 1].requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequesters[i]),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (i == 5) ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (i < 5) {
                                focusRequesters[i + 1].requestFocus()
                            }
                        },
                        onDone = {
                            // Hide keyboard or trigger verification
                            focusRequesters[i].freeFocus()
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = S_INPUT_BACKGROUND,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    )
                )
            }
        }
        
        // Auto-focus first empty field when the screen loads
        LaunchedEffect(Unit) {
            val firstEmptyIndex = uiState.verificationCode.indexOfFirst { it.isEmpty() }
            if (firstEmptyIndex != -1) {
                focusRequesters[firstEmptyIndex].requestFocus()
            } else {
                focusRequesters[0].requestFocus()
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Resend Verification Code",
            color = S_YELLOW,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { /* TODO: Resend code */ }
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(64.dp))
        PrimaryButton(
            text = "Verify & Continue",
            onClick = {
                onEvent(RegistrationEvent.NextStep)
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Entered the wrong email? ",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Go Back",
                color = Color(0xFF1D4EDA),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onEvent(RegistrationEvent.PreviousStep) }
            )
        }
    }
}

@Composable
fun Step3AlmostThere(
    uiState: RegistrationState,
    onEvent: (RegistrationEvent) -> Unit,
    onNavigateToServiceSelection: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    Column{
        Text(
            "How will you primarily use SevaLK?",
            fontWeight = FontWeight.Medium,
            color = S_LIGHT_TEXT,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.padding(start = 8.dp),
        ) {
            UserType.entries.forEach { userType ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(RegistrationEvent.UserTypeChanged(userType)) }
                ) {
                    RadioButton(
                        selected = uiState.userType == userType,
                        onClick = { onEvent(RegistrationEvent.UserTypeChanged(userType)) },
                        colors = RadioButtonDefaults.colors(selectedColor = S_YELLOW),
                    )
                    Text(text = userType.displayName, color = Color.Black)
                }
            }
        }
        Spacer(modifier = Modifier.height(36.dp))
        HorizontalDivider(
            color = Color.LightGray
        )
        Spacer(modifier = Modifier.height(36.dp))

        CustomTextField(
            value = uiState.password,
            onValueChange = { onEvent(RegistrationEvent.PasswordChanged(it)) },
            label = "Create Password",
            placeholder = "Enter your password",
            leadingIcon = painterResource(id = R.drawable.lock),
            keyboardType = KeyboardType.Password,
            isPasswordField = true,
            isPasswordVisible = uiState.isPasswordVisible,
            trailingIcon = if (uiState.isPasswordVisible) {
                painterResource(id = R.drawable.eye_slash)
            } else {
                painterResource(id = R.drawable.eye)
            },
            onTrailingIconClick = { onEvent(RegistrationEvent.TogglePasswordVisibility) }
        )

        CustomTextField(
            value = uiState.confirmPassword,
            onValueChange = { onEvent(RegistrationEvent.ConfirmPasswordChanged(it)) },
            label = "Confirm Password",
            placeholder = "Re Enter your password",
            leadingIcon = painterResource(id = R.drawable.lock),
            keyboardType = KeyboardType.Password,
            isPasswordField = true,
            isPasswordVisible = uiState.isConfirmPasswordVisible,
            trailingIcon = if (uiState.isConfirmPasswordVisible) {
                painterResource(id = R.drawable.eye_slash)
            } else {
                painterResource(id = R.drawable.eye)
            },
            onTrailingIconClick = { onEvent(RegistrationEvent.ToggleConfirmPasswordVisibility) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = "Create My Account",
            onClick = {
                if (uiState.userType == UserType.SERVICE_PROVIDER) {
                    onEvent(RegistrationEvent.SubmitServiceProviderRegistration(onNavigateToServiceSelection))
                } else {
                    onEvent(RegistrationEvent.SubmitCustomerRegistration(onNavigateToHome))
                }
            }
        )
    }
}

@Preview
@Composable
fun RegistrationScreenPreview() {
    SevaLKTheme {
        Step2VerifyEmail(
            uiState = RegistrationState(
                email = "bimsaraudara25@gmail.com"
            ),
            onEvent = {}
        )
}
}