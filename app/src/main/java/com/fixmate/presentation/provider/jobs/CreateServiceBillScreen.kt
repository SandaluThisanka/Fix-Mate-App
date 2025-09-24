package com.fixmate.presentation.provider.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fixmate.data.models.*
import com.fixmate.presentation.provider.jobs.components.AddAdditionalCostDialog
import com.fixmate.ui.theme.S_YELLOW
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceBillScreen(
    booking: Booking,
    onBackClick: () -> Unit,
    onConfirmBill: () -> Unit,
    viewModel: CreateServiceBillViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddCostDialog by remember { mutableStateOf(false) }
    val decimalFormat = DecimalFormat("#,##0.00")
    
    // Load provider services when screen loads
    LaunchedEffect(booking.id) {
        viewModel.loadProviderServices(booking)
    }
    
    // Handle bill creation success
    LaunchedEffect(uiState.billCreated) {
        if (uiState.billCreated) {
            onConfirmBill()
        }
    }
    
    // Show error messages
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // You can show a snackbar here if needed
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top bar
        TopAppBar(
            title = { Text("Create Service Bill", fontWeight = FontWeight.SemiBold, fontSize = 24.sp) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = S_YELLOW)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Job details header
                item {
                    Column {
                        Text(
                            text = "Job Title: ${booking.toJobTitle()}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Customer: ${booking.customerName}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Date: ${booking.toJobDate()}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                // Service Details section
                item {
                    Text(
                        text = "Service Details",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(uiState.serviceItems) { item ->
                    ServiceItemCard(
                        serviceItem = item,
                        onValueChange = { quantity ->
                            viewModel.updateServiceItemQuantity(item.serviceId, quantity)
                        }
                    )
                }
                
                // Additional Costs section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Additional Costs",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Button(
                            onClick = { showAddCostDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = S_YELLOW),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add Cost", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
                
                items(uiState.additionalCosts) { cost ->
                    AdditionalCostItem(
                        cost = cost,
                        onRemove = { viewModel.removeAdditionalCost(cost) }
                    )
                }
                
                // Additional Total
                if (uiState.additionalCosts.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Additional Total",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "LKR ${decimalFormat.format(uiState.additionalCosts.sumOf { it.amount })}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                // Notes section
                item {
                    Text(
                        text = "Notes (Optional)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        placeholder = { Text("Add any additional notes...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Bill Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Bill Summary",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal:", fontSize = 14.sp)
                                Text("LKR ${decimalFormat.format(uiState.subtotal)}", fontSize = 14.sp)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Platform Fee (2%):", fontSize = 14.sp)
                                Text("LKR ${decimalFormat.format(uiState.platformFee)}", fontSize = 14.sp)
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "LKR ${decimalFormat.format(uiState.total)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Confirm Bill button
        Button(
            onClick = {
                viewModel.confirmBill(
                    onSuccess = onConfirmBill,
                    onError = { /* Handle error */ }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = S_YELLOW),
            shape = RoundedCornerShape(12.dp),
            enabled = !uiState.isCreatingBill && uiState.serviceItems.any { item ->
                when (item.pricingModel) {
                    PricingModel.FIXED -> item.calculatedAmount > 0
                    else -> item.quantity.isNotBlank() && item.calculatedAmount > 0
                }
            }
        ) {
            if (uiState.isCreatingBill) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (uiState.isCreatingBill) "Creating Bill..." else "✓ Confirm Bill",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
    
    // Add Cost Dialog
    if (showAddCostDialog) {
        AddAdditionalCostDialog(
            onDismiss = { showAddCostDialog = false },
            onConfirm = { name, amount ->
                viewModel.addAdditionalCost(name, amount)
                showAddCostDialog = false
            }
        )
    }
}

@Composable
fun ServiceItemCard(
    serviceItem: BillServiceItem,
    onValueChange: (String) -> Unit
) {
    val decimalFormat = DecimalFormat("#,##0.00")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = serviceItem.serviceName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "LKR ${decimalFormat.format(serviceItem.calculatedAmount)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (serviceItem.calculatedAmount > 0) Color.Black else Color.Gray
                )
            }
            
            Text(
                text = "LKR ${serviceItem.basePrice} ${serviceItem.pricingModel.displayName.lowercase()}",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = serviceItem.getInputLabel(),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            
            OutlinedTextField(
                value = if (serviceItem.pricingModel == PricingModel.FIXED) 
                    "Fixed Amount: LKR ${serviceItem.basePrice}" 
                else 
                    serviceItem.quantity,
                onValueChange = { value ->
                    // Fixed pricing doesn't need quantity input
                    if (serviceItem.pricingModel == PricingModel.FIXED) {
                        return@OutlinedTextField
                    }
                    
                    // Validate numeric input
                    if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                        onValueChange(value)
                    }
                },
                placeholder = { Text(serviceItem.getInputPlaceholder()) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(8.dp),
                enabled = serviceItem.pricingModel != PricingModel.FIXED,
                colors = if (serviceItem.pricingModel == PricingModel.FIXED) {
                    OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Gray,
                        disabledBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                } else {
                    OutlinedTextFieldDefaults.colors()
                }
            )
            
            if (serviceItem.pricingModel == PricingModel.FIXED) {
                Text(
                    text = "Fixed price service",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AdditionalCostItem(
    cost: BillAdditionalCost,
    onRemove: () -> Unit
) {
    val decimalFormat = DecimalFormat("#,##0.00")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cost.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = "LKR ${decimalFormat.format(cost.amount)}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.Red
            )
        }
    }
}

@Preview
@Composable
fun CreateServiceBillScreenPreview() {
    CreateServiceBillScreen(
        booking = Booking(
            id = "1",
            customerName = "John Smith",
            serviceName = "Kitchen Plumbing Repair",
            description = "Kitchen sink repair",
            scheduledDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // Tomorrow
            scheduledTime = "10:00 AM",
            status = BookingStatus.ACCEPTED,
            createdAt = System.currentTimeMillis() - (2 * 60 * 60 * 1000) // 2 hours ago
        ),
        onBackClick = {},
        onConfirmBill = {}
    )
}
