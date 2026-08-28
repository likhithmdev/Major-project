# Android App Plan

## App Name

Smart Ambulance Driver

## Package Name

```text
com.smartambulance.driver
```

## Core Screens

1. Login
2. Ambulance driver dashboard
3. Police dashboard
4. Hospital dashboard

## First Version Behavior

- Pre-register users in Firebase/DB manager.
- Login with user ID and PIN only; no signup.
- Route user to dashboard by role.
- Driver selects patient severity and hospital.
- Driver activates emergency.
- App writes trip, ambulance, police alert, and hospital alert data.
- App opens Google Maps directions to the selected hospital.
- Driver GPS uploads to Firebase every 10 seconds while emergency is active.
- Vehicle LoRa GPS unit handles junction preemption locally; the app mirrors current GPS and trip state to Firebase.
- Police role watches incoming ambulance alerts for the assigned junction.
- Police dashboard shows GPS-LoRa preemption mode, approach distance, and RSSI fallback status when available.
- Hospital role watches incoming ambulance alerts for the assigned hospital.

## Android Dependencies

Recommended:

```text
Firebase Authentication
Firebase Realtime Database
Google Play Services Location
Google Maps SDK
Eclipse Paho MQTT Client
AndroidX Lifecycle
Material Components
```

## Permission Requirements

```text
ACCESS_FINE_LOCATION
ACCESS_COARSE_LOCATION
ACCESS_BACKGROUND_LOCATION
INTERNET
POST_NOTIFICATIONS
```

## Build Later

- Background GPS updates during active emergency.
- Firebase Authentication or custom token auth.
- DB manager/admin panel for registered users.
- Real hospital search using location distance.
- Route ETA.
- Green corridor status from `junctions`, `junctionEvents`, and `loraTelemetry`.
- Optional MQTT subscription for `junctions/{junctionId}/approach` and signal-state topics.
