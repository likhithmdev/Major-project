# Smart Ambulance — Complete System Guide

Single-file A-to-Z reference for the Smart Ambulance Priority Traffic Control System (SAPTCS). It describes what the project is, how hardware and software fit together, how an emergency trip actually runs, and what is implemented today versus still prototype-limited.

Related topic files still exist under `docs/` (architecture, MQTT, schema, roadmap). This guide is the combined walkthrough.

---

## 1. What this project is

This repo is a **proof-of-concept** where an ambulance on an emergency trip can get a green at a smart junction **before it reaches the stop line**, then the signal returns to normal **when the vehicle actually clears the junction**.

The important split:

- **Local radios (LoRa + RFID) control the lights.** That works even if Wi-Fi is down.
- **Phone GPS + Firebase + MQTT** are for maps, alerts, and the control-room dashboard. They do **not** decide the traffic lights.

Prototype scope:

- One ambulance unit with ESP32, GPS, LoRa transmitter, RFID tag, and mobile app support.
- Two or three smart junction units.
- Each junction has a LoRa ground station for long-range approach tracking and an RC522 RFID reader at the physical stop line for exact clearance.
- Local traffic signal override works without internet.
- Cloud tracking and analytics work through Firebase and MQTT.
- Dashboard shows ambulance location, junction status, emergency events, and reports.

---

## 2. Problem it solves

City traffic lights run a fixed cycle. An ambulance can sit in a queue even when it is close. This prototype:

1. Detects the ambulance **hundreds of meters away** using GPS sent over LoRa.
2. Turns the **ambulance lane green** and **cross traffic red**.
3. Confirms the vehicle **physically crossed the stop line** with an RFID tag.
4. Restores the normal cycle.
5. Logs everything to the cloud so police, hospital, and a web dashboard can watch.

**One-sentence mental model:** the phone tells humans and the cloud that an emergency exists; the vehicle LoRa tells the junction where the ambulance is; RFID tells the junction the ambulance has passed; the ESP32 owns the lights either way.

---

## 3. Repository layout

```text
smart_ambulance/
├── dashboard/                  # Traffic police / control room web dashboard
├── docs/                       # Architecture, schema, API, roadmap, this guide
├── firmware/
│   ├── ambulance_unit/         # ESP32 ambulance-side firmware
│   └── traffic_signal_unit/    # ESP32 junction controller firmware
└── mobile_app/                 # Native Android app (driver, police, hospital, admin)
```

Locked starter decisions:

- Mobile app: native Android (Kotlin + Jetpack Compose).
- Dashboard: React + Vite, responsive for phone and laptop.
- Cloud/IoT: Firebase Realtime Database plus MQTT.
- Default RFID: RC522, kept replaceable.
- Long-range preemption: GPS over LoRa, Haversine distance + bearing at the junction.
- GPS failure fallback: LoRa RSSI proximity with a conservative timeout.

---

## 4. Four system layers

| Layer | Physical / software piece | Job |
|---|---|---|
| Ambulance | Phone + optional ESP32 + GPS + LoRa + RFID tag | Start emergency, publish GPS to cloud, broadcast GPS over LoRa, identify at stop line |
| Junction | ESP32 + LoRa + RC522 + LEDs/relays | Decide preemption locally, change lights, restore after RFID or timeout |
| Cloud | Firebase Realtime Database + MQTT | Store trips, locations, junction state, events |
| Apps | Android app + React dashboard | Driver / police / hospital / admin UI, and control-room view |

There is **no custom backend**. Apps and firmware talk to Firebase (and MQTT) directly. `docs/api-design.md` describes REST routes for a later server.

---

## 5. Hardware

### 5.1 Ambulance side

| Part | Role |
|---|---|
| Android phone | Driver app. Starts/ends emergency. Uploads GPS to Firebase every 10 seconds. Opens Google Maps to the hospital. Does **not** talk to the traffic light. |
| ESP32 | Reads GPS, broadcasts JSON over LoRa about once per second while emergency is on. |
| Neo-6M GPS | UART: module TX → GPIO 16, RX → GPIO 17, 9600 baud. If no module, Serial `SIM lat,lng,heading,speed` still works. |
| LoRa transceiver (433 MHz) | NSS 5, RST 14, DIO0 26. Sends packets the junction already parses. |
| Emergency button | GPIO 4 to GND. Toggles broadcast on/off. |
| Passive RFID tag | Mounted under the chassis (demo ID `RFID_TAG_001`). Read only at the stop line. |

