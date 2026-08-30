# UI Upgrade Testing Checklist

## Pre-Testing Setup

### 1. Install Updated APK
- APK Location: `mobile_app/app/build/outputs/apk/debug/app-debug.apk`
- Uninstall previous version (optional)
- Install new APK on Android device/emulator

### 2. Firebase Configuration
- Ensure Firebase Console has unauthenticated rules (`.read: true, .write: true`)
- Verify project: `smart-ambulance-36f9d`
- Check Firebase Realtime Database is accessible

### 3. Demo Data
- Open the app
- Click **"Load demo accounts"** on login screen
- Wait for "Firebase demo data ready" message

## Login Screen Testing

### ✅ Visual Checks
- [ ] Dark gradient background is displayed
- [ ] Logo with red circle and emergency icon is visible
- [ ] "SAPTCS" title appears in uppercase
- [ ] Subtitle appears in monospace font
- [ ] User ID and PIN inputs have dark background
- [ ] Red focus border appears when tapping inputs
- [ ] Amber "Sign In" button is visible
- [ ] 2x2 grid of quick-login cards appears (Driver, Police, Hospital, Admin)
- [ ] Each card shows role icon, role name, username, and PIN

### ✅ Functionality Tests
- [ ] Tap Driver card → auto-fills `driver_001` / `1111`
- [ ] Tap Police card → auto-fills `police_001` / `2222`
- [ ] Tap Hospital card → auto-fills `hospital_001` / `3333`
- [ ] Tap Admin card → auto-fills `admin_001` / `0000`
- [ ] Click "Sign In" with valid credentials → successful login
- [ ] Click "Sign In" with invalid credentials → error message appears
- [ ] Click "Load demo accounts" → seeds Firebase data
- [ ] Error state shows red tinted banner

## Driver Dashboard Testing

### ✅ Visual Checks
- [ ] Header shows ambulance icon with red background
- [ ] Driver name and vehicle ID (AMB001) appear
- [ ] LIVE badge appears when emergency is active
- [ ] Exit button is visible
- [ ] 3 tabs: Mission, Navigate, Status
- [ ] Tab indicator shows correct color (red for driver)

### ✅ Mission Tab
- [ ] Status banner shows "STANDBY" when idle
- [ ] Status banner shows "EMERGENCY ACTIVE" when active
- [ ] Severity selector shows 3 cards (P1 Critical/red, P2 Severe/amber, P3 Moderate/blue)
- [ ] Hospital list shows cards with green/red availability dots
- [ ] Hospital cards show name, distance, ETA
- [ ] "FULL" badge appears for unavailable hospitals
- [ ] "START EMERGENCY" button is red
- [ ] "END EMERGENCY" button has red border

### ✅ Navigate Tab
- [ ] Map placeholder is visible
- [ ] Stats row shows ETA (amber), Distance (blue), Speed (green)
- [ ] Junction clearance list shows 4 junctions
- [ ] Junction status badges show CLEARED (green), APPROACHING (amber), PENDING (gray)

### ✅ Status Tab
- [ ] Trip info card shows Trip ID, Severity, Destination, Status
- [ ] System signals card shows LoRa dBm, RFID status, GPS satellites, Hospital link
- [ ] All values appear in monospace font where appropriate

### ✅ Functionality Tests
- [ ] Select P1 Critical → severity updates
- [ ] Select hospital → hospital updates in status
- [ ] Click "START EMERGENCY" → emergency activates
- [ ] LIVE badge appears
- [ ] Status banner changes to active
- [ ] Click "END EMERGENCY" → emergency deactivates
- [ ] LIVE badge disappears
- [ ] Status banner changes to standby

## Police Dashboard Testing

### ✅ Visual Checks
- [ ] Header shows police badge with blue background
- [ ] Officer name and junction ID appear
- [ ] "MONITORING" badge appears (blue)
- [ ] Exit button is visible
- [ ] 3 tabs: Live Map, Junctions, Alerts
- [ ] Tab indicator shows correct color (blue for police)

### ✅ Live Map Tab
- [ ] Map placeholder is visible
- [ ] Active ambulances list shows multiple cards
- [ ] Each card shows severity badge (P1=red, P2=amber)
- [ ] Ambulance ID appears in monospace
- [ ] ETA and speed appear on right
- [ ] RFID status dot appears (green=connected, gray=disconnected)
- [ ] LoRa signal dBm appears

