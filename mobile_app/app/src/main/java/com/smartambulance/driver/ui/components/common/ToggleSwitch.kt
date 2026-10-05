package com.smartambulance.driver.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartambulance.driver.ui.theme.SuccessGreen

/**
 * iOS-style toggle switch component
 */
@Composable
fun ToggleSwitch(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val trackColor = if (isOn) SuccessGreen else Color(0xFF3D4F6E)
    val thumbColor = if (isOn) Color.White else Color(0xFF6B7A99)

    Box(
        modifier = modifier
            .width(48.dp)
            .height(28.dp)
            .background(
                color = trackColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onToggle(!isOn) },
        contentAlignment = if (isOn) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .padding(2.dp)
                .background(
                    color = thumbColor,
                    shape = CircleShape
                )
        )
    }
}