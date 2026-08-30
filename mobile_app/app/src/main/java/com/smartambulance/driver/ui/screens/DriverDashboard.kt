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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartambulance.driver.data.AppUser
import com.smartambulance.driver.data.Hospital
import com.smartambulance.driver.data.HospitalOption
import com.smartambulance.driver.ui.components.common.AppHeader
import com.smartambulance.driver.ui.components.common.HospitalCard
import com.smartambulance.driver.ui.components.common.SeveritySelector
import com.smartambulance.driver.ui.components.common.StatCard
import com.smartambulance.driver.ui.components.common.StatusBadge
import com.smartambulance.driver.ui.theme.*

/**
 * Upgraded Driver Dashboard with 3-tab layout
 */
@Composable
fun DriverDashboard(
    user: AppUser,
    hospitals: List<HospitalOption>,
    emergencyActive: Boolean,
    selectedSeverity: String,
    selectedHospital: HospitalOption,
    status: String,
    onSeverityChange: (String) -> Unit,
    onHospitalChange: (HospitalOption) -> Unit,
    onStartEmergency: () -> Unit,
    onEndEmergency: () -> Unit,
    onSearchHospital: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mission", "Navigate", "Status")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = CardBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DriverRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Ambulance",
                        tint = DriverRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = user.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = user.ambulanceId ?: "AMB001",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (emergencyActive) {
                    StatusBadge(
                        text = "LIVE",
                        backgroundColor = PrimaryRed.copy(alpha = 0.2f),
                        textColor = PrimaryRed
                    )
                }
                IconButton(onClick = onSearchHospital) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Hospitals",
                        tint = TextPrimary
                    )
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Exit",
                        tint = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ElevatedCard,
            contentColor = Color.White,
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(PrimaryRed)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) PrimaryRed else TextPrimary,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (selectedTab) {
            0 -> MissionTab(
                emergencyActive = emergencyActive,
                selectedSeverity = selectedSeverity,
                selectedHospital = selectedHospital,
                hospitals = hospitals,
                status = status,
                onSeverityChange = onSeverityChange,
                onHospitalChange = onHospitalChange,
                onStartEmergency = onStartEmergency,
                onEndEmergency = onEndEmergency
            )
            1 -> NavigateTab(selectedHospital = selectedHospital, onGetLocation = onSearchHospital)
            2 -> StatusTab(user = user, selectedHospital = selectedHospital)
        }
    }
}

@Composable
fun MissionTab(
    emergencyActive: Boolean,
    selectedSeverity: String,
    selectedHospital: HospitalOption,
    hospitals: List<HospitalOption>,
    status: String,
    onSeverityChange: (String) -> Unit,
    onHospitalChange: (HospitalOption) -> Unit,
    onStartEmergency: () -> Unit,
    onEndEmergency: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (emergencyActive) PrimaryRed.copy(alpha = 0.1f) else CardBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = if (emergencyActive) "EMERGENCY ACTIVE - ${selectedHospital.name}" else "STANDBY - Ready for emergency",
                color = if (emergencyActive) PrimaryRed else TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Severity Selector
        Text(
            text = "Severity Level",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SeveritySelector(
                label = "P1 Critical",
                description = "Life-threatening emergency",
                color = PrimaryRed,
                isSelected = selectedSeverity == "Critical",
                onClick = { onSeverityChange("Critical") }
            )
            SeveritySelector(
                label = "P2 Severe",
                description = "Serious but stable condition",
                color = SecondaryAmber,
                isSelected = selectedSeverity == "Serious",
                onClick = { onSeverityChange("Serious") }
            )
            SeveritySelector(
                label = "P3 Moderate",
                description = "Moderate urgency",
                color = PoliceBlue,
                isSelected = selectedSeverity == "Moderate",
                onClick = { onSeverityChange("Moderate") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hospital Selector
        Text(
            text = "Destination Hospital",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            hospitals.forEach { hospital ->
                HospitalCard(
                    name = hospital.name,
                    distance = hospital.distance,
                    eta = hospital.eta,
                    isAvailable = hospital.beds > 0,
                    isSelected = selectedHospital.id == hospital.id,
                    onClick = { onHospitalChange(hospital) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency Button
        Button(
            onClick = if (emergencyActive) onEndEmergency else onStartEmergency,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = if (emergencyActive) {
                ButtonDefaults.buttonColors(
                    containerColor = CardBackground,
                    contentColor = PrimaryRed
                )
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = Color.White
                )
            }
        ) {
            Text(
                text = if (emergencyActive) "⬛ END EMERGENCY" else "🚨 START EMERGENCY",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun NavigateTab(
    selectedHospital: HospitalOption,
    onGetLocation: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Map placeholder with destination info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = CardBackground,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Map View",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Destination: ${selectedHospital.name}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Distance: ${selectedHospital.distance}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location-based hospital discovery button
        Button(
            onClick = onGetLocation,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HospitalGreen,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "📍 Get Location & Find Nearby Hospitals",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "ETA",
                value = selectedHospital.eta,
                valueColor = SecondaryAmber,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Distance",
                value = selectedHospital.distance,
                valueColor = PoliceBlue,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Speed",
                value = "42 km/h",
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Junction Clearance List
        Text(
            text = "Junction Clearance",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "JNC001" to SuccessGreen,
                "JNC002" to SecondaryAmber,
                "JNC003" to TextDim,
                "JNC004" to TextDim
            ).forEach { (junction, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = CardBackground,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = junction,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    StatusBadge(
                        text = when (color) {
                            SuccessGreen -> "CLEARED"
                            SecondaryAmber -> "APPROACHING"
                            else -> "PENDING"
                        },
                        backgroundColor = color.copy(alpha = 0.2f),
                        textColor = color
                    )
                }
            }
        }
    }
}

@Composable
fun StatusTab(user: AppUser, selectedHospital: HospitalOption) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Trip Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = CardBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Trip Information",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Trip ID",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "TRIP001",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Severity",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Serious",
                        color = SecondaryAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Destination",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = selectedHospital.name,
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Status",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    StatusBadge(
                        text = "ACTIVE",
                        backgroundColor = PrimaryRed.copy(alpha = 0.2f),
                        textColor = PrimaryRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Signals Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = CardBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "System Signals",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "LoRa Signal",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "-72 dBm",
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "RFID Status",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    StatusBadge(
                        text = "CONNECTED",
                        backgroundColor = SuccessGreen.copy(alpha = 0.2f),
                        textColor = SuccessGreen
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "GPS Satellites",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "12",
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hospital Link",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    StatusBadge(
                        text = "ONLINE",
                        backgroundColor = SuccessGreen.copy(alpha = 0.2f),
                        textColor = SuccessGreen
                    )
                }
            }
        }
    }
}