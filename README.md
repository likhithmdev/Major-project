# Smart Ambulance Priority Traffic Control System

This folder is the working home for the Smart Ambulance project.

The project goal is to build a proof-of-concept system where an ambulance gets traffic priority at smart junctions using hybrid GPS-LoRa approach tracking, RC522 RFID stop-line clearance, ESP32-based signal control, Firebase/MQTT telemetry, and a monitoring dashboard.

## Prototype Scope

- One ambulance unit with ESP32, GPS module, LoRa transmitter, RFID tag, and mobile app support.
- Two or three smart junction units.
- Each junction has a LoRa ground station for long-range approach tracking and an RC522 RFID reader at the physical stop line for exact clearance.
- Local traffic signal override works without internet.
- Cloud tracking and analytics work through Firebase and MQTT.
- Dashboard shows ambulance location, junction status, emergency events, and reports on mobile and laptop screens.

## Folder Structure

```text
smart_ambulance/
├── dashboard/                  # Traffic police/control room web dashboard
├── docs/                       # Architecture, schema, API, roadmap
├── firmware/
│   ├── ambulance_unit/         # ESP32 ambulance-side firmware notes/code
│   └── traffic_signal_unit/    # ESP32 junction controller notes/code
└── mobile_app/                 # Driver mobile app
```

## Recommended First Build

Build the prototype in this order:

1. Traffic signal ESP32 logic with manual simulated priority input.
2. RC522 stop-line RFID clearance for one junction.
3. LoRa GPS packet receiving with distance and bearing calculation.
4. Firebase data model and event logging.
5. Dashboard showing junction state and live events.
6. Driver mobile app with emergency activation and GPS tracking.
7. Hospital recommendation and route display.
8. Expand from one junction to two or three junctions.

## Locked Starter Decisions

- Mobile app: native Android.
- Dashboard framework: React + Vite with responsive mobile + laptop layout.
- Cloud/IoT: Firebase plus MQTT.
- Default RFID module: RC522 for the first low-cost prototype, kept replaceable so it can be changed later.
- Long-range preemption: GPS coordinates over local LoRa, with Haversine distance and bearing checks at the junction.
- GPS failure fallback: LoRa RSSI proximity tracking with conservative safety timeout.

See `docs/complete-system-guide.md` for the full hardware-to-software walkthrough in one file.
See `docs/get-started.md` for the immediate next steps.
See `docs/hybrid-preemption.md` for the added GPS-LoRa-RFID workflow.
