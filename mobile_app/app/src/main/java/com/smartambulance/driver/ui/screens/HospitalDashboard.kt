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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.smartambulance.driver.ui.components.common.StatusBadge
import com.smartambulance.driver.ui.components.common.ToggleSwitch
import com.smartambulance.driver.ui.theme.*

/**
 * Upgraded Hospital Dashboard with 3-tab layout
 */
@Composable
fun HospitalDashboard(
    user: AppUser,
    hospitalId: String,
    alert: String,
    telemetry: String,
    readiness: String,
    onReady: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Incoming", "Bay Ready", "History")

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
                        .background(HospitalGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Hospital",
                        tint = HospitalGreen,
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
                        text = hospitalId,
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
                    text = "1 INCOMING",
                    backgroundColor = HospitalGreen.copy(alpha = 0.2f),
                    textColor = HospitalGreen
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

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ElevatedCard,
            contentColor = TextPrimary,
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(HospitalGreen)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    selectedContentColor = HospitalGreen,
                    unselectedContentColor = TextPrimary,
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (selectedTab) {
            0 -> IncomingTab()
            1 -> BayReadyTab(onReady = onReady)
            2 -> HistoryTab()
        }
    }
}

@Composable
fun IncomingTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Alert Card
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
                            text = "P1",
                            backgroundColor = PrimaryRed.copy(alpha = 0.2f),
                            textColor = PrimaryRed
                        )
                        Text(
                            text = "AMB001",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(PrimaryRed)
                        )
                        Text(
                            text = "LIVE",
                            color = PrimaryRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "10:45 AM",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = ElevatedCard,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Condition",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Critical",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = ElevatedCard,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Patient Age",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "45",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = ElevatedCard,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "ETA",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "8 min",
                                color = SecondaryAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = ElevatedCard,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Distance",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "2.4 km",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Bay readiness mini-row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("👨‍⚕️", "🛏️", "🩺", "✅").forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = ElevatedCard,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun BayReadyTab(onReady: (String) -> Unit) {
    var responseTeam by remember { mutableStateOf(false) }
    var bedBay by remember { mutableStateOf(false) }
    var doctorOnDuty by remember { mutableStateOf(false) }
    var patientReceived by remember { mutableStateOf(false) }

    val readinessLevel = listOf(responseTeam, bedBay, doctorOnDuty, patientReceived).count { it }
    val isFullyReady = readinessLevel == 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Context Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryRed.copy(alpha = 0.1f),
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
                    StatusBadge(
                        text = "P1",
                        backgroundColor = PrimaryRed.copy(alpha = 0.2f),
                        textColor = PrimaryRed
                    )
                    Text(
                        text = "8 min",
                        color = SecondaryAmber,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Critical Condition",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AMB001 · Driver One · Age 45",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Rows
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ToggleRow(
                icon = "👨‍⚕️",
                label = "Response Team",
                isOn = responseTeam,
                onToggle = { 
                    responseTeam = it
                    onReady("team_alerted")
                }
            )
            ToggleRow(
                icon = "🛏️",
                label = "Bed + Bay",
                isOn = bedBay,
                onToggle = { 
                    bedBay = it
                    onReady("bed_ready")
                }
            )
            ToggleRow(
                icon = "🩺",
                label = "Doctor On-Duty",
                isOn = doctorOnDuty,
                onToggle = { 
                    doctorOnDuty = it
                    onReady("doctor_ready")
                }
            )
            ToggleRow(
                icon = "✅",
                label = "Patient Received",
                isOn = patientReceived,
                onToggle = { 
                    patientReceived = it
                    onReady("completed")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar
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
                    Text(
                        text = "Readiness Progress",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$readinessLevel / 4",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { readinessLevel.toFloat() / 4f },
                    modifier = Modifier.fillMaxWidth(),
                    color = SuccessGreen,
                    trackColor = ElevatedCard
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Success Message
        if (isFullyReady) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SuccessGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓ BAY FULLY READY",
                    color = SuccessGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ToggleRow(
    icon: String,
    label: String,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = CardBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        ToggleSwitch(
            isOn = isOn,
            onToggle = onToggle
        )
    }
}

@Composable
fun HistoryTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Past Trips",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "Critical" to "10:30 AM",
                "Serious" to "09:15 AM",
                "Moderate" to "08:45 AM"
            ).forEach { (severity, time) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = CardBackground,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusBadge(
                                    text = when (severity) {
                                        "Critical" -> "P1"
                                        "Serious" -> "P2"
                                        else -> "P3"
                                    },
                                    backgroundColor = when (severity) {
                                        "Critical" -> PrimaryRed.copy(alpha = 0.2f)
                                        "Serious" -> SecondaryAmber.copy(alpha = 0.2f)
                                        else -> PoliceBlue.copy(alpha = 0.2f)
                                    },
                                    textColor = when (severity) {
                                        "Critical" -> PrimaryRed
                                        "Serious" -> SecondaryAmber
                                        else -> PoliceBlue
                                    }
                                )
                                Text(
                                    text = severity,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = time,
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        StatusBadge(
                            text = "✓ Done",
                            backgroundColor = SuccessGreen.copy(alpha = 0.2f),
                            textColor = SuccessGreen
                        )
                    }
                }
            }
        }
    }
}