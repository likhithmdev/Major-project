package com.smartambulance.driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.smartambulance.driver.data.AdminSummary
import com.smartambulance.driver.data.AppUser
import com.smartambulance.driver.data.DemoRepository
import com.smartambulance.driver.data.Hospital
import com.smartambulance.driver.data.HospitalOption
import com.smartambulance.driver.mqtt.MqttManager
import com.smartambulance.driver.mqtt.MqttTopics
import com.smartambulance.driver.services.HospitalDiscoveryService
import com.smartambulance.driver.services.NavigationService
import com.smartambulance.driver.ui.screens.AdminActions
import com.smartambulance.driver.ui.screens.AdminDashboard
import com.smartambulance.driver.ui.screens.AdminScreen
import com.smartambulance.driver.ui.screens.DriverDashboard
import com.smartambulance.driver.ui.screens.DriverScreen
import com.smartambulance.driver.ui.screens.HospitalDashboard
import com.smartambulance.driver.ui.screens.HospitalScreen
import com.smartambulance.driver.ui.screens.HospitalSearchScreen
import com.smartambulance.driver.ui.screens.LoginScreen
import com.smartambulance.driver.ui.screens.PoliceDashboard
import com.smartambulance.driver.ui.screens.PoliceScreen
import com.smartambulance.driver.ui.theme.SmartAmbulanceTheme

class MainActivity : ComponentActivity() {
    private val repository = DemoRepository()
    private val locationHandler = Handler(Looper.getMainLooper())
    private val auth = FirebaseAuth.getInstance()
    private lateinit var mqttManager: MqttManager
    private lateinit var hospitalDiscoveryService: HospitalDiscoveryService
    private lateinit var navigationService: NavigationService
    
    private var showHospitalSearch by mutableStateOf(false)

    private var userId by mutableStateOf("")
    private var pin by mutableStateOf("")
    private var message by mutableStateOf("Use a pre-registered Firebase account.")
    private var loading by mutableStateOf(false)
    private var user by mutableStateOf<AppUser?>(null)
    private var selectedSeverity by mutableStateOf("Serious")
    private var selectedHospital by mutableStateOf(repository.hospitals.first())
    private var emergencyActive by mutableStateOf(false)
    private var status by mutableStateOf("Standby · waiting for emergency activation")
    private var gps by mutableStateOf("GPS-LoRa: phone GPS starts after you activate emergency.")
    private var policeAlert by mutableStateOf("Waiting for a junction alert.")
    private var hospitalAlert by mutableStateOf("Waiting for a hospital alert.")
    private var telemetry by mutableStateOf("Waiting for live ambulance location.")
    private var ambulanceLocation by mutableStateOf<Pair<Double?, Double?>>(null to null)
    private var driverCurrentLocation by mutableStateOf<Pair<Double?, Double?>>(null to null)
    private var loraTelemetry by mutableStateOf<Pair<Double?, Double?>>(null to null)
    private var readiness by mutableStateOf("Mark each bay item as the receiving team gets ready.")
    private var adminMessage by mutableStateOf("Ready to register project data in Firebase.")
    private var adminRecords by mutableStateOf("Loading Firebase records...")
    private var selectedGoogleHospital by mutableStateOf<Hospital?>(null)

    private var alertListener: ValueEventListener? = null
    private var ambulanceListener: ValueEventListener? = null
    private var loraTelemetryListener: ValueEventListener? = null

