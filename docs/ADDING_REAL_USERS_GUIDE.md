# Adding Real Users Guide

## Current Authentication Approach

The app currently uses **Firebase Realtime Database** for authentication:
- User credentials (userId, PIN) are stored in Firebase Realtime Database
- Login validates against the database
- No Firebase Authentication (Auth) is used

**Security Note**: This is suitable for demo/development but not recommended for production because:
- PINs are stored in plain text in the database
- No token-based authentication
- No session management
- No password hashing
- Vulnerable to database breaches

## Option 1: Continue Using Database-Based Auth (Simple)

The app already has registration methods in `DemoRepository.kt`:

### Register a Driver

The `registerDriver` method is available but needs to be exposed in the UI. Currently, only the admin can add users through the admin screen.

**Data Structure**:
```json
{
  "users": {
    "driver_002": {
      "userId": "driver_002",
      "name": "John Smith",
      "pin": "1234",
      "role": "ambulance_driver",
      "ambulanceId": "AMB002",
      "active": true,
      "updatedAt": 1234567890
    }
  },
  "drivers": {
    "driver_002": {
      "driverId": "driver_002",
      "userId": "driver_002",
      "name": "John Smith",
      "phone": "9876543210",
      "assignedAmbulanceId": "AMB002",
      "active": true,
      "updatedAt": 1234567890
    }
  }
}
```

**Method**:
```kotlin
repository.registerDriver(
    userId = "driver_002",
    name = "John Smith",
    pin = "1234",
    ambulanceId = "AMB002",
    phone = "9876543210",
    onResult = { success, message ->
        // Handle result
    }
)
```

### Register a Police Officer

```kotlin
repository.registerPolice(
    userId = "police_002",
    name = "Officer Jane",
    pin = "2345",
    junctionId = "JNC002",
    onResult = { success, message ->
        // Handle result
    }
)
```

### Register a Hospital User

```kotlin
repository.registerHospitalUser(
    userId = "hospital_002",
    name = "Emergency Desk",
    pin = "3456",
    hospitalId = "HOSP002",
    onResult = { success, message ->
        // Handle result
    }
)
```

### Add Registration UI

Currently, only the admin can register users. To add a self-registration feature:

1. **Add a Registration Screen** in `ui/screens/RoleScreens.kt`:
```kotlin
@Composable
fun RegistrationScreen(
    onRegister: (userId: String, name: String, pin: String, role: String, details: Map<String, String>) -> Unit,
    onBack: () -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ambulance_driver") }
    var ambulanceId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var junctionId by remember { mutableStateOf("") }
    var hospitalId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register New User", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("User ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Role:", style = MaterialTheme.typography.body1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ambulance_driver", "police", "hospital").forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { selectedRole = role },
                    label = { Text(role.replace("_", " ").capitalize()) }
                )
            }
        }

        when (selectedRole) {
            "ambulance_driver" -> {
                OutlinedTextField(
                    value = ambulanceId,
                    onValueChange = { ambulanceId = it },
                    label = { Text("Ambulance ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "police" -> {
                OutlinedTextField(
                    value = junctionId,
                    onValueChange = { junctionId = it },
                    label = { Text("Assigned Junction ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "hospital" -> {
                OutlinedTextField(
                    value = hospitalId,
                    onValueChange = { hospitalId = it },
                    label = { Text("Hospital ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val details = mutableMapOf<String, String>()
                when (selectedRole) {
                    "ambulance_driver" -> {
                        details["ambulanceId"] = ambulanceId
                        details["phone"] = phone
                    }
                    "police" -> {
                        details["junctionId"] = junctionId
                    }
                    "hospital" -> {
                        details["hospitalId"] = hospitalId
                    }
                }
                onRegister(userId, name, pin, selectedRole, details)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Login")
        }
    }
}
```

