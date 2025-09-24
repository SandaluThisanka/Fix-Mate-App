package com.fixmate.presentation.provider.jobs.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fixmate.ui.theme.S_YELLOW

@Composable
fun AddAdditionalCostDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var costName by remember { mutableStateOf("") }
    var costAmount by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Additional Cost",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = costName,
                    onValueChange = { 
                        costName = it
                        isError = false
                    },
                    label = { Text("Cost Description") },
                    placeholder = { Text("e.g., Materials, Transport fee") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = isError && costName.isBlank()
                )
                
                OutlinedTextField(
                    value = costAmount,
                    onValueChange = { 
                        // Only allow numeric input
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            costAmount = it
                            isError = false
                        }
                    },
                    label = { Text("Amount (LKR)") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    isError = isError && (costAmount.isBlank() || costAmount.toDoubleOrNull() == null || costAmount.toDoubleOrNull()!! <= 0)
                )
                
                if (isError) {
                    Text(
                        text = "Please fill in all fields with valid values",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val amount = costAmount.toDoubleOrNull()
                            if (costName.isNotBlank() && amount != null && amount > 0) {
                                onConfirm(costName, amount)
                            } else {
                                isError = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = S_YELLOW),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add", color = Color.White)
                    }
                }
            }
        }
    }
}
