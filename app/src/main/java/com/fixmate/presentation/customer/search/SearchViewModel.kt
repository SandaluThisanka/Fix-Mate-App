package com.fixmate.presentation.customer.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.repositories.ServiceProviderRepository
import com.fixmate.presentation.components.map.ServiceProvider
import com.fixmate.presentation.components.map.ServiceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ServiceProviderRepository
) : ViewModel() {

    private val _serviceProviders = MutableStateFlow<List<ServiceProvider>>(emptyList())
    val serviceProviders: StateFlow<List<ServiceProvider>> = _serviceProviders.asStateFlow()

    init {
        loadAllProviders()
    }

    private fun loadAllProviders() {
        viewModelScope.launch {
            try {
                val providers = repository.getServiceProviders()
                _serviceProviders.value = providers
                Timber.d("Loaded ${providers.size} providers")
                providers.forEach { provider ->
                    Timber.d("Provider: ${provider.name} at (${provider.latitude}, ${provider.longitude})")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading providers")
            }
        }
    }

    fun searchProviders(query: String, serviceType: ServiceType) {
        viewModelScope.launch {
            try {
                val allProviders = if (query.isBlank()) {
                    repository.getServiceProviders()
                } else {
                    repository.searchServiceProviders(query)
                }
                
                // Filter by service type if not ALL
                val filteredProviders = if (serviceType == ServiceType.ALL) {
                    allProviders
                } else {
                    allProviders.filter { it.type == serviceType }
                }
                
                _serviceProviders.value = filteredProviders
                Timber.d("Found ${filteredProviders.size} providers for query: '$query' and type: $serviceType")
            } catch (e: Exception) {
                Timber.e(e, "Error searching providers")
                _serviceProviders.value = emptyList()
            }
        }
    }
}