Firmware: `firmware/ambulance_unit/ambulance_unit.ino`.

Arduino libraries: LoRa (Sandeep Mistry), TinyGPSPlus (Mikal Hart), ArduinoJson (Benoit Blanchon).

Ambulance firmware starts with emergency **on** (`START_IN_EMERGENCY = true`) so a bench demo can transmit immediately. Hardcoded IDs: ambulance `AMB001`, trip `TRIP001`. Broadcast interval: 1000 ms.

The phone app still owns Firebase GPS. This unit only sends local LoRa packets to the junction.

Ambulance Serial (115200):

```text
EMERGENCY ON
EMERGENCY OFF
SIM 12.9750,77.5946,185,42
GPS OFF
GPS ON
STATUS
```

`SIM lat,lng,heading,speed` starts emergency and broadcasts even if GPS is not wired. `GPS OFF` sends `gpsFix: false` so the junction can test RSSI fallback.

### 5.2 Junction side (one unit per intersection)

| Part | Role |
|---|---|
| ESP32 | Whole state machine: LoRa, RFID, LEDs, optional Wi-Fi / MQTT / Firebase. |
| LoRa receiver | Same 433 MHz pin map as the ambulance. Measures **RSSI** for GPS-failure fallback. |
| RC522 RFID | SS GPIO 21, RST GPIO 22. Stop-line clearance, not long-range detection. |
| Four LEDs (or later relays) | Ambulance green 32, ambulance red 33, cross green 25, cross red 27. |
| Wi-Fi | Optional. Empty `WIFI_SSID` = fully offline lights. If set, events go to HiveMQ and Firebase. |

Firmware: `firmware/traffic_signal_unit/traffic_signal_unit.ino`.

