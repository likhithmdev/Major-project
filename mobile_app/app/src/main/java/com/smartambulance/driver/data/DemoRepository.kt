package com.smartambulance.driver.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

data class AppUser(
    val userId: String,
    val name: String,
    val pin: String,
    val role: String,
    val ambulanceId: String? = null,
    val assignedJunctionId: String? = null,
    val hospitalId: String? = null
)

data class HospitalOption(
    val id: String,
    val name: String,
    val distance: String,
    val eta: String,
    val beds: Int
)

data class AdminSummary(
    val users: List<String>,
    val ambulances: List<String>,
    val hospitals: List<String>,
    val rfidTags: List<String>,
    val junctions: List<String>
)

class DemoRepository {
    private val database = FirebaseDatabase.getInstance().reference
    private val tripId = "TRIP001"

    val demoUsers = listOf(
        AppUser("driver_001", "Driver One", "1111", "ambulance_driver", ambulanceId = "AMB001"),
        AppUser("police_001", "Traffic Police", "2222", "police", assignedJunctionId = "JNC001"),
        AppUser("hospital_001", "City Care Desk", "3333", "hospital", hospitalId = "HOSP001"),
        AppUser("admin_001", "System Admin", "0000", "admin")
    )

    val hospitals = listOf(
        HospitalOption("HOSP001", "City Care Hospital", "2.4 km", "6 min", 8),
        HospitalOption("HOSP002", "Metro Emergency Center", "3.1 km", "9 min", 3),
        HospitalOption("HOSP003", "St. Mark Trauma Unit", "4.6 km", "12 min", 11)
    )

