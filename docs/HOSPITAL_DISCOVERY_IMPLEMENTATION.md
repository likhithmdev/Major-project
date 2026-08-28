# Hospital Discovery & Navigation Implementation Guide

## Priority Feature: Real-time Hospital Discovery with Google Maps API

This guide provides step-by-step implementation for adding real-time hospital discovery and advanced navigation to the Smart Ambulance app.

---

## 1. Google Maps Platform Setup

### 1.1 Get Google Maps API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable required APIs:
   - **Places API** (for hospital search)
   - **Maps SDK for Android** (for map display)
   - **Directions API** (for routing)
   - **Distance Matrix API** (for ETA calculation)
4. Create API key with appropriate restrictions
5. Enable billing (free tier available)

### 1.2 Configure Android Project
Update `mobile_app/app/build.gradle.kts`:

```kotlin
dependencies {
    // Existing dependencies...
    
    // Google Maps and Places
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-places:17.1.0")
    implementation("com.google.android.libraries.places:places:3.5.0")
    
    // Location services
    implementation("com.google.android.gms:play-services-location:21.3.0")
}
```

Update `mobile_app/app/src/main/AndroidManifest.xml`:

```xml
<manifest>
    <!-- Existing permissions -->
    
    <!-- Google Maps API key -->
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_GOOGLE_MAPS_API_KEY" />
</manifest>
```

---

## 2. Hospital Discovery Service

### 2.1 Create Hospital Data Models
Create `mobile_app/app/src/main/java/com/smartambulance/driver/data/Hospital.kt`:

```kotlin
data class Hospital(
    val placeId: String,
    val name: String,
    val address: String,
    val location: LatLng,
    val phone: String,
    val rating: Float,
    val distance: Double, // in meters
    val duration: String, // ETA
    val isOpen: Boolean,
    val types: List<String>,
    val emergencyServices: Boolean = false
)

data class HospitalDetails(
    val hospital: Hospital,
    val openingHours: List<OpeningHour>,
    val reviews: List<Review>,
    val photos: List<String>,
    val website: String,
    val emergencyPhoneNumber: String?,
    val emergencyRoomCapacity: Int?,
    val currentWaitTime: Int?
)

data class OpeningHour(
    val day: String,
    val hours: String
)

data class Review(
    val author: String,
    val rating: Float,
    val text: String,
    val time: String
)
```

### 2.2 Create Hospital Discovery Service
Create `mobile_app/app/src/main/java/com/smartambulance/driver/services/HospitalDiscoveryService.kt`:

```kotlin
package com.smartambulance.driver.services

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.smartambulance.driver.data.Hospital
import com.smartambulance.driver.data.HospitalDetails
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HospitalDiscoveryService(private val context: Context) {
    
    private val placesClient: PlacesClient = Places.createClient(context)
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    companion object {
        private const val HOSPITAL_SEARCH_RADIUS = 10000 // 10km in meters
        private val HOSPITAL_TYPES = listOf(
            PlaceTypes.HOSPITAL,
            PlaceTypes.DOCTOR,
            PlaceTypes.HEALTH
        )
    }
    
    /**
     * Find nearby hospitals based on current location
     */
    suspend fun findNearbyHospitals(location: LatLng): Result<List<Hospital>> = 
        suspendCancellableCoroutine { continuation ->
            val request = FindCurrentPlaceRequest.newInstance(
                listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, 
                      Place.Field.LAT_LNG, Place.Field.PHONE_NUMBER, Place.Field.RATING,
                      Place.Field.OPENING_HOURS, Place.Field.TYPES, Place.Field.BUSINESS_STATUS)
            )
            
            // Use Places Nearby Search (requires different approach)
            // This is a simplified version - you'll need to implement the actual Nearby Search
            val nearbySearchRequest = com.google.android.libraries.places.api.net.NearbySearchRequest.newBuilder()
                .setLocation(location)
                .setRadius(HOSPITAL_SEARCH_RADIUS)
                .setType(PlaceTypes.HOSPITAL)
                .build()
            
            placesClient.nearbySearch(nearbySearchRequest)
                .addOnSuccessListener { response ->
                    val hospitals = response.places.map { place ->
                        Hospital(
                            placeId = place.id ?: "",
                            name = place.name ?: "Unknown Hospital",
                            address = place.address ?: "",
                            location = place.latLng ?: location,
                            phone = place.phoneNumber ?: "",
                            rating = place.rating ?: 0f,
                            distance = calculateDistance(location, place.latLng ?: location),
                            duration = "", // Will be calculated with Distance Matrix API
                            isOpen = place.isOpen ?: true,
                            types = place.types ?: emptyList(),
                            emergencyServices = place.types?.contains(PlaceTypes.HOSPITAL) == true
                        )
                    }
                    continuation.resume(Result.success(hospitals))
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    
    /**
     * Search hospitals by query
     */
    suspend fun searchHospitals(query: String, location: LatLng): Result<List<Hospital>> =
        suspendCancellableCoroutine { continuation ->
            val request = FindPlaceRequest.newInstance(
                query,
                listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, 
                      Place.Field.LAT_LNG, Place.Field.PHONE_NUMBER, Place.Field.RATING,
                      Place.Field.OPENING_HOURS, Place.Field.TYPES)
            )
            
            placesClient.findPlace(request)
                .addOnSuccessListener { response ->
                    val hospitals = response.placeList.map { place ->
                        Hospital(
                            placeId = place.id ?: "",
                            name = place.name ?: "Unknown Hospital",
                            address = place.address ?: "",
                            location = place.latLng ?: location,
                            phone = place.phoneNumber ?: "",
                            rating = place.rating ?: 0f,
                            distance = calculateDistance(location, place.latLng ?: location),
                            duration = "",
                            isOpen = place.isOpen ?: true,
                            types = place.types ?: emptyList(),
                            emergencyServices = place.types?.contains(PlaceTypes.HOSPITAL) == true
                        )
                    }
                    continuation.resume(Result.success(hospitals))
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    
    /**
     * Get detailed information about a hospital
     */
    suspend fun getHospitalDetails(placeId: String): Result<HospitalDetails> =
        suspendCancellableCoroutine { continuation ->
            val placeFields = listOf(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.PHONE_NUMBER, Place.Field.RATING,
                Place.Field.OPENING_HOURS, Place.Field.WEBSITE_URI, Place.Field.REVIEWS,
                Place.Field.PHOTOS
            )
            
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)
            
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    val details = HospitalDetails(
                        hospital = Hospital(
                            placeId = place.id ?: "",
                            name = place.name ?: "Unknown Hospital",
                            address = place.address ?: "",
                            location = place.latLng ?: LatLng(0.0, 0.0),
                            phone = place.phoneNumber ?: "",
                            rating = place.rating ?: 0f,
                            distance = 0.0,
                            duration = "",
                            isOpen = place.isOpen ?: true,
                            types = place.types ?: emptyList(),
                            emergencyServices = place.types?.contains(PlaceTypes.HOSPITAL) == true
                        ),
                        openingHours = place.openingHours?.periods?.map { period ->
                            OpeningHour(
                                day = period.day?.name ?: "",
                                hours = "${period.open?.time} - ${period.close?.time}"
                            )
                        } ?: emptyList(),
                        reviews = place.reviews?.map { review ->
                            Review(
                                author = review.authorName ?: "",
                                rating = review.rating ?: 0f,
                                text = review.text ?: "",
                                time = review.relativeTimeDescription ?: ""
                            )
                        } ?: emptyList(),
                        photos = place.photoMetadatas?.map { it.toString() } ?: emptyList(),
                        website = place.websiteUri?.toString() ?: "",
                        emergencyPhoneNumber = place.phoneNumber,
                        emergencyRoomCapacity = null, // Would come from hospital API
                        currentWaitTime = null // Would come from hospital API
                    )
                    continuation.resume(Result.success(details))
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    
    /**
     * Calculate distance between two points (Haversine formula)
     */
    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val lat1 = from.latitude * Math.PI / 180
        val lat2 = to.latitude * Math.PI / 180
        val lon1 = from.longitude * Math.PI / 180
        val lon2 = to.longitude * Math.PI / 180
        
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return 6371000 * c // Earth's radius in meters
    }
    
    /**
     * Get current device location
     */
    suspend fun getCurrentLocation(): Result<LatLng> = 
        suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(Result.success(
                                LatLng(location.latitude, location.longitude)
                            ))
                        } else {
                            continuation.resumeWithException(Exception("Unable to get location"))
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
}
```