2. **Add the registration handler in MainActivity.kt**:
```kotlin
fun registerUser(userId: String, name: String, pin: String, role: String, details: Map<String, String>) {
    loading = true
    when (role) {
        "ambulance_driver" -> {
            repository.registerDriver(
                userId = userId,
                name = name,
                pin = pin,
                ambulanceId = details["ambulanceId"] ?: "",
                phone = details["phone"] ?: ""
            ) { success, message ->
                loading = false
                message = if (success) "Registration successful" else "Registration failed: $message"
            }
        }
        "police" -> {
            repository.registerPolice(
                userId = userId,
                name = name,
                pin = pin,
                junctionId = details["junctionId"] ?: ""
            ) { success, message ->
                loading = false
                message = if (success) "Registration successful" else "Registration failed: $message"
            }
        }
        "hospital" -> {
            repository.registerHospitalUser(
                userId = userId,
                name = name,
                pin = pin,
                hospitalId = details["hospitalId"] ?: ""
            ) { success, message ->
                loading = false
                message = if (success) "Registration successful" else "Registration failed: $message"
            }
        }
    }
}
```

3. **Add a "Register" button to LoginScreen**:
```kotlin
OutlinedButton(
    onClick = { showRegistration = true },
    modifier = Modifier.fillMaxWidth()
) {
    Text("Register New User")
}
```

## Option 2: Use Firebase Authentication (Recommended for Production)

For a production system, use Firebase Authentication for identity management and Realtime Database for profile data.

### Benefits of Firebase Authentication

✅ **Secure**: Uses OAuth tokens, not plain text credentials
✅ **Session Management**: Automatic token refresh
✅ **Multiple Auth Methods**: Email/password, phone, Google, Apple, etc.
✅ **Built-in Security**: Password hashing, rate limiting, account protection
✅ **User Management**: Built-in user CRUD in Firebase Console
✅ **Audit Logging**: Authentication events are logged

### Implementation Steps

#### Step 1: Add Firebase Authentication Dependency

In `mobile_app/app/build.gradle.kts`:
```kotlin
dependencies {
    // Already present in your project
    implementation("com.google.firebase:firebase-auth:22.3.1")
}
```

#### Step 2: Enable Authentication in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: `smart-ambulance-36f9d`
3. Go to **Build** → **Authentication**
4. Click **Get Started**
5. Enable **Email/Password** sign-in method
6. Optionally enable **Phone** authentication

#### Step 3: Create AuthService

Create `mobile_app/app/src/main/java/com/smartambulance/driver/services/AuthService.kt`:
```kotlin
package com.smartambulance.driver.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

data class UserProfile(
    val userId: String,
    val name: String,
    val role: String,
    val ambulanceId: String? = null,
    val assignedJunctionId: String? = null,
    val hospitalId: String? = null,
    val phone: String? = null,
    val active: Boolean = true
)

class AuthService {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        role: String,
        additionalData: Map<String, String> = emptyMap(),
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                user?.updateProfile(profileUpdates)
                    ?.addOnSuccessListener {
                        // Save user profile to Realtime Database
                        val userProfile = UserProfile(
                            userId = user.uid,
                            name = name,
                            role = role,
                            ambulanceId = additionalData["ambulanceId"],
                            assignedJunctionId = additionalData["junctionId"],
                            hospitalId = additionalData["hospitalId"],
                            phone = additionalData["phone"]
                        )

                        database.child("users").child(user.uid).setValue(userProfile)
                            .addOnSuccessListener {
                                onComplete(true, "Registration successful")
                            }
                            .addOnFailureListener { error ->
                                onComplete(false, "Failed to save profile: ${error.message}")
                            }
                    }
                    ?.addOnFailureListener { error ->
                        onComplete(false, "Failed to update profile: ${error.message}")
                    }
            }
            .addOnFailureListener { error ->
                onComplete(false, "Registration failed: ${error.message}")
            }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onComplete: (FirebaseUser?, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                onComplete(result.user, null)
            }
            .addOnFailureListener { error ->
                onComplete(null, error.message)
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun sendPasswordResetEmail(
        email: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onComplete(true, "Password reset email sent")
            }
            .addOnFailureListener { error ->
                onComplete(false, error.message)
            }
    }

    fun getUserProfile(
        userId: String,
        onComplete: (UserProfile?, String?) -> Unit
    ) {
        database.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val profile = UserProfile(
                        userId = snapshot.child("userId").getValue(String::class.java) ?: userId,
                        name = snapshot.child("name").getValue(String::class.java) ?: "",
                        role = snapshot.child("role").getValue(String::class.java) ?: "",
                        ambulanceId = snapshot.child("ambulanceId").getValue(String::class.java),
                        assignedJunctionId = snapshot.child("assignedJunctionId").getValue(String::class.java),
                        hospitalId = snapshot.child("hospitalId").getValue(String::class.java),
                        phone = snapshot.child("phone").getValue(String::class.java),
                        active = snapshot.child("active").getValue(Boolean::class.java) ?: true
                    )
                    onComplete(profile, null)
                } else {
                    onComplete(null, "User profile not found")
                }
            }
            .addOnFailureListener { error ->
                onComplete(null, error.message)
            }
    }
}
```