    val bangaloreHospitals = listOf(
        mapOf(
            "hospitalId" to "HOSP004",
            "name" to "Sathya Sai Orthopaedic & Multispeciality Hospital",
            "address" to "Old Madras Rd, next to RTO, Banashankari Layout, Bhattarahalli, Bangalore - 560049",
            "contact" to "+91 7022649111 / 080-88930745",
            "pincode" to "560049",
            "bedsAvailable" to 50,
            "emergencyAvailable" to true,
            "latitude" to 13.0285,
            "longitude" to 77.6585
        ),
        mapOf(
            "hospitalId" to "HOSP005",
            "name" to "Ovum Woman & Child Speciality Hospital",
            "address" to "2nd Floor, Venkatadri Complex, 29p/32, Old Madras Rd, Bhattarahalli Gate, Bangalore - 560049",
            "contact" to "080-45309999",
            "pincode" to "560049",
            "bedsAvailable" to 30,
            "emergencyAvailable" to true,
            "latitude" to 13.0290,
            "longitude" to 77.6590
        ),
        mapOf(
            "hospitalId" to "HOSP006",
            "name" to "Hope Hospital",
            "address" to "Sy No. 32/2, Site No. 1–4, Old Madras Rd, Sannatammanahalli, Bhattarahalli, Bangalore - 560049",
            "contact" to "+91 7483257544",
            "pincode" to "560049",
            "bedsAvailable" to 25,
            "emergencyAvailable" to true,
            "latitude" to 13.0295,
            "longitude" to 77.6595
        ),
        mapOf(
            "hospitalId" to "HOSP007",
            "name" to "Geetha Hospital",
            "address" to "Old Madras Rd, Muneshwara Layout, Virgonagar, Avalahalli, Bangalore - 560049",
            "contact" to "+91 6364300440 / +91 9108474852 / 080-29899440",
            "pincode" to "560049",
            "bedsAvailable" to 20,
            "emergencyAvailable" to true,
            "latitude" to 13.0300,
            "longitude" to 77.6600
        ),
        mapOf(
            "hospitalId" to "HOSP008",
            "name" to "VR Revankar Hospital & Cardiac Care Center",
            "address" to "Near Ravindu Toyota, Old Madras Road, Virgonagar, Bangalore - 560049",
            "contact" to "+91 8123189099 / +91 8123819099",
            "pincode" to "560049",
            "bedsAvailable" to 40,
            "emergencyAvailable" to true,
            "latitude" to 13.0305,
            "longitude" to 77.6605
        ),
        mapOf(
            "hospitalId" to "HOSP009",
            "name" to "Currex Hospital",
            "address" to "No. 55 & 56, Seegehalli Main Rd, Bhoo Samartha Layout, Seegehalli, Bangalore - 560049",
            "contact" to "+91 8296512929 / 080-29904293",
            "pincode" to "560049",
            "bedsAvailable" to 15,
            "emergencyAvailable" to true,
            "latitude" to 13.0310,
            "longitude" to 77.6610
        ),
        mapOf(
            "hospitalId" to "HOSP010",
            "name" to "East Point Super-Specialty Hospital",
            "address" to "East Point Campus, Virgo Nagar Post, Jnana Prabha, Avalahalli, Bangalore - 560049",
            "contact" to "+91 7866911911",
            "pincode" to "560049",
            "bedsAvailable" to 100,
            "emergencyAvailable" to true,
            "latitude" to 13.0315,
            "longitude" to 77.6615
        ),
        mapOf(
            "hospitalId" to "HOSP011",
            "name" to "S.N. Hospital",
            "address" to "28/1, Old Madras Rd, Muneshwara Layout, Virgonagar, Bangalore - 560049",
            "contact" to "Visit OPD directly",
            "pincode" to "560049",
            "bedsAvailable" to 18,
            "emergencyAvailable" to true,
            "latitude" to 13.0320,
            "longitude" to 77.6620
        ),
        mapOf(
            "hospitalId" to "HOSP012",
            "name" to "Primary Health Center (PHC) Seegehalli",
            "address" to "Seegehalli Main Road, Krishnarajapura, Bangalore - 560049",
            "contact" to "Govt facility - Visit OPD directly",
            "pincode" to "560049",
            "bedsAvailable" to 10,
            "emergencyAvailable" to true,
            "latitude" to 13.0325,
            "longitude" to 77.6625
        ),
        mapOf(
            "hospitalId" to "HOSP013",
            "name" to "Shanthinilaya Hospital",
            "address" to "Thambuchetty Palya Main Road, Battarahalli, Bangalore - 560049",
            "contact" to "Visit clinic directly",
            "pincode" to "560049",
            "bedsAvailable" to 12,
            "emergencyAvailable" to true,
            "latitude" to 13.0330,
            "longitude" to 77.6630
        ),
        mapOf(
            "hospitalId" to "HOSP014",
            "name" to "Goodlife Hospital",
            "address" to "Kurudusonnenahalli Main Road, Seegehalli-Avalahalli area, Bangalore - 560049",
            "contact" to "Not publicly listed",
            "pincode" to "560049",
            "bedsAvailable" to 8,
            "emergencyAvailable" to true,
            "latitude" to 13.0335,
            "longitude" to 77.6635
        ),
        mapOf(
            "hospitalId" to "HOSP015",
            "name" to "Elan Poly Clinic & Hospital",
            "address" to "Seegehalli Main Road, Krishnarajapuram, Bangalore - 560049",
            "contact" to "Not publicly listed",
            "pincode" to "560049",
            "bedsAvailable" to 10,
            "emergencyAvailable" to true,
            "latitude" to 13.0340,
            "longitude" to 77.6640
        ),
        mapOf(
            "hospitalId" to "HOSP016",
            "name" to "Altor Hospitals",
            "address" to "Service Road, Near TC Palya Signal, Cambridge College Rd, KR Puram, Bangalore - 560036",
            "contact" to "080-68249699 / +91 8884481081",
            "pincode" to "560036",
            "bedsAvailable" to 35,
            "emergencyAvailable" to true,
            "latitude" to 13.0120,
            "longitude" to 77.6800
        ),
        mapOf(
            "hospitalId" to "HOSP017",
            "name" to "Aster Whitefield Hospital",
            "address" to "Plot No. 3 & 4, Main Road, Sadaramangala Industrial Area, Whitefield, Bangalore - 560066",
            "contact" to "080-45104510",
            "pincode" to "560066",
            "bedsAvailable" to 150,
            "emergencyAvailable" to true,
            "latitude" to 12.9690,
            "longitude" to 77.7510
        ),
        mapOf(
            "hospitalId" to "HOSP018",
            "name" to "Manipal Hospital Whitefield",
            "address" to "ITPL Main Rd, KIADB Export Promotion Industrial Area, Whitefield, Bangalore - 560066",
            "contact" to "080-40474047 / 1800-102-5555",
            "pincode" to "560066",
            "bedsAvailable" to 200,
            "emergencyAvailable" to true,
            "latitude" to 12.9700,
            "longitude" to 77.7520
        ),
        mapOf(
            "hospitalId" to "HOSP019",
            "name" to "Surgecare Super Speciality Hospital",
            "address" to "Site No 1&2, 5th Cross, Ramamurthi Nagar Main Rd, Dayananda Layout, Bangalore - 560016",
            "contact" to "+91 9606026006 / +91 9606036006",
            "pincode" to "560016",
            "bedsAvailable" to 45,
            "emergencyAvailable" to true,
            "latitude" to 13.0160,
            "longitude" to 77.6500
        ),
        mapOf(
            "hospitalId" to "HOSP020",
            "name" to "Koshys Multispeciality Hospital",
            "address" to "127, 7th Main Rd, Raghavendra Nagar, T.C. Palya Road Extension, Ramamurthy Nagar, Bangalore - 560016",
            "contact" to "080-46634663 / +91 7618737390",
            "pincode" to "560016",
            "bedsAvailable" to 60,
            "emergencyAvailable" to true,
            "latitude" to 13.0170,
            "longitude" to 77.6510
        ),
        mapOf(
            "hospitalId" to "HOSP021",
            "name" to "Sunrise Hospital",
            "address" to "9, Jayanti Nagar Main Rd, near Raghavendra Circle, Ramamurthy Nagar, Bangalore - 560016",
            "contact" to "+91 9035028441",
            "pincode" to "560016",
            "bedsAvailable" to 25,
            "emergencyAvailable" to true,
            "latitude" to 13.0180,
            "longitude" to 77.6520
        ),
        mapOf(
            "hospitalId" to "HOSP022",
            "name" to "Sri Lakshmi Super Speciality Hospital",
            "address" to "3rd Main Road, Old Extension, Krishnarajapuram, Bangalore - 560036",
            "contact" to "080-25618111 / 080-25613333",
            "pincode" to "560036",
            "bedsAvailable" to 40,
            "emergencyAvailable" to true,
            "latitude" to 13.0130,
            "longitude" to 77.6810
        ),
        mapOf(
            "hospitalId" to "HOSP023",
            "name" to "Deepa Hospital",
            "address" to "Devasandra Main Road, near KR Puram, Bangalore - 560036",
            "contact" to "080-25615550",
            "pincode" to "560036",
            "bedsAvailable" to 20,
            "emergencyAvailable" to true,
            "latitude" to 13.0140,
            "longitude" to 77.6820
        )
    )

