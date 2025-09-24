package com.fixmate.presentation.customer.search

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.fixmate.R
import com.fixmate.presentation.components.map.MapService
import com.fixmate.presentation.components.map.ServiceProvider
import com.fixmate.presentation.components.map.ServiceType
import com.fixmate.presentation.components.map.SearchBar
import com.fixmate.presentation.components.map.ServiceTypeFilters
import com.fixmate.presentation.components.common.PrimaryButton
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.res.painterResource
import com.fixmate.ui.theme.S_YELLOW
import androidx.navigation.NavController
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.LatLng
import com.fixmate.presentation.components.CustomerAvatar
import com.fixmate.presentation.components.map.calculateDistance


@Composable
fun ServiceProviderMapScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    navController: NavController? = null,
    onNavigateToBooking: () -> Unit = {},
    initialServiceType: com.fixmate.presentation.components.map.ServiceType? = null,
    onNavigateToMessages: () -> Unit = {}
) {
    var selectedServiceType by remember { mutableStateOf(initialServiceType ?: ServiceType.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<ServiceProvider?>(null) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val providers by viewModel.serviceProviders.collectAsState()

    // If initialServiceType changes (e.g. from HomeScreen), update selectedServiceType
    LaunchedEffect(initialServiceType) {
        if (initialServiceType != null) {
            selectedServiceType = initialServiceType
        }
    }

    LaunchedEffect(searchQuery, selectedServiceType) {
        viewModel.searchProviders(searchQuery, selectedServiceType)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        MapService(
            serviceProviders = providers,
            selectedServiceType = selectedServiceType,
            showCurrentLocation = true,
            onMarkerClick = { provider ->
                selectedProvider = provider
            },
            onLocationChanged = { location -> 
                currentLocation = location
            },
            modifier = Modifier.fillMaxSize()
        )

        SearchBar(
            query = searchQuery,
            onQueryChange = { 
                searchQuery = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.TopStart)
                .offset(y = 10.dp)
                .zIndex(1f)
        )

        ServiceTypeFilters(
            selectedType = selectedServiceType,
            onTypeSelected = { selectedServiceType = it },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .offset(y = 80.dp)
                .zIndex(1f)
        )
        selectedProvider?.let { provider ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .zIndex(2f)
                    .clickable { selectedProvider = null }
            )
            
            AnimatedVisibility(
                visible = selectedProvider != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .zIndex(3f)
            ) {
                ProviderInfoCard(
                    provider = provider,
                    onDismiss = { selectedProvider = null },
                    onBookNow = {
                        selectedProvider = null
                        // Pass the provider ID to the booking screen
                        navController?.navigate("booking/${provider.id}")
                    },
                    currentLocation = currentLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 1.dp),
                    onMessageClick = {
                        selectedProvider = null
                        // Create chat ID using the standard pattern
                        val chatId = "chat_${provider.id}"
                        navController?.navigate("inbox/$chatId/${provider.id}/${provider.name}")
                    }
                )
            }
        }
    }
}

@Composable 
fun ProviderInfoCard(
    provider: ServiceProvider,
    onDismiss: () -> Unit,
    onBookNow: () -> Unit = {},
    currentLocation: LatLng? = null,
    modifier: Modifier = Modifier,
    onMessageClick: () -> Unit = {}
) {
    var isFavorite by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val distance = remember(currentLocation) {
        if (currentLocation != null) {
            val distanceInMeters = calculateDistance(
                currentLocation.latitude,
                currentLocation.longitude, 
                provider.latitude,
                provider.longitude
            )
            String.format("%.1f km", distanceInMeters / 1000)
        } else "N/A"
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
                    .align(Alignment.CenterHorizontally)
                    .clickable { onDismiss() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Provider Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                ) {
                    CustomerAvatar(
                        customerId = provider.id,
                        isProvider = true,
                        size = 60.dp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${provider.rating} (127)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "• $distance",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    
                    // Display service name with additional count like "Cleaning (Residential) +5 more"
                    Text(
                        text = provider.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2196F3)
                    )
                }
                
                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Jobs",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${provider.completedJobs} jobs",
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.Gray
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.circle_dollar_sign_14_14),
                        contentDescription = "Price",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LKR ${provider.hourlyRate.toInt()}/hr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = "Book Now",
                    onClick = onBookNow,
                    modifier = Modifier.weight(1f),
                    backgroundColor = S_YELLOW,
                    foregroundColor = Color.White
                )
                
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${provider.phone}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(52.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.phone_20_20),
                        contentDescription = "Call",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Button(
                    onClick = onMessageClick,
                    modifier = Modifier.size(52.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.message_circle_20_20),
                        contentDescription = "Message",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}