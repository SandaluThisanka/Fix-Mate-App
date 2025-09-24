package com.fixmate.presentation.provider.location

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.fixmate.R
import com.fixmate.data.models.LocationMethod
import com.fixmate.data.models.ServiceLocation
import com.fixmate.presentation.components.common.PrimaryButton
import com.fixmate.ui.theme.S_LIGHT_YELLOW
import com.fixmate.ui.theme.SevaLKTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetLocationScreen(
    onSetupComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: SetLocationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val serviceRadius by viewModel.serviceRadius.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var hasRequestedInitialPermission by remember { mutableStateOf(false) }
    
    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Permission granted, get location
            if (hasRequestedInitialPermission) {
                viewModel.initializeWithPermission()
            } else {
                viewModel.getCurrentLocationWithPermission()
            }
        } else {
            // Permission denied, show error or fallback
            viewModel.onLocationPermissionDenied()
        }
    }
    
    // Request permission on initial load
    LaunchedEffect(Unit) {
        if (!hasRequestedInitialPermission) {
            hasRequestedInitialPermission = true
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    
    LaunchedEffect(uiState.setupCompleted) {
        if (uiState.setupCompleted) {
            onSetupComplete()
        }
    }

    Surface (
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
    ){
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Set Your Service Location",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "Let customers know where you provide your services",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                LocationMethodSection(
                    methods = uiState.locationMethods,
                    onMethodSelected = { methodId ->
                        if (methodId == "current_location") {
                            locationPermissionLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        } else {
                            viewModel.selectLocationMethod(methodId)
                        }
                    }
                )

                // Map Section (only show when "Select on Map" is selected)
                val selectedMethod = uiState.locationMethods.find { it.isSelected }
                if (selectedMethod?.id == "select_on_map") {
                    GoogleMapSelectionSection(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onLocationSelected = viewModel::setLocationFromMap,
                        currentLocation = uiState.selectedMapLocation
                    )
                }

                // Primary Service Location
                uiState.primaryLocation?.let { location ->
                    PrimaryLocationSection(
                        location = location,
                        onChangeLocation = viewModel::changeLocation
                    )
                }

                // Service Radius
                ServiceRadiusSection(
                    radius = serviceRadius,
                    onRadiusChange = viewModel::updateServiceRadius
                )

                Spacer(modifier = Modifier.weight(1f))

                // Complete Setup Button
                PrimaryButton(
                    text = "Complete Setup",
                    onClick = {
                        viewModel.completeSetup()
                    }
                )
                
                // Error message
                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearError() }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss error",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFC107)
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationMethodSection(
    methods: List<LocationMethod>,
    onMethodSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Choose Location method",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        
        methods.forEach { method ->
            LocationMethodCard(
                method = method,
                onSelected = { onMethodSelected(method.id) }
            )
        }
    }
}

@Composable
private fun LocationMethodCard(
    method: LocationMethod,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (method.isSelected) Color(0xFFFFF8E1) else Color.White
        ),
        border = if (method.isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFC107))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFC107)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (method.id == "current_location") painterResource(id = R.drawable.crosshair) else painterResource(id = R.drawable.globe),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = method.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = method.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // Check mark
            if (method.isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun PrimaryLocationSection(
    location: ServiceLocation,
    onChangeLocation: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Primary Service Location",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            TextButton(
                onClick = onChangeLocation,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFFFC107)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Change")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = location.address,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "${location.city}, ${location.province}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ServiceRadiusSection(
    radius: Float,
    onRadiusChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Service Radius: ${radius.toInt()} km",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        
        Slider(
            value = radius,
            onValueChange = onRadiusChange,
            valueRange = 1f..50f,
            steps = 49,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color(0xFFFFC107),
                inactiveTrackColor = Color.LightGray
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1 km", fontSize = 12.sp, color = Color.Gray)
            Text("25 km", fontSize = 12.sp, color = Color.Gray)
            Text("50 km", fontSize = 12.sp, color = Color.Gray)
        }
        
        Text(
            text = "You'll be visible to customers within ${radius.toInt()} km of your location",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun GoogleMapSelectionSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onLocationSelected: (Double, Double) -> Unit,
    currentLocation: LatLng?
) {
    var selectedLocation by remember { mutableStateOf(currentLocation ?: LatLng(6.0329, 80.2168)) } // Default to Colombo
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Map with Search Bar overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            // Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { clickedLocation ->
                    selectedLocation = clickedLocation
                    onLocationSelected(clickedLocation.latitude, clickedLocation.longitude)
                }
            ) {
                Marker(
                    state = MarkerState(position = selectedLocation),
                    title = "Selected Location",
                    snippet = "Tap to confirm this location"
                )
            }
            
            // Search Bar positioned at top of map
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { 
                    Text(
                        text = "Search location...",
                        color = Color.Gray
                    )
                },
                leadingIcon = { 
                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        painter = painterResource(id = R.drawable.search), 
                        tint = Color.Unspecified, 
                        contentDescription = "Search Icon"
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = S_LIGHT_YELLOW,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color(0xFFFFC107),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        // Location info
        Text(
            text = "Lat: ${String.format("%.6f", selectedLocation.latitude)}, Lng: ${String.format("%.6f", selectedLocation.longitude)}",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Tap on the map to select your service location",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Preview
@Composable
fun PreviewSetLocationScreen() {
    SevaLKTheme {
        SetLocationScreen()
    }
}