---

## 3. Advanced Navigation Service

### 3.1 Create Navigation Service
Create `mobile_app/app/src/main/java/com/smartambulance/driver/services/NavigationService.kt`:

```kotlin
package com.smartambulance.driver.services

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NavigationService(private val context: Context) {
    
    private val geoApiContext: GeoApiContext = GeoApiContext.Builder()
        .apiKey("YOUR_GOOGLE_MAPS_API_KEY")
        .build()
    
    /**
     * Get directions with traffic consideration
     */
    suspend fun getDirections(
        origin: LatLng,
        destination: LatLng,
        considerTraffic: Boolean = true
    ): Result<DirectionsResult> = suspendCancellableCoroutine { continuation ->
        
        try {
            val request = DirectionsApi.newRequest(geoApiContext)
                .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.DRIVING)
                .departureTime(System.currentTimeMillis())
            
            if (considerTraffic) {
                request.departureTime(System.currentTimeMillis())
            }
            
            val result = request.await()
            continuation.resume(Result.success(result))
            
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * Get multiple route options
     */
    suspend fun getRouteAlternatives(
        origin: LatLng,
        destination: LatLng
    ): Result<List<DirectionsResult>> = suspendCancellableCoroutine { continuation ->
        
        try {
            // This would require implementing multiple route requests
            // For now, returning single route
            val request = DirectionsApi.newRequest(geoApiContext)
                .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.DRIVING)
                .alternatives(true)
            
            val result = request.await()
            val routes = mutableListOf<DirectionsResult>()
            
            // Main route
            routes.add(result)
            
            // You would need to implement alternative route logic here
            // Google Maps API supports alternatives parameter
            
            continuation.resume(Result.success(routes))
            
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * Calculate ETA and distance for multiple destinations
     */
    suspend fun calculateDistances(
        origin: LatLng,
        destinations: List<LatLng>
    ): Result<Map<LatLng, Pair<String, Int>>> = suspendCancellableCoroutine { continuation ->
        
        try {
            val origins = listOf(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
            val destPoints = destinations.map { 
                com.google.maps.model.LatLng(it.latitude, it.longitude) 
            }
            
            val distanceMatrix = com.google.maps.DistanceMatrixApi.newRequest(geoApiContext)
                .origins(*origins.toTypedArray())
                .destinations(*destPoints.toTypedArray())
                .mode(TravelMode.DRIVING)
                .await()
            
            val results = mutableMapOf<LatLng, Pair<String, Int>>()
            
            destinations.forEachIndexed { index, latLng ->
                val element = distanceMatrix.rows[0].elements[index]
                val duration = element.duration?.humanReadable ?: "Unknown"
                val distance = element.distance?.inMeters ?: 0
                results[latLng] = Pair(duration, distance)
            }
            
            continuation.resume(Result.success(results))
            
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}
```

---

## 4. Enhanced UI Components

### 4.1 Create Hospital Search Screen
Create `mobile_app/app/src/main/java/com/smartambulance/driver/ui/screens/HospitalSearchScreen.kt`:

```kotlin
package com.smartambulance.driver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.smartambulance.driver.data.Hospital
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
    
    // Get current location on load
    LaunchedEffect(Unit) {
        isLoading = true
        hospitalDiscoveryService.getCurrentLocation()
            .onSuccess { location ->
                currentLocation = location
                hospitalDiscoveryService.findNearbyHospitals(location)
                    .onSuccess { nearbyHospitals ->
                        hospitals = nearbyHospitals.sortedBy { it.distance }
                    }
            }
        isLoading = false
    }
    
    // Search functionality
    LaunchedEffect(searchQuery, currentLocation) {
        if (searchQuery.length >= 2 && currentLocation != null) {
            hospitalDiscoveryService.searchHospitals(searchQuery, currentLocation)
                .onSuccess { searchResults ->
                    hospitals = searchResults
                }
        } else if (searchQuery.isEmpty && currentLocation != null) {
            hospitalDiscoveryService.findNearbyHospitals(currentLocation)
                .onSuccess { nearbyHospitals ->
                    hospitals = nearbyHospitals.sortedBy { it.distance }
                }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Hospital") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                placeholder = { Text("Search hospitals...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Hospital list
            if (hospitals.isNotEmpty()) {
                Text(
                    text = "${hospitals.size} hospitals found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(hospitals) { hospital ->
                        HospitalCard(
                            hospital = hospital,
                            onClick = { onHospitalSelected(hospital) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else if (!isLoading) {
                Text(
                    text = "No hospitals found. Try a different search.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HospitalCard(
    hospital: Hospital,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = hospital.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (hospital.emergencyServices) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "24/7",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = hospital.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Distance",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(hospital.distance / 1000).toString()} km",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "ETA",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = hospital.duration.ifEmpty { "Calculating..." },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
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
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            if (!hospital.isOpen) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Currently closed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
```

---

## 5. Integration with MainActivity

### 5.1 Update MainActivity
Update `mobile_app/app/src/main/java/com/smartambulance/driver/MainActivity.kt`:

```kotlin
// Add these imports
import com.smartambulance.driver.services.HospitalDiscoveryService
import com.smartambulance.driver.services.NavigationService
import com.smartambulance.driver.ui.screens.HospitalSearchScreen

class MainActivity : ComponentActivity() {
    // Add new services
    private lateinit var hospitalDiscoveryService: HospitalDiscoveryService
    private lateinit var navigationService: NavigationService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize new services
        hospitalDiscoveryService = HospitalDiscoveryService(this)
        navigationService = NavigationService(this)
        
        // ... existing code ...
        
        setContent {
            SmartAmbulanceTheme {
                // Update DriverScreen to use new hospital discovery
                when (current?.role) {
                    "ambulance_driver" -> DriverScreen(
                        // ... existing parameters ...
                        onSearchHospital = { showHospitalSearch() }
                    )
                    // ... other screens ...
                }
            }
        }
    }
    
    private fun showHospitalSearch() {
        // This would show the hospital search screen
        // Implementation depends on your navigation approach
    }
}
```

---

## 6. Testing Strategy

### 6.1 Unit Tests
```kotlin
class HospitalDiscoveryServiceTest {
    @Test
    fun testDistanceCalculation() {
        val service = HospitalDiscoveryService(context)
        val distance = service.calculateDistance(
            LatLng(12.9716, 77.5946),
            LatLng(12.9750, 77.5946)
        )
        assertTrue(distance > 0)
    }
    
    @Test
    fun testHospitalSearch() = runBlocking {
        val service = HospitalDiscoveryService(context)
        val location = LatLng(12.9716, 77.5946)
        val result = service.findNearbyHospitals(location)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isNotEmpty() == true)
    }
}
```

### 6.2 Integration Tests
- Test with real Google Maps API key
- Test in different locations
- Test with various search queries
- Test edge cases (no hospitals found, network errors)

---

## 7. Deployment Checklist

### Pre-deployment
- [ ] Google Maps API key properly configured
- [ ] API usage limits and billing set up
- [ ] Error handling for API failures
- [ ] Fallback for offline mode
- [ ] Privacy policy for location data
- [ ] Terms of service compliance

### Testing
- [ ] Test in multiple geographic regions
- [ ] Test with poor network conditions
- [ ] Test with various device types
- [ ] Performance testing
- [ ] Security testing

### Documentation
- [ ] API key management documentation
- [ ] User guide for hospital search
- [ ] Troubleshooting guide
- [ ] Privacy policy update

---

## 8. Cost Optimization

### Free Tier Usage
- Google Maps offers $200 free credit monthly
- Monitor usage to stay within free tier
- Implement caching to reduce API calls
- Use debouncing for search queries

### Optimization Strategies
- Cache hospital results for 5-10 minutes
- Batch distance calculations
- Implement intelligent refresh strategies
- Use offline maps when possible

This implementation guide provides a complete roadmap for adding real-time hospital discovery and advanced navigation to your Smart Ambulance app, addressing your primary production enhancement priority.