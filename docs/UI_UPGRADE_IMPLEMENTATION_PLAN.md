# Android App UI Upgrade Implementation Plan

## Overview

Upgrade the existing Android Smart Ambulance app with a production-level design system, improved UI, and enhanced features while maintaining all existing Firebase + MQTT functionality.

## Design System Implementation

### Color Palette
```kotlin
// Theme colors
val Background = Color(0xFF070B12)      // Dark background
val CardBackground = Color(0xFF0F1623)  // Card background
val ElevatedCard = Color(0xFF162030)    // Elevated card
val PrimaryRed = Color(0xFFEF233C)       // Emergency Red
val SecondaryAmber = Color(0xFFF59E0B)  // Amber
val SuccessGreen = Color(0xFF10B981)    // Success
val Muted = Color(0xFF1E2940)           // Muted
val Border = Color(0xFF1E2D42)          // Border
val TextPrimary = Color(0xFFF0F4FF)    // Primary text
val TextMuted = Color(0xFF6B7A99)       // Muted text
val TextDim = Color(0xFF3D4F6E)         // Dim text

// Role colors
val DriverRed = Color(0xFFEF233C)
val PoliceBlue = Color(0xFF3B82F6)
val HospitalGreen = Color(0xFF10B981)
val AdminAmber = Color(0xFFF59E0B)
```

### Typography
```kotlin
// Font families
val FontSans = FontFamily(Font(R.font.dm_sans))
val FontMono = FontFamily(Font(R.font.jetbrains_mono))

// Typography scale
val Typography = Typography(
    h1 = TextStyle(fontFamily = FontSans, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    h2 = TextStyle(fontFamily = FontSans, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    h3 = TextStyle(fontFamily = FontSans, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontSans, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontSans, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = FontSans, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = FontMono, fontSize = 14.sp, letterSpacing = 0.5.sp),
    labelMedium = TextStyle(fontFamily = FontMono, fontSize = 12.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = FontMono, fontSize = 10.sp, letterSpacing = 0.5.sp),
)
```

### Animations
```kotlin
// Pulse animation for live indicators
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    color: Color = PrimaryRed
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(16.dp)
            .drawBehind {
                drawCircle(
                    color = color,
                    alpha = alpha,
                    radius = size.width / 2 * scale,
                    center = center
                )
            }
    )
}

// Blink animation for alerts
@Composable
fun BlinkAnimation(
    modifier: Modifier = Modifier,
    color: Color = PrimaryRed
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .drawBehind {
                drawCircle(
                    color = color,
                    alpha = alpha,
                    radius = size.width / 2,
                    center = center
                )
            }
    )
}
```

## Screen-by-Screen Upgrade Plan

### 1. Login Screen

**Current**: Simple login with username/PIN inputs

**New**: Full-screen dark layout with map grid background

**Changes**:
- Add map grid background (repeating linear gradient)
- Add red radial glow from top
- Centered card with animated red logo badge (pulse animation)
- App title "SAPTCS" + subtitle in JetBrains Mono uppercase
- Username + PIN inputs with dark background, red focus border
- Error state with red tinted banner
- Amber "Sign In" button, full width, rounded-xl
- 2x2 grid of quick-login demo account cards (tap to autofill)
  - Each card shows role icon, role name in role color, username/PIN in mono
  - Roles: Driver (red), Police (blue), Hospital (green), Admin (amber)

**Implementation**: Update `LoginScreen.kt` in `ui/screens/`

### 2. Driver Dashboard

**Current**: Single screen with emergency controls

**New**: 3-tab layout (Mission | Navigate | Status)

**Changes**:

**Header**:
- Ambulance icon badge, driver name, vehicle ID
- Live timer badge (red pulsing dot + "LIVE MM:SS" when emergency active)
- Exit button

**Mission Tab**:
- Status banner: idle (dark) or active (red tinted) with destination name
- Severity selector: 3 radio-style cards (P1 Critical/red, P2 Severe/amber, P3 Moderate/blue)
- Hospital selector: list cards with green/red availability dot, name, distance + ETA
- Large action button: "🚨 START EMERGENCY" (red) or "⬛ END EMERGENCY" (dark with red border)

**Navigate Tab**:
- Use existing Leaflet map (or Google Maps) instead of SVG
- 3-column stat row: ETA (amber), Distance (blue), Speed (green)
- Junction clearance list: 4 junctions with status badges

**Status Tab**:
- Trip info card: trip ID, severity, destination, duration, status
- System signals card: LoRa dBm, RFID, GPS satellites, Hospital link