    fun seedBangaloreHospitals(onResult: ((Boolean, String) -> Unit)? = null) {
        // Add hospitals one by one to avoid type mismatch
        var addedCount = 0
        val totalHospitals = bangaloreHospitals.size

        bangaloreHospitals.forEach { hospital ->
            val hospitalId = hospital["hospitalId"] as String
            database.child(FirebasePaths.HOSPITALS).child(hospitalId).setValue(hospital)
                .addOnSuccessListener {
                    addedCount++
                    if (addedCount == totalHospitals) {
                        onResult?.invoke(true, "Added $totalHospitals Bangalore hospitals successfully")
                    }
                }
                .addOnFailureListener { e ->
                    onResult?.invoke(false, "Failed to add hospital $hospitalId: ${e.message}")
                }
        }
    }

    fun seedDemoData(onResult: ((Boolean, String) -> Unit)? = null) {
        val seedTasks = listOf(
            database.child(FirebasePaths.USERS).updateChildren(
            demoUsers.associate { user ->
                user.userId to mapOf(
                    "userId" to user.userId,
                    "name" to user.name,
                    "pin" to user.pin,
                    "role" to user.role,
                    "ambulanceId" to user.ambulanceId,
                    "assignedJunctionId" to user.assignedJunctionId,
                    "hospitalId" to user.hospitalId,
                    "active" to true
                )
            }
        ),

        database.child(FirebasePaths.HOSPITALS).updateChildren(
            hospitals.associate { hospital ->
                hospital.id to mapOf(
                    "hospitalId" to hospital.id,
                    "name" to hospital.name,
                    "distance" to hospital.distance,
                    "eta" to hospital.eta,
                    "bedsAvailable" to hospital.beds,
                    "emergencyAvailable" to true
                )
            }
        ),

        database.child(FirebasePaths.DRIVERS).child("DRV001").updateChildren(
            mapOf(
                "driverId" to "DRV001",
                "userId" to "driver_001",
                "name" to "Driver One",
                "phone" to "9999999999",
                "assignedAmbulanceId" to "AMB001",
                "active" to true
            )
        ),

        database.child(FirebasePaths.AMBULANCES).child("AMB001").updateChildren(
            mapOf(
                "ambulanceId" to "AMB001",
                "rfidTagId" to "RFID_TAG_001",
                "loraNodeId" to "LORA_AMB001",
                "driverId" to "driver_001",
                "status" to "available",
                "emergencyActive" to false,
                "lastLoRaTelemetry" to mapOf(
                    "junctionId" to "JNC001",
                    "gpsFix" to true,
                    "rssi" to -72,
                    "distanceMeters" to 500,
                    "approaching" to false,
                    "preemptionEligible" to false,
                    "updatedAt" to ServerValue.TIMESTAMP
                ),
                "updatedAt" to ServerValue.TIMESTAMP
            )
        ),

        database.child(FirebasePaths.RFID_TAGS).child("RFID_TAG_001").updateChildren(
            mapOf(
                "rfidTagId" to "RFID_TAG_001",
                "ambulanceId" to "AMB001",
                "authorized" to true,
                "active" to true,
                "updatedAt" to ServerValue.TIMESTAMP
            )
        ),

        database.child(FirebasePaths.JUNCTIONS).child("JNC001").updateChildren(
            mapOf(
                "junctionId" to "JNC001",
                "name" to "Main Road Junction",
                "activeLane" to "northbound",
                "signalState" to "normal",
                "preemptionMode" to "none",
                "approachThresholdMeters" to 500,
                "bearingToleranceDeg" to 35,
                "rssiFallbackThresholdDbm" to -65,
                "rssiConsecutivePacketCount" to 3,
                "gpsPacketTimeoutMs" to 5000,
                "clearanceTimeoutMs" to 90000,
                "updatedAt" to ServerValue.TIMESTAMP
            )
        ),

        database.child(FirebasePaths.LORA_TELEMETRY).child("JNC001").child("AMB001").updateChildren(
            mapOf(
                "ambulanceId" to "AMB001",
                "tripId" to tripId,
                "lat" to 12.9716,
                "lng" to 77.5946,
                "speedKmph" to 0,
                "headingDeg" to 185,
                "gpsFix" to true,
                "rssi" to -72,
                "distanceMeters" to 500,
                "bearingToJunctionDeg" to 182,
                "approaching" to false,
                "preemptionEligible" to false,
                "updatedAt" to ServerValue.TIMESTAMP
            )
        ))

        Tasks.whenAllComplete(seedTasks).addOnSuccessListener { results ->
            val failed = results.firstOrNull { !it.isSuccessful }
            if (failed == null) {
                onResult?.invoke(true, "Firebase demo data ready")
            } else {
                onResult?.invoke(false, failed.exception?.message ?: "Firebase seed failed")
            }
        }.addOnFailureListener { error ->
            onResult?.invoke(false, error.message ?: "Firebase seed failed")
        }
    }

