# Implementation Roadmap

## Week 1: Finalize Prototype Plan

- Confirm hardware modules.
- Confirm mobile app framework.
- Confirm cloud platform.
- Draw final block diagram.
- Create project presentation outline.

## Week 2: Junction Firmware

- Build ESP32 traffic light LED prototype.
- Add normal signal cycle.
- Add emergency override state.
- Add timeout restore.
- Test with simulated LoRa GPS packets and simulated RC522 tag IDs.

## Week 3: Hybrid Detection Integration

- Connect LoRa ground station receiver.
- Parse ambulance GPS packets.
- Add Haversine distance calculation.
- Add bearing/heading approach validation.
- Connect stop-line RC522 reader for clearance.
- Add authorized ambulance ID and RFID tag validation.
- Log GPS preempt, RSSI fallback, RFID clearance, and timeout timestamps.
- Calculate dwell time from preemption start to RFID clearance.

## Week 4: Firebase Integration

- Create database.
- Add junction event writes.
- Add LoRa telemetry writes.
- Add ambulance/trip records.
- Add Wi-Fi reconnect handling.

## Week 5: Dashboard

- Create React dashboard.
- Show active emergency.
- Show junction status.
- Show GPS-LoRa approach distance, RSSI fallback mode, and RC522 clearance events.
- Show event logs.
- Add manual override UI.

## Week 6: Mobile App

- Create driver app.
- Add login/auth screen.
- Add emergency activation.
- Add GPS tracking.
- Add hospital recommendation.
- Show green corridor state from GPS-LoRa/RFID junction events.

## Week 7: End-to-End Demo

- Connect app, dashboard, Firebase, and junction ESP32.
- Demonstrate LoRa GPS preemption, signal override, stop-line RC522 clearance, restore.
- Test internet-offline case.
- Test GPS failure and RSSI fallback case.
- Test invalid RFID tag case.

## Week 8: Documentation and Presentation

- Final report.
- Research paper draft.
- Diagrams and screenshots.
- Cost estimation.
- Risk analysis.
- Future scope.
