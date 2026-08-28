# Database Schema

Recommended Firebase Realtime Database structure:

```json
{
  "users": {
    "driver_001": {
      "userId": "driver_001",
      "name": "Driver One",
      "pin": "1111",
      "role": "ambulance_driver",
      "ambulanceId": "AMB001",
      "active": true
    },
    "police_001": {
      "userId": "police_001",
      "name": "Traffic Police",
      "pin": "2222",
      "role": "police",
      "assignedJunctionId": "JNC001",
      "active": true
    },
    "hospital_001": {
      "userId": "hospital_001",
      "name": "City Care Desk",
      "pin": "3333",
      "role": "hospital",
      "hospitalId": "HOSP001",
      "active": true
    }
  },
  "ambulances": {
    "AMB001": {
      "ambulanceId": "AMB001",
      "rfidTagId": "RFID_TAG_001",
      "loraNodeId": "LORA_AMB001",
      "driverId": "DRV001",
      "status": "available",
      "severity": "Serious",
      "destinationHospitalId": "HOSP001",
      "lastLocation": {
        "lat": 12.9716,
        "lng": 77.5946,
        "updatedAt": 1720000000000
      },
      "lastLoRaTelemetry": {
        "junctionId": "JNC001",
        "lat": 12.9716,
        "lng": 77.5946,
        "speedKmph": 42,
        "headingDeg": 185,
        "gpsFix": true,
        "rssi": -72,
        "distanceMeters": 420,
        "bearingToJunctionDeg": 182,
        "approaching": true,
        "updatedAt": 1720000000000
      }
    }
  },
  "drivers": {
    "DRV001": {
      "name": "Driver Name",
      "phone": "9999999999",
      "assignedAmbulanceId": "AMB001"
    }
  },
  "junctions": {
    "JNC001": {
      "name": "Main Road Junction",
      "location": {
        "lat": 12.9716,
        "lng": 77.5946
      },
      "activeLane": "northbound",
      "signalState": "normal",
      "preemptionMode": "none",
      "approachThresholdMeters": 500,
      "bearingToleranceDeg": 35,
      "rssiFallbackThresholdDbm": -65,
      "gpsPacketTimeoutMs": 5000,
      "clearanceTimeoutMs": 90000,
      "lastUpdatedAt": 1720000000000
    }
  },
  "loraTelemetry": {
    "JNC001": {
      "AMB001": {
        "ambulanceId": "AMB001",
        "tripId": "TRIP001",
        "lat": 12.9716,
        "lng": 77.5946,
        "speedKmph": 42,
        "headingDeg": 185,
        "gpsFix": true,
        "rssi": -72,
        "distanceMeters": 420,
        "bearingToJunctionDeg": 182,
        "approaching": true,
        "preemptionEligible": true,
        "updatedAt": 1720000000000
      }
    }
  },
  "emergencyTrips": {
    "TRIP001": {
      "tripId": "TRIP001",
      "ambulanceId": "AMB001",
      "driverId": "DRV001",
      "status": "active",
      "startedAt": 1720000000000,
      "endedAt": null,
      "destinationHospitalId": "HOSP001",
      "destinationHospitalName": "City Care Hospital",
      "severity": "Very Emergency"
    }
  },
  "junctionEvents": {
    "EVT001": {
      "eventId": "EVT001",
      "tripId": "TRIP001",
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
  },
  "hospitals": {
    "HOSP001": {
      "name": "City Hospital",
      "location": {
        "lat": 12.9701,
        "lng": 77.6001
      },
      "emergencyAvailable": true,
      "bedsAvailable": 8,
      "phone": "08000000000"
    }
  },
  "hospitalAlerts": {
    "HOSP001": {
      "TRIP001": {
        "tripId": "TRIP001",
        "ambulanceId": "AMB001",
        "severity": "Very Emergency",
        "status": "incoming",
        "eta": "6 min",
        "message": "Ambulance incoming to City Care Hospital",
        "updatedAt": 1720000000000
      }
    }
  },
  "policeAlerts": {
    "JNC001": {
      "TRIP001": {
        "tripId": "TRIP001",
        "ambulanceId": "AMB001",
        "severity": "Very Emergency",
        "destinationHospitalId": "HOSP001",
        "status": "ambulance_approaching",
        "message": "Ambulance approaching junction",
        "updatedAt": 1720000000000
      }
    }
  }
}
```

## Important Indexes

For Firebase rules/indexing later:

- `ambulances/rfidTagId`
- `emergencyTrips/status`
- `junctionEvents/tripId`
- `junctionEvents/junctionId`
- `junctionEvents/timestamp`
- `loraTelemetry/{junctionId}/{ambulanceId}/updatedAt`
- `loraTelemetry/{junctionId}/{ambulanceId}/preemptionEligible`
- `users/role`
- `hospitalAlerts/{hospitalId}/status`
- `policeAlerts/{junctionId}/status`