    private fun reportTask(onResult: ((Boolean, String) -> Unit)?, successMessage: String) {
        onResult?.invoke(true, successMessage)
    }

    private fun reportError(onResult: ((Boolean, String) -> Unit)?, error: Exception) {
        onResult?.invoke(false, error.message ?: "Firebase write failed")
    }

    private fun reportAll(onResult: ((Boolean, String) -> Unit)?, successMessage: String, vararg tasks: com.google.android.gms.tasks.Task<Void>) {
        Tasks.whenAllComplete(tasks.toList()).addOnSuccessListener { results ->
            val failed = results.firstOrNull { !it.isSuccessful }
            if (failed == null) {
                reportTask(onResult, successMessage)
            } else {
                onResult?.invoke(false, failed.exception?.message ?: "Firebase write failed")
            }
        }.addOnFailureListener { error ->
            reportError(onResult, error)
        }
    }

    fun refreshDemoData(onResult: (Boolean, String) -> Unit) {
        seedDemoData(onResult)
    }

    fun loadAdminSummary(onResult: (AdminSummary?, String?) -> Unit) {
        val paths = listOf(
            FirebasePaths.USERS,
            FirebasePaths.AMBULANCES,
            FirebasePaths.HOSPITALS,
            FirebasePaths.RFID_TAGS,
            FirebasePaths.JUNCTIONS
        )
        val tasks = paths.map { path -> database.child(path).get() }

        Tasks.whenAllSuccess<com.google.firebase.database.DataSnapshot>(tasks)
            .addOnSuccessListener { snapshots ->
                val summary = AdminSummary(
                    users = summarizeUsers(snapshots[0]),
                    ambulances = summarizeNodes(snapshots[1], "ambulanceId", "status"),
                    hospitals = summarizeNodes(snapshots[2], "hospitalId", "bedsAvailable"),
                    rfidTags = summarizeNodes(snapshots[3], "rfidTagId", "ambulanceId"),
                    junctions = summarizeNodes(snapshots[4], "junctionId", "signalState")
                )
                onResult(summary, null)
            }
            .addOnFailureListener { error ->
                onResult(null, error.message ?: "Firebase read failed")
            }
    }

