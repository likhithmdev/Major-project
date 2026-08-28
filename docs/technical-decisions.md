# Technical Decisions

## Mobile App

Decision: native Android.

Recommended implementation:

- Kotlin.
- Android Studio.
- Firebase SDK.
- Google Maps SDK.
- Fused Location Provider.

Reason:

- The project only needs Android for the driver app.
- Native Android gives direct GPS, background location, permissions, and map control.

## Cloud and IoT

Decision: use both Firebase and MQTT.

Recommended split:

- Firebase Realtime Database stores durable app/dashboard state.
- MQTT carries live IoT messages from ESP32 junction units.
- Dashboard can read Firebase first and optionally subscribe to MQTT for faster live telemetry.

Suggested MQTT topics:

```text
smart-ambulance/junctions/{junctionId}/events
smart-ambulance/junctions/{junctionId}/approach
smart-ambulance/junctions/{junctionId}/signal
smart-ambulance/ambulances/{ambulanceId}/lora-gps
smart-ambulance/ambulances/{ambulanceId}/status
smart-ambulance/trips/{tripId}/events
```

## Hybrid Preemption

Decision: use GPS over LoRa for early approach detection, then RC522 RFID for exact stop-line clearance.

Reason:

- GPS gives enough range to clear traffic before the ambulance reaches the junction.
- LoRa keeps the approach signal local and independent from mobile internet.
- Haversine distance and bearing checks reduce false triggers.
- RC522 clearance avoids close-range GPS drift and latency.
- RSSI fallback gives a degraded safety mode if GPS fails.

Design rule:

- GPS/LoRa can start preemption only when the active ambulance is within the threshold and approaching the configured lane.
- RFID clearance is the authoritative signal that the ambulance reached the stop line.
- RSSI fallback must be clearly marked as lower confidence in logs and dashboard state.

## RFID Module

Decision: default to RC522 for stop-line clearance, changeable later.

Reason:

- Low cost.
- Common ESP32 examples.
- Good for tabletop demonstration.

Design rule:

- Keep RFID reading separate from signal logic.
- The signal state machine should only receive `tagId`, `readerType`, `clearanceZone`, and `timestamp`.
- If RC522 is replaced by PN532 or active RFID later, only the reader adapter should change.

## Dashboard

Decision: responsive mobile + laptop dashboard.

Recommended implementation:

- React + Vite.
- Firebase SDK.
- Optional MQTT over WebSocket.
- Mobile-first responsive layout.