**Implementation**: Create new `DriverDashboard.kt` with tab layout, replace existing `DriverScreen.kt`

### 3. Police Dashboard

**Current**: Single screen with junction alerts

**New**: 3-tab layout (Live Map | Junctions | Alerts)

**Changes**:

**Header**:
- Police badge icon (blue), officer name, junction ID
- "MONITORING" badge (blue pulse)
- Exit button

**Live Map Tab**:
- Use existing Leaflet map with blue accent
- Active ambulances list with severity, ambulance ID, driver name, ETA, speed, RFID status, LoRa signal

**Junctions Tab**:
- 3-column summary row: Cleared (green), Approaching (amber), Pending (gray)
- Junction cards with ID, intersection name, signal dot, status badge, data grid
- Auto-update every 3 seconds

**Alerts Tab**:
- "Priority Alert Active" banner with scan-line animation
- RFID Clearance Log table
- Preemption Events list

**Implementation**: Create new `PoliceDashboard.kt` with tab layout, replace existing `PoliceScreen.kt`

### 4. Hospital Dashboard

**Current**: Single screen with hospital alerts

**New**: 3-tab layout (Incoming | Bay Ready | History)

**Changes**:

**Header**:
- Hospital icon (green), hospital name, HOS ID
- "N INCOMING" counter badge (green pulse)
- Exit button

**Incoming Tab**:
- Alert cards (tappable → goes to Bay Ready tab)
- Severity badge, ambulance ID, blinking live dot, timestamp
- 2x2 grid: Condition, Patient Age, ETA, Distance
- Bay readiness mini-row: 4 emoji icons (👨‍⚕️🛏️🩺✅)
- "Tap to manage →" label

**Bay Ready Tab**:
- Context card at top with severity-colored border
- 4 toggle rows: Response Team, Bed+Bay, Doctor On-Duty, Patient Received
- iOS-style toggle switches (green when on)
- Progress bar: "X / 4" readiness with green fill
- "BAY FULLY READY" success message

**History Tab**:
- List of past trips with severity, condition, timestamp, trip ID, ambulance ID, duration

**Implementation**: Create new `HospitalDashboard.kt` with tab layout, replace existing `HospitalScreen.kt`

### 5. Admin Dashboard

**Current**: Single screen with admin controls

**New**: Horizontal scrollable nav + content area

**Changes**:

**Header**:
- Gear icon (amber), admin name, "ADM-001 · SAPTCS Admin"
- Exit button

**Nav bar (scrollable)**:
- Overview | Users | Vehicles | Hospitals | Junctions
- Active item gets amber tinted pill with border

**Overview Section**:
- 2x2 stat card grid: Active Trips (red), Online Users (blue), Ambulances X/Y (amber), Junctions X Clear (green)
- System Health card: 5 services (Firebase DB, LoRa, GPS, RFID, Hospital API) with green pulse dot, uptime %
- Recent Events log: colored dot per type, message text, timestamp

**Users Section**:
- "+ Add User" button (amber outlined)
- User cards: role pill, name, username in mono, vehicle if driver
- Toggle switch to activate/deactivate user

**Vehicles Section**:
- "+ Register" button
- Ambulance cards: ID in mono, reg plate, status badge
- 2x2 data grid: Driver, RFID Tag, Last Seen, Status

**Hospitals Section**:
- "+ Add" button
- Hospital cards: name, ID in mono, AVAILABLE/FULL badge, distance + ETA

**Junctions Section**:
- "+ Add" button
- Junction cards: ID in mono, intersection name, signal dot
- 3-col grid: Signal color, Status, ESP32 online

**Implementation**: Create new `AdminDashboard.kt` with horizontal nav, replace existing `AdminScreen.kt`

## New Features to Add

### 1. Bay Ready System (Hospital)
- Toggle switches for: Response Team, Bed+Bay, Doctor On-Duty, Patient Received
- Progress bar showing readiness (X/4)
- "BAY FULLY READY" success message
- Sync with Firebase Realtime Database

### 2. System Health Monitoring (Admin)
- Monitor 5 services: Firebase DB, LoRa, GPS, RFID, Hospital API
- Display uptime percentage
- Green pulse dot for online status
- Recent events log with colored indicators

### 3. Live Timer (Driver)
- Real-time emergency duration timer
- Red pulsing dot + "LIVE MM:SS" badge
- Auto-start when emergency activated
- Auto-stop when emergency completed

### 4. Junction Auto-Update (Police)
- Auto-refresh junction data every 3 seconds
- Live signal status updates
- Real-time approach direction changes

### 5. Enhanced Statistics
- ETA, Distance, Speed overlays
- Junction clearance status
- System signal strengths (LoRa dBm, GPS satellites, etc.)