### ✅ Junctions Tab
- [ ] Summary row shows Cleared (green), Approaching (amber), Pending (gray) counts
- [ ] Junction cards show ID, intersection name
- [ ] Signal dot shows color (green/amber/red)
- [ ] Status badge shows NORMAL/PREEMPTION/STANDBY
- [ ] Data grid shows Signal, LoRa, Approach direction

### ✅ Alerts Tab
- [ ] Priority Alert banner appears with red background
- [ ] Blinking dot is visible
- [ ] RFID Clearance Log shows tag ID, ambulance, status
- [ ] Preemption Events list shows junction, action, status

### ✅ Functionality Tests
- [ ] Login as police_001 / 2222 → Police Dashboard loads
- [ ] Tab switching works smoothly
- [ ] Junction data auto-updates (if implemented)
- [ ] Click "Refresh" → data refreshes

## Hospital Dashboard Testing

### ✅ Visual Checks
- [ ] Header shows hospital icon with green background
- [ ] Hospital name and HOS ID appear
- [ ] "1 INCOMING" badge appears (green)
- [ ] Exit button is visible
- [ ] 3 tabs: Incoming, Bay Ready, History
- [ ] Tab indicator shows correct color (green for hospital)

### ✅ Incoming Tab
- [ ] Alert card shows severity badge (P1=red)
- [ ] Ambulance ID appears in monospace
- [ ] LIVE dot blinks when status=incoming
- [ ] Timestamp appears
- [ ] 2x2 grid shows Condition, Patient Age, ETA, Distance
- [ ] Bay readiness mini-row shows 4 emoji icons
- [ ] Tap to manage label appears (if functional)

### ✅ Bay Ready Tab
- [ ] Context card shows severity-colored border
- [ ] Condition and ETA appear
- [ ] Ambulance/driver/age summary appears
- [ ] 4 toggle rows appear: Response Team, Bed+Bay, Doctor On-Duty, Patient Received
- [ ] Each row shows emoji icon, label, iOS-style toggle
- [ ] Toggle turns green when on
- [ ] Progress bar shows "X / 4" readiness
- [ ] Progress bar fills with green
- [ ] "BAY FULLY READY" success message appears when all 4 toggled

### ✅ History Tab
- [ ] Past trips list shows multiple entries
- [ ] Each entry shows severity badge, condition, timestamp
- [ ] Trip ID and ambulance ID appear
- [ ] "✓ Done" pill appears in green

### ✅ Functionality Tests
- [ ] Login as hospital_001 / 3333 → Hospital Dashboard loads
- [ ] Toggle Response Team → Firebase updates
- [ ] Toggle Bed+Bay → Firebase updates
- [ ] Toggle Doctor On-Duty → Firebase updates
- [ ] Toggle Patient Received → Firebase updates
- [ ] Progress bar updates with each toggle
- [ ] Success message appears when all 4 toggled
- [ ] Tab switching works smoothly

## Admin Dashboard Testing

### ✅ Visual Checks
- [ ] Header shows settings icon with amber background
- [ ] Admin name and "ADM-001 · SAPTCS Admin" appear
- [ ] Exit button is visible
- [ ] Horizontal scrollable nav appears
- [ ] Nav items: Overview, Users, Vehicles, Hospitals, Junctions
- [ ] Active nav item has amber tinted background

### ✅ Overview Section
- [ ] 2x2 stat card grid appears
- [ ] Active Trips (red), Online Users (blue), Ambulances (amber), Junctions Clear (green)
- [ ] System Health card shows 5 services
- [ ] Each service shows green pulse dot, uptime %, "online" pill
- [ ] Recent Events log shows colored dots per type
- [ ] Event messages and timestamps appear

### ✅ Users Section
- [ ] "+ Add User" button appears (amber outlined)
- [ ] User cards show role badge (color-coded)
- [ ] Name and username in monospace appear
- [ ] Vehicle info appears for drivers
- [ ] Toggle switch appears on right
- [ ] Toggle switches work to activate/deactivate