    fun deactivateUser(userId: String, onResult: (Boolean, String) -> Unit) {
        val cleanUserId = userId.trim()
        if (cleanUserId.isBlank()) {
            onResult(false, "Enter a user ID to deactivate")
            return
        }

        database.child(FirebasePaths.USERS).child(cleanUserId).child("active").setValue(false)
            .addOnSuccessListener { onResult(true, "User deactivated: $cleanUserId") }
            .addOnFailureListener { error -> onResult(false, error.message ?: "Firebase write failed") }
    }

    private fun summarizeUsers(snapshot: com.google.firebase.database.DataSnapshot): List<String> {
        return snapshot.children.map { child ->
            val id = child.key.orEmpty()
            val role = child.child("role").getValue(String::class.java).orEmpty()
            val active = child.child("active").getValue(Boolean::class.java) ?: false
            "$id | $role | ${if (active) "active" else "inactive"}"
        }.ifEmpty { listOf("No users found") }
    }

    private fun summarizeNodes(snapshot: com.google.firebase.database.DataSnapshot, idField: String, detailField: String): List<String> {
        return snapshot.children.map { child ->
            val id = child.child(idField).getValue(String::class.java) ?: child.key.orEmpty()
            val detail = child.child(detailField).value?.toString().orEmpty().ifBlank { "ready" }
            "$id | $detailField: $detail"
        }.ifEmpty { listOf("No records found") }
    }

