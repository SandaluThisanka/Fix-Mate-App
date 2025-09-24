package com.fixmate.presentation.auth.google

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fixmate.data.models.UserType
import com.fixmate.presentation.auth.components.AuthHeader
import com.fixmate.presentation.components.common.PrimaryButton
import com.fixmate.ui.theme.S_LIGHT_TEXT
import com.fixmate.ui.theme.S_YELLOW

@Composable
fun UserTypeSelectionScreen(
    viewModel: UserTypeSelectionViewModel = hiltViewModel(),
    userEmail: String,
    userName: String,
    onNavigateToServiceSelection: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsState().value

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column {
            AuthHeader(
                title = "Complete Your Profile"
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
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "How will you primarily use FixMate?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = S_LIGHT_TEXT,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    UserType.entries.forEach { userType ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    viewModel.onEvent(UserTypeSelectionEvent.UserTypeChanged(userType))
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = uiState.selectedUserType == userType,
                                onClick = { 
                                    viewModel.onEvent(UserTypeSelectionEvent.UserTypeChanged(userType))
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = S_YELLOW),
                            )
                            
                            Column(
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Text(
                                    text = userType.displayName,
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = when (userType) {
                                        UserType.CUSTOMER -> "Find and book trusted local services"
                                        UserType.SERVICE_PROVIDER -> "Offer your services to the community"
                                    },
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                PrimaryButton(
                    text = if (uiState.isLoading) "Creating Account..." else "Continue",
                    onClick = {
                        viewModel.onEvent(
                            UserTypeSelectionEvent.CreateAccount(
                                email = userEmail,
                                fullName = userName,
                                onNavigateToServiceSelection = onNavigateToServiceSelection,
                                onNavigateToHome = onNavigateToHome
                            )
                        )
                    },
                    enabled = !uiState.isLoading
                )
            }
        }
    }
}