    private val locationPublisher = object : Runnable {
        override fun run() {
            publishCurrentLocation()
            if (emergencyActive) locationHandler.postDelayed(this, 10000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        requestLocationPermission()

        // Sign in to Firebase Authentication anonymously
        signInAnonymously()

        // Initialize MQTT Manager
        mqttManager = MqttManager(this)
        mqttManager.setOnConnectionStatusChanged { connected ->
            runOnUiThread {
                if (connected) {
                    // Subscribe to relevant topics when connected
                    mqttManager.subscribeToAmbulanceTopics("AMB001")
                    mqttManager.subscribeToJunctionTopics("JNC001")
                }
            }
        }
        mqttManager.setOnMessageReceived { topic, message ->
            runOnUiThread {
                // Handle incoming MQTT messages
                when {
                    topic.contains("/lora-gps") -> {
                        // Update telemetry display with LoRa GPS data
                        telemetry = "MQTT LoRa GPS: $message"
                    }
                    topic.contains("/events") -> {
                        // Handle junction events
                        status = "Junction event: $message"
                    }
                    topic.contains("/signal") -> {
                        // Handle signal state changes
                        status = "Signal state: $message"
                    }
                }
            }
        }
        mqttManager.connect()
        
        // Initialize hospital discovery and navigation services
        hospitalDiscoveryService = HospitalDiscoveryService(this)
        navigationService = NavigationService(this)
        
        repository.refreshDemoData { ok, result ->
            runOnUiThread {
                message = if (ok) "$result Demo users are ready." else "Firebase write failed: $result"
            }
        }
        setContent {
            SmartAmbulanceTheme {
                val current = user
                when (current?.role) {
                    "ambulance_driver" -> DriverDashboard(
                        user = current,
                        hospitals = repository.hospitals,
                        emergencyActive = emergencyActive,
                        selectedSeverity = selectedSeverity,
                        selectedHospital = selectedHospital,
                        status = status,
                        currentLocation = loraTelemetry,
                        dataSource = "LoRa GPS",
                        onSeverityChange = { selectedSeverity = it },
                        onHospitalChange = {
                            selectedHospital = it
                            status = "Destination locked · ${it.name}"
                        },
                        onStartEmergency = { startEmergency(current) },
                        onEndEmergency = { completeEmergency(current) },
                        onSearchHospital = { showHospitalSearch = true },
                        onLogout = { logout() }
                    )
                    "police" -> PoliceDashboard(
                        user = current,
                        junctionId = current.assignedJunctionId ?: "JNC001",
                        alert = policeAlert,
                        telemetry = telemetry,
                        ambulanceLocation = loraTelemetry,
                        onRefresh = { bindPolice(current) },
                        onLogout = { logout() }
                    )
                    "hospital" -> HospitalDashboard(
                        user = current,
                        hospitalId = current.hospitalId ?: "HOSP001",
                        alert = hospitalAlert,
                        telemetry = telemetry,
                        readiness = readiness,
                        onReady = { key ->
                            repository.updateHospitalAlertStatus(current.hospitalId ?: "HOSP001", key)
                            readiness = when (key) {
                                "team_alerted" -> "Emergency team has been alerted."
                                "bed_ready" -> "Trauma bed is ready."
                                "doctor_ready" -> "Doctor is standing by."
                                else -> "Ambulance received at the bay."
                            }
                        },
                        onLogout = { logout() }
                    )
                    "admin" -> AdminDashboard(
                        user = current,
                        message = adminMessage,
                        records = adminRecords,
                        actions = adminActions(),
                        onLogout = { logout() }
                    )
                    else -> LoginScreen(
                        userId = userId,
                        pin = pin,
                        message = message,
                        loading = loading,
                        onUserId = { userId = it },
                        onPin = { pin = it },
                        onFill = { id, secret ->
                            userId = id
                            pin = secret
                        },
                        onLogin = { login() },
                        onSeed = {
                            message = "Writing demo data to Firebase..."
                            repository.refreshDemoData { ok, result ->
                                runOnUiThread {
                                    message = if (ok) result else "Firebase write failed: $result"
                                }
                            }
                        },
                        onSeedHospitals = {
                            message = "Adding Bangalore hospitals to Firebase..."
                            repository.seedBangaloreHospitals { ok, result ->
                                runOnUiThread {
                                    message = if (ok) result else "Failed: $result"
                                }
                            }
                        }
                    )
                }
            }
            
            // Hospital search overlay
            if (showHospitalSearch) {
                HospitalSearchScreen(
                    hospitalDiscoveryService = hospitalDiscoveryService,
                    onHospitalSelected = { hospital ->
                        selectedGoogleHospital = hospital
                        // Convert Google Hospital to HospitalOption
                        val hospitalOption = HospitalOption(
                            id = hospital.placeId,
                            name = hospital.name,
                            distance = "${(hospital.distance / 1000).toInt()} km",
                            eta = hospital.duration.ifEmpty { "Calculating..." },
                            beds = 8 // Default value for OSM
                        )
                        selectedHospital = hospitalOption
                        status = "Selected ${hospital.name} via Google Maps"
                        showHospitalSearch = false
                    },
                    onBack = { showHospitalSearch = false }
                )
            }
        }
    }

    private fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    // Successfully signed in
                }
                .addOnFailureListener { error ->
                    // Sign in failed - log the error for debugging
                    android.util.Log.e("FirebaseAuth", "Anonymous sign-in failed: ${error.message}", error)
                }
        }
    }

    private fun login() {
        loading = true
        message = "Authenticating with Firebase..."

        // Ensure Firebase Auth is signed in before reading database
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    message = "Checking Firebase user..."
                    performLogin()
                }
                .addOnFailureListener { error ->
                    android.util.Log.e("FirebaseAuth", "Auth failed during login: ${error.message}", error)
                    // Fallback: Try to login anyway - user may have rules that allow unauthenticated access
                    message = "Auth warning: ${error.message}. Checking user data..."
                    performLogin()
                }
        } else {
            message = "Checking Firebase user..."
            performLogin()
        }
    }

    private fun performLogin() {
        repository.login(userId, pin) { found, error ->
            runOnUiThread {
                loading = false
                if (found == null) {
                    message = error ?: "Invalid pre-registered user ID or PIN"
                    return@runOnUiThread
                }
                user = found
                when (found.role) {
                    "police" -> bindPolice(found)
                    "hospital" -> bindHospital(found)
                    "admin" -> refreshAdminRecords()
                    else -> Unit
                }
            }
        }
    }

    private fun logout() {
        emergencyActive = false
        locationHandler.removeCallbacks(locationPublisher)
        alertListener = null
        ambulanceListener = null
        mqttManager.disconnect()
        user = null
        message = "Signed out."
    }

    private fun startEmergency(current: AppUser) {
        emergencyActive = true
        repository.startEmergencyTrip(current, selectedSeverity, selectedHospital)
        status = "Emergency live · $selectedSeverity · ${selectedHospital.name}"
        gps = "GPS-LoRa: phone GPS publishing. Vehicle LoRa handles junction preemption."

        // Publish emergency status via MQTT
        if (mqttManager.isConnected()) {
            val ambulanceId = current.ambulanceId ?: "AMB001"
            mqttManager.publish(
                MqttTopics.ambulanceStatus(ambulanceId),
                """{"ambulanceId":"$ambulanceId","status":"emergency_active","severity":"$selectedSeverity","destinationHospitalId":"${selectedHospital.id}","timestamp":${System.currentTimeMillis()}}"""
            )
        }

        locationHandler.removeCallbacks(locationPublisher)
        locationHandler.post(locationPublisher)
        // openDirections(selectedHospital) // Temporarily disabled for UI upgrade
    }

    private fun completeEmergency(current: AppUser) {
        emergencyActive = false
        locationHandler.removeCallbacks(locationPublisher)
        repository.endEmergencyTrip(current, selectedHospital)
        status = "Trip completed · corridor released"
        gps = "GPS-LoRa: tracking stopped"
        
        // Publish trip completion via MQTT
        if (mqttManager.isConnected()) {
            val ambulanceId = current.ambulanceId ?: "AMB001"
            mqttManager.publish(
                MqttTopics.ambulanceStatus(ambulanceId),
                """{"ambulanceId":"$ambulanceId","status":"available","timestamp":${System.currentTimeMillis()}}"""
            )
            
            // Publish trip event
            mqttManager.publish(
                MqttTopics.tripEvents("TRIP001"),
                """{"tripId":"TRIP001","ambulanceId":"$ambulanceId","eventType":"trip_completed","timestamp":${System.currentTimeMillis()}}"""
            )
        }
    }

    private fun bindPolice(current: AppUser) {
        val junctionId = current.assignedJunctionId ?: "JNC001"
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latest = snapshot.children.lastOrNull()
                policeAlert = if (latest == null) {
                    "No active alert for $junctionId."
                } else {
                    val ambulanceId = latest.child("ambulanceId").getValue(String::class.java) ?: "AMB001"
                    val severity = latest.child("severity").getValue(String::class.java) ?: "Unknown"
                    val text = latest.child("message").getValue(String::class.java) ?: "Ambulance approaching junction"
                    val mode = latest.child("preemptionMode").getValue(String::class.java) ?: "gps_lora"
                    val distance = latest.child("distanceMeters").value?.toString() ?: "--"
                    "$text\nAmbulance  $ambulanceId\nSeverity  $severity\nMode  $mode\nDistance  $distance m"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                policeAlert = "Firebase error: ${error.message}"
            }
        }
        alertListener = listener
        repository.observePoliceAlerts(junctionId, listener)
        bindAmbulanceTelemetry()
    }

    private fun bindHospital(current: AppUser) {
        val hospitalId = current.hospitalId ?: "HOSP001"
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latest = snapshot.children.lastOrNull()
                hospitalAlert = if (latest == null) {
                    "No active alert for $hospitalId."
                } else {
                    val ambulanceId = latest.child("ambulanceId").getValue(String::class.java) ?: "AMB001"
                    val severity = latest.child("severity").getValue(String::class.java) ?: "Unknown"
                    val eta = latest.child("eta").getValue(String::class.java) ?: "Unknown"
                    val tripStatus = latest.child("status").getValue(String::class.java) ?: "incoming"
                    "Ambulance  $ambulanceId\nSeverity  $severity\nETA  $eta\nStatus  $tripStatus"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hospitalAlert = "Firebase error: ${error.message}"
            }
        }
        alertListener = listener
        repository.observeHospitalAlerts(hospitalId, listener)
        bindAmbulanceTelemetry()
    }

    private fun bindAmbulanceTelemetry() {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val severity = snapshot.child("severity").getValue(String::class.java) ?: "Unknown"
                val hospital = snapshot.child("destinationHospitalId").getValue(String::class.java) ?: "Not selected"
                val lat = snapshot.child("lastLocation").child("lat").getValue(Double::class.java)
                val lng = snapshot.child("lastLocation").child("lng").getValue(Double::class.java)
                val distance = snapshot.child("lastLoRaTelemetry").child("distanceMeters").value?.toString() ?: "--"
                val rssi = snapshot.child("lastLoRaTelemetry").child("rssi").value?.toString() ?: "--"
                telemetry = "Destination  $hospital\nSeverity  $severity\nLocation  ${lat ?: "--"}, ${lng ?: "--"}\nApproach  $distance m  ·  RSSI $rssi dBm"
                ambulanceLocation = lat to lng
            }

            override fun onCancelled(error: DatabaseError) {
                telemetry = "Firebase error: ${error.message}"
            }
        }
        ambulanceListener = listener
        repository.observeAmbulance("AMB001", listener)
        
        // Also bind to LoRa telemetry from Receiver ESP32
        bindLoRaTelemetry()
    }
    
    private fun bindLoRaTelemetry() {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // This data comes from Receiver ESP32 which received it from Ambulance ESP32 Transmitter via LoRa
                val lat = snapshot.child("lat").getValue(Double::class.java)
                val lng = snapshot.child("lng").getValue(Double::class.java)
                val speed = snapshot.child("speedKmph").getValue(Double::class.java)
                val heading = snapshot.child("headingDeg").getValue(Double::class.java)
                val distance = snapshot.child("distanceMeters").getValue(Double::class.java)
                val bearing = snapshot.child("bearingToJunctionDeg").getValue(Double::class.java)
                val gpsFix = snapshot.child("gpsFix").getValue(Boolean::class.java) ?: false
                
                // Store LoRa telemetry for dashboards
                loraTelemetry = lat to lng
                
                // Update telemetry string with LoRa data
                telemetry = "LoRa GPS: ${lat ?: "--"}, ${lng ?: "--"}\n" +
                            "Speed: ${speed ?: "--"} km/h\n" +
                            "Heading: ${heading ?: "--"}°\n" +
                            "Distance: ${distance ?: "--"} m\n" +
                            "Bearing: ${bearing ?: "--"}°\n" +
                            "GPS Fix: ${if (gpsFix) "YES" else "NO"}"
            }

            override fun onCancelled(error: DatabaseError) {
                telemetry = "LoRa telemetry error: ${error.message}"
            }
        }
        loraTelemetryListener = listener
        repository.observeLoRaTelemetry("JNC001", "AMB001", listener)
    }

    private fun adminActions() = AdminActions(
        refresh = { refreshAdminRecords() },
        seed = {
            adminMessage = "Writing demo data to Firebase..."
            repository.refreshDemoData { ok, result ->
                runOnUiThread {
                    adminMessage = if (ok) result else "Firebase write failed: $result"
                    refreshAdminRecords()
                }
            }
        },
        deactivate = { id ->
            adminMessage = "Deactivating user..."
            repository.deactivateUser(id) { ok, result ->
                runOnUiThread {
                    adminMessage = if (ok) result else "Firebase write failed: $result"
                    refreshAdminRecords()
                }
            }
        },
        saveDriver = { userId, name, pin, ambulance, phone ->
            adminMessage = "Saving driver..."
            repository.registerDriver(userId, name, pin, ambulance, phone) { ok, result ->
                runOnUiThread { adminMessage = if (ok) result else "Firebase write failed: $result" }
            }
        },
        savePolice = { userId, name, pin, junction ->
            adminMessage = "Saving police officer..."
            repository.registerPolice(userId, name, pin, junction) { ok, result ->
                runOnUiThread { adminMessage = if (ok) result else "Firebase write failed: $result" }
            }
        },
        saveHospital = { userId, desk, pin, hospitalId, hospitalName, beds, phone ->
            adminMessage = "Saving hospital..."
            repository.registerHospital(hospitalId, hospitalName, beds.toIntOrNull() ?: 0, phone) { hospitalOk, hospitalResult ->
                if (!hospitalOk) {
                    runOnUiThread { adminMessage = "Firebase write failed: $hospitalResult" }
                    return@registerHospital
                }
                repository.registerHospitalUser(userId, desk, pin, hospitalId) { userOk, userResult ->
                    runOnUiThread {
                        adminMessage = if (userOk) "$hospitalResult\n$userResult" else "Firebase write failed: $userResult"
                    }
                }
            }
        },
        saveAmbulance = { ambulanceId, driverId, tag ->
            adminMessage = "Saving ambulance and RFID..."
            repository.registerAmbulance(ambulanceId, driverId, tag) { ok, result ->
                runOnUiThread { adminMessage = if (ok) result else "Firebase write failed: $result" }
            }
        },
        saveJunction = { junctionId, name, lane ->
            adminMessage = "Saving junction..."
            repository.registerJunction(junctionId, name, lane) { ok, result ->
                runOnUiThread { adminMessage = if (ok) result else "Firebase write failed: $result" }
            }
        }
    )

    private fun refreshAdminRecords() {
        adminRecords = "Loading Firebase records..."
        repository.loadAdminSummary { summary, error ->
            runOnUiThread {
                adminRecords = if (summary == null) {
                    "Firebase read failed: ${error ?: "Unknown error"}"
                } else {
                    formatAdminSummary(summary)
                }
            }
        }
    }

    private fun formatAdminSummary(summary: AdminSummary): String {
        return listOf(
            "Users",
            summary.users.joinToString("\n").ifBlank { "None" },
            "",
            "Ambulances",
            summary.ambulances.joinToString("\n").ifBlank { "None" },
            "",
            "Hospitals",
            summary.hospitals.joinToString("\n").ifBlank { "None" },
            "",
            "RFID tags",
            summary.rfidTags.joinToString("\n").ifBlank { "None" },
            "",
            "Junctions",
            summary.junctions.joinToString("\n").ifBlank { "None" }
        ).joinToString("\n")
    }

    private fun publishCurrentLocation() {
        val ambulanceId = user?.ambulanceId ?: return
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            gps = "GPS: location permission required"
            requestLocationPermission()
            return
        }

        LocationServices.getFusedLocationProviderClient(this)
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (location == null) {
                    gps = "GPS: waiting for current location"
                    return@addOnSuccessListener
                }
                repository.updateLocation(ambulanceId, location.latitude, location.longitude)
                driverCurrentLocation = location.latitude to location.longitude
                gps = "GPS: ${"%.5f".format(location.latitude)}, ${"%.5f".format(location.longitude)}"
                
                // Publish location via MQTT if connected
                if (mqttManager.isConnected()) {
                    mqttManager.publish(
                        MqttTopics.ambulanceLoRaGps(ambulanceId),
                        """{"ambulanceId":"$ambulanceId","lat":${location.latitude},"lng":${location.longitude},"timestamp":${System.currentTimeMillis()}}"""
                    )
                }
            }
            .addOnFailureListener { error ->
                gps = "GPS: ${error.message ?: "location update failed"}"
            }
    }

    private fun openDirections(hospital: HospitalOption) {
        val uri = Uri.parse("google.navigation:q=${Uri.encode(hospital.name)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(hospital.name)}")))
        }
    }

    private fun requestLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
        }
    }
}