    fun registerDriver(userId: String, name: String, pin: String, ambulanceId: String, phone: String, onResult: ((Boolean, String) -> Unit)? = null) {
        val cleanUserId = userId.trim()
        val cleanAmbulanceId = ambulanceId.trim().uppercase()
        reportAll(
            onResult,
            "Driver saved: $cleanUserId",
            database.child(FirebasePaths.USERS).child(cleanUserId).updateChildren(
                mapOf(
                    "userId" to cleanUserId,
                    "name" to name.trim(),
                    "pin" to pin.trim(),
                    "role" to "ambulance_driver",
                    "ambulanceId" to cleanAmbulanceId,
                    "active" to true,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            ),
            database.child(FirebasePaths.DRIVERS).child(cleanUserId).updateChildren(
                mapOf(
                    "driverId" to cleanUserId,
                    "userId" to cleanUserId,
                    "name" to name.trim(),
                    "phone" to phone.trim(),
                    "assignedAmbulanceId" to cleanAmbulanceId,
                    "active" to true,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            )
        )
    }

    fun login(userId: String, pin: String, onResult: (AppUser?, String?) -> Unit) {
        val cleanUserId = userId.trim()
        val cleanPin = pin.trim()
        if (cleanUserId.isBlank() || cleanPin.isBlank()) {
            onResult(null, "Enter user ID and PIN")
            return
        }

        database.child(FirebasePaths.USERS).child(cleanUserId).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onResult(null, "User is not pre-registered in Firebase")
                    return@addOnSuccessListener
                }

                val storedPin = snapshot.child("pin").getValue(String::class.java).orEmpty()
                val active = snapshot.child("active").getValue(Boolean::class.java) ?: false
                if (!active) {
                    onResult(null, "This user is inactive")
                    return@addOnSuccessListener
                }
                if (storedPin != cleanPin) {
                    onResult(null, "Invalid PIN")
                    return@addOnSuccessListener
                }

                val user = AppUser(
                    userId = snapshot.child("userId").getValue(String::class.java) ?: cleanUserId,
                    name = snapshot.child("name").getValue(String::class.java) ?: cleanUserId,
                    pin = storedPin,
                    role = snapshot.child("role").getValue(String::class.java).orEmpty(),
                    ambulanceId = snapshot.child("ambulanceId").getValue(String::class.java),
                    assignedJunctionId = snapshot.child("assignedJunctionId").getValue(String::class.java),
                    hospitalId = snapshot.child("hospitalId").getValue(String::class.java)
                )
                onResult(user, null)
            }
            .addOnFailureListener { error ->
                onResult(null, error.message ?: "Firebase login failed")
            }
    }

    fun startEmergencyTrip(user: AppUser, severity: String, hospital: HospitalOption) {
        val ambulanceId = user.ambulanceId ?: "AMB001"
        database.child(FirebasePaths.AMBULANCES).child(ambulanceId).updateChildren(
            mapOf(
                "ambulanceId" to ambulanceId,
                "driverId" to user.userId,
                "emergencyActive" to true,
                "status" to "emergency_active",
                "destinationHospitalId" to hospital.id,
                "severity" to severity,
                "lastLoRaTelemetry" to mapOf(
                    "junctionId" to "JNC001",
                    "lat" to 12.9716,
                    "lng" to 77.5946,
                    "speedKmph" to 42,
                    "headingDeg" to 185,
                    "gpsFix" to true,
                    "rssi" to -72,
                    "distanceMeters" to 420,
                    "bearingToJunctionDeg" to 182,
                    "approaching" to true,
                    "preemptionEligible" to true,
                    "updatedAt" to ServerValue.TIMESTAMP
                ),
                "lastLocation" to mapOf(
                    "lat" to 12.9716,
                    "lng" to 77.5946,
                    "source" to "android_demo",
                    "updatedAt" to ServerValue.TIMESTAMP
                ),
                "updatedAt" to ServerValue.TIMESTAMP
            )
        )

        database.child(FirebasePaths.EMERGENCY_TRIPS).child(tripId).updateChildren(
            mapOf(
                "tripId" to tripId,
                "ambulanceId" to ambulanceId,
                "driverId" to user.userId,
                "destinationHospitalId" to hospital.id,
                "destinationHospitalName" to hospital.name,
                "severity" to severity,
                "status" to "active",
                "startedAt" to ServerValue.TIMESTAMP,
                "endedAt" to null
            )
        )

        database.child(FirebasePaths.HOSPITAL_ALERTS).child(hospital.id).child(tripId).updateChildren(
            mapOf(
                "tripId" to tripId,
                "ambulanceId" to ambulanceId,
                "severity" to severity,
                "status" to "incoming",
                "eta" to hospital.eta,
                "message" to "Ambulance incoming to ${hospital.name}",
                "updatedAt" to ServerValue.TIMESTAMP
            )
        )

        database.child(FirebasePaths.POLICE_ALERTS).child("JNC001").child(tripId).updateChildren(
            mapOf(
                "tripId" to tripId,
                "ambulanceId" to ambulanceId,
                "severity" to severity,
                "destinationHospitalId" to hospital.id,
                "status" to "ambulance_approaching",
                "message" to "Ambulance approaching junction through GPS-LoRa preemption",
                "preemptionMode" to "gps_lora",
                "distanceMeters" to 420,
                "updatedAt" to ServerValue.TIMESTAMP
            )
        )

        database.child(FirebasePaths.LORA_TELEMETRY).child("JNC001").child(ambulanceId).updateChildren(
            mapOf(
                "ambulanceId" to ambulanceId,
                "tripId" to tripId,
                "lat" to 12.9716,
                "lng" to 77.5946,
                "speedKmph" to 42,
                "headingDeg" to 185,
                "gpsFix" to true,
                "rssi" to -72,
                "distanceMeters" to 420,
                "bearingToJunctionDeg" to 182,
                "approaching" to true,
                "preemptionEligible" to true,
                "source" to "gps_lora",
                "updatedAt" to ServerValue.TIMESTAMP
            )
        )
    }

