# Dashboard

Recommended framework: React + Vite.

## Dashboard Users

- Traffic police.
- Control room operator.
- Project demo evaluator.

## Views

- Live emergency overview.
- Ambulance map.
- Junction status cards.
- GPS-LoRa, RSSI fallback, and RFID clearance timeline.
- Manual override controls.
- Analytics and reports.

## First Dashboard Milestone

Build a single-screen demo dashboard:

- Active ambulance card.
- Junction 1 signal status.
- Last GPS preempt, RSSI fallback, RFID clearance, or timeout event.
- Event log table.
- Manual override buttons.

Use Firebase Realtime Database for live updates.

## Run Locally

```bash
npm install
npm run dev
```

Default local URL:

```text
http://localhost:5180
```

## Current Mode

The dashboard starts in simulator mode so software can be tested before ESP32 LoRa/RFID hardware is ready.

Use the junction buttons to simulate:

- GPS-LoRa approach preemption.
- RSSI fallback preemption.
- Stop-line RC522 RFID clearance.
- Timeout restore.
- Manual reset.

Later, these simulated actions can be replaced with MQTT messages from the ESP32 traffic signal unit and LoRa ground station.
