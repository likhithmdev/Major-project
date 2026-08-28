package com.smartambulance.driver.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.smartambulance.driver.data.AppUser
import com.smartambulance.driver.ui.components.AppTopBar
import com.smartambulance.driver.ui.components.IdentityCard
import com.smartambulance.driver.ui.components.InfoPanel
import com.smartambulance.driver.ui.components.StatTile
import com.smartambulance.driver.ui.theme.Amber
import com.smartambulance.driver.ui.theme.Blue
import com.smartambulance.driver.ui.theme.Teal

data class AdminActions(
    val refresh: () -> Unit,
    val seed: () -> Unit,
    val deactivate: (String) -> Unit,
    val saveDriver: (String, String, String, String, String) -> Unit,
    val savePolice: (String, String, String, String) -> Unit,
    val saveHospital: (String, String, String, String, String, String, String) -> Unit,
    val saveAmbulance: (String, String, String) -> Unit,
    val saveJunction: (String, String, String) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    user: AppUser,
    message: String,
    records: String,
    actions: AdminActions,
    onLogout: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Driver", "Police", "Hospital", "Fleet")

    Scaffold(topBar = { AppTopBar("Control registry", "System admin", Amber, onLogout) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 16.dp) {
                tabs.forEachIndexed { index, label ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(label) })
                }
            }
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (tab) {
                    0 -> OverviewTab(user, message, records, actions)
                    1 -> DriverTab(actions)
                    2 -> PoliceTab(actions)
                    3 -> HospitalTab(actions)
                    4 -> FleetTab(actions)
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(user: AppUser, message: String, records: String, actions: AdminActions) {
    IdentityCard(user.name, "Firebase pre-registration", Amber)
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile("Users", "Register", Blue, Modifier.weight(1f))
        StatTile("RFID", "Stop line", Teal, Modifier.weight(1f))
        StatTile("Signals", "Junctions", Amber, Modifier.weight(1f))
    }
    InfoPanel("Database manager", message, Amber)
    InfoPanel("Existing records", records, Blue)
    FilledTonalButton(onClick = actions.refresh, modifier = Modifier.fillMaxWidth()) { Text("Refresh records") }
    FilledTonalButton(onClick = actions.seed, modifier = Modifier.fillMaxWidth()) { Text("Seed demo data") }
    var deactivateId by remember { mutableStateOf("driver_002") }
    OutlinedTextField(deactivateId, { deactivateId = it }, label = { Text("Deactivate user ID") }, modifier = Modifier.fillMaxWidth())
    Button(onClick = { actions.deactivate(deactivateId) }, modifier = Modifier.fillMaxWidth()) { Text("Deactivate user") }
}

@Composable
private fun DriverTab(actions: AdminActions) {
    var userId by remember { mutableStateOf("driver_002") }
    var name by remember { mutableStateOf("Driver Two") }
    var pin by remember { mutableStateOf("4444") }
    var ambulance by remember { mutableStateOf("AMB002") }
    var phone by remember { mutableStateOf("9999999999") }
    FormField("Driver user ID", userId) { userId = it }
    FormField("Driver name", name) { name = it }
    FormField("PIN", pin, password = true) { pin = it }
    FormField("Ambulance ID", ambulance) { ambulance = it }
    FormField("Phone", phone) { phone = it }
    Button(onClick = { actions.saveDriver(userId, name, pin, ambulance, phone) }, modifier = Modifier.fillMaxWidth()) {
        Text("Save driver")
    }
}

@Composable
private fun PoliceTab(actions: AdminActions) {
    var userId by remember { mutableStateOf("police_002") }
    var name by remember { mutableStateOf("Junction Officer") }
    var pin by remember { mutableStateOf("5555") }
    var junction by remember { mutableStateOf("JNC002") }
    FormField("Police user ID", userId) { userId = it }
    FormField("Name", name) { name = it }
    FormField("PIN", pin, password = true) { pin = it }
    FormField("Junction ID", junction) { junction = it }
    Button(onClick = { actions.savePolice(userId, name, pin, junction) }, modifier = Modifier.fillMaxWidth()) { Text("Save police") }
}

@Composable
private fun HospitalTab(actions: AdminActions) {
    var userId by remember { mutableStateOf("hospital_002") }
    var desk by remember { mutableStateOf("Metro Desk") }
    var pin by remember { mutableStateOf("6666") }
    var hospitalId by remember { mutableStateOf("HOSP002") }
    var hospitalName by remember { mutableStateOf("Metro Emergency Center") }
    var beds by remember { mutableStateOf("5") }
    var phone by remember { mutableStateOf("08000000000") }
    FormField("Desk user ID", userId) { userId = it }
    FormField("Desk name", desk) { desk = it }
    FormField("PIN", pin, password = true) { pin = it }
    FormField("Hospital ID", hospitalId) { hospitalId = it }
    FormField("Hospital name", hospitalName) { hospitalName = it }
    FormField("Beds", beds) { beds = it }
    FormField("Phone", phone) { phone = it }
    Button(
        onClick = { actions.saveHospital(userId, desk, pin, hospitalId, hospitalName, beds, phone) },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Save hospital") }
}

@Composable
private fun FleetTab(actions: AdminActions) {
    var ambulanceId by remember { mutableStateOf("AMB002") }
    var driverId by remember { mutableStateOf("driver_002") }
    var tag by remember { mutableStateOf("RFID_TAG_002") }
    var junctionId by remember { mutableStateOf("JNC002") }
    var junctionName by remember { mutableStateOf("Ring Road Junction") }
    var lane by remember { mutableStateOf("northbound") }
    Text("Ambulance and RFID")
    FormField("Ambulance ID", ambulanceId) { ambulanceId = it }
    FormField("Driver user ID", driverId) { driverId = it }
    FormField("RFID tag", tag) { tag = it }
    Button(onClick = { actions.saveAmbulance(ambulanceId, driverId, tag) }, modifier = Modifier.fillMaxWidth()) {
        Text("Save ambulance")
    }
    Text("Junction")
    FormField("Junction ID", junctionId) { junctionId = it }
    FormField("Junction name", junctionName) { junctionName = it }
    FormField("Default lane", lane) { lane = it }
    Button(onClick = { actions.saveJunction(junctionId, junctionName, lane) }, modifier = Modifier.fillMaxWidth()) {
        Text("Save junction")
    }
}

@Composable
private fun FormField(label: String, value: String, password: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth()
    )
}