    fun endEmergencyTrip(user: AppUser, hospital: HospitalOption) {
        val ambulanceId = user.ambulanceId ?: "AMB001"
        database.child(FirebasePaths.AMBULANCES).child(ambulanceId).updateChildren(
            mapOf(
                "emergencyActive" to false,
                "status" to "available",
                "updatedAt" to ServerValue.TIMESTAMP
            )
        )

        database.child(FirebasePaths.EMERGENCY_TRIPS).child(tripId).updateChildren(
            mapOf(
                "status" to "completed",
                "endedAt" to ServerValue.TIMESTAMP
            )
        )

        database.child(FirebasePaths.HOSPITAL_ALERTS).child(hospital.id).child(tripId).updateChildren(
            mapOf(
                "status" to "completed",
                "updatedAt" to ServerValue.TIMESTAMP
            )
        )
    }

    fun updateLocation(ambulanceId: String, latitude: Double, longitude: Double) {
        database.child(FirebasePaths.AMBULANCES).child(ambulanceId).child("lastLocation").updateChildren(
            mapOf(
                "lat" to latitude,
                "lng" to longitude,
                "source" to "android_gps",
                "updatedAt" to ServerValue.TIMESTAMP
            )
        )
    }

    fun updateHospitalAlertStatus(hospitalId: String, status: String) {
        database.child(FirebasePaths.HOSPITAL_ALERTS).child(hospitalId).child(tripId).updateChildren(
            mapOf(
                "status" to status,
                "updatedAt" to ServerValue.TIMESTAMP
            )
        )
    }

