# MQTT Topics

Use these topics for ESP32 and dashboard communication.

## LoRa GPS Telemetry

Topic:

```text
smart-ambulance/ambulances/{ambulanceId}/lora-gps
```

Payload:

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

## Junction Approach Decision

Topic:

```text
smart-ambulance/junctions/{junctionId}/approach
```

Payload:

```json
{
  "junctionId": "JNC001",
  "ambulanceId": "AMB001",
  "tripId": "TRIP001",
  "distanceMeters": 420,
  "bearingToJunctionDeg": 182,
  "approachLane": "northbound",
  "approaching": true,
  "preemptionEligible": true,
  "source": "gps_lora",
  "rssi": -72,
  "timestamp": 1720000000000
}
```

## Junction Events

Topic:

```text
smart-ambulance/junctions/{junctionId}/events
```

Payload:

```json
{
  "eventId": "EVT001",
  "junctionId": "JNC001",
  "ambulanceId": "AMB001",
  "rfidTagId": "RFID_TAG_001",
  "eventType": "gps_preempt_started",
  "lane": "northbound",
  "preemptionMode": "gps_lora",
  "distanceMeters": 420,
  "rssi": -72,
  "timestamp": 1720000000000
}
```

## Signal State

Topic:

```text
smart-ambulance/junctions/{junctionId}/signal
```

Payload:

```json
{
  "junctionId": "JNC001",
  "signalState": "priority_active",
  "activeLane": "northbound",
  "activeAmbulanceId": "AMB001",
  "preemptionMode": "gps_lora",
  "updatedAt": 1720000000000
}
```

Signal `preemptionMode` values:

- `none`
- `gps_lora`
- `rssi_fallback`
- `manual`

Clearance should be published as a junction event with `eventType: "rfid_clearance"` when the stop-line RC522 reader scans the active ambulance tag.

## Trip Events

Topic:

```text
smart-ambulance/trips/{tripId}/events
```

Payload:

```json
{
  "tripId": "TRIP001",
  "ambulanceId": "AMB001",
  "eventType": "emergency_started",
  "timestamp": 1720000000000
}
```

## Broker Choice

For the first demo, use one of these:

- Mosquitto local broker on laptop.
- HiveMQ public broker for quick testing.
- EMQX cloud if you want a hosted dashboard-friendly setup.

Use local Mosquitto for the safest college demo because it does not depend on external internet.