Arduino libraries: LoRa, MFRC522, ArduinoJson, PubSubClient (Nick O'Leary).

Hardcoded demo junction:

- ID `JNC001`, name Main Road Junction
- Location **12.9716, 77.5946**
- Approach lane **northbound**
- Authorized ambulance `AMB001`, tag `RFID_TAG_001`

Cloud (when Wi-Fi is configured):

- MQTT broker: `broker.hivemq.com:1883`
- Dashboard WebSocket: `wss://broker.hivemq.com:8884/mqtt`
- Firebase host: `smart-ambulance-36f9d-default-rtdb.firebaseio.com`
- Project ID: `smart-ambulance-36f9d`

Signal control still works if Wi-Fi is down. When you scan a real RC522 card, Serial prints the UID; put that hex value into `AUTHORIZED_RFID_TAG`.

### 5.3 Two tracking zones

**Zone 1 — long range (GPS over LoRa)**

Purpose: detect the ambulance before the junction, start preemption early enough to clear queues.

Ambulance packet:

```json
{
  "ambulanceId": "AMB001",
  "tripId": "TRIP001",
  "lat": 12.9750,
  "lng": 77.5946,
  "speedKmph": 42,
  "headingDeg": 185,
  "gpsFix": true,
  "timestamp": 12345
}
```

Junction math:

- `distanceMeters = haversine(ambulanceLatLng, junctionLatLng)`
- `bearingToJunctionDeg = bearing(ambulanceLatLng, junctionLatLng)`
- `approaching` if heading error is within `bearingToleranceDeg`
- Preempt if distance ≤ **500 m** and heading error ≤ **35°**

A vehicle going away or on another road should not trigger.

**Zone 2 — stop line (RC522)**

Purpose: detect the exact moment the ambulance crosses the white line, without GPS drift or cloud latency.

When preemption is already active and the **same authorized tag** is scanned, the junction marks `rfid_clearance` and, after a **3 second hold**, restores the normal cycle. Dwell time is logged from preemption start to RFID clearance.

**GPS failure fallback (LoRa RSSI)**

If `gpsFix` is false or GPS is marked unhealthy:

- Watch LoRa RSSI getting **stronger** over consecutive packets.
- Need RSSI ≥ **-65 dBm** for **3 consecutive** packets.
- Then start `RSSI_PREEMPT_ACTIVE` (lower confidence; no lane proof).
- Logs and dashboard label this `rssi_fallback`.
- Use a conservative timeout because RSSI cannot prove direction.

---

## 6. Junction state machine (the actual light logic)

```text
NORMAL
  → APPROACH_TRACKING     (authorized LoRa packet arrives)
  → GPS_PREEMPT_ACTIVE    (distance + bearing OK)
  → RSSI_PREEMPT_ACTIVE   (GPS unhealthy + RSSI rules)

GPS_PREEMPT_ACTIVE or RSSI_PREEMPT_ACTIVE
  → RFID_CLEARED          (authorized stop-line tag)
  → TIMEOUT_RESTORE       (no RFID within 90 s)

RFID_CLEARED
  → NORMAL                (after 3 s hold)

TIMEOUT_RESTORE
  → NORMAL                (after warning event and signal reset)
```

Lights:

- **Normal:** ambulance red, cross green
- **Priority:** ambulance green, cross red

Timeouts (firmware constants):

| Setting | Value |
|---|---|
| Approach threshold | 500 m |
| Bearing tolerance | 35° |
| RSSI fallback threshold | -65 dBm |
| RSSI consecutive packets | 3 |
| GPS / approach packet stale | 5 s → back to NORMAL |
| Clearance timeout | 90 s → `timeout_restore` |
| Post-clearance hold | 3 s |

Core rules:

- LoRa GPS starts priority only for an active authorized ambulance.
- RFID clearance is the authoritative “vehicle reached the stop line” signal.
- Unauthorized ambulance IDs are ignored.
- Wrong RFID during preemption logs `invalid_rfid_tag`; Serial prints the UID for mapping.
- **Internet is never required for lights.** MQTT/Firebase are best-effort after the lights already changed.

RFID reading is kept separate from signal logic so RC522 can later be swapped for PN532 or active RFID without rewriting the state machine.

---

## 7. End-to-end emergency trip

1. Admin seeds Firebase (or uses demo users).
2. Driver logs in, chooses hospital and severity, taps emergency.
3. Firebase: ambulance `emergency_active`, trip active, police + hospital alerts. Dashboard shows emergency. Maps opens. Phone GPS starts every 10 seconds.
4. Ambulance ESP32 broadcasts LoRa JSON every 1 second.
5. Junction ESP32: packet → approach tracking → Haversine + bearing → **green corridor**. If Wi-Fi is on, MQTT + Firebase get `gps_preempt_started`.
6. Ambulance reaches the stop line; RC522 reads the tag → **RFID_CLEARED** → 3 s hold → normal cycle. Event: `rfid_clearance` and dwell time.
7. If the tag never appears in 90 s: `timeout_restore`. Incomplete passage is logged as a warning.
8. Driver completes trip; alerts and ambulance status go back to available.

If GPS is bad: `GPS OFF` on the ambulance serial, or a packet with `"gpsFix": false`. Junction uses RSSI instead.

---

## 8. Software — Android app

Location: `mobile_app/`
Package: `com.smartambulance.driver`
Stack: Kotlin, Jetpack Compose, Firebase Realtime Database, Fused Location Provider.

Open `mobile_app/` in Android Studio and allow Gradle sync. Firebase config: `mobile_app/app/google-services.json`.

### 8.1 Login

Login is **user ID + PIN**, no signup. Users live under `users/{userId}` with `active` and `pin`. Inactive users cannot log in.

Demo accounts:

| User | PIN | Role |
|---|---|---|
| `driver_001` | `1111` | Ambulance driver (`AMB001`) |
| `police_001` | `2222` | Traffic police (junction `JNC001`) |
| `hospital_001` | `3333` | Hospital desk (`HOSP001`) |
| `admin_001` | `0000` | Registry / seed data |

### 8.2 Driver

- Pick severity: Fine / Serious / Very Emergency.
- Pick destination hospital (demo list).
- Start emergency: writes ambulance status, trip `TRIP001`, police alert, hospital alert, and demo LoRa telemetry.
- GPS uploads every 10 s to `ambulances/{id}/lastLocation`.
- Open Google Maps navigation to the hospital.
- Complete trip: clears emergency and marks trip completed.

### 8.3 Police

Live `policeAlerts/{junctionId}` plus AMB001 location / LoRa telemetry (distance, RSSI, preemption mode).

### 8.4 Hospital

`hospitalAlerts/{hospitalId}` plus bay readiness: Team / Bed / Doctor / Received.

### 8.5 Admin

Seed demo data, list users / ambulances / hospitals / RFID / junctions, deactivate users, register new drivers, police, hospitals, ambulances, RFID tags, and junctions.

### 8.6 Permissions

`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `INTERNET`, `POST_NOTIFICATIONS`.

MQTT topic helpers exist in `MqttTopics.kt`. The live path today is Firebase, not a broker from the phone.

Key source files:

- `MainActivity.kt` — login routing, GPS loop, Maps intent
- `data/DemoRepository.kt` — Firebase reads/writes
- `data/FirebasePaths.kt` — database path constants
- `ui/screens/RoleScreens.kt` — login, driver, police, hospital
- `ui/screens/AdminScreen.kt` — admin registry

---

## 9. Software — web dashboard

Location: `dashboard/`
Stack: React 19 + Vite, Firebase SDK, MQTT over WebSocket, lucide-react icons.

Run:

```bash
cd dashboard
npm install
npm run dev
```

URL: `http://localhost:5180`

Behavior:

- Subscribes to the **entire Firebase root**.
- MQTT over WebSocket: `wss://broker.hivemq.com:8884/mqtt`
- Topics: junction `events` / `signal` / `approach`, ambulance `lora-gps`

UI:

- Emergency status, AMB001 GPS, Firebase/MQTT health
- Simulated corridor progress (not a real map yet)
- Hospital list
- Three junction cards: JNC001, JNC002, JNC003
- Event log
- Simulator buttons: GPS preempt, RSSI, RFID, timeout, reset — they write the same Firebase paths the ESP32 would

The dashboard can be driven by real hardware when junction `WIFI_SSID` is set.

Key source files:

- `src/main.jsx` — full UI and simulator
- `src/integrations/firebaseClient.js`
- `src/integrations/mqttClient.js`
- `src/integrations/mqttTopics.js`

---

## 10. Cloud: Firebase and MQTT

| System | Use |
|---|---|
| Firebase Realtime Database | Durable state: users, ambulances, trips, junctions, events, hospitals, alerts |
| MQTT | Fast ESP32 → dashboard telemetry |

Firebase project: `smart-ambulance-36f9d`.

### 10.1 MQTT topics

```text
smart-ambulance/ambulances/{ambulanceId}/lora-gps
smart-ambulance/junctions/{junctionId}/approach
smart-ambulance/junctions/{junctionId}/events
smart-ambulance/junctions/{junctionId}/signal
smart-ambulance/ambulances/{ambulanceId}/status
smart-ambulance/trips/{tripId}/events
```

Broker options: local Mosquitto (safest college demo), HiveMQ public test broker (current firmware/dashboard default), or EMQX cloud later.

### 10.2 Event types

Used in Firebase and MQTT:

- `lora_gps_packet`
- `approach_tracking`
- `gps_preempt_started`
- `rssi_preempt_started`
- `rfid_clearance`
- `timeout_restore`
- `manual_reset`

Firmware also logs extras such as `invalid_rfid_tag`, `normal_restored`, `approach_tracking_expired`.

Signal `preemptionMode` values: `none`, `gps_lora`, `rssi_fallback`, `manual`.

### 10.3 Firebase nodes

Main nodes: `users`, `ambulances`, `drivers`, `junctions`, `loraTelemetry`, `emergencyTrips`, `junctionEvents`, `hospitals`, `hospitalAlerts`, `policeAlerts`, `rfidTags`.

Ambulance last location is what the **phone** writes. Junction approach numbers are what the **ESP32** (or dashboard simulator) writes under `loraTelemetry` and `junctions`.

Example ambulance record:

```json
{
  "AMB001": {
    "ambulanceId": "AMB001",
    "rfidTagId": "RFID_TAG_001",
    "loraNodeId": "LORA_AMB001",
    "status": "available",
    "severity": "Serious",
    "destinationHospitalId": "HOSP001",
    "lastLocation": { "lat": 12.9716, "lng": 77.5946 }
  }
}
```

Full schema: see `docs/database-schema.md`.

Pins stored in Firebase are acceptable for a lab demo, not for production.

---

## 11. Bench demo without radios

**Junction ESP32** — Serial 115200, leave `WIFI_SSID` empty for offline:

```text
SIM {"ambulanceId":"AMB001","tripId":"TRIP001","lat":12.9750,"lng":77.5946,"speedKmph":42,"headingDeg":185,"gpsFix":true}
RFID RFID_TAG_001
```

Ambulance-lane green should turn on after the SIM packet, then restore after RFID.

RSSI fallback test:

```text
SIMRSSI -60
SIM {"ambulanceId":"AMB001","tripId":"TRIP001","lat":0,"lng":0,"speedKmph":0,"headingDeg":0,"gpsFix":false}
```

(Repeat packets until three consecutive stronger RSSI readings meet the threshold.)

**Ambulance ESP32:**

```text
SIM 12.9750,77.5946,185,42
GPS OFF
```

**Dashboard:** `cd dashboard && npm install && npm run dev`
**App:** open `mobile_app/` in Android Studio.

Recommended first-build order:

1. Traffic signal ESP32 logic with simulated priority input
2. RC522 stop-line RFID clearance for one junction
3. LoRa GPS packets with distance and bearing
4. Firebase data model and event logging
5. Dashboard
6. Driver app GPS tracking
7. Hospital recommendation and route
8. Expand to two or three junctions

---

## 12. Why this hybrid design

- GPS + LoRa: early warning, no cellular delay at the light.
- Bearing check: reduces false greens for vehicles leaving or on another road.
- RFID: exact clearance despite GPS drift.
- RSSI: degraded mode if GPS dies.
- Cloud: observability only; lights stay local.

---

## 13. What is built vs still prototype

**Working in code today**

- Full junction state machine + Serial simulation
- Ambulance LoRa transmitter + GPS / SIM
- Firebase-backed Android roles
- Dashboard live Firebase + MQTT + simulator buttons
- Cloud logging from junction when `WIFI_SSID` is set

**Still demo / limited**

- One ambulance ID and one trip ID (`AMB001` / `TRIP001`) in firmware
- Junction GPS and authorized IDs are compile-time constants
- Dashboard “map” is a progress bar, not Google Maps
- Hospitals are a hardcoded list, not live nearby search
- MQTT on the phone is placeholders
- HiveMQ public broker is for class demos, not production
- Multi-junction corridor is UI-ready (JNC002 / JNC003) more than firmware-ready
- Background GPS, stronger auth, and live hospital search are later work

Week-by-week plan: `docs/roadmap.md`.

---

## 14. Source map

| Topic | Path |
|---|---|
| Project overview | `README.md` |
| This combined guide | `docs/complete-system-guide.md` |
| Architecture | `docs/architecture.md` |
| Hybrid preemption | `docs/hybrid-preemption.md` |
| Get started / milestones | `docs/get-started.md` |
| MQTT topics | `docs/mqtt-topics.md` |
| Database schema | `docs/database-schema.md` |
| Technical decisions | `docs/technical-decisions.md` |
| Planned REST API | `docs/api-design.md` |
| Android plan | `docs/android-app-plan.md` |
| Roadmap | `docs/roadmap.md` |
| Ambulance firmware | `firmware/ambulance_unit/` |
| Junction firmware | `firmware/traffic_signal_unit/` |
| Dashboard | `dashboard/` |
| Android app | `mobile_app/` |
