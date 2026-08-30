package com.smartambulance.driver.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Ambulance
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Police
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartambulance.driver.ui.theme.*

/**
 * Quick login card for demo accounts
 */
@Composable
fun QuickLoginCard(
    role: String,
    username: String,
    pin: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (roleColor, icon) = when (role) {
        "Driver" -> DriverRed to Icons.Default.Ambulance
        "Police" -> PoliceBlue to Icons.Default.Police
        "Hospital" -> HospitalGreen to Icons.Default.LocalHospital
        "Admin" -> AdminAmber to Icons.Default.Shield
        else -> TextMuted to Icons.Default.Shield
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CardBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Border,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = role,
            tint = roleColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = role,
            color = roleColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = username,
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = pin,
            color = TextDim,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}