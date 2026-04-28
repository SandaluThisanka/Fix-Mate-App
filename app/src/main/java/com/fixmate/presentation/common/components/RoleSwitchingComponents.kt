package com.fixmate.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Button to switch from customer to provider mode
 * 
 * Shows "Become a Provider" when user is in customer mode and has no provider account
 * Shows "Switch to Provider" when user has an existing provider account
 */
@Composable
fun SwitchToProviderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    hasProviderAccount: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EA),
            contentColor = Color.White,
            disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = if (hasProviderAccount) "Switch to Provider" else "Become a Provider",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Button to switch from provider to customer mode
 */
@Composable
fun SwitchToCustomerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1976D2),
            contentColor = Color.White,
            disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Switch to Customer",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Role indicator badge showing current mode
 */
@Composable
fun RoleIndicator(
    isProviderMode: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isProviderMode) Color(0xFF6200EA) else Color(0xFF1976D2)
    val label = if (isProviderMode) "Provider Mode" else "Customer Mode"

    Box(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Card showing role switching options with current mode
 */
@Composable
fun RoleSwitchingCard(
    isProviderMode: Boolean,
    hasProviderAccount: Boolean,
    onSwitchToProvider: () -> Unit,
    onSwitchToCustomer: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = "Account Management",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Current role indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Role:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                RoleIndicator(isProviderMode)
            }

            Divider(color = Color.LightGray, thickness = 1.dp)

            // Error message if any
            errorMessage?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(4.dp))
                        .padding(8.dp)
                )
            }

            // Switch buttons
            if (isProviderMode && hasProviderAccount) {
                SwitchToCustomerButton(
                    onClick = onSwitchToCustomer,
                    isLoading = isLoading,
                    enabled = !isLoading
                )
            } else if (!isProviderMode) {
                SwitchToProviderButton(
                    onClick = onSwitchToProvider,
                    isLoading = isLoading,
                    enabled = !isLoading,
                    hasProviderAccount = hasProviderAccount
                )
            }

            // Help text
            Text(
                text = if (!isProviderMode && !hasProviderAccount) 
                    "Become a provider to expand your services" 
                else if (!isProviderMode && hasProviderAccount)
                    "Switch to access your provider account"
                else
                    "Switch back to your customer account",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Section divider with role switching capabilities
 */
@Composable
fun RoleSwitchingSection(
    isProviderMode: Boolean,
    hasProviderAccount: Boolean,
    onSwitchToProvider: () -> Unit,
    onSwitchToCustomer: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Switch Account",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )

        if (isProviderMode && hasProviderAccount) {
            SwitchToCustomerButton(
                onClick = onSwitchToCustomer,
                isLoading = isLoading,
                enabled = !isLoading
            )
        } else if (!isProviderMode) {
            SwitchToProviderButton(
                onClick = onSwitchToProvider,
                isLoading = isLoading,
                enabled = !isLoading,
                hasProviderAccount = hasProviderAccount
            )
        }
    }
}

// Preview composables
@Preview(showBackground = true)
@Composable
fun SwitchToProviderButtonPreview() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("With existing provider account:")
        SwitchToProviderButton(
            onClick = {},
            hasProviderAccount = true
        )

        Text("Without provider account:")
        SwitchToProviderButton(
            onClick = {},
            hasProviderAccount = false
        )

        Text("Loading state:")
        SwitchToProviderButton(
            onClick = {},
            isLoading = true,
            hasProviderAccount = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SwitchToCustomerButtonPreview() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Normal state:")
        SwitchToCustomerButton(onClick = {})

        Text("Loading state:")
        SwitchToCustomerButton(
            onClick = {},
            isLoading = true
        )

        Text("Disabled state:")
        SwitchToCustomerButton(
            onClick = {},
            enabled = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RoleIndicatorPreview() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Customer Mode:")
        RoleIndicator(isProviderMode = false)

        Text("Provider Mode:")
        RoleIndicator(isProviderMode = true)
    }
}

@Preview(showBackground = true)
@Composable
fun RoleSwitchingCardPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("In Customer Mode (no provider account):")
        RoleSwitchingCard(
            isProviderMode = false,
            hasProviderAccount = false,
            onSwitchToProvider = {},
            onSwitchToCustomer = {}
        )

        Text("In Provider Mode (has provider account):")
        RoleSwitchingCard(
            isProviderMode = true,
            hasProviderAccount = true,
            onSwitchToProvider = {},
            onSwitchToCustomer = {}
        )

        Text("In Customer Mode (has provider account):")
        RoleSwitchingCard(
            isProviderMode = false,
            hasProviderAccount = true,
            onSwitchToProvider = {},
            onSwitchToCustomer = {}
        )

        Text("With error message:")
        RoleSwitchingCard(
            isProviderMode = false,
            hasProviderAccount = false,
            onSwitchToProvider = {},
            onSwitchToCustomer = {},
            errorMessage = "Failed to switch account. Please try again."
        )
    }
}
