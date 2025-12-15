package com.fixmate.presentation.components.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.fixmate.R
import com.fixmate.utils.Constants

data class ServiceProvider(
    val id: String,
    val name: String,
    val type: ServiceType,
    val latitude: Double,
    val longitude: Double,
    val rating: Float = 0f,
    val description: String = "",
    val hourlyRate: Double = 0.0,
    val phone: String = "",
    val address: String = "",
    val completedJobs: Int = 0,
    val profileImageUrl: String = "",
)

enum class ServiceType(val displayName: String, val icon: ImageVector, val color: Color) {
    ALL("All", Icons.Default.AccountBox, Color(0xFFFFC107)),
    MECHANICAL("Mechanical", Icons.Default.Build, Color(0xFFEF4444)),
    ELECTRICAL("Electrical", Icons.Default.Settings, Color(0xFFF59E0B)),
    SUSPENSION("Suspension", Icons.Default.Build, Color(0xFF3B82F6)),
    BRAKES("Brakes", Icons.Default.Warning, Color(0xFF8B5CF6)),
    BODY_CARE("Body & Care", Icons.Default.Star, Color(0xFF10B981)),
    TIRES("Tires", Icons.Default.Build, Color(0xFF6366F1)),
    EMERGENCY("Emergency", Icons.Default.Call, Color(0xFFEC4899))
}

@Composable
fun MapService(
    serviceProviders: List<ServiceProvider> = emptyList(),
    selectedServiceType: ServiceType = ServiceType.ALL,
    showCurrentLocation: Boolean = true,
    onMarkerClick: ((ServiceProvider) -> Unit)? = null,
    onLocationChanged: ((LatLng) -> Unit)? = null,
    modifier: Modifier = Modifier,
    initialZoom: Float = 14f
) {
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        if (showCurrentLocation) {
            requestLocationPermission(context)
            getCurrentLocation(context, fusedLocationClient) { location ->
                currentLocation = location
            }
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, initialZoom)
            onLocationChanged?.invoke(location)
        }
    }

    val filteredProviders = remember(serviceProviders, currentLocation, selectedServiceType) {
        serviceProviders.filter { provider ->
            val isTypeMatch = selectedServiceType == ServiceType.ALL || provider.type == selectedServiceType
            val isInRange = currentLocation?.let { current ->
                val distance = calculateDistance(
                    current.latitude,
                    current.longitude,
                    provider.latitude,
                    provider.longitude
                )
                distance <= Constants.NEARBY_PROVIDER_RADIUS_KM * 1000
            } ?: true
            isTypeMatch && isInRange
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = true
            ),
            properties = MapProperties(
                isMyLocationEnabled = showCurrentLocation && currentLocation != null
            )
        ) {
            filteredProviders.forEach { provider ->
                Marker(
                    state = MarkerState(LatLng(provider.latitude, provider.longitude)),
                    title = provider.name,
                    snippet = "${provider.type.displayName} (${
                        currentLocation?.let { current ->
                            val distance = calculateDistance(
                                current.latitude,
                                current.longitude,
                                provider.latitude,
                                provider.longitude
                            )
                            String.format("%.1f km", distance / 1000)
                        } ?: "Unknown distance"
                    })",
                    icon = BitmapDescriptorFactory.fromResource(
                        when (provider.type) {
                            ServiceType.MECHANICAL -> R.drawable.map4_24
                            ServiceType.ELECTRICAL -> R.drawable.map3_24
                            ServiceType.SUSPENSION -> R.drawable.map2_24
                            ServiceType.BRAKES -> R.drawable.map1_24
                            ServiceType.BODY_CARE -> R.drawable.map1_24
                            ServiceType.TIRES -> R.drawable.map2_24
                            ServiceType.EMERGENCY -> R.drawable.map4_24
                            ServiceType.ALL -> R.drawable.map2_24
                        }
                    ),
                    onClick = {
                        onMarkerClick?.invoke(provider)
                        false
                    }
                )
            }
        }

        if (showCurrentLocation) {
            FloatingActionButton(
                onClick = {
                    currentLocation?.let { location ->
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(location, initialZoom)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Current Location"
                )
            }
        }
    }
}

private fun requestLocationPermission(context: Context) {
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            context as android.app.Activity,
            permissions,
            1
        )
    }
}

private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationResult: (LatLng) -> Unit
) {
    try {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        onLocationResult(LatLng(it.latitude, it.longitude))
                    } ?: run {
                        fusedLocationClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                            null
                        ).addOnSuccessListener { freshLocation ->
                            freshLocation?.let {
                                onLocationResult(LatLng(it.latitude, it.longitude))
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Float {
    val results = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, results)
    return results[0]
}