### ✅ Vehicles Section
- [ ] "+ Register" button appears
- [ ] Ambulance cards show ID in monospace
- [ ] Registration plate appears
- [ ] Status badge shows ACTIVE/STANDBY/OFFLINE
- [ ] 2x2 data grid shows Driver, RFID Tag, Last Seen, Status

### ✅ Hospitals Section
- [ ] "+ Add" button appears
- [ ] Hospital cards show name, ID in monospace
- [ ] AVAILABLE/FULL badge appears
- [ ] Distance + ETA appear

### ✅ Junctions Section
- [ ] "+ Add" button appears
- [ ] Junction cards show ID in monospace
- [ ] Intersection name appears
- [ ] Signal dot shows color
- [ ] 3-column grid shows Signal color, Status, ESP32 online

### ✅ Functionality Tests
- [ ] Login as admin_001 / 0000 → Admin Dashboard loads
- [ ] Horizontal nav scrolls smoothly
- [ ] Nav item selection changes section
- [ ] Toggle switches work in Users section
- [ ] "+ Add" buttons are visible (functionality not implemented)
- [ ] Tab switching works smoothly

## Firebase + MQTT Integration Testing

### ✅ Firebase Realtime Database
- [ ] Login works with demo accounts
- [ ] "Load demo accounts" seeds data successfully
- [ ] Emergency activation updates Firebase
- [ ] Emergency completion updates Firebase
- [ ] Hospital readiness toggles sync to Firebase
- [ ] All data reads work correctly

### ✅ MQTT (if broker is configured)
- [ ] MQTT connection status shows in app
- [ ] Emergency activation publishes MQTT message
- [ ] Emergency completion publishes MQTT message
- [ ] Trip events publish to MQTT
- [ ] Telemetry updates appear in Police Dashboard

## Cross-Role Testing

### ✅ Logout & Re-login
- [ ] Logout from Driver → returns to login screen
- [ ] Logout from Police → returns to login screen
- [ ] Logout from Hospital → returns to login screen
- [ ] Logout from Admin → returns to login screen
- [ ] Re-login with different role → correct dashboard loads

### ✅ Data Consistency
- [ ] Emergency activated by Driver → visible in Police/Hospital dashboards
- [ ] Hospital readiness updated → visible in Firebase
- [ ] Junction status changes → visible in Police Dashboard
- [ ] Admin changes → reflected in other roles

## Performance & UX Testing

### ✅ Performance
- [ ] App launches within 3 seconds
- [ ] Screen transitions are smooth
- [ ] No lag when switching tabs
- [ ] No lag when toggling switches
- [ ] No lag when activating emergency

### ✅ User Experience
- [ ] All text is readable on dark background
- [ ] Color contrast is sufficient
- [ ] Buttons are tappable with sufficient padding
- [ ] Inputs are easy to tap
- [ ] Navigation is intuitive
- [ ] Error messages are clear

## Known Limitations (Acceptable for This Phase)

- Maps are placeholders (will be replaced with actual maps in future)
- Some buttons (Add User, Register, etc.) are UI-only (functionality to be implemented)
- System Health uptime percentages are simulated (actual monitoring to be implemented)
- Junction auto-update may not be implemented yet
- Navigation from emergency completion is temporarily disabled

## Bug Reporting

If you encounter any issues:
1. Note the screen and action that caused the issue
2. Describe what happened vs. what should happen
3. Check Firebase Console for any errors
4. Check app logs if possible
5. Report the issue with screenshots if possible

## Test Completion Checklist

After testing all items above:
- [ ] All login scenarios work
- [ ] All dashboards load correctly
- [ ] All tabs switch smoothly
- [ ] All new features work (Bay Ready, System Health)
- [ ] Firebase integration works
- [ ] MQTT integration works (if configured)
- [ ] No crashes or major bugs
- [ ] Performance is acceptable
- [ ] UX is intuitive

## Next Steps After Testing

If testing is successful:
1. Merge `ui-upgrade` branch to `main`
2. Delete old screens (if new ones are working perfectly)
3. Implement remaining functionality (maps, user registration, etc.)
4. Add more production features
5. Transition to production Firebase rules

If testing reveals issues:
1. Document all bugs found
2. Prioritize bugs by severity
3. Fix critical bugs first
4. Re-test after fixes
5. Continue with production rules only after all bugs resolved