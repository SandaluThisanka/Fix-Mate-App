package com.fixmate.presentation.customer.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fixmate.ui.theme.S_YELLOW

// Data class for each favorite service item
data class FavoriteItem(
    val title: String,
    val description: String
)

// Dummy favorite services across categories
val dummyFavorites = listOf(
    FavoriteItem("Plumbing - QuickFix", "Professional leak repair and pipe fitting services."),
    FavoriteItem("Electronics - SmartTech", "Repair for TVs, laptops, and mobile devices."),
    FavoriteItem("Cleaning - SparklePro", "Home and office deep cleaning services."),
    FavoriteItem("Auto Repair - AutoCare", "Full-service vehicle diagnostics and repair."),
    FavoriteItem("Plumbing - DrainXperts", "24/7 emergency drain cleaning services."),
    FavoriteItem("Electronics - GadgetFix", "Fix broken screens, batteries, and circuits."),
    FavoriteItem("Cleaning - GreenClean", "Eco-friendly cleaning solutions for your home."),
    FavoriteItem("Auto Repair - GearHeads", "Oil change, engine tune-up, brake checks.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                ) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (dummyFavorites.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = S_YELLOW
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Favorites",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This is where you'll see your favorite services",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Show list of favorite services
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Your Favorite Services",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(dummyFavorites) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE0E0E0)
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = S_YELLOW
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