## Technical Implementation Strategy

### Approach: Incremental Upgrade with Rebuild

**Why Rebuild?**
- Complete UI overhaul requires significant changes
- Tab layouts, navigation patterns, and component structure need redesign
- New features require new data models and state management
- Cleaner to rebuild with new architecture than patch existing code

**What's Preserved:**
- All Firebase + MQTT integration
- All business logic (emergency activation, hospital discovery, etc.)
- All data models (AppUser, HospitalOption, etc.)
- All services (MqttManager, HospitalDiscoveryService, NavigationService)

**What's Rebuilt:**
- UI components and screens
- Navigation patterns
- Design system implementation
- New feature UI (Bay Ready, System Health, etc.)

### File Structure Changes

**New Files**:
```
mobile_app/app/src/main/java/com/smartambulance/driver/
├── ui/
│   ├── theme/
│   │   ├── Color.kt (new color palette)
│   │   ├── Type.kt (typography scale)
│   │   ├── Theme.kt (theme composable)
│   │   └── Animation.kt (pulse, blink animations)
│   ├── components/
│   │   ├── common/
│   │   │   ├── AppHeader.kt
│   │   │   ├── QuickLoginCard.kt
│   │   │   ├── SeveritySelector.kt
│   │   │   ├── HospitalCard.kt
│   │   │   ├── StatusBadge.kt
│   │   │   ├── ToggleSwitch.kt
│   │   │   └── StatCard.kt
│   │   └── screens/
│   │       ├── LoginScreen.kt (upgraded)
│   │       ├── DriverDashboard.kt (new)
│   │       ├── PoliceDashboard.kt (new)
│   │       ├── HospitalDashboard.kt (new)
│   │       └── AdminDashboard.kt (new)
```

**Preserved Files**:
```
mobile_app/app/src/main/java/com/smartambulance/driver/
├── MainActivity.kt (updated for new screens)
├── data/
│   ├── AppUser.kt (preserved)
│   ├── HospitalOption.kt (preserved)
│   ├── DemoRepository.kt (preserved)
│   └── FirebasePaths.kt (preserved)
├── mqtt/
│   ├── MqttManager.kt (preserved)
│   └── MqttTopics.kt (preserved)
└── services/
    ├── HospitalDiscoveryService.kt (preserved)
    └── NavigationService.kt (preserved)
```

### Data Model Additions

**New data models for Bay Ready system**:
```kotlin
data class BayReadiness(
    val responseTeam: Boolean = false,
    val bedBay: Boolean = false,
    val doctorOnDuty: Boolean = false,
    val patientReceived: Boolean = false
) {
    val readinessLevel: Int
        get() = listOf(responseTeam, bedBay, doctorOnDuty, patientReceived)
            .count { it }

    val isFullyReady: Boolean
        get() = readinessLevel == 4
}

data class SystemHealth(
    val firebaseDb: ServiceStatus = ServiceStatus.Online,
    val loRa: ServiceStatus = ServiceStatus.Online,
    val gps: ServiceStatus = ServiceStatus.Online,
    val rfid: ServiceStatus = ServiceStatus.Online,
    val hospitalApi: ServiceStatus = ServiceStatus.Online
)

enum class ServiceStatus {
    Online, Degraded, Offline
}
```

### Firebase Schema Additions

**Bay Readiness**:
```
hospital_alerts/{hospitalId}/{tripId}/bayReadiness/
  responseTeam: boolean
  bedBay: boolean
  doctorOnDuty: boolean
  patientReceived: boolean
```

**System Health** (optional, for admin monitoring):
```
system_health/
  firebaseDb/
    status: "online"
    uptime: 99.5
    lastCheck: timestamp
  loRa/
    status: "online"
    uptime: 98.2
    lastCheck: timestamp
  ...
```

## Implementation Order

### Phase 1: Design System Foundation
1. Add color palette (`Color.kt`)
2. Add typography scale (`Type.kt`)
3. Create theme composable (`Theme.kt`)
4. Add animations (`Animation.kt`)
5. Add font files (DM Sans, JetBrains Mono)

### Phase 2: Common Components
1. Create reusable UI components:
   - AppHeader
   - QuickLoginCard
   - SeveritySelector
   - HospitalCard
   - StatusBadge
   - ToggleSwitch
   - StatCard

### Phase 3: Login Screen Upgrade
1. Update LoginScreen with new design
2. Add map grid background
3. Add quick-login cards
4. Test with Firebase authentication

