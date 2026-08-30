package com.smartambulance.driver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.smartambulance.driver.data.Hospital
import com.smartambulance.driver.data.HospitalFilter
import com.smartambulance.driver.services.HospitalDiscoveryService
import com.smartambulance.driver.ui.theme.*

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Hospitals") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search hospitals...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HospitalGreen,
                    unfocusedBorderColor = Border,
                    focusedLabelColor = HospitalGreen,
                    unfocusedLabelColor = TextMuted,
                    cursorColor = HospitalGreen
                ),
                singleLine = true
            )

            // Location info
            currentLocation?.let { location ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            color = HospitalGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HospitalGreen)
                        )
                        Text(
                            text = "Location: ${location.latitude}, ${location.longitude}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HospitalGreen)
                }
            }

            // Error message
            errorMessage?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            color = PrimaryRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = error,
                        color = PrimaryRed,
                        fontSize = 12.sp
                    )
                }
            }

            // Hospital list
            if (!isLoading && errorMessage == null) {
                if (hospitals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hospitals found",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(hospitals) { hospital ->
                            HospitalListItem(
                                hospital = hospital,
                                onClick = { onHospitalSelected(hospital) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalListItem(
    hospital: Hospital,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = CardBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = hospital.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(hospital.distance / 1000).toInt()} km",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (hospital.distance < 1000) "< 1 km" else "${(hospital.distance / 1000).toInt()} km",
                    color = HospitalGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = hospital.duration.ifEmpty { "Calculating..." },
                    color = SecondaryAmber,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}