#### Step 4: Update Firebase Rules for Auth

Update `docs/FIREBASE_SECURITY_RULES.md` with rules that use Firebase Auth UID:
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null && (auth.uid == $uid || root.child('users').child(auth.uid).child('role').val() == 'admin')",
        ".write": "auth != null && (auth.uid == $uid || root.child('users').child(auth.uid).child('role').val() == 'admin')"
      }
    },
    "ambulances": {
      "$ambulanceId": {
        ".read": "auth != null",
        ".write": "auth != null && (root.child('users').child(auth.uid).child('role').val() == 'admin' || root.child('users').child(auth.uid).child('ambulanceId').val() == $ambulanceId)"
      }
    },
    "emergency_trips": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "hospital_alerts": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "police_alerts": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

#### Step 5: Update MainActivity to Use Auth

Replace the login logic in `MainActivity.kt`:
```kotlin
private val authService = AuthService()

fun loginWithAuth(email: String, password: String) {
    loading = true
    authService.loginWithEmail(email, password) { user, error ->
        loading = false
        if (user != null) {
            // Fetch user profile from Realtime Database
            authService.getUserProfile(user.uid) { profile, profileError ->
                if (profile != null) {
                    current = AppUser(
                        userId = profile.userId,
                        name = profile.name,
                        pin = "", // Not used with Auth
                        role = profile.role,
                        ambulanceId = profile.ambulanceId,
                        assignedJunctionId = profile.assignedJunctionId,
                        hospitalId = profile.hospitalId
                    )
                    message = "Login successful"
                } else {
                    message = "Failed to load profile: $profileError"
                }
            }
        } else {
            message = "Login failed: $error"
        }
    }
}
```

#### Step 6: Add Auth Registration UI

Create a registration screen that uses email/password:
```kotlin
@Composable
fun AuthRegistrationScreen(
    onRegister: (email: String, password: String, name: String, role: String, details: Map<String, String>) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ambulance_driver") }
    // ... rest similar to previous registration screen

    Column {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        // ... rest of the form
    }
}
```

## Comparison: Database vs Firebase Auth

| Feature | Database-Based (Current) | Firebase Authentication |
|---------|-------------------------|------------------------|
| Security | ⚠️ PIN stored in plain text | ✅ Secure token-based auth |
| Session Management | ❌ None | ✅ Automatic token refresh |
| Password Reset | ❌ Manual | ✅ Built-in email reset |
| Multiple Devices | ❌ Manual sync | ✅ Automatic sync |
| User Management | ❌ Manual database edits | ✅ Firebase Console UI |
| Production Ready | ❌ Not recommended | ✅ Recommended |
| Implementation Effort | ✅ Already implemented | ⚠️ Requires migration |

## Recommendation

**For Production**: Use Firebase Authentication (Option 2)

**For Quick Testing**: Use database-based registration (Option 1) with demo rules

## Migration Path

If you want to migrate from database-based to Firebase Auth:

1. Keep both systems temporarily
2. Add Firebase Auth registration
3. Migrate existing users to Firebase Auth
4. Update login to use Auth
5. Remove old PIN-based login
6. Update Firebase rules to use `auth.uid`

## Quick Start: Add Real User Now

**Fastest way to add a real user right now**:

1. Use the Admin screen (login as `admin_001` / `0000`)
2. Use the existing registration functions in the admin panel
3. Or manually add user data in Firebase Console:
   - Go to Realtime Database → Data
   - Navigate to `users`
   - Add a new child with the user ID
   - Fill in: `userId`, `name`, `pin`, `role`, `ambulanceId` (if driver), `active: true`

**Example manual Firebase Console entry**:
```json
{
  "driver_002": {
    "userId": "driver_002",
    "name": "John Smith",
    "pin": "1234",
    "role": "ambulance_driver",
    "ambulanceId": "AMB002",
    "active": true
  }
}
```

Then login with: `driver_002` / `1234`