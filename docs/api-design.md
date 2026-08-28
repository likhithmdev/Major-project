# API Design

For the prototype, direct Firebase SDK reads/writes are enough. If a backend is added later, use these REST-style routes.

## Ambulance

### Start Emergency

`POST /api/emergencies/start`

Request:

```json
{
  "ambulanceId": "AMB001",
  "driverId": "DRV001",
  "destinationHospitalId": "HOSP001"
}
```

Response:

```json
{
  "tripId": "TRIP001",
  "status": "active"
}
```

### Update Location

`POST /api/ambulances/{ambulanceId}/location`

Request:

```json
{
  "lat": 12.9716,
  "lng": 77.5946,
  "speed": 42,
  "timestamp": 1720000000000
}
```

### Update LoRa GPS Telemetry

`POST /api/ambulances/{ambulanceId}/lora-gps`

Request:

```json
{
  "tripId": "TRIP001",
  "junctionId": "JNC001",
  "lat": 12.9716,
  "lng": 77.5946,
  "speedKmph": 42,
  "headingDeg": 185,
  "gpsFix": true,
  "rssi": -72,
  "timestamp": 1720000000000
}
```

## Junction

### Log Hybrid Junction Event

`POST /api/junctions/{junctionId}/events`

Request:

```json
{
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

Valid event types:

- `lora_gps_packet`
- `approach_tracking`
- `gps_preempt_started`
- `rssi_preempt_started`
- `rfid_clearance`
- `timeout_restore`
- `manual_reset`

### Update Approach Decision

`POST /api/junctions/{junctionId}/approach`

Request:

```json
{
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

### Update Signal State

`POST /api/junctions/{junctionId}/signal-state`

Request:

```json
{
  "signalState": "priority_active",
  "activeLane": "northbound",
  "preemptionMode": "gps_lora",
  "tripId": "TRIP001",
  "timestamp": 1720000000000
}
```

Use `preemptionMode: "rssi_fallback"` when GPS is unhealthy and the signal was triggered by LoRa signal-strength proximity.

## Hospital

### List Nearby Hospitals

`GET /api/hospitals/nearby?lat=12.9716&lng=77.5946`

Response:

```json
{
  "hospitals": [
    {
      "hospitalId": "HOSP001",
      "name": "City Hospital",
      "distanceKm": 2.4,
      "emergencyAvailable": true,
      "bedsAvailable": 8
    }
  ]
}
```
