package com.fixmate.presentation.components.common

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fixmate.utils.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPermissionDialog(
    onPermissionResult: (Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
        showDialog = false
    }

    // Only show if permission is not already granted
    if (!PermissionUtils.checkNotificationPermission(context) && showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onPermissionResult(false)
            },
            title = {
                Text(
                    text = "Enable Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "FixMate needs notification permission to keep you updated about your bookings and services."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {3
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            showDialog = false
                            onPermissionResult(true)
                        }
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onPermissionResult(false)
                    }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
}