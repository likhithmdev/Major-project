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
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartambulance.driver.ui.components.common.QuickLoginCard
import com.smartambulance.driver.ui.theme.*

/**
 * Upgraded Login Screen with new design system
 */
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
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Background,
                        CardBackground
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Logo with pulse animation
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(PrimaryRed.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PrimaryRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = "SAPTCS",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App title
        Text(
            text = "SAPTCS",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "SMART AMBULANCE PRIORITY TRAFFIC CONTROL SYSTEM",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Login card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = CardBackground,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Text(
                text = "Sign In",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User ID input
            OutlinedTextField(
                value = userId,
                onValueChange = onUserId,
                label = { Text("User ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryRed,
                    unfocusedBorderColor = Border,
                    focusedLabelColor = PrimaryRed,
                    unfocusedLabelColor = TextMuted,
                    cursorColor = PrimaryRed
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // PIN input
            OutlinedTextField(
                value = pin,
                onValueChange = onPin,
                label = { Text("PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryRed,
                    unfocusedBorderColor = Border,
                    focusedLabelColor = PrimaryRed,
                    unfocusedLabelColor = TextMuted,
                    cursorColor = PrimaryRed
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            if (message.isNotEmpty() && message != "Use a pre-registered Firebase account.") {
                Text(
                    text = message,
                    color = PrimaryRed,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = PrimaryRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Sign in button
            Button(
                onClick = onLogin,
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryAmber,
                    contentColor = Background
                )
            ) {
                Text(
                    text = if (loading) "Authenticating..." else "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Load demo accounts button
            TextButton(
                onClick = onSeed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Load demo accounts",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick login cards
        Text(
            text = "Quick Demo Accounts",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickLoginCard(
                role = "Driver",
                username = "driver_001",
                pin = "1111",
                onClick = { onFill("driver_001", "1111") },
                modifier = Modifier.weight(1f)
            )
            QuickLoginCard(
                role = "Police",
                username = "police_001",
                pin = "2222",
                onClick = { onFill("police_001", "2222") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickLoginCard(
                role = "Hospital",
                username = "hospital_001",
                pin = "3333",
                onClick = { onFill("hospital_001", "3333") },
                modifier = Modifier.weight(1f)
            )
            QuickLoginCard(
                role = "Admin",
                username = "admin_001",
                pin = "0000",
                onClick = { onFill("admin_001", "0000") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}