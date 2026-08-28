package com.smartambulance.driver.data

import com.google.android.gms.maps.model.LatLng

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
    val currentWaitTime: Int?,
    val specialties: List<String> = emptyList(),
    val hasEmergencyRoom: Boolean = false,
    val hasTraumaCenter: Boolean = false,
    val hasICU: Boolean = false
)

data class OpeningHour(
    val day: String,
    val hours: String,
    val isOpen: Boolean = true
)

data class Review(
    val author: String,
    val rating: Float,
    val text: String,
    val time: String
)

data class HospitalSearchResult(
    val hospitals: List<Hospital>,
    val searchLocation: LatLng,
    val searchRadius: Int = 10000, // 10km default
    val timestamp: Long = System.currentTimeMillis()
)

data class HospitalFilter(
    val emergencyOnly: Boolean = false,
    val maxDistance: Int = 10000, // meters
    val minRating: Float = 0f,
    val isOpenNow: Boolean = false,
    val specialties: List<String> = emptyList()
)

data class HospitalRoute(
    val hospital: Hospital,
    val distance: Double, // meters
    val duration: String, // human-readable duration
    val durationSeconds: Int, // for sorting
    val trafficCondition: String, // "light", "moderate", "heavy"
    val alternativeRoutes: List<AlternativeRoute> = emptyList()
)

data class AlternativeRoute(
    val distance: Double,
    val duration: String,
    val durationSeconds: Int,
    val summary: String
)

data class HospitalFavorite(
    val hospitalId: String,
    val hospitalName: String,
    val address: String,
    val addedAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val useCount: Int = 0
)

// Helper function to get Firebase-compatible ID from Hospital
fun Hospital.toFirebaseId(): String = placeId