    fun registerPolice(userId: String, name: String, pin: String, junctionId: String, onResult: ((Boolean, String) -> Unit)? = null) {
        val cleanUserId = userId.trim()
        reportAll(
            onResult,
            "Police user saved: $cleanUserId",
            database.child(FirebasePaths.USERS).child(cleanUserId).updateChildren(
                mapOf(
                    "userId" to cleanUserId,
                    "name" to name.trim(),
                    "pin" to pin.trim(),
                    "role" to "police",
                    "assignedJunctionId" to junctionId.trim().uppercase(),
                    "active" to true,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            )
        )
    }

    fun registerHospitalUser(userId: String, name: String, pin: String, hospitalId: String, onResult: ((Boolean, String) -> Unit)? = null) {
        val cleanUserId = userId.trim()
        reportAll(
            onResult,
            "Hospital user saved: $cleanUserId",
            database.child(FirebasePaths.USERS).child(cleanUserId).updateChildren(
                mapOf(
                    "userId" to cleanUserId,
                    "name" to name.trim(),
                    "pin" to pin.trim(),
                    "role" to "hospital",
                    "hospitalId" to hospitalId.trim().uppercase(),
                    "active" to true,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            )
        )
    }

    fun registerHospital(hospitalId: String, name: String, beds: Int, phone: String, onResult: ((Boolean, String) -> Unit)? = null) {
        val cleanHospitalId = hospitalId.trim().uppercase()
        reportAll(
            onResult,
            "Hospital saved: $cleanHospitalId",
            database.child(FirebasePaths.HOSPITALS).child(cleanHospitalId).updateChildren(
                mapOf(
                    "hospitalId" to cleanHospitalId,
                    "name" to name.trim(),
                    "bedsAvailable" to beds,
                    "phone" to phone.trim(),
                    "emergencyAvailable" to true,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            )
        )
    }

    fun registerAmbulance(ambulanceId: String, driverId: String, rfidTagId: String, onResult: ((Boolean, String) -> Unit)? = null) {
        val cleanAmbulanceId = ambulanceId.trim().uppercase()
        val cleanTagId = rfidTagId.trim().uppercase()
        reportAll(
            onResult,
            "Ambulance and RFID saved: $cleanAmbulanceId",
            database.child(FirebasePaths.AMBULANCES).child(cleanAmbulanceId).updateChildren(
                mapOf(
                    "ambulanceId" to cleanAmbulanceId,
                    "driverId" to driverId.trim(),
                    "rfidTagId" to cleanTagId,
                    "loraNodeId" to "LORA_$cleanAmbulanceId",
                    "status" to "available",
                    "emergencyActive" to false,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            ),
            database.child(FirebasePaths.RFID_TAGS).child(cleanTagId).updateChildren(
                mapOf(
                    "rfidTagId" to cleanTagId,
                    "ambulanceId" to cleanAmbulanceId,
                    "authorized" to true,
                    "active" to true,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            )
        )
    }

    fun registerRfidTag(rfidTagId: String, ambulanceId: String, onResult: ((Boolean, String) -> Unit)? = null) {
        val cleanTagId = rfidTagId.trim().uppercase()
        reportAll(
            onResult,
            "RFID tag saved: $cleanTagId",
            database.child(FirebasePaths.RFID_TAGS).child(cleanTagId).updateChildren(
                mapOf(
                    "rfidTagId" to cleanTagId,
                    "ambulanceId" to ambulanceId.trim().uppercase(),
                    "authorized" to true,
                    "active" to true,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            )
        )
    }

    fun registerJunction(junctionId: String, name: String, activeLane: String, onResult: ((Boolean, String) -> Unit)? = null) {
        val cleanJunctionId = junctionId.trim().uppercase()
        reportAll(
            onResult,
            "Junction saved: $cleanJunctionId",
            database.child(FirebasePaths.JUNCTIONS).child(cleanJunctionId).updateChildren(
                mapOf(
                    "junctionId" to cleanJunctionId,
                    "name" to name.trim(),
                    "activeLane" to activeLane.trim().ifBlank { "normal" },
                    "signalState" to "normal",
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            )
        )
    }

    fun observePoliceAlerts(junctionId: String, listener: ValueEventListener) {
        database.child(FirebasePaths.POLICE_ALERTS).child(junctionId).addValueEventListener(listener)
    }

    fun observeHospitalAlerts(hospitalId: String, listener: ValueEventListener) {
        database.child(FirebasePaths.HOSPITAL_ALERTS).child(hospitalId).addValueEventListener(listener)
    }

    fun observeAmbulance(ambulanceId: String, listener: ValueEventListener) {
        database.child(FirebasePaths.AMBULANCES).child(ambulanceId).addValueEventListener(listener)
    }
    
    fun observeLoRaTelemetry(junctionId: String, ambulanceId: String, listener: ValueEventListener) {
        database.child(FirebasePaths.LORA_TELEMETRY).child(junctionId).child(ambulanceId).addValueEventListener(listener)
    }
}
