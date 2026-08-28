# Get Started

## What We Are Building First

The first milestone should be a working one-junction demo:

```text
Ambulance RFID tag
  -> Ambulance GPS broadcasts coordinates over LoRa
  -> Junction ESP32 calculates distance and bearing
  -> Ambulance lane turns green before the stop line
  -> Stop-line RC522 reader detects same tag
  -> Normal signal cycle restored
  -> Event is logged to cloud/dashboard
```

This proves the most important technical idea before we spend time on the full app and analytics.

## Milestone 1: One Junction Offline Demo

Deliverables:

- ESP32 traffic signal controller.
- Authorized ambulance RFID ID list.
- LoRa GPS packet receiving.
- Haversine distance calculation.
- Bearing/approach lane validation.
- Stop-line RC522 clearance handling.
- RSSI fallback proximity handling.
- Signal state machine:
  - NORMAL
  - APPROACH_TRACKING
  - GPS_PREEMPT_ACTIVE
  - RSSI_PREEMPT_ACTIVE
  - RFID_CLEARED
  - RESTORE_NORMAL
  - TIMEOUT_RESTORE
- Serial monitor logs for GPS packet, preempt, RFID clearance, RSSI fallback, invalid tag, timeout.

Success test:

- Send simulated LoRa GPS packet within 500 meters and approaching the junction.
- Ambulance lane LED turns green.
- Cross traffic LEDs turn red.
- Scan same authorized tag at the stop-line RC522 reader.
- Normal cycle resumes.

## Milestone 2: Cloud Logging

Deliverables:

- Firebase project.
- MQTT broker/topic plan.
- Realtime Database structure.
- ESP32 uploads junction events when Wi-Fi is available.
- ESP32 publishes junction events to MQTT when Wi-Fi is available.
- Offline local signal control still works if Wi-Fi is unavailable.

Success test:

- LoRa GPS, preempt, RFID clearance, and timeout events appear in Firebase.
- LoRa GPS, approach decision, signal, and clearance events are published to MQTT topics.
- If Wi-Fi is off, traffic signal logic still works locally.

## Milestone 3: Dashboard

Deliverables:

- Live map or simulated route view.
- Junction cards showing signal state.
- Active emergency panel.
- Event log table.
- Manual override buttons.
- Responsive layout for phone and laptop screens.

Success test:

- Dashboard updates when ESP32 logs GPS preempt, RSSI fallback, RFID clearance, and timeout events.

## Milestone 4: Driver App

Deliverables:

- Driver login screen.
- Ambulance ID/authentication.
- Emergency activation button.
- Live GPS upload.
- Nearest hospital list.
- Route status screen.

Success test:

- Driver starts emergency.
- GPS coordinates appear in Firebase and dashboard.

## Current Decisions

1. Driver app: native Android.
2. Cloud/IoT: Firebase and MQTT.
3. Long-range preemption: GPS over LoRa, with Haversine distance and bearing validation.
4. RFID reader: default to RC522 for stop-line clearance, but keep the firmware reader layer replaceable.
5. Fallback: use LoRa RSSI only when GPS is unhealthy.
6. Dashboard: responsive for mobile and laptop.

## Next Build Step

Bench-test one junction without radios:

1. Flash `firmware/traffic_signal_unit/traffic_signal_unit.ino`.
2. Serial: `SIM {"ambulanceId":"AMB001","tripId":"TRIP001","lat":12.9750,"lng":77.5946,"speedKmph":42,"headingDeg":185,"gpsFix":true}`
3. Serial: `RFID RFID_TAG_001`
4. Optional: set `WIFI_SSID` so events appear on the dashboard over MQTT and Firebase.

Then flash `firmware/ambulance_unit/ambulance_unit.ino` and use `SIM 12.9750,77.5946,185,42` on the vehicle ESP32.
