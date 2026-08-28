package com.smartambulance.driver.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.smartambulance.driver.data.Hospital
import com.smartambulance.driver.data.HospitalFilter
import com.smartambulance.driver.services.HospitalDiscoveryService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalSearchScreen(
    hospitalDiscoveryService: HospitalDiscoveryService,
    onHospitalSelected: (Hospital) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var hospitals by remember { mutableStateOf<List<Hospital>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(HospitalFilter()) }
    
    // Get current location on load
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        hospitalDiscoveryService.getCurrentLocation()
            .onSuccess { location ->
                currentLocation = location
                hospitalDiscoveryService.findNearbyHospitals(location)
                    .onSuccess { nearbyHospitals ->
                        hospitals = nearbyHospitals.sortedBy { it.distance }
                    }
                    .onFailure { exception ->
                        errorMessage = "Error finding hospitals: ${exception.message}"
                    }
            }
            .onFailure { exception ->
                errorMessage = "Error getting location: ${exception.message}"
                // Use default location as fallback
                val defaultLocation = LatLng(12.9716, 77.5946) // Bangalore default
                currentLocation = defaultLocation
                hospitalDiscoveryService.findNearbyHospitals(defaultLocation)
                    .onSuccess { nearbyHospitals ->
                        hospitals = nearbyHospitals.sortedBy { it.distance }
                    }
            }
        isLoading = false
    }
    
    // Search functionality with debouncing
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            val location = currentLocation
            if (location != null) {
                kotlinx.coroutines.delay(500) // Debounce
                hospitalDiscoveryService.searchHospitals(searchQuery, location)
                    .onSuccess { searchResults ->
                        hospitals = searchResults
                    }
            }
        } else if (searchQuery.isBlank()) {
            val location = currentLocation
            if (location != null) {
                hospitalDiscoveryService.findNearbyHospitals(location)
                    .onSuccess { nearbyHospitals ->
                        hospitals = nearbyHospitals.sortedBy { it.distance }
                    }
            }
        }
    }
    
    // Get current location on load
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        hospitalDiscoveryService.getCurrentLocation()
            .onSuccess { location ->
                currentLocation = location
                hospitalDiscoveryService.findNearbyHospitals(location)
                    .onSuccess { nearbyHospitals ->
                        hospitals = nearbyHospitals.sortedBy { it.distance }
                    }
                    .onFailure { exception ->
                        errorMessage = "Error finding hospitals: ${exception.message}"
                    }
            }
            .onFailure { exception ->
                errorMessage = "Error getting location: ${exception.message}"
                // Use default location as fallback
                val defaultLocation = LatLng(12.9716, 77.5946) // Bangalore default
                currentLocation = defaultLocation
                hospitalDiscoveryService.findNearbyHospitals(defaultLocation)
                    .onSuccess { nearbyHospitals ->
                        hospitals = nearbyHospitals.sortedBy { it.distance }
                    }
            }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Hospital") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search hospitals by name or specialty...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Finding nearby hospitals...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Filter chips
            if (currentFilter.emergencyOnly || currentFilter.isOpenNow) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentFilter.emergencyOnly) {
                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text("Emergency Only") },
                            leadingIcon = {
                                Icon(Icons.Default.LocalHospital, contentDescription = null)
                            }
                        )
                    }
                    if (currentFilter.isOpenNow) {
                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text("Open Now") },
                            leadingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = null)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Hospital count
            if (hospitals.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${hospitals.size} hospitals found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    currentLocation?.let { location ->
                        Text(
                            text = "Near ${location.latitude}, ${location.longitude}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Hospital list
            if (hospitals.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(hospitals) { hospital ->
                        HospitalCard(
                            hospital = hospital,
                            onClick = { onHospitalSelected(hospital) }
                        )
                    }
                }
            } else if (!isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalHospital,
                            contentDescription = "No hospitals",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) {
                                "No hospitals found nearby"
                            } else {
                                "No hospitals found for \"$searchQuery\""
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try a different search or location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // Filter dialog
    if (showFilterDialog) {
        HospitalFilterDialog(
            currentFilter = currentFilter,
            onFilterChanged = { newFilter ->
                currentFilter = newFilter
                hospitals = hospitalDiscoveryService.filterHospitals(hospitals, newFilter)
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun HospitalCard(
    hospital: Hospital,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with name and emergency badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hospital.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (hospital.emergencyServices) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "24/7",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Address
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Address",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = hospital.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Stats row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Distance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = "Distance",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDistance(hospital.distance),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Rating
                if (hospital.rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = hospital.rating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Phone
                if (hospital.phone.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Phone",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Call",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Open status
            if (!hospital.isOpen) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Status",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Currently closed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun HospitalFilterDialog(
    currentFilter: HospitalFilter,
    onFilterChanged: (HospitalFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var emergencyOnly by remember { mutableStateOf(currentFilter.emergencyOnly) }
    var isOpenNow by remember { mutableStateOf(currentFilter.isOpenNow) }
    var maxDistance by remember { mutableStateOf(currentFilter.maxDistance) }
    var minRating by remember { mutableStateOf(currentFilter.minRating) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Hospitals") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Emergency only
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Emergency services only")
                    Switch(
                        checked = emergencyOnly,
                        onCheckedChange = { emergencyOnly = it }
                    )
                }
                
                // Open now
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Open now")
                    Switch(
                        checked = isOpenNow,
                        onCheckedChange = { isOpenNow = it }
                    )
                }
                
                // Max distance
                Column {
                    Text("Maximum distance: ${maxDistance / 1000} km")
                    Slider(
                        value = maxDistance.toFloat(),
                        onValueChange = { maxDistance = it.toInt() },
                        valueRange = 1f..50f,
                        steps = 49
                    )
                }
                
                // Minimum rating
                Column {
                    Text("Minimum rating: ${minRating} stars")
                    Slider(
                        value = minRating,
                        onValueChange = { minRating = it },
                        valueRange = 0f..5f,
                        steps = 10
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onFilterChanged(
                        HospitalFilter(
                            emergencyOnly = emergencyOnly,
                            maxDistance = maxDistance,
                            minRating = minRating,
                            isOpenNow = isOpenNow
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatDistance(meters: Double): String {
    return when {
        meters < 1000 -> "${meters.toInt()} m"
        else -> String.format("%.1f km", meters / 1000)
    }
}