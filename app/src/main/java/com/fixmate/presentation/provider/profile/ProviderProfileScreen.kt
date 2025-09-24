package com.fixmate.presentation.provider.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CalendarToday
import com.fixmate.presentation.navigation.Screen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fixmate.R
import com.fixmate.presentation.customer.profile.EditProfilePopup
import com.fixmate.presentation.customer.profile.ImagePickerDialog
import com.fixmate.presentation.customer.profile.UserProfile
import android.util.Log
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

data class ProviderProfile(
    val name: String,
    val memberSince: String,
    val completedJobs: Int,
    val totalJobs: Int,
    val location: String,
    val totalEarnings: String,
    val email: String,
    val phoneNumber: String,
    val isAvailable: Boolean = true,
    val responseTime: String = "within 1 hour",
    val profileImageUrl: String? = null // Add profile image URL field
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    navController: NavController,
    viewModel: ProviderProfileViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit,
    onSwitchToCustomerClick: () -> Unit
) {
    val providerProfile by viewModel.providerProfile.collectAsState()
    val isUploadingImage by viewModel.isUploadingImage.collectAsState()
    var showEditProfilePopup by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    // Add debug logging
    LaunchedEffect(Unit) {
        Log.d("ProviderProfileScreen", "Screen launched")
    }

    providerProfile?.let { profile ->
        Log.d("ProviderProfileScreen", "Rendering profile for: ${profile.name}")

        val YellowHighlight = Color(0xFF1D4EDA) // Changed to blue
        val CardBackground = Color(0xFFF8F9FA)

        Scaffold(
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Button(
                        onClick = onSwitchToCustomerClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowHighlight,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                            .align(Alignment.End)
                    ) {
                        Text("Switch to Customer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    // Profile Header Section
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Profile Picture
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            ) {
                                // Display profile image or placeholder
                                if (profile.profileImageUrl != null && profile.profileImageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = profile.profileImageUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Show default avatar icon when no profile image
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Default Avatar",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.Center),
                                        tint = Color.Gray
                                    )
                                }
                                
                                // Show loading indicator when uploading (overlay on top of image)
                                if (isUploadingImage) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .clip(CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color(0xFFFDD835),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                            
                            // Edit icon for profile picture - open image picker
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(YellowHighlight)
                                    .border(1.dp, Color.White, CircleShape)
                                    .clickable { showImagePicker = true }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.camera),
                                    contentDescription = "Edit Profile Picture",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Member since ${profile.memberSince}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "${profile.completedJobs} / ${profile.totalJobs} jobs completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = profile.location,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                        // Edit profile icon
                        IconButton(
                            onClick = { showEditProfilePopup = true },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Business Overview Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Business Overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BusinessStat(label = "Total Jobs", value = profile.totalJobs.toString())
                                BusinessStat(label = "Total Earnings", value = profile.totalEarnings)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Canvas(modifier = Modifier.size(8.dp)) {
                                        drawCircle(
                                            color = if (profile.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (profile.isAvailable) "Available" else "Unavailable",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (profile.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                    )
                                }
                                Text(
                                    text = "Responses in ${profile.responseTime}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact Information Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Contact Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ContactInfoItem(icon = Icons.Default.Email, label = "Email Address", value = profile.email)
                            ContactInfoItem(icon = Icons.Default.Phone, label = "Phone Number", value = profile.phoneNumber)
                            ContactInfoItem(icon = Icons.Default.CalendarToday, label = "Join Date", value = "Member since ${profile.memberSince}")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigation Items Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column {
                            NavigationItem(
                                icon = Icons.Default.Build,
                                label = "My Services",
                                description = "Manage service offerings",
                                onClick = {
                                    navController.navigate(Screen.MyServices.route)
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp)
                            NavigationItem(
                                icon = Icons.Default.Payments,
                                label = "Payment Methods",
                                description = "Manage costs & payments",
                                onClick = {
                                    navController.navigate(Screen.PaymentMethods.route)
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp)
                            NavigationItem(
                                icon = Icons.Default.Lock,
                                label = "Privacy & Security",
                                description = "Account personnel",
                                onClick = {
                                    navController.navigate(Screen.PrivacySecurity.route)
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp)
                            NavigationItem(
                                icon = Icons.Default.HelpOutline,
                                label = "Help & Support",
                                description = "Get assistance",
                                onClick = {
                                    navController.navigate(Screen.HelpSupport.route)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Log Out Button
                    OutlinedButton(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Red
                        ),
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.log_out),
                                contentDescription = "Log Out",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Red
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Log Out",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Image Picker Dialog
                if (showImagePicker) {
                    ImagePickerDialog(
                        onDismiss = { showImagePicker = false },
                        onImageSelected = { uri ->
                            viewModel.uploadProfileImage(uri)
                        }
                    )
                }

                // Edit Profile Popup
                if (showEditProfilePopup) {
                    EditProfilePopup(
                        userProfile = UserProfile(
                            name = profile.name,
                            phoneNumber = profile.phoneNumber,
                            memberSince = profile.memberSince,
                            location = profile.location,
                            totalBookings = 0,
                            completedBookings = 0,
                            rating = 0.0,
                            email = profile.email,
                            joinDate = profile.memberSince,
                            profileImageUrl = profile.profileImageUrl
                        ),
                        onDismiss = { showEditProfilePopup = false },
                        onSave = { newName, newPhoneNumber ->
                            viewModel.updateProviderProfile(
                                name = newName,
                                phoneNumber = newPhoneNumber
                            )
                            showEditProfilePopup = false
                        }
                    )
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun BusinessStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ContactInfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun NavigationItem(icon: ImageVector, label: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProviderProfileScreenPreview() {
    MaterialTheme {
        val sampleProfile = ProviderProfile(
            name = "John Plumbing",
            memberSince = "March 2023",
            completedJobs = 43,
            totalJobs = 327,
            location = "Weligama, Southern Province",
            totalEarnings = "LKR 45,600",
            email = "john.obus@email.com",
            phoneNumber = "+94 77 123 4567",
            isAvailable = true,
            responseTime = "1 hour"
        )
        ProviderProfileScreen(
            navController = rememberNavController(),
            onLogoutClick = {},
            onSwitchToCustomerClick = {}
        )
    }
}