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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.smartambulance.driver.ui.components.common.AppHeader
import com.smartambulance.driver.ui.components.common.StatCard
import com.smartambulance.driver.ui.components.common.StatusBadge
import com.smartambulance.driver.ui.theme.*

/**
 * Upgraded Police Dashboard with 3-tab layout
 */
@Composable
fun PoliceDashboard(
    user: AppUser,
    junctionId: String,
    alert: String,
    telemetry: String,
    ambulanceLocation: Pair<Double?, Double?> = null to null,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Live Map", "Junctions", "Alerts")

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
                        .background(PoliceBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Police",
                        tint = PoliceBlue,
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
                        text = junctionId,
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
                StatusBadge(
                    text = "MONITORING",
                    backgroundColor = PoliceBlue.copy(alpha = 0.2f),
                    textColor = PoliceBlue
                )
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

        // Tab Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Button(
                    onClick = { selectedTab = index },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == index) PoliceBlue else ElevatedCard,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (selectedTab) {
            0 -> LiveMapTab(alert = alert, telemetry = telemetry, ambulanceLocation = ambulanceLocation)
            1 -> JunctionsTab(onRefresh = onRefresh)
            2 -> AlertsTab(alert = alert)
        }
    }
}

@Composable
fun AmbulanceCard(
    ambulanceId: String,
    priority: String,
    eta: String,
    speed: String,
    driver: String,
    rfidStatus: String,
    rssi: String,
    hospital: String,
    location: Pair<Double?, Double?> = null to null
) {
    val priorityColor = when (priority) {
        "P1" -> PrimaryRed
        "P2" -> SecondaryAmber
        else -> PoliceBlue
    }
    
    val rfidColor = if (rfidStatus == "CONNECTED") SuccessGreen else TextDim

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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(
                        text = priority,
                        backgroundColor = priorityColor.copy(alpha = 0.2f),
                        textColor = priorityColor
                    )
                    Text(
                        text = ambulanceId,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = eta,
                        color = SecondaryAmber,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = speed,
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = driver,
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(rfidColor)
                    )
                    Text(
                        text = "RFID",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = rssi,
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = hospital,
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Location",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = if (location.first != null && location.second != null) {
                        "${String.format("%.4f", location.first)}°N, ${String.format("%.4f", location.second)}°E"
                    } else {
                        "Waiting for GPS..."
                    },
                    color = PoliceBlue,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun LiveMapTab(alert: String, telemetry: String, ambulanceLocation: Pair<Double?, Double?> = null to null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Map placeholder
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
            Text(
                text = "Live Map View",
                color = TextMuted,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Ambulances
        Text(
            text = "Active Ambulances",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Ambulance Card 1
            AmbulanceCard(
                ambulanceId = "AMB001",
                priority = "P1",
                eta = "8 min",
                speed = "42 km/h",
                driver = "Driver One",
                rfidStatus = "CONNECTED",
                rssi = "-72 dBm",
                hospital = "City Care Hospital",
                location = ambulanceLocation
            )

            // Ambulance Card 2
            AmbulanceCard(
                ambulanceId = "AMB002",
                priority = "P2",
                eta = "12 min",
                speed = "35 km/h",
                driver = "Driver Two",
                rfidStatus = "CONNECTED",
                rssi = "-65 dBm",
                hospital = "Metro Emergency Center",
                location = null to null
            )
        }
    }
}

@Composable
fun JunctionsTab(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "Cleared",
                value = "2",
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Approaching",
                value = "1",
                valueColor = SecondaryAmber,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Pending",
                value = "1",
                valueColor = TextDim,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Junction Cards
        Text(
            text = "Junction Status",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("JNC001", "Main Road Junction", SuccessGreen),
                Triple("JNC002", "High Street Junction", SecondaryAmber),
                Triple("JNC003", "Central Avenue", TextDim),
                Triple("JNC004", "North Junction", TextDim)
            ).forEach { (id, name, color) ->
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
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = id,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                            StatusBadge(
                                text = when (color) {
                                    SuccessGreen -> "NORMAL"
                                    SecondaryAmber -> "PREEMPTION"
                                    else -> "STANDBY"
                                },
                                backgroundColor = color.copy(alpha = 0.2f),
                                textColor = color
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = name,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Signal",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = when (color) {
                                    SuccessGreen -> "GREEN"
                                    SecondaryAmber -> "RED"
                                    else -> "NORMAL"
                                },
                                color = color,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "LoRa",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "-72 dBm",
                                color = SuccessGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Approach",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "NORTHBOUND",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertsTab(alert: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Priority Alert Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryRed)
                )
                Text(
                    text = "Priority Alert Active",
                    color = PrimaryRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RFID Clearance Log
        Text(
            text = "RFID Clearance Log",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "RFID_TAG_001" to "AMB001",
                "RFID_TAG_002" to "AMB002"
            ).forEach { (tag, ambulance) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = CardBackground,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = tag,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = ambulance,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        StatusBadge(
                            text = "CLEARED",
                            backgroundColor = SuccessGreen.copy(alpha = 0.2f),
                            textColor = SuccessGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preemption Events
        Text(
            text = "Preemption Events",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "JNC001" to "AMB001",
                "JNC002" to "AMB002"
            ).forEach { (junction, ambulance) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = CardBackground,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = junction,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = ambulance,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        StatusBadge(
                            text = "PREEMPTED",
                            backgroundColor = SecondaryAmber.copy(alpha = 0.2f),
                            textColor = SecondaryAmber
                        )
                    }
                }
            }
        }
    }
}