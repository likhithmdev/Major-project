# Driver Mobile App

Framework: native Android.

Recommended language: Kotlin.

## Screens

- Driver login.
- Ambulance authentication.
- Emergency activation.
- Active trip status.
- GPS tracking.
- GPS-LoRa corridor status.
- Nearby hospital recommendation.
- Route view.
- Trip complete screen.

## Firebase Data Writes

The app should write:

- Driver login status.
- Ambulance current location.
- Ambulance LoRa/GPS telemetry mirror when available.
- Emergency trip start/end.
- Selected hospital.

## MQTT Role

The Android app can use Firebase as its main app database. MQTT is mainly for IoT device telemetry from ESP32 LoRa/RFID junctions, but the app may subscribe to green corridor topics later if live signal status is needed directly.

## First App Milestone

Build the simplest version first:

- One hardcoded ambulance ID.
- One emergency button.
- GPS location upload every few seconds.
- Active/inactive trip status in Firebase.
- Optional green corridor status from LoRa approach and RC522 clearance events.

Maps and hospital routing can come after this works.

## Current Starter Project

This folder now contains a native Android starter project that can be opened in Android Studio.

The current app supports four role-based demo logins:

```text
driver_001 / 1111
police_001 / 2222
hospital_001 / 3333
admin_001 / 0000
```

The current app includes:

- AMB001 demo mode.
- Firebase role-based login with no signup.
- Login reads pre-registered active users from `users/{userId}`.
- Driver, police, hospital, and admin DB manager dashboards.
- Admin existing-record view for users, ambulances, hospitals, RFID tags, and junctions.
- Admin user deactivation using `users/{userId}/active = false`.
- Patient severity selection.
- Nearby hospital selection.
- Start emergency button.
- Complete trip button.
- Admin pre-registration forms for drivers, police, hospitals, ambulances, RFID tags, and junctions.
- Realtime Database writes for ambulance and trip status.
- Police alert writes under `policeAlerts`.
- Hospital alert writes under `hospitalAlerts`.
- GPS location upload every 10 seconds while emergency mode is active.
- Firebase paths for `loraTelemetry` and MQTT topic placeholders for GPS-LoRa approach decisions.
- Location permissions declared.

Open `mobile_app/` in Android Studio and allow Gradle sync.

Firebase Android config has been added at:

```text
app/google-services.json
```

## Suggested Android Packages

```text
com.smartambulance.app
├── data
├── location
├── mqtt
├── firebase
├── ui
└── domain
```
