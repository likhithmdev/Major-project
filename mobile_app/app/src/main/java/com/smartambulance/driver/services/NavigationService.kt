package com.smartambulance.driver.services

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.smartambulance.driver.data.Hospital
import com.smartambulance.driver.data.HospitalRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NavigationService(private val context: Context) {
    
    private val httpClient = OkHttpClient()
    private val gson = Gson()
    
    companion object {
        private const val TAG = "NavigationService"
        private const val OSRM_API_URL = "https://router.project-osrm.org/route/v1/driving"
    }
    
    /**
     * Get directions using OSRM (Open Source Routing Machine)
     */
    suspend fun getDirections(
        origin: LatLng,
        destination: LatLng,
        considerTraffic: Boolean = true
    ): Result<JsonObject> = withContext(Dispatchers.IO) {
        try {
            val url = "$OSRM_API_URL/${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}?overview=full&geometries=geojson"
            
            val request = Request.Builder()
                .url(url)
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API request failed: ${response.code}"))
            }
            
            val responseBody = response.body?.string()
            if (responseBody.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Empty response"))
            }
            
            val json = gson.fromJson(responseBody, JsonObject::class.java)
            Result.success(json)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting directions", e)
            Result.failure(e)
        }
    }
    
    /**
     * Calculate ETA and distance for multiple destinations
     */
    suspend fun calculateDistances(
        origin: LatLng,
        destinations: List<LatLng>
    ): Result<Map<LatLng, Pair<String, Int>>> = withContext(Dispatchers.IO) {
        try {
            val results = mutableMapOf<LatLng, Pair<String, Int>>()
            
            for (destination in destinations) {
                getDirections(origin, destination)
                    .onSuccess { response ->
                        val route = response.getAsJsonArray("routes").get(0).asJsonObject
                        val duration = route.get("duration").asDouble
                        val distance = route.get("distance").asDouble
                        
                        results[destination] = Pair(
                            formatDuration(duration),
                            distance.toInt()
                        )
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Error calculating distance to destination", exception)
                        results[destination] = Pair("Unknown", 0)
                    }
            }
            
            Result.success(results)
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception in calculateDistances", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get route information for a hospital
     */
    suspend fun getHospitalRoute(
        origin: LatLng,
        hospital: Hospital
    ): Result<HospitalRoute> {
        return getDirections(origin, hospital.location)
            .map { response ->
                val route = response.getAsJsonArray("routes").get(0).asJsonObject
                val duration = route.get("duration").asDouble
                val distance = route.get("distance").asDouble
                
                HospitalRoute(
                    hospital = hospital,
                    distance = distance,
                    duration = formatDuration(duration),
                    durationSeconds = duration.toInt(),
                    trafficCondition = determineTrafficCondition(duration.toInt()),
                    alternativeRoutes = emptyList()
                )
            }
    }
    
    /**
     * Get routes for multiple hospitals
     */
    suspend fun getHospitalRoutes(
        origin: LatLng,
        hospitals: List<Hospital>
    ): Result<List<HospitalRoute>> = withContext(Dispatchers.IO) {
        try {
            val routes = mutableListOf<HospitalRoute>()
            
            for (hospital in hospitals) {
                getHospitalRoute(origin, hospital)
                    .onSuccess { route ->
                        routes.add(route)
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Error getting route for ${hospital.name}", exception)
                    }
            }
            
            Result.success(routes.sortedBy { it.durationSeconds })
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getHospitalRoutes", e)
            Result.failure(e)
        }
    }
    
    /**
     * Search address and convert to coordinates using Nominatim
     */
    suspend fun geocodeAddress(address: String): Result<LatLng> = 
        withContext(Dispatchers.IO) {
            try {
                val url = "https://nominatim.openstreetmap.org/search?format=json&q=${java.net.URLEncoder.encode(address, "UTF-8")}&limit=1"
                
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
                
                val jsonArray = gson.fromJson(responseBody, JsonArray::class.java)
                if (jsonArray.size() == 0) {
                    return@withContext Result.failure(Exception("No results found"))
                }
                
                val result = jsonArray.get(0).asJsonObject
                val lat = result.get("lat").asDouble
                val lon = result.get("lon").asDouble
                
                Result.success(LatLng(lat, lon))
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception in geocodeAddress", e)
                Result.failure(e)
            }
        }
    
    /**
     * Reverse geocode coordinates to address using Nominatim
     */
    suspend fun reverseGeocode(location: LatLng): Result<String> = 
        withContext(Dispatchers.IO) {
            try {
                val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=${location.latitude}&lon=${location.longitude}"
                
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
                
                val json = gson.fromJson(responseBody, JsonObject::class.java)
                val address = json.get("display_name").asString
                
                Result.success(address)
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception in reverseGeocode", e)
                Result.failure(e)
            }
        }
    
    /**
     * Determine traffic condition based on duration
     */
    private fun determineTrafficCondition(durationSeconds: Int): String {
        // This is a simplified logic - OSRM doesn't provide traffic data directly
        // In production, you'd integrate with real-time traffic services
        return when {
            durationSeconds < 300 -> "light"      // Less than 5 minutes
            durationSeconds < 600 -> "moderate"   // 5-10 minutes
            else -> "heavy"                        // More than 10 minutes
        }
    }
    
    /**
     * Format duration in seconds to human-readable string
     */
    private fun formatDuration(seconds: Double): String {
        val minutes = (seconds / 60).toInt()
        return when {
            minutes < 60 -> "${minutes} min"
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                "${hours}h ${remainingMinutes}min"
            }
        }
    }
}