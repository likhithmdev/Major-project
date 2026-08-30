package com.smartambulance.driver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.smartambulance.driver.ui.components.common.StatCard
import com.smartambulance.driver.ui.components.common.StatusBadge
import com.smartambulance.driver.ui.components.common.ToggleSwitch
import com.smartambulance.driver.ui.theme.*

/**
 * Upgraded Admin Dashboard with horizontal scrollable navigation
 */
@Composable
fun AdminDashboard(
    user: AppUser,
    message: String,
    records: String,
    actions: Any,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf("Overview", "Users", "Vehicles", "Hospitals", "Junctions")

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
                        .background(AdminAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Admin",
                        tint = AdminAmber,
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
                        text = "ADM-001 · SAPTCS Admin",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Exit",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Scrollable Nav
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sections.forEachIndexed { index, section ->
                Box(
                    modifier = Modifier
                        .background(
                            color = if (selectedSection == index) AdminAmber.copy(alpha = 0.2f) else CardBackground,
                            shape = RoundedCornerShape(20.dp)
                        )

                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { selectedSection = index }
                ) {
                    Text(
                        text = section,
                        color = if (selectedSection == index) AdminAmber else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (selectedSection == index) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Content
        when (selectedSection) {
            0 -> OverviewSection(records = records)
            1 -> UsersSection()
            2 -> VehiclesSection()
            3 -> HospitalsSection()
            4 -> JunctionsSection()
        }
    }
}

@Composable
fun OverviewSection(records: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 2x2 Stat Card Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "Active Trips",
                value = "3",
                valueColor = PrimaryRed,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Online Users",
                value = "8",
                valueColor = PoliceBlue,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "Ambulances",
                value = "4/5",
                valueColor = AdminAmber,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Junctions Clear",
                value = "3/4",
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Health Card
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
                    text = "System Health",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Firebase DB" to "99.5%",
                        "LoRa" to "98.2%",
                        "GPS" to "97.8%",
                        "RFID" to "99.9%",
                        "Hospital API" to "96.5%"
                    ).forEach { (service, uptime) ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Text(
                                    text = service,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = uptime,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                StatusBadge(
                                    text = "online",
                                    backgroundColor = SuccessGreen.copy(alpha = 0.2f),
                                    textColor = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Events Log
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
                    text = "Recent Events",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("Emergency", "Trip TRIP001 started by driver_001", PrimaryRed),
                        Triple("RFID", "Junction JNC001 cleared by AMB001", SuccessGreen),
                        Triple("Hospital", "Bay readiness updated at HOSP001", HospitalGreen),
                        Triple("Auth", "User driver_002 logged in", PoliceBlue)
                    ).forEach { (type, message, color) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "2 min ago",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsersSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Users",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .background(
                        color = AdminAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { }
            ) {
                Text(
                    text = "+ Add User",
                    color = AdminAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("Driver", "driver_001", DriverRed),
                Triple("Police", "police_001", PoliceBlue),
                Triple("Hospital", "hospital_001", HospitalGreen),
                Triple("Admin", "admin_001", AdminAmber)
            ).forEach { (role, username, color) ->
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatusBadge(
                                text = role,
                                backgroundColor = color.copy(alpha = 0.2f),
                                textColor = color
                            )
                            Column {
                                Text(
                                    text = when (role) {
                                        "Driver" -> "Driver One"
                                        "Police" -> "Traffic Police"
                                        "Hospital" -> "City Care Desk"
                                        else -> "System Admin"
                                    },
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = username,
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        ToggleSwitch(
                            isOn = true,
                            onToggle = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehiclesSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vehicles",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .background(
                        color = AdminAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { }
            ) {
                Text(
                    text = "+ Register",
                    color = AdminAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "AMB001" to "KA-01-AB-1234",
                "AMB002" to "KA-01-CD-5678"
            ).forEach { (id, plate) ->
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
                                text = id,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            StatusBadge(
                                text = "ACTIVE",
                                backgroundColor = SuccessGreen.copy(alpha = 0.2f),
                                textColor = SuccessGreen
                            )
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
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Driver",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "driver_001",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
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
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "RFID Tag",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "RFID_TAG_001",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
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
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Last Seen",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "2 min ago",
                                        color = TextPrimary,
                                        fontSize = 11.sp
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
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Status",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "On Route",
                                        color = SuccessGreen,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalsSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hospitals",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .background(
                        color = AdminAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { }
            ) {
                Text(
                    text = "+ Add",
                    color = AdminAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("City Care Hospital", "HOSP001", 8),
                Triple("Metro Emergency Center", "HOSP002", 3),
                Triple("St. Mark Trauma Unit", "HOSP003", 11)
            ).forEach { (name, id, beds) ->
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
                            Text(
                                text = name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = id,
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${beds} beds",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            StatusBadge(
                                text = if (beds > 0) "AVAILABLE" else "FULL",
                                backgroundColor = if (beds > 0) SuccessGreen.copy(alpha = 0.2f) else PrimaryRed.copy(alpha = 0.2f),
                                textColor = if (beds > 0) SuccessGreen else PrimaryRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JunctionsSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Junctions",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .background(
                        color = AdminAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { }
            ) {
                Text(
                    text = "+ Add",
                    color = AdminAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("JNC001", "Main Road Junction", SuccessGreen),
                Triple("JNC002", "High Street Junction", SecondaryAmber),
                Triple("JNC003", "Central Avenue", SuccessGreen),
                Triple("JNC004", "North Junction", SuccessGreen)
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
                                    .padding(8.dp)
                            ) {
                                Column {
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
                                        fontSize = 11.sp,
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
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Status",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "Online",
                                        color = SuccessGreen,
                                        fontSize = 11.sp
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
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "ESP32",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "Connected",
                                        color = SuccessGreen,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}