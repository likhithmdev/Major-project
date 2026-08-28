# Hybrid GPS-LoRa-RFID Preemption

This design adds two tracking zones around each smart junction so the ambulance lane can be cleared before the vehicle reaches the stop line, while still using RFID for exact physical clearance.

## Tracking Zones

### Zone 1: Long-Range Approach

Purpose:

- Detect the ambulance before it reaches the junction.
- Calculate distance and approach direction.
- Start signal preemption early enough to clear traffic queues.

Hardware:

- Ambulance-side GPS module.
- Ambulance-side LoRa transceiver.
- Junction-side LoRa ground station.
- Junction ESP32 traffic controller.

Data packet from ambulance:

```json
{
  "ambulanceId": "AMB001",
  "tripId": "TRIP001",
  "lat": 12.9716,
  "lng": 77.5946,
  "speedKmph": 42,
  "headingDeg": 185,
  "gpsFix": true,
  "rssi": -72,
  "timestamp": 1720000000000
}
```

Junction calculation:

- Use the Haversine formula to calculate distance between ambulance GPS and junction GPS.
- Use bearing trigonometry to determine whether the ambulance is approaching the junction lane.
- Trigger preemption when distance is below the configured threshold, for example 500 meters.
- Require the vehicle to be approaching, not moving away, before changing signals.

### Zone 2: Stop-Line Clearance

Purpose:

- Detect the exact moment the ambulance physically crosses the stop line.
- Restore the normal signal cycle without waiting for GPS drift or cloud latency.

Hardware:

- RC522 RFID reader at the stop line.
- Passive RFID tag mounted under the ambulance chassis.

Clearance rule:

- When the stop-line RC522 reader scans the active ambulance tag, mark `rfid_clearance`.
- Restore the signal cycle immediately after the configured safety hold.
- Log dwell time from `gps_preempt_started` or `priority_active` to `rfid_clearance`.

## Fallback Mode

If GPS data is missing, stale, or invalid:

- Use LoRa RSSI as a proximity fallback.
- Track whether RSSI is getting stronger over consecutive packets.
- Trigger a conservative safety clearance window if RSSI crosses the configured threshold and the emergency trip is active.
- Mark the event source as `rssi_fallback` so the dashboard can show reduced confidence.

Recommended fallback rules:

- `gpsFix = false` or packet age above timeout means GPS is unhealthy.
- RSSI threshold example: `-65 dBm` for near range, adjusted after field testing.
- Require at least 3 consecutive packets with stronger RSSI before triggering preemption.
- Use a shorter timeout restore window than GPS mode, because RSSI does not provide lane direction.

## Preemption State Machine

```text
NORMAL
  -> APPROACH_TRACKING when LoRa packets arrive for an active trip
  -> GPS_PREEMPT_ACTIVE when distance <= threshold and bearing confirms approach
  -> RSSI_PREEMPT_ACTIVE when GPS is unhealthy and RSSI fallback confirms proximity

GPS_PREEMPT_ACTIVE or RSSI_PREEMPT_ACTIVE
  -> RFID_CLEARED when stop-line RC522 scans the active ambulance tag
  -> TIMEOUT_RESTORE when clearance is not detected in time

RFID_CLEARED
  -> NORMAL after safety hold and event logging

TIMEOUT_RESTORE
  -> NORMAL after warning event and normal cycle restore
```

## Event Types

Use these event types in Firebase and MQTT:

- `lora_gps_packet`
- `approach_tracking`
- `gps_preempt_started`
- `rssi_preempt_started`
- `rfid_clearance`
- `timeout_restore`
- `manual_reset`

## Configuration

Store threshold settings per junction:

```json
{
  "approachThresholdMeters": 500,
  "bearingToleranceDeg": 35,
  "rssiFallbackThresholdDbm": -65,
  "rssiConsecutivePacketCount": 3,
  "gpsPacketTimeoutMs": 5000,
  "clearanceTimeoutMs": 90000,
  "postClearanceHoldMs": 3000
}
```

## Why This Hybrid Design Is Better

- GPS plus LoRa gives early warning before the ambulance reaches the junction.
- Bearing checks prevent false triggers from ambulances moving away or on another road.
- RFID gives exact stop-line clearance without GPS drift.
- RSSI fallback keeps a degraded but usable safety mode if GPS fails.
- Local LoRa/RFID control means the traffic signal does not depend on internet access.
