package com.fixmate.presentation.provider.service

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fixmate.R
import com.fixmate.data.models.PricingModel
import com.fixmate.data.models.Service
import com.fixmate.data.models.ServiceCategory
import com.fixmate.ui.theme.S_LIGHT_YELLOW
import com.fixmate.ui.theme.S_STROKE_COLOR
import com.fixmate.ui.theme.S_YELLOW
import com.fixmate.ui.theme.SevaLKTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceSelectionScreen(
    viewModel: ServiceViewModel = hiltViewModel(),
    onNavigateToNext: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCount = uiState.selectedServiceCount

    // Handle error display
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // You can show a snackbar or toast here
            // For now, we'll just log it
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier
            .padding(bottom = 40.dp),
        topBar = {
            TopAppBar(
                title = { Text("Set Up Your Services", fontWeight = FontWeight.Medium, fontSize = 24.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomBar(
                selectedCount = selectedCount, 
                isLoading = uiState.isLoading,
                onComplete = {
                    viewModel.saveSelectedServices(
                        onSuccess = onNavigateToNext,
                        onError = { /* Error is handled in uiState */ }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select all the services you provide. You can always add or remove services later.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                query = uiState.searchQuery,
                onQueryChanged = viewModel::onSearchQueryChanged
            )
            Spacer(modifier = Modifier.height(16.dp))
            ServiceList(
                categories = uiState.filteredCategories,
                onCategoryToggled = viewModel::onCategoryToggled,
                onServiceSelected = viewModel::onServiceSelected,
                onPriceChanged = viewModel::onPriceChanged
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search services...") },
        leadingIcon = {
            Icon(
                modifier = Modifier
                    .size(24.dp),
                painter = painterResource(id = R.drawable.search),
                tint = Color.Unspecified,
                contentDescription = "Search Icon")

                      },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF0F0F0),
            focusedContainerColor = Color(0xFFF0F0F0),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = S_LIGHT_YELLOW
        ),
        singleLine = true
    )
}

@Composable
fun ServiceList(
    categories: List<ServiceCategory>,
    onCategoryToggled: (String) -> Unit,
    onServiceSelected: (String, Int, Boolean) -> Unit,
    onPriceChanged: (String, Int, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it.name }) { category ->
            ServiceCategoryItem(
                category = category,
                onCategoryToggled = { onCategoryToggled(category.name) },
                onServiceSelected = { serviceId, isSelected -> onServiceSelected(category.name, serviceId, isSelected) },
                onPriceChanged = { serviceId, price -> onPriceChanged(category.name, serviceId, price) }
            )
        }
    }
}

@Composable
fun ServiceCategoryItem(
    category: ServiceCategory,
    onCategoryToggled: () -> Unit,
    onServiceSelected: (Int, Boolean) -> Unit,
    onPriceChanged: (Int, String) -> Unit
) {
    val selectedInCategory = category.services.count { it.isSelected }
    val totalInCategory = category.services.size

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCategoryToggled)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${category.name} ($selectedInCategory/$totalInCategory)",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (category.isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = "Toggle Category"
            )
        }

        if (category.isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                category.services.forEach { service ->
                    ServiceRow(
                        service = service,
                        onServiceSelected = { isSelected -> onServiceSelected(service.id, isSelected) },
                        onPriceChanged = { price -> onPriceChanged(service.id, price) }
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceRow(
    service: Service,
    onServiceSelected: (Boolean) -> Unit,
    onPriceChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, S_STROKE_COLOR, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = service.isSelected,
                onCheckedChange = onServiceSelected,
                colors = CheckboxDefaults.colors(checkedColor = S_YELLOW)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = service.name, fontWeight = FontWeight.Medium)
                Text(text = "Pricing: ${service.pricingModel.displayName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        if (service.isSelected) {
            Spacer(modifier = Modifier.height(12.dp))
            PriceInput(service = service, onPriceChanged = onPriceChanged)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceInput(service: Service, onPriceChanged: (String) -> Unit) {
    val suffix = when(service.pricingModel) {
        PricingModel.HOURLY -> "/ hour"
        PricingModel.PER_SQ_FT -> "/ sq. ft."
        else -> ""
    }

    OutlinedTextField(
        value = service.price,
        onValueChange = onPriceChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("LKR Enter price") },
        trailingIcon = { if(suffix.isNotEmpty()) Text(suffix, color = Color.Gray, modifier = Modifier.padding(end=12.dp)) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFFAFAFA),
            focusedContainerColor = Color(0xFFFAFAFA),
            unfocusedBorderColor = S_STROKE_COLOR,
            focusedBorderColor = S_LIGHT_YELLOW,
            focusedLabelColor = Color.Gray
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceProviderInput(name: String, onNameChanged: (String) -> Unit) {
    Column {
        Text("Service Provider Name", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., Amal's Electricians") },
            leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = "Provider Name") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF0F0F0),
                focusedContainerColor = Color(0xFFF0F0F0),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )
    }
}

@Composable
fun BottomBar(selectedCount: Int, isLoading: Boolean = false, onComplete: () -> Unit) {
    Surface(
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$selectedCount service${if (selectedCount != 1) "s" else ""} selected",
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = onComplete,
                enabled = selectedCount > 0 && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = S_YELLOW,
                    contentColor = Color.Black
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isLoading) "Saving..." else "Complete",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview
@Composable
fun ServiceSelectionScreenPreview() {
    SevaLKTheme {
        // For preview, we need to provide a mock viewModel since Hilt isn't available in previews
        // This is just for preview purposes
    }
}