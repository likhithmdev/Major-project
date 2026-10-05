package com.smartambulance.driver.services

import android.content.Context
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.smartambulance.driver.data.Hospital
import com.smartambulance.driver.data.HospitalDetails
import com.smartambulance.driver.data.HospitalFilter
import com.smartambulance.driver.data.HospitalSearchResult
import com.smartambulance.driver.data.OpeningHour
import com.smartambulance.driver.data.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class HospitalDiscoveryService(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    private val httpClient = OkHttpClient()
    private val gson = Gson()
    private val database = FirebaseDatabase.getInstance().reference
    
    companion object {
        private const val TAG = "HospitalDiscovery"
        private const val HOSPITAL_SEARCH_RADIUS = 10000 // 10km in meters
        private const val EARTH_RADIUS_METERS = 6371000.0
        private const val OVERPASS_API_URL = "https://overpass-api.de/api/interpreter"
        private const val NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search"
    }
    
    /**
     * Find nearby hospitals using OpenStreetMap Overpass API
     */
    suspend fun findNearbyHospitals(location: LatLng): Result<List<Hospital>> =
        withContext(Dispatchers.IO) {
            try {
                // Build Overpass query to find hospitals within radius
                val bbox = buildBoundingBox(location, HOSPITAL_SEARCH_RADIUS.toDouble())
                val query = buildOverpassQuery(bbox)

                // Use GET request with proper headers to avoid 406 error
                val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                val url = "$OVERPASS_API_URL?data=$encodedQuery"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "SmartAmbulance/1.0")
                    .build()

                val response = httpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e(TAG, "API request failed with code: ${response.code}")
                    return@withContext Result.failure(Exception("API request failed: ${response.code}"))
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("Empty response"))
                }

                val osmHospitals = parseOverpassResponse(responseBody, location)
                
                // Also get Firebase hospitals and merge
                val firebaseHospitals = getFirebaseHospitals(location).getOrNull() ?: emptyList()
                
                // Merge and deduplicate
                val allHospitals = (osmHospitals + firebaseHospitals)
                    .distinctBy { it.name }
                    .sortedBy { it.distance }
                
                Result.success(allHospitals)

            } catch (e: Exception) {
                Log.e(TAG, "Error finding nearby hospitals", e)
                Result.failure(e)
            }
        }
    
    /**
     * Get hospitals from Firebase database
     */
    suspend fun getFirebaseHospitals(location: LatLng): Result<List<Hospital>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = database.child("hospitals").get().await()
                val hospitals = mutableListOf<Hospital>()
                
                snapshot.children.forEach { hospitalSnapshot ->
                    val hospitalMap = hospitalSnapshot.value as? Map<*, *>
                    if (hospitalMap != null) {
                        val lat = (hospitalMap["latitude"] as? Double) ?: 0.0
                        val lon = (hospitalMap["longitude"] as? Double) ?: 0.0
                        
                        if (lat != 0.0 && lon != 0.0) {
                            val hospitalLocation = LatLng(lat, lon)
                            val distance = calculateDistance(location, hospitalLocation)
                            
                            // Only include if within search radius
                            if (distance <= HOSPITAL_SEARCH_RADIUS) {
                                hospitals.add(
                                    Hospital(
                                        placeId = hospitalMap["hospitalId"] as? String ?: "",
                                        name = hospitalMap["name"] as? String ?: "Unknown Hospital",
                                        address = hospitalMap["address"] as? String ?: "",
                                        location = hospitalLocation,
                                        phone = hospitalMap["contact"] as? String ?: "",
                                        rating = 0f,
                                        distance = distance,
                                        duration = "",
                                        isOpen = true,
                                        types = listOf("hospital"),
                                        emergencyServices = hospitalMap["emergencyAvailable"] as? Boolean ?: true
                                    )
                                )
                            }
                        }
                    }
                }
                
                Result.success(hospitals.sortedBy { it.distance })
            } catch (e: Exception) {
                Log.e(TAG, "Error getting Firebase hospitals", e)
                Result.failure(e)
            }
        }
    
    /**
     * Search hospitals by name using Nominatim API
     */
    suspend fun searchHospitals(query: String, location: LatLng): Result<List<Hospital>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$NOMINATIM_API_URL?format=json&q=${java.net.URLEncoder.encode("hospital $query", "UTF-8")}&limit=20"
                
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "SmartAmbulance/1.0")
                    .build()
                
                val response = httpClient.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("API request failed: ${response.code}"))
                }
                
                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("Empty response"))
                }
                
                val hospitals = parseNominatimResponse(responseBody, location)
                Result.success(hospitals)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error searching hospitals", e)
                Result.failure(e)
            }
        }
    
    /**
     * Get detailed information about a hospital
     */
    suspend fun getHospitalDetails(placeId: String): Result<HospitalDetails> =
        withContext(Dispatchers.IO) {
            try {
                // For OSM, we can use the placeId to get more details
                // This is a simplified version - in production you'd fetch more details
                val url = "https://nominatim.openstreetmap.org/details?format=json&osmid=$placeId&osmtype=W"
                
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "SmartAmbulance/1.0")
                    .build()
                
                val response = httpClient.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("API request failed: ${response.code}"))
                }
                
                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("Empty response"))
                }
                
                val details = parseHospitalDetails(responseBody)
                Result.success(details)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting hospital details", e)
                Result.failure(e)
            }
        }
    
    /**
     * Filter hospitals based on criteria
     */
    fun filterHospitals(hospitals: List<Hospital>, filter: HospitalFilter): List<Hospital> {
        return hospitals.filter { hospital ->
            // Filter by emergency services
            if (filter.emergencyOnly && !hospital.emergencyServices) {
                return@filter false
            }
            
            // Filter by distance
            if (hospital.distance > filter.maxDistance) {
                return@filter false
            }
            
            // Filter by rating
            if (hospital.rating < filter.minRating) {
                return@filter false
            }
            
            // Filter by open status
            if (filter.isOpenNow && !hospital.isOpen) {
                return@filter false
            }
            
            true
        }.sortedBy { it.distance }
    }
    
    /**
     * Calculate distance between two points (Haversine formula)
     */
    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val lat1 = from.latitude * PI / 180
        val lat2 = to.latitude * PI / 180
        val lon1 = from.longitude * PI / 180
        val lon2 = to.longitude * PI / 180
        
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return EARTH_RADIUS_METERS * c
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
                        Log.e(TAG, "Error getting current location", exception)
                        continuation.resumeWithException(exception)
                    }
                    
            } catch (e: Exception) {
                Log.e(TAG, "Exception in getCurrentLocation", e)
                continuation.resumeWithException(e)
            }
        }
    
    /**
     * Search with location auto-detection
     */
    suspend fun searchNearbyWithCurrentLocation(): Result<HospitalSearchResult> {
        return getCurrentLocation()
            .map { location ->
                val hospitals = findNearbyHospitals(location).getOrNull() ?: emptyList()
                HospitalSearchResult(
                    hospitals = hospitals,
                    searchLocation = location,
                    searchRadius = HOSPITAL_SEARCH_RADIUS
                )
            }
    }
    
    /**
     * Get hospitals within a specific radius
     */
    suspend fun getHospitalsWithinRadius(
        location: LatLng,
        radiusMeters: Int
    ): Result<List<Hospital>> {
        return findNearbyHospitals(location).map { hospitals ->
            hospitals.filter { it.distance <= radiusMeters }
        }
    }
    
    /**
     * Sort hospitals by multiple criteria
     */
    fun sortHospitals(
        hospitals: List<Hospital>,
        sortBy: SortCriteria = SortCriteria.DISTANCE
    ): List<Hospital> {
        return when (sortBy) {
            SortCriteria.DISTANCE -> hospitals.sortedBy { it.distance }
            SortCriteria.RATING -> hospitals.sortedByDescending { it.rating }
            SortCriteria.NAME -> hospitals.sortedBy { it.name }
            SortCriteria.EMERGENCY_FIRST -> hospitals.sortedWith(
                compareByDescending<Hospital> { it.emergencyServices }
                    .thenBy { it.distance }
            )
        }
    }
    
    // Helper methods for OSM API
    
    private fun buildBoundingBox(location: LatLng, radiusMeters: Double): String {
        val latDelta = (radiusMeters / EARTH_RADIUS_METERS) * (180 / PI)
        val lonDelta = (radiusMeters / EARTH_RADIUS_METERS) * (180 / PI) / kotlin.math.cos(location.latitude * PI / 180)
        
        val south = location.latitude - latDelta
        val north = location.latitude + latDelta
        val west = location.longitude - lonDelta
        val east = location.longitude + lonDelta
        
        return "$south,$west,$north,$east"
    }
    
    private fun buildOverpassQuery(bbox: String): String {
        return """
            [out:json][timeout:25];
            (
              node["amenity"="hospital"]($bbox);
              way["amenity"="hospital"]($bbox);
              relation["amenity"="hospital"]($bbox);
            );
            out center;
        """.trimIndent()
    }
    
    private fun parseOverpassResponse(responseBody: String, currentLocation: LatLng): List<Hospital> {
        val json = gson.fromJson(responseBody, JsonObject::class.java)
        val elements = json.getAsJsonArray("elements")
        
        return elements.mapNotNull { element ->
            val elementObj = element.asJsonObject
            val tags = elementObj.getAsJsonObject("tags")
            
            val name = tags.get("name")?.asString ?: "Unknown Hospital"
            val lat = elementObj.get("lat")?.asDouble ?: elementObj.getAsJsonObject("center")?.get("lat")?.asDouble
            val lon = elementObj.get("lon")?.asDouble ?: elementObj.getAsJsonObject("center")?.get("lon")?.asDouble
            
            if (lat != null && lon != null) {
                val location = LatLng(lat, lon)
                val phone = tags.get("phone")?.asString ?: tags.get("contact:phone")?.asString ?: ""
                val emergency = tags.get("emergency")?.asString == "yes"
                
                Hospital(
                    placeId = elementObj.get("id").asString,
                    name = name,
                    address = tags.get("addr:street")?.asString ?: tags.get("addr:full")?.asString ?: "",
                    location = location,
                    phone = phone,
                    rating = 0f, // OSM doesn't provide ratings
                    distance = calculateDistance(currentLocation, location),
                    duration = "", // Would need routing API
                    isOpen = true, // OSM doesn't provide hours in basic query
                    types = listOf("hospital"),
                    emergencyServices = emergency
                )
            } else {
                null
            }
        }.sortedBy { it.distance }
    }
    
    private fun parseNominatimResponse(responseBody: String, currentLocation: LatLng): List<Hospital> {
        val jsonArray = gson.fromJson(responseBody, JsonArray::class.java)
        
        return jsonArray.mapNotNull { element ->
            val obj = element.asJsonObject
            val lat = obj.get("lat")?.asDouble
            val lon = obj.get("lon")?.asDouble
            
            if (lat != null && lon != null) {
                val location = LatLng(lat, lon)
                Hospital(
                    placeId = obj.get("osm_id")?.asString ?: "",
                    name = obj.get("display_name")?.asString ?: "Unknown Hospital",
                    address = obj.get("display_name")?.asString ?: "",
                    location = location,
                    phone = "",
                    rating = 0f,
                    distance = calculateDistance(currentLocation, location),
                    duration = "",
                    isOpen = true,
                    types = listOf("hospital"),
                    emergencyServices = true
                )
            } else {
                null
            }
        }.sortedBy { it.distance }
    }
    
    private fun parseHospitalDetails(responseBody: String): HospitalDetails {
        val json = gson.fromJson(responseBody, JsonObject::class.java)
        val tags = json.getAsJsonObject("tags")
        
        val location = LatLng(
            json.get("lat").asDouble,
            json.get("lon").asDouble
        )
        
        return HospitalDetails(
            hospital = Hospital(
                placeId = json.get("osm_id").asString,
                name = tags.get("name")?.asString ?: "Unknown Hospital",
                address = tags.get("addr:street")?.asString ?: "",
                location = location,
                phone = tags.get("phone")?.asString ?: "",
                rating = 0f,
                distance = 0.0,
                duration = "",
                isOpen = true,
                types = listOf("hospital"),
                emergencyServices = tags.get("emergency")?.asString == "yes"
            ),
            openingHours = emptyList(),
            reviews = emptyList(),
            photos = emptyList(),
            website = tags.get("website")?.asString ?: "",
            emergencyPhoneNumber = tags.get("emergency")?.asString,
            emergencyRoomCapacity = null,
            currentWaitTime = null,
            specialties = emptyList(),
            hasEmergencyRoom = tags.get("emergency")?.asString == "yes",
            hasTraumaCenter = false,
            hasICU = false
        )
    }
    
    enum class SortCriteria {
        DISTANCE, RATING, NAME, EMERGENCY_FIRST
    }
}