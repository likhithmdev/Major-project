package com.smartambulance.driver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.smartambulance.driver.data.AppUser
import com.smartambulance.driver.data.HospitalOption
import com.smartambulance.driver.ui.components.AppTopBar
import com.smartambulance.driver.ui.components.IdentityCard
import com.smartambulance.driver.ui.components.InfoPanel
import com.smartambulance.driver.ui.components.RoleChip
import com.smartambulance.driver.ui.components.SelectCard
import com.smartambulance.driver.ui.components.StatTile
import com.smartambulance.driver.ui.components.StatusBanner
import com.smartambulance.driver.ui.theme.Amber
import com.smartambulance.driver.ui.theme.Blue
import com.smartambulance.driver.ui.theme.Crimson
import com.smartambulance.driver.ui.theme.Ink
import com.smartambulance.driver.ui.theme.Mute
import com.smartambulance.driver.ui.theme.Teal

@Composable
fun LoginScreen(
    userId: String,
    pin: String,
    message: String,
    loading: Boolean,
    onUserId: (String) -> Unit,
    onPin: (String) -> Unit,
    onFill: (String, String) -> Unit,
    onLogin: () -> Unit,
    onSeed: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF140814), Ink)))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(36.dp))
        Box(
            Modifier.size(88.dp).clip(CircleShape).background(Crimson),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Emergency, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("SMART AMBULANCE", style = MaterialTheme.typography.labelSmall, color = Crimson)
        Spacer(Modifier.height(6.dp))
        Text("Command Center", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Priority corridor control for drivers, police, hospitals, and admins.",
            style = MaterialTheme.typography.bodyMedium,
            color = Mute,
            modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
        )
        Spacer(Modifier.height(28.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color(0xFF121826)).padding(20.dp)
        ) {
            Text("Sign in", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(userId, onUserId, label = { Text("User ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = onPin,
                label = { Text("PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onLogin,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Crimson)
            ) {
                Text(if (loading) "Checking access..." else "Enter command center")
            }
            TextButton(onClick = onSeed, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Load demo accounts")
            }
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Mute)
        }
        Spacer(Modifier.height(20.dp))
        Text("Quick demo roles", style = MaterialTheme.typography.labelSmall, color = Mute)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RoleChip("Driver", false, Crimson, Icons.Filled.Emergency) { onFill("driver_001", "1111") }
            RoleChip("Police", false, Blue, Icons.Filled.LocalPolice) { onFill("police_001", "2222") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RoleChip("Hospital", false, Teal, Icons.Filled.LocalHospital) { onFill("hospital_001", "3333") }
            RoleChip("Admin", false, Amber, Icons.Filled.AdminPanelSettings) { onFill("admin_001", "0000") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    user: AppUser,
    hospitals: List<HospitalOption>,
    selectedHospital: HospitalOption,
    severity: String,
    emergencyActive: Boolean,
    status: String,
    gps: String,
    onSeverity: (String) -> Unit,
    onHospital: (HospitalOption) -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onNavigate: () -> Unit,
    onSearchHospital: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar("Driver", "Ambulance unit", Crimson, onLogout) },
        floatingActionButton = {
            if (!emergencyActive) {
                FloatingActionButton(onClick = onStart, containerColor = Crimson, contentColor = Color.White) {
                    Icon(Icons.Filled.Emergency, contentDescription = "Activate emergency")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IdentityCard(user.name, user.ambulanceId ?: "AMB001", Crimson)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("Trip", if (emergencyActive) "Live" else "Standby", Blue, Modifier.weight(1f))
                StatTile("GPS-LoRa", "500 m", Teal, Modifier.weight(1f))
                StatTile("RFID", "Stop line", Amber, Modifier.weight(1f))
            }
            StatusBanner(status, emergencyActive)
            Text("Patient severity", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Fine", "Serious", "Very Emergency").forEach { item ->
                    RoleChip(item, item == severity, Crimson, Icons.Filled.Emergency) { onSeverity(item) }
                }
            }
            Text("Destination hospital", style = MaterialTheme.typography.titleMedium)
            hospitals.forEach { hospital ->
                SelectCard(
                    hospital.name,
                    "${hospital.distance}  ·  ${hospital.eta}  ·  ${hospital.beds} beds",
                    hospital.id == selectedHospital.id,
                    Crimson
                ) { onHospital(hospital) }
            }
            FilledTonalButton(
                onClick = onSearchHospital,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Blue)
            ) {
                Icon(Icons.Filled.LocalHospital, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Search nearby hospitals")
            }
            OutlinedButton(onClick = onNavigate, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Filled.Navigation, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Open navigation")
            }
            if (emergencyActive) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) { Text("Complete trip") }
            }
            Text(gps, style = MaterialTheme.typography.bodyMedium, color = Mute)
            Spacer(Modifier.height(48.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoliceScreen(
    user: AppUser,
    junctionId: String,
    alert: String,
    telemetry: String,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(topBar = { AppTopBar("Junction watch", "Traffic police", Blue, onLogout) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IdentityCard(user.name, "Assigned $junctionId", Blue)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("Lane", "Priority", Crimson, Modifier.weight(1f))
                StatTile("Signal", "Auto", Teal, Modifier.weight(1f))
                StatTile("Feed", "Live", Blue, Modifier.weight(1f))
            }
            InfoPanel("Incoming ambulance", alert, Crimson)
            InfoPanel("Corridor logic", "GPS-LoRa starts early green. RC522 at the stop line restores the cycle.", Amber)
            InfoPanel("Ambulance GPS-LoRa", telemetry, Blue)
            FilledTonalButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Text("Refresh live alerts") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalScreen(
    user: AppUser,
    hospitalId: String,
    alert: String,
    telemetry: String,
    readiness: String,
    onReady: (String) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(topBar = { AppTopBar("Receiving bay", "Hospital desk", Teal, onLogout) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IdentityCard(user.name, hospitalId, Teal)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("Beds", "Live", Teal, Modifier.weight(1f))
                StatTile("ETA", "Tracked", Blue, Modifier.weight(1f))
                StatTile("Team", "Standby", Amber, Modifier.weight(1f))
            }
            InfoPanel("Incoming ambulance", alert, Crimson)
            InfoPanel("Ambulance GPS-LoRa", telemetry, Blue)
            InfoPanel("Bay readiness", readiness, Teal)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(onClick = { onReady("team_alerted") }, modifier = Modifier.weight(1f)) { Text("Team") }
                FilledTonalButton(onClick = { onReady("bed_ready") }, modifier = Modifier.weight(1f)) { Text("Bed") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(onClick = { onReady("doctor_ready") }, modifier = Modifier.weight(1f)) { Text("Doctor") }
                FilledTonalButton(onClick = { onReady("ambulance_received") }, modifier = Modifier.weight(1f)) { Text("Received") }
            }
        }
    }
}