### Phase 4: Driver Dashboard
1. Create DriverDashboard with tab layout
2. Implement Mission tab
3. Implement Navigate tab (use existing map)
4. Implement Status tab
5. Add live timer
6. Test with Firebase + MQTT

### Phase 5: Police Dashboard
1. Create PoliceDashboard with tab layout
2. Implement Live Map tab
3. Implement Junctions tab with auto-update
4. Implement Alerts tab
5. Test with Firebase + MQTT

### Phase 6: Hospital Dashboard
1. Create HospitalDashboard with tab layout
2. Implement Incoming tab
3. Implement Bay Ready tab with toggles
4. Implement History tab
5. Add Bay Readiness to Firebase schema
6. Test with Firebase

### Phase 7: Admin Dashboard
1. Create AdminDashboard with horizontal nav
2. Implement Overview section
3. Implement Users section
4. Implement Vehicles section
5. Implement Hospitals section
6. Implement Junctions section
7. Add System Health monitoring
8. Test with Firebase

### Phase 8: Integration & Testing
1. Update MainActivity to use new screens
2. Test all role logins
3. Test emergency flow end-to-end
4. Test hospital discovery
5. Test MQTT integration
6. Test Firebase synchronization
7. Fix any bugs

### Phase 9: Polish & Deployment
1. Refine animations
2. Optimize performance
3. Add error handling
4. Update documentation
5. Build and test APK
6. Push to GitHub

## Estimated Timeline

- Phase 1: Design System Foundation - 2-3 hours
- Phase 2: Common Components - 3-4 hours
- Phase 3: Login Screen - 2-3 hours
- Phase 4: Driver Dashboard - 4-5 hours
- Phase 5: Police Dashboard - 4-5 hours
- Phase 6: Hospital Dashboard - 4-5 hours
- Phase 7: Admin Dashboard - 5-6 hours
- Phase 8: Integration & Testing - 3-4 hours
- Phase 9: Polish & Deployment - 2-3 hours

**Total Estimated Time**: 29-38 hours

## Dependencies to Add

```kotlin
// Add to build.gradle.kts
implementation("androidx.compose.material:material-icons-extended")
implementation("androidx.compose.animation:animation")
// Fonts (download and add to res/font/)
```

## Fonts to Download

1. **DM Sans** (for UI text)
   - Download from: https://fonts.google.com/specimen/DM+Sans
   - Add to: `app/src/main/res/font/dm_sans_regular.ttf`
   - Add to: `app/src/main/res/font/dm_sans_medium.ttf`
   - Add to: `app/src/main/res/font/dm_sans_bold.ttf`

2. **JetBrains Mono** (for data/labels/status)
   - Download from: https://fonts.google.com/specimen/JetBrains+Mono
   - Add to: `app/src/main/res/font/jetbrains_mono_regular.ttf`
   - Add to: `app/src/main/res/font/jetbrains_mono_medium.ttf`

## Risk Mitigation

**Backup Strategy**:
- Create a new branch before starting: `git checkout -b ui-upgrade`
- Commit after each phase
- If issues arise, can rollback to previous commit

**Incremental Testing**:
- Test each screen individually after implementation
- Test Firebase + MQTT integration after each phase
- Don't wait until the end to test

**Fallback Plan**:
- Keep old screens in a separate package
- If new screens have issues, can temporarily revert to old screens
- Gradual migration allows for quick rollbacks

## Success Criteria

✅ All screens implement new design system
✅ All new features (Bay Ready, System Health, etc.) work
✅ Firebase + MQTT integration maintained
✅ All role logins work correctly
✅ Emergency flow works end-to-end
✅ Hospital discovery works
✅ Dashboard displays live data
✅ Animations perform smoothly
✅ APK builds successfully
✅ No regression in existing functionality

## Questions for Approval

1. **Font Files**: Do you want me to include font file download instructions, or will you add them manually?
2. **Map Implementation**: Should I use the existing Google Maps/Leaflet integration, or keep it simple without maps for now?
3. **Animation Complexity**: Should I implement all animations (pulse, blink, scan-line), or keep them simple?
4. **Bay Ready Sync**: Should Bay Ready status sync to Firebase immediately, or only when "BAY FULLY READY"?
5. **System Health**: Should System Health be actual monitoring (ping services), or simulated uptime percentages?

## Next Steps

Once you approve this plan:
1. Create new Git branch for UI upgrade
2. Start Phase 1: Design System Foundation
3. Implement incrementally, testing after each phase
4. Commit changes after each phase
5. Final integration and testing
6. Merge to main branch when complete

Please review this plan and confirm if you'd like me to proceed, or if you'd like any changes to the approach.