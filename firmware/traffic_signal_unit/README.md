# Traffic Signal Unit Firmware

Target hardware:

- ESP32.
- LoRa receiver or transceiver ground station.
- Stop-line RC522 RFID reader.
- Traffic light LEDs or relay interface.
- Optional OLED/LCD display.

## State Machine

```text
NORMAL
  -> APPROACH_TRACKING when LoRa packets arrive for an active trip
  -> GPS_PREEMPT_ACTIVE when distance and bearing meet the configured safety threshold
  -> RSSI_PREEMPT_ACTIVE when GPS is unhealthy and RSSI fallback confirms proximity

GPS_PREEMPT_ACTIVE
  -> RFID_CLEARED when same tag is detected by the stop-line RC522 reader
  -> TIMEOUT_RESTORE when clearance is not detected in time

RSSI_PREEMPT_ACTIVE
  -> RFID_CLEARED when same tag is detected by the stop-line RC522 reader
  -> TIMEOUT_RESTORE when clearance is not detected in time

RFID_CLEARED
  -> RESTORE_NORMAL after safety hold

RESTORE_NORMAL
  -> NORMAL after signal reset

TIMEOUT_RESTORE
  -> NORMAL after signal reset and event logging
```

## Core Rules

- LoRa GPS starts priority only for an active authorized ambulance.
- Haversine distance must be inside the configured approach threshold.
- Bearing must confirm that the ambulance is approaching the junction lane.
- If GPS fails, RSSI fallback can start a conservative preemption window.
- Stop-line RC522 clearance must match the active ambulance tag.
- Signal restoration should not depend only on fixed delay or GPS.
- Timeout exists as safety fallback.
- Local signal control must work without internet.
- Cloud logging is optional and should not block signal control.

## Required Calculations

- `distanceMeters = haversine(ambulanceLatLng, junctionLatLng)`
- `bearingToJunctionDeg = bearing(ambulanceLatLng, junctionLatLng)`
- `approaching = heading difference is within bearingToleranceDeg`
- `rssiFallbackActive = gps unhealthy and RSSI is above threshold for consecutive packets`

## Bench Test Without Radio

Flash `traffic_signal_unit.ino` and open Serial at 115200. Leave `WIFI_SSID` empty for a fully offline demo.

```text
SIM {"ambulanceId":"AMB001","tripId":"TRIP001","lat":12.9750,"lng":77.5946,"speedKmph":42,"headingDeg":185,"gpsFix":true}
RFID RFID_TAG_001
```

Ambulance-lane green should turn on after the SIM packet, then return to normal after the RFID command.

When you scan a real RC522 card, Serial prints the UID. Put that hex value into `AUTHORIZED_RFID_TAG`.

To log events to the dashboard, set `WIFI_SSID` and `WIFI_PASSWORD`. MQTT uses HiveMQ (`broker.hivemq.com:1883`). The dashboard subscribes over WebSocket at `wss://broker.hivemq.com:8884/mqtt`. Firebase writes use `smart-ambulance-36f9d`. Signal control still works if Wi-Fi is